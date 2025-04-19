package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** Top module for the PDFD (Parallel Decision Feedback Decoder) project.
  * Lookahead Parallel Decision Feedback Decoder (LaPDFD) with Decision Feedback Pre-filter (DFP)
  * 
  * Inputs: 
  *  - 4 channel symbols (ffe team says 18-bits each)
  *  - 14 channel coefficients (should match bitsize of channel symbols)
  * Outputs:
  *  - 1 byte of decoded rx data
  *  - 1 rx valid signal
  */

// todo need to understand how to get 14 channel coefficients

class LaPDFD()
    extends Module {
  val numTaps = 14 // from paper
  val tapWidth = 8 // guessed
  val sampleWidth = 8 // from ffe team
  val io = IO(new Bundle {
    val rxSamples  = Input(Vec(4, SInt(sampleWidth.W))) // From DFF/ECHO
    val taps = Input(Vec(numTaps, SInt(tapWidth.W))) //todo maybe from TL MMIO instead
    val rxData  = Output(UInt(8.W)) 
    val rxValid = Output(Bool())
  })
  // local parameters
  val upSizeWidth = 13 // dictated by DFP
  val pam5 = Seq(-103.S(8.W), -52.S(8.W), 0.S(8.W), 51.S(8.W), 102.S(8.W))
  val pam5Thresholds = Seq(-77.S(8.W), -26.S(8.W), 25.S(8.W), 76.S(8.W))
  // one unit per channel
  val dfp   = Seq.fill(4)(Module(new DFP(numTaps, tapWidth, sampleWidth, upSizeWidth, pam5, pam5Thresholds)))
  val laBmu = Seq.fill(4)(Module(new OneDimLaBMU(tapWidth, upSizeWidth, pam5, pam5Thresholds)))

  // one unit per state 
  val muxu    = Seq.fill(8)(Module(new MUXU(upSizeWidth)))
  val bmuEven = Seq.fill(8)(Module(new FourDimBMU(upSizeWidth, true)))
  val bmuOdd  = Seq.fill(8)(Module(new FourDimBMU(upSizeWidth, false)))
  val acsu    = Seq.fill(8)(Module(new ACSU(upSizeWidth)))
  val smu     = Seq.fill(8)(Module(new SMU()))
  
  for (i <- 0 until 4) {
    // DFP <- IO
    dfp(i).io.rxSample := io.rxSamples(i)
    dfp(i).io.taps := io.taps
  
    // 1D-LaBMU <- DFP
    laBmu(i).io.rxFilter := dfp(i).io.rxFilter
    laBmu(i).io.tapOne := io.taps(0)
  }

  for (i <- 0 until 8) {
    // MUXU <- LaBMU (SMU survivor symbols)
    muxu(i).io.symSelects := smu(i).io.symSelects
    muxu(i).io.symMetricsA := VecInit(Seq(laBmu(0).io.symMetricsA, laBmu(1).io.symMetricsA, laBmu(2).io.symMetricsA, laBmu(3).io.symMetricsA))
    muxu(i).io.symMetricsB := VecInit(Seq(laBmu(0).io.symMetricsB, laBmu(1).io.symMetricsB, laBmu(2).io.symMetricsB, laBmu(3).io.symMetricsB))

    // 4D-BMU <- MUXU (SMU survivor symbols)
    if (i % 2 == 0) {
      bmuEven(i / 2).io.brMetricsA := muxu(i).io.brMetricsA
      bmuEven(i / 2).io.brMetricsB := muxu(i).io.brMetricsB
    } else {
      bmuOdd(i / 2).io.brMetricsA := muxu(i).io.brMetricsA
      bmuOdd(i / 2).io.brMetricsB := muxu(i).io.brMetricsB
    }

    // ACSU <- 4D-BMU
    if (i < 4) {
      acsu(i).io.brMetrics4D := bmuEven(i / 2).io.brMetrics4D
    } else {
      acsu(i).io.brMetrics4D := bmuOdd(i / 2).io.brMetrics4D
    }

    // SMU <- ACSU (4D-BMU survivor symbols)
    smu(i).io.pathSelect := acsu(i).io.pathSelect
    if (i % 2 == 0) {
      smu(i).io.stateSymSelects := bmuEven(i / 2).io.brSyms4D
    } else {
      smu(i).io.stateSymSelects := VecInit(Seq(smu(i).io.symSelects(7), smu(i).io.symSelects(5), smu(i).io.symSelects(3), smu(i).io.symSelects(1)))
    }

  }
  // not sure how to do ACSU <- ACSU in the loop
    acsu(0).io.pathMetrics := VecInit(Seq(acsu(0).io.pathMetric, acsu(2).io.pathMetric, acsu(4).io.pathMetric, acsu(6).io.pathMetric))
    acsu(1).io.pathMetrics := VecInit(Seq(acsu(2).io.pathMetric, acsu(0).io.pathMetric, acsu(6).io.pathMetric, acsu(4).io.pathMetric))
    acsu(2).io.pathMetrics := VecInit(Seq(acsu(4).io.pathMetric, acsu(6).io.pathMetric, acsu(0).io.pathMetric, acsu(2).io.pathMetric))
    acsu(3).io.pathMetrics := VecInit(Seq(acsu(6).io.pathMetric, acsu(4).io.pathMetric, acsu(2).io.pathMetric, acsu(0).io.pathMetric))
    acsu(4).io.pathMetrics := VecInit(Seq(acsu(1).io.pathMetric, acsu(3).io.pathMetric, acsu(5).io.pathMetric, acsu(7).io.pathMetric))
    acsu(5).io.pathMetrics := VecInit(Seq(acsu(3).io.pathMetric, acsu(1).io.pathMetric, acsu(7).io.pathMetric, acsu(5).io.pathMetric))
    acsu(6).io.pathMetrics := VecInit(Seq(acsu(5).io.pathMetric, acsu(7).io.pathMetric, acsu(1).io.pathMetric, acsu(3).io.pathMetric))
    acsu(7).io.pathMetrics := VecInit(Seq(acsu(7).io.pathMetric, acsu(5).io.pathMetric, acsu(3).io.pathMetric, acsu(1).io.pathMetric))


  // SMU -> output
  // todo need to implement - current solution doesnt work
  // io.decodedByte := smu(minIndex(VecInit(Seq(acsu(7).io.pathMetric, acsu(6).io.pathMetric, acsu(5).io.pathMetric, acsu(4).io.pathMetric, acsu(3).io.pathMetric, acsu(2).io.pathMetric, acsu(1).io.pathMetric, acsu(0).io.pathMetric)))).io.decodedByte
  

}
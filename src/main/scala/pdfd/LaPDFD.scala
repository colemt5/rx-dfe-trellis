package pdfd

import chisel3._
import _root_.circt.stage.ChiselStage
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

class LaPDFD()
    extends Module {
  val numTaps = 14 // from paper
  val tapWidth = 8 // guessed
  val sampleWidth = 8 // from ffe team
  val io = IO(new Bundle {
    val rxSamples  = Input(Vec(4, SInt(sampleWidth.W))) // From DFF/ECHO
    val taps = Input(Vec(numTaps, SInt(tapWidth.W))) //todo maybe from TL MMIO instead
    val rxData  = Output(UInt(12.W)) 
    val rxValid = Output(Bool())
  })
  // local parameters
  val upSizeWidth = 13 // dictated by DFP
  val pam5 = Seq(-103, -52, 0, 51, 101)
  val pam5Thresholds = Seq(-77, -26, 25, 76)
  // one unit per channel
  val dfp   = Seq.fill(4)(Module(new DFP(numTaps, tapWidth, sampleWidth, upSizeWidth, pam5, pam5Thresholds)))
  val laBmu = Seq.fill(4)(Module(new OneDimLaBMU(tapWidth, sampleWidth, upSizeWidth, pam5)))

  // one unit per state 
  val muxu    = Seq.fill(8)(Module(new MUXU(sampleWidth)))
  val bmuEven = Seq.fill(4)(Module(new FourDimBMU(sampleWidth, upSizeWidth, true)))
  val bmuOdd  = Seq.fill(4)(Module(new FourDimBMU(sampleWidth, upSizeWidth, false)))
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
    for (j <- 0 until 4) {
      muxu(i).io.symsA(j) := laBmu(j).io.symsA
      muxu(i).io.symsB(j) := laBmu(j).io.symsB
      muxu(i).io.symMetricsA(j) := laBmu(j).io.symMetricsA
      muxu(i).io.symMetricsB(j) := laBmu(j).io.symMetricsB
    }
    muxu(i).io.symSelects := smu(i).io.symSelects

    // 4D-BMU <- MUXU (SMU survivor symbols)
    if (i % 2 == 0) {
      bmuEven(i / 2).io.brMetricsA := muxu(i).io.brMetricsA
      bmuEven(i / 2).io.brMetricsB := muxu(i).io.brMetricsB
      bmuEven(i / 2).io.brSymsA := muxu(i).io.brSymsA
      bmuEven(i / 2).io.brSymsB := muxu(i).io.brSymsB
    } else {
      bmuOdd(i / 2).io.brMetricsA := muxu(i).io.brMetricsA
      bmuOdd(i / 2).io.brMetricsB := muxu(i).io.brMetricsB
      bmuOdd(i / 2).io.brSymsA := muxu(i).io.brSymsA
      bmuOdd(i / 2).io.brSymsB := muxu(i).io.brSymsB
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
      smu(i).io.stateSymSelects := bmuOdd(i / 2).io.brSyms4D
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

  // not sure how to do SMU <- SMU in the loop
  smu(0).io.byteInputs(0) := smu(0).io.byteChoices
  smu(0).io.byteInputs(1) := smu(2).io.byteChoices
  smu(0).io.byteInputs(2) := smu(4).io.byteChoices
  smu(0).io.byteInputs(3) := smu(6).io.byteChoices

  smu(1).io.byteInputs(0) := smu(2).io.byteChoices
  smu(1).io.byteInputs(1) := smu(0).io.byteChoices
  smu(1).io.byteInputs(2) := smu(6).io.byteChoices
  smu(1).io.byteInputs(3) := smu(4).io.byteChoices

  smu(2).io.byteInputs(0) := smu(4).io.byteChoices
  smu(2).io.byteInputs(1) := smu(6).io.byteChoices
  smu(2).io.byteInputs(2) := smu(0).io.byteChoices
  smu(2).io.byteInputs(3) := smu(2).io.byteChoices

  smu(3).io.byteInputs(0) := smu(6).io.byteChoices
  smu(3).io.byteInputs(1) := smu(4).io.byteChoices
  smu(3).io.byteInputs(2) := smu(2).io.byteChoices
  smu(3).io.byteInputs(3) := smu(0).io.byteChoices
  
  smu(4).io.byteInputs(0) := smu(1).io.byteChoices
  smu(4).io.byteInputs(1) := smu(3).io.byteChoices
  smu(4).io.byteInputs(2) := smu(5).io.byteChoices
  smu(4).io.byteInputs(3) := smu(7).io.byteChoices
  
  smu(5).io.byteInputs(0) := smu(3).io.byteChoices
  smu(5).io.byteInputs(1) := smu(1).io.byteChoices
  smu(5).io.byteInputs(2) := smu(7).io.byteChoices
  smu(5).io.byteInputs(3) := smu(5).io.byteChoices

  smu(6).io.byteInputs(0) := smu(5).io.byteChoices
  smu(6).io.byteInputs(1) := smu(7).io.byteChoices
  smu(6).io.byteInputs(2) := smu(1).io.byteChoices
  smu(6).io.byteInputs(3) := smu(3).io.byteChoices

  smu(7).io.byteInputs(0) := smu(7).io.byteChoices
  smu(7).io.byteInputs(1) := smu(5).io.byteChoices
  smu(7).io.byteInputs(2) := smu(3).io.byteChoices
  smu(7).io.byteInputs(3) := smu(1).io.byteChoices



  // SMU -> output
  io.rxData := smu(0).io.byteDecision
  io.rxValid := 1.U

}

/**
 * Generate Verilog sources and save it in file Elaborate.v
 */
object LaPDFD extends App {
  ChiselStage.emitSystemVerilogFile(
    new LaPDFD,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
}
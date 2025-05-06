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

class LaPDFD(level: Int = 52, tapScale: Int = 128, tapWidth: Int = 8, sampleWidth: Int = 8)
    extends Module {
  
  // require that tapWidth is large enough to hold scaled tap values
  require(tapWidth > log2Floor(tapScale), 
    s"tapWidth ($tapWidth) must be greater than log2Floor(tapScale)= ${log2Floor(tapScale)}")

  // local parameters for the LaPDFD
  val numTaps   = 14 // DONT CHANGE (hardcoded in DFP)
  val fracWidth = log2Floor(tapScale) // fixed point width
  val smWidth   = 6 // highest sm width that meets timing
  val bmWidth   = smWidth + 2 // 1 bit for sign and 1 bit for merge

  // IO Ports
  val io = IO(new Bundle {
    val rxSamples  = Input(Vec(4, SInt(sampleWidth.W))) // From DFF/ECHO
    val taps = Input(Vec(numTaps, SInt(tapWidth.W))) //todo maybe from TL MMIO instead
    val rxSymbols  = Output(UInt(12.W)) 
    val rxValid = Output(Bool())
  })
  // PAM5 levels used for slicing
  val pam5 = Seq(-level*2, -level, 0, level, level*2)
  val pam5Thresholds = Seq(-(3*level)/2, -level/2, level/2, (3*level)/2)
  
  // one unit per channel
  val dfp   = Seq.fill(4)(Module(new DFP(numTaps, tapWidth, fracWidth, sampleWidth, pam5, pam5Thresholds)))
  val laBmu = Seq.fill(4)(Module(new OneDimLaBMU(tapWidth, fracWidth, sampleWidth, smWidth, pam5)))

  // one unit per state 
  val muxu    = Seq.fill(8)(Module(new MUXU(smWidth)))
  val bmuEven = Seq.fill(4)(Module(new FourDimBMU(smWidth, bmWidth, true)))
  val bmuOdd  = Seq.fill(4)(Module(new FourDimBMU(smWidth, bmWidth, false)))
  val acsu    = Seq.fill(8)(Module(new ACSU(bmWidth)))
  val smu     = Seq.fill(8)(Module(new SMU()))
  
  for (i <- 0 until 4) {
    // DFP <- IO
    dfp(i).io.rxSample := io.rxSamples(i)
    dfp(i).io.taps := io.taps
  
    // 1D-LaBMU <- DFP
    laBmu(i).io.rxFilter := dfp(i).io.rxFilter
    laBmu(i).io.tapOne := io.taps(0)
  }
  
  val symMetrics_init = VecInit(Seq.fill(5)(0.U(sampleWidth.W)))
  val syms_init = VecInit(Seq.fill(5)(0.S(3.W)))

  for (i <- 0 until 8) {
    // MUXU <- LaBMU (SMU survivor symbols)
    for (j <- 0 until 4) {
      muxu(i).io.symsA(j) := RegNext(laBmu(j).io.symsA, syms_init)
      muxu(i).io.symsB(j) := RegNext(laBmu(j).io.symsB, syms_init)
      muxu(i).io.symMetricsA(j) := RegNext(laBmu(j).io.symMetricsA, symMetrics_init)
      muxu(i).io.symMetricsB(j) := RegNext(laBmu(j).io.symMetricsB, symMetrics_init)
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

    smu(i).io.pathSelect := acsu(i).io.pathSelect
  }
  // not sure how to do ACSU <- ACSU in the loop
  acsu(0).io.pathMetrics := VecInit(Seq(acsu(0).io.pathMetric, acsu(2).io.pathMetric, acsu(4).io.pathMetric, acsu(6).io.pathMetric))
  acsu(0).io.brMetrics4D := VecInit(Seq(bmuEven(0).io.brMetrics4D(0), bmuEven(1).io.brMetrics4D(1), bmuEven(2).io.brMetrics4D(2), bmuEven(3).io.brMetrics4D(3)))

  acsu(1).io.pathMetrics := VecInit(Seq(acsu(0).io.pathMetric, acsu(2).io.pathMetric, acsu(4).io.pathMetric, acsu(6).io.pathMetric))
  acsu(1).io.brMetrics4D := VecInit(Seq(bmuEven(0).io.brMetrics4D(1), bmuEven(1).io.brMetrics4D(0), bmuEven(2).io.brMetrics4D(3), bmuEven(3).io.brMetrics4D(2)))

  acsu(2).io.pathMetrics := VecInit(Seq(acsu(0).io.pathMetric, acsu(2).io.pathMetric, acsu(4).io.pathMetric, acsu(6).io.pathMetric))
  acsu(2).io.brMetrics4D := VecInit(Seq(bmuEven(0).io.brMetrics4D(2), bmuEven(1).io.brMetrics4D(3), bmuEven(2).io.brMetrics4D(0), bmuEven(3).io.brMetrics4D(1)))

  acsu(3).io.pathMetrics := VecInit(Seq(acsu(0).io.pathMetric, acsu(2).io.pathMetric, acsu(4).io.pathMetric, acsu(6).io.pathMetric))
  acsu(3).io.brMetrics4D := VecInit(Seq(bmuEven(0).io.brMetrics4D(3), bmuEven(1).io.brMetrics4D(2), bmuEven(2).io.brMetrics4D(1), bmuEven(3).io.brMetrics4D(0)))

  acsu(4).io.pathMetrics := VecInit(Seq(acsu(1).io.pathMetric, acsu(3).io.pathMetric, acsu(5).io.pathMetric, acsu(7).io.pathMetric))
  acsu(4).io.brMetrics4D := VecInit(Seq(bmuOdd(0).io.brMetrics4D(0), bmuOdd(1).io.brMetrics4D(1), bmuOdd(2).io.brMetrics4D(2), bmuOdd(3).io.brMetrics4D(3)))

  acsu(5).io.pathMetrics := VecInit(Seq(acsu(1).io.pathMetric, acsu(3).io.pathMetric, acsu(5).io.pathMetric, acsu(7).io.pathMetric))
  acsu(5).io.brMetrics4D := VecInit(Seq(bmuOdd(0).io.brMetrics4D(1), bmuOdd(1).io.brMetrics4D(0), bmuOdd(2).io.brMetrics4D(3), bmuOdd(3).io.brMetrics4D(2)))

  acsu(6).io.pathMetrics := VecInit(Seq(acsu(1).io.pathMetric, acsu(3).io.pathMetric, acsu(5).io.pathMetric, acsu(7).io.pathMetric))
  acsu(6).io.brMetrics4D := VecInit(Seq(bmuOdd(0).io.brMetrics4D(2), bmuOdd(1).io.brMetrics4D(3), bmuOdd(2).io.brMetrics4D(0), bmuOdd(3).io.brMetrics4D(1)))

  acsu(7).io.pathMetrics := VecInit(Seq(acsu(1).io.pathMetric, acsu(3).io.pathMetric, acsu(5).io.pathMetric, acsu(7).io.pathMetric))
  acsu(7).io.brMetrics4D := VecInit(Seq(bmuOdd(0).io.brMetrics4D(3), bmuOdd(1).io.brMetrics4D(2), bmuOdd(2).io.brMetrics4D(1), bmuOdd(3).io.brMetrics4D(0)))

  // not sure how to do SMU <- SMU in the loop
  for (i <- 0 until 4) {
    smu(i).io.byteInputs(0) := smu(0).io.byteChoices
    smu(i).io.byteInputs(1) := smu(2).io.byteChoices
    smu(i).io.byteInputs(2) := smu(4).io.byteChoices
    smu(i).io.byteInputs(3) := smu(6).io.byteChoices
  }

  for (i <- 4 until 8) {
    smu(i).io.byteInputs(0) := smu(1).io.byteChoices
    smu(i).io.byteInputs(1) := smu(3).io.byteChoices
    smu(i).io.byteInputs(2) := smu(5).io.byteChoices
    smu(i).io.byteInputs(3) := smu(7).io.byteChoices
  }

  smu(0).io.stateSymSelects(0) := bmuEven(0).io.brSyms4D(0)
  smu(0).io.stateSymSelects(1) := bmuEven(1).io.brSyms4D(1)
  smu(0).io.stateSymSelects(2) := bmuEven(2).io.brSyms4D(2)
  smu(0).io.stateSymSelects(3) := bmuEven(3).io.brSyms4D(3)

  smu(1).io.stateSymSelects(0) := bmuEven(0).io.brSyms4D(1)
  smu(1).io.stateSymSelects(1) := bmuEven(1).io.brSyms4D(0)
  smu(1).io.stateSymSelects(2) := bmuEven(2).io.brSyms4D(3)
  smu(1).io.stateSymSelects(3) := bmuEven(3).io.brSyms4D(2)

  smu(2).io.stateSymSelects(0) := bmuEven(0).io.brSyms4D(2)
  smu(2).io.stateSymSelects(1) := bmuEven(1).io.brSyms4D(3)
  smu(2).io.stateSymSelects(2) := bmuEven(2).io.brSyms4D(0)
  smu(2).io.stateSymSelects(3) := bmuEven(3).io.brSyms4D(1)

  smu(3).io.stateSymSelects(0) := bmuEven(0).io.brSyms4D(3)
  smu(3).io.stateSymSelects(1) := bmuEven(1).io.brSyms4D(2)
  smu(3).io.stateSymSelects(2) := bmuEven(2).io.brSyms4D(1)
  smu(3).io.stateSymSelects(3) := bmuEven(3).io.brSyms4D(0)

  smu(4).io.stateSymSelects(0) := bmuOdd(0).io.brSyms4D(0)
  smu(4).io.stateSymSelects(1) := bmuOdd(1).io.brSyms4D(1)
  smu(4).io.stateSymSelects(2) := bmuOdd(2).io.brSyms4D(2)
  smu(4).io.stateSymSelects(3) := bmuOdd(3).io.brSyms4D(3)

  smu(5).io.stateSymSelects(0) := bmuOdd(0).io.brSyms4D(1)
  smu(5).io.stateSymSelects(1) := bmuOdd(1).io.brSyms4D(0)
  smu(5).io.stateSymSelects(2) := bmuOdd(2).io.brSyms4D(3)
  smu(5).io.stateSymSelects(3) := bmuOdd(3).io.brSyms4D(2)
  
  smu(6).io.stateSymSelects(0) := bmuOdd(0).io.brSyms4D(2)
  smu(6).io.stateSymSelects(1) := bmuOdd(1).io.brSyms4D(3)
  smu(6).io.stateSymSelects(2) := bmuOdd(2).io.brSyms4D(0)
  smu(6).io.stateSymSelects(3) := bmuOdd(3).io.brSyms4D(1)

  smu(7).io.stateSymSelects(0) := bmuOdd(0).io.brSyms4D(3)
  smu(7).io.stateSymSelects(1) := bmuOdd(1).io.brSyms4D(2)
  smu(7).io.stateSymSelects(2) := bmuOdd(2).io.brSyms4D(1)
  smu(7).io.stateSymSelects(3) := bmuOdd(3).io.brSyms4D(0)

  // SMU -> output
  io.rxSymbols := smu(0).io.byteDecision
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
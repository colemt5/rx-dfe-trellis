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

class LaPDFD(symBitWidth: Int = 18, chanBitWidth: Int, numTaps: Int = 14)
    extends Module {
  val io = IO(new Bundle {
    // Input symbols from DFF/ECHO
    val inSymbols  = Input(Vec(4, SInt(symBitWidth.W)))
    // Input from //todo from MMIO or PCS or other
    val chanCoeffs = Input(Vec(numTaps, SInt(chanBitWidth.W)))
    
    // Output to PCS
    val rxData  = Output(UInt(8.W))
    val rxValid = Output(Bool())
  })
  
  // one unit per channel
  val dfp   = Seq.fill(4)(Module(new DFP(symBitWidth, numTaps)))
  val laBmu = Seq.fill(4)(Module(new OneDimLaBMU(symBitWidth)))

  // one unit per state 
  val muxu    = Seq.fill(8)(Module(new MUXU(symBitWidth)))
  val bmuEven = Seq.fill(8)(Module(new FourDimBMU(symBitWidth, true)))
  val bmuOdd  = Seq.fill(8)(Module(new FourDimBMU(symBitWidth, false)))
  val acsu    = Seq.fill(8)(Module(new ACSU(symBitWidth)))
  val smu     = Seq.fill(8)(Module(new SMU()))
  
  for (i <- 0 until 4) {
    // DFP <- IO
    dfp(i).io.inSymbol := io.inSymbols(i)
    dfp(i).io.chanCoeffs := io.chanCoeffs
  
    // 1D-LaBMU <- DFP
    laBmu(i).io.preFilteredSymbol := dfp(i).io.preFilteredSymbol
    laBmu(i).io.chanCoeff1 := io.chanCoeffs(0)
  }

  for (i <- 0 until 8) {
    // MUXU <- LaBMU (SMU survivor symbols)
    muxu(i).io.symSelects := smu(i).io.symSelects
    muxu(i).io.symMetricsA := VecInit(Seq(laBmu(3).io.symMetricsA, laBmu(2).io.symMetricsA, laBmu(1).io.symMetricsA, laBmu(0).io.symMetricsA))
    muxu(i).io.symMetricsB := VecInit(Seq(laBmu(3).io.symMetricsB, laBmu(2).io.symMetricsB, laBmu(1).io.symMetricsB, laBmu(0).io.symMetricsB))

    // 4D-BMU <- MUXU (SMU survivor symbols)
    if (i % 2 == 0) {
      bmuEven(i / 2).io.brMetricsA := muxu(i).io.brMetricsA
      bmuEven(i / 2).io.brMetricsB := muxu(i).io.brMetricsB
    } else {
      bmuOdd(i / 2).io.brMetricsA := muxu(i).io.brMetricsA
      bmuOdd(i / 2).io.brMetricsB := muxu(i).io.brMetricsB
    }

    // ACSU <- 4D-BMU
    if (i % 2 == 0) {
      acsu(i).io.brMetrics4D := bmuEven(i / 2).io.brMetrics4D
      acsu(i).io.pathMetrics := VecInit(Seq(acsu(6).io.pathMetric, acsu(4).io.pathMetric, acsu(2).io.pathMetric, acsu(0).io.pathMetric))
    } else {
      acsu(i).io.brMetrics4D := bmuOdd(i / 2).io.brMetrics4D
      acsu(i).io.pathMetrics := VecInit(Seq(acsu(7).io.pathMetric, acsu(5).io.pathMetric, acsu(3).io.pathMetric, acsu(1).io.pathMetric))
    }

    // SMU <- ACSU (4D-BMU survivor symbols)
    smu(i).io.pathSelect := acsu(i).io.pathSelect
    if (i % 2 == 0) {
      smu(i).io.stateSymSelects := VecInit(Seq(smu(i).io.symSelects(6), smu(i).io.symSelects(4), smu(i).io.symSelects(2), smu(i).io.symSelects(0)))
    } else {
      smu(i).io.stateSymSelects := VecInit(Seq(smu(i).io.symSelects(7), smu(i).io.symSelects(5), smu(i).io.symSelects(3), smu(i).io.symSelects(1)))
    }

  }
  // SMU -> output
  // todo need to implement - current solution doesnt work
  // io.decodedByte := smu(minIndex(VecInit(Seq(acsu(7).io.pathMetric, acsu(6).io.pathMetric, acsu(5).io.pathMetric, acsu(4).io.pathMetric, acsu(3).io.pathMetric, acsu(2).io.pathMetric, acsu(1).io.pathMetric, acsu(0).io.pathMetric)))).io.decodedByte
  

}
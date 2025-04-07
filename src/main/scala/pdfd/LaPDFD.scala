package pdfd

import chisel3._
import chisel3.util.log2Ceil

/** Top module for the PDFD (Parallel Decision Feedback Decoder) project.
  * Lookahead Parallel Decision Feedback Decoder (LaPDFD) with Decision Feedback Pre-filter (DFP)
  * 
  * Inputs: 
  *  - 4 channel symbols (ffe team says 18-bits each)
  *  - 14 channel coefficients (should match bitsize of channel symbols)
  *     - // todo check if channel coefficients should be passed with MMIO (Tilelink)
  * Outputs:
  *  - 1 byte of decoded rx data
  * 
  */

// todo need to verify if int or fixed point
// todo need to verify what the output looks like
// todo need to understand how to get 14 channel coefficients

class LaPDFD(bitWidth: Int) // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    val inSymbols  = Input(Vec(4, SInt(bitWidth.W)))
    val chanCoeffs   = Input(Vec(14, SInt(bitWidth.W)))
    val decodedByte = Output(UInt(8.W))
  })
  
  val dfp   = Seq.fill(4)(Module(new DFP())) // one DFP per channel
  val laBmu = Seq.fill(4)(Module(new OneDimLaBMU())) // one 1D LaBMU per channel
  val muxu  = Seq.fill(8)(Module(new MUXU())) // one MUXU for each state
  val bmu   = Seq.fill(8)(Module(new FourDimBMU())) // one 4D BMU for each state
  val acsu  = Seq.fill(8)(Module(new ACSU())) // one ACSU for each state
  val smu   = Seq.fill(8)(Module(new SMU())) // one SMU for each state
  
  for (i <- 0 until 4) {
    // IO -> DFP
    dfp(i).io.inSymbol := io.inSymbols(i)
    dfp(i).io.chanCoeffs := io.chanCoeffs
  
    // DFP -> LaBMU
    laBmu(i).io.preFilteredSymbol := dfp(i).io.preFilteredSymbol
    laBmu(i).io.chanCoeff1 := dfp(i).io.chanCoeffs(0)
  }

  for (i <- 0 until 8) {
    // LaBMU -> MUXU <- SMU
    muxu(i).io.symSelects := smu(i).io.symSelects
    muxu(i).io.brMetricsA := laBmu.io.symMetricsA // ? not sure if this works
    muxu(i).io.brMetricsB := laBmu.io.symMetricsB // ? not sure if this works
    // muxu(i).io.brMetricsA := {laBmu(3).io.symMetricsA, laBmu(2).io.symMetricsA, laBmu(1).io.symMetricsA, laBmu(0).io.symMetricsA}
    // muxu(i).io.brMetricsB := {laBmu(3).io.symMetricsB, laBmu(2).io.symMetricsB, laBmu(1).io.symMetricsB, laBmu(0).io.symMetricsB}

    // MUXU -> 4D BMU
    bmu(i).io.symSelects := muxu(i).io.symSelects
    bmu(i).io.chanBrMetricsA := muxu(i).io.chanBrMetricsA
    bmu(i).io.chanBrMetricsB := muxu(i).io.chanBrMetricsB

   // 4D BMU -> ACSU
    if (i % 2 == 0) {
      acsu(i).io.pathMetrics := {acsu(6).io.pathMetric, acsu(4).io.pathMetric, acsu(2).io.pathMetric, acsu(0).io.pathMetric}
    } else {
      acsu(i).io.pathMetrics := {acsu(7).io.pathMetric, acsu(5).io.pathMetric, acsu(3).io.pathMetric, acsu(1).io.pathMetric}
    }
    acsu(i).io.brMetrics4D := bmu(i).io.brMetrics4D

    // ACSU -> SMU <- 4D BMU
    smu(i).io.pathSelect := acsu(i).io.pathSelect
    smu(i).io.brMetrics4D := bmu(i).io.brMetrics4D

  }
  // SMU -> output
  io.decodedByte := smu(minIndex(acsu.io.pathMetric)).io.decodedByte // ? not sure if this works
  //io.decodedByte := smu(minIndex({acsu(7).io.pathMetric, acsu(6).io.pathMetric, acsu(5).io.pathMetric, acsu(4).io.pathMetric, acsu(3).io.pathMetric, acsu(2).io.pathMetric, acsu(1).io.pathMetric, acsu(0).io.pathMetric})).io.decodedByte
  // todo connect the modules together

}
package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** 1D LaBMU module that computes the branch metrics for the 1D Lookahead
  * Branch Metric Unit (LaBMU) for each of the 4 channel symbols.
  */
class OneDimLaBMU(tapWidth: Int, fracWidth: Int, sampleWidth: Int, pam5: Seq[Int])
    extends Module {
  val io = IO(new Bundle {
    val rxFilter = Input(SInt((tapWidth + fracWidth).W))
    val tapOne  = Input(SInt(tapWidth.W))
    val symMetricsA = Output(Vec(5, UInt(sampleWidth.W)))
    val symMetricsB = Output(Vec(5, UInt(sampleWidth.W)))
    val symsA = Output(Vec(5, SInt(3.W)))
    val symsB = Output(Vec(5, SInt(3.W)))
  })

  // convert to Chisel
  val pam5Vals = pam5.map(_.S(sampleWidth.W))
  
  // the midpoint of {-1, 1} is 0
  val pam5ThreshA = Seq(pam5Vals(2))
  
  // the midpoints of {-2, 0, 2} is {-1, 1}
  val pam5ThreshB = Seq(pam5Vals(1), pam5Vals(3))

  val pam5A = Seq(pam5Vals(1), pam5Vals(3))
  val pam5B = Seq(pam5Vals(0), pam5Vals(2), pam5Vals(4))
  
  val estSym = Wire(Vec(5, SInt(sampleWidth.W)))
  val closeA = Wire(Vec(5, SInt(sampleWidth.W)))
  val closeB = Wire(Vec(5, SInt(sampleWidth.W)))
  
  // diff to closest A/B Pam5 symbol
  val diffA = Wire(Vec(5, SInt(sampleWidth.W)))
  val diffB = Wire(Vec(5, SInt(sampleWidth.W)))


  for (i <- 0 until 5) {
    estSym(i) := (io.rxFilter - (pam5Vals(i) * io.tapOne)) >> fracWidth
    closeA(i) := levelSlicer(estSym(i), pam5A, pam5ThreshA)
    closeB(i) := levelSlicer(estSym(i), pam5B, pam5ThreshB)
    diffA(i) := estSym(i) - closeA(i)
    diffB(i) := estSym(i) - closeB(i)
    io.symMetricsA(i) := saturatingSquare(diffA(i), sampleWidth)
    io.symMetricsB(i) := saturatingSquare(diffB(i), sampleWidth)
    io.symsA(i) := Mux(closeA(i) === pam5A(0), -1.S, 1.S)
    io.symsB(i) := MuxCase(0.S, Array(
      (closeB(i) === pam5B(0)) -> -2.S, 
      (closeB(i) === pam5B(2)) -> 2.S))
  }

}
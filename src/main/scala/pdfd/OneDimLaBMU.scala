package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** 1D LaBMU module that computes the branch metrics for the 1D Lookahead
  * Branch Metric Unit (LaBMU) for each of the 4 channel symbols.
  *
  * @param symBitWidth the bit width of the input and output signals
  */
class OneDimLaBMU(tapWidth: Int = 8, sampleWidth: Int, upSizeWidth: Int, pam5: Seq[Int]) // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    val rxFilter = Input(SInt(upSizeWidth.W))
    val tapOne  = Input(SInt(tapWidth.W))
    val symMetricsA = Output(Vec(5, UInt(sampleWidth.W))) // todo need to verify if int or fixed point
    val symMetricsB = Output(Vec(5, UInt(sampleWidth.W))) // todo need to verify if int or fixed point
    val symsA = Output(Vec(5, SInt(3.W)))
    val symsB = Output(Vec(5, SInt(3.W)))
  })
  
  val pam5Vals = pam5.map(_.S(sampleWidth.W))
  // the midpoint of {-1, 1} is 0
  val pam5ThreshA = Seq(pam5Vals(2))
  // the midpoints of {-2, 0, 2} is {-1, 1}
  val pam5ThreshB = Seq(pam5Vals(1), pam5Vals(3))

  val pam5A = Seq(pam5Vals(1), pam5Vals(3))
  val pam5B = Seq(pam5Vals(0), pam5Vals(2), pam5Vals(4))
  
  val estSym = Wire(Vec(5, SInt(upSizeWidth.W)))
  // diff to closest A/B Pam5 symbol
  val diffA = Wire(Vec(5, SInt(upSizeWidth.W)))
  val diffB = Wire(Vec(5, SInt(upSizeWidth.W)))

  for (i <- 0 until 5) {
    estSym(i) := io.rxFilter - pam5Vals(i) * io.tapOne
    diffA(i) := estSym(i) - levelSlicer(estSym(i), pam5A, pam5ThreshA)
    diffB(i) := estSym(i) - levelSlicer(estSym(i), pam5B, pam5ThreshB)
    io.symMetricsA(i) := saturatingSquare(diffA(i), sampleWidth)
    io.symMetricsB(i) := saturatingSquare(diffB(i), sampleWidth)
    io.symsA(i) := Mux(estSym(i) === pam5A(0), -1.S, 1.S)
    io.symsB(i) := MuxCase(0.S, Array(
      (estSym(i) === pam5B(0)) -> -2.S, 
      (estSym(i) === pam5B(2)) -> 2.S))
  }

}
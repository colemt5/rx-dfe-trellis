package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** 1D LaBMU module that computes the branch metrics for the 1D Lookahead
  * Branch Metric Unit (LaBMU) for each of the 4 channel symbols.
  *
  * @param symBitWidth the bit width of the input and output signals
  */
class OneDimLaBMU(tapWidth: Int = 8, upSizeWidth: Int = 13, pam5: Seq[SInt], pam5Thresholds: Seq[SInt]) // todo add parameters 
    extends Module {
  val sliceThresh = 51
  val io = IO(new Bundle {
    val rxFilter = Input(SInt(upSizeWidth.W))
    val tapOne  = Input(SInt(tapWidth.W))
    val symMetricsA = Output(Vec(5, SInt(upSizeWidth.W))) // todo need to verify if int or fixed point
    val symMetricsB = Output(Vec(5, SInt(upSizeWidth.W))) // todo need to verify if int or fixed point
    val symsA = Output(Vec(5, SInt(3.W)))
    val symsB = Output(Vec(5, SInt(3.W)))
  })

  val pam5ThreshA = Seq(pam5Thresholds(1), pam5Thresholds(3))
  val pam5ThreshB = Seq(pam5Thresholds(0), pam5Thresholds(2), pam5Thresholds(4))
  val pam5A = VecInit(Seq(pam5(1), pam5(3)))
  val pam5B = VecInit(Seq(pam5(0), pam5(2), pam5(4)))
  
  val estSym = Wire(Vec(4, SInt(upSizeWidth.W)))
  // diff to closest A/B Pam5 symbol
  val diffA = Vec(5, SInt(upSizeWidth.W))
  val diffB = Vec(5, SInt(upSizeWidth.W))


  for (i <- 0 until 4) {
    estSym(i) := io.rxFilter - pam5(i) * io.tapOne
    diffA(i) := estSym(i) - levelSlicer(estSym(i), pam5A, pam5ThreshA)
    diffB(i) := estSym(i) - levelSlicer(estSym(i), pam5B, pam5ThreshB)
    io.symMetricsA(i) := diffA(i)*diffA(i)
    io.symMetricsB(i) := diffB(i)*diffB(i)
    io.symsA(i) := Mux(diffA(i) === pam5A(0), -1.S, 1.S)
    io.symsB(i) := MuxCase(0.S, Array(
      (diffB(i) === pam5B(0)) -> -2.S, 
      (diffB(i) === pam5B(2)) -> 2.S))
  }

}
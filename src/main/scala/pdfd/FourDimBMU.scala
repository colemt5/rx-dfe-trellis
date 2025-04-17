package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** 4d BMU module that computes the combined branch metric for all 4 channels
  * from the 1D LaBMU modules.
  *
  * @param symBitWidth the bit width of the input and output signals
  */
class FourDimBMU(symBitWidth: Int, isEvenState: Boolean)
    extends Module {
  val io = IO(new Bundle { // [0] is Channel 1, [1] is Channel 2, [2] is Channel 3, [3] is Channel 4
    val brMetricsA = Input(Vec(4, SInt(symBitWidth.W))) // todo need to verify if int or fixed point
    val brMetricsB = Input(Vec(4, SInt(symBitWidth.W))) // todo need to verify if int or fixed point
    val brMetrics4D = Output(Vec(4, SInt(symBitWidth.W))) // todo need to verify if int or fixed point
  })
  val brMetricA = Vec(4, SInt(symBitWidth.W))
  val brMetricB = Vec(4, SInt(symBitWidth.W))

  if (isEvenState) {
    brMetricA(0) := io.brMetricsA(0) + io.brMetricsA(1) + io.brMetricsA(2) + io.brMetricsA(3) // AAAA S0
    brMetricB(0) := io.brMetricsB(0) + io.brMetricsB(1) + io.brMetricsB(2) + io.brMetricsB(3) // BBBB S0
    brMetricA(1) := io.brMetricsA(0) + io.brMetricsA(1) + io.brMetricsB(2) + io.brMetricsB(3) // AABB S2
    brMetricB(1) := io.brMetricsB(0) + io.brMetricsB(1) + io.brMetricsA(2) + io.brMetricsA(3) // BBAA S2
    brMetricA(2) := io.brMetricsA(0) + io.brMetricsB(1) + io.brMetricsB(2) + io.brMetricsA(3) // ABBA S4
    brMetricB(2) := io.brMetricsB(0) + io.brMetricsA(1) + io.brMetricsA(2) + io.brMetricsB(3) // BAAB S4
    brMetricA(3) := io.brMetricsA(0) + io.brMetricsB(1) + io.brMetricsA(2) + io.brMetricsB(3) // ABAB S6
    brMetricB(3) := io.brMetricsB(0) + io.brMetricsA(1) + io.brMetricsB(2) + io.brMetricsA(3) // BABA S6
  } else {
    brMetricA(0) := io.brMetricsA(0) + io.brMetricsA(1) + io.brMetricsA(2) + io.brMetricsB(3) // AAAB S1
    brMetricB(0) := io.brMetricsB(0) + io.brMetricsB(1) + io.brMetricsB(2) + io.brMetricsA(3) // BBBA S1
    brMetricA(1) := io.brMetricsA(0) + io.brMetricsA(1) + io.brMetricsB(2) + io.brMetricsA(3) // AABA S3
    brMetricB(1) := io.brMetricsB(0) + io.brMetricsB(1) + io.brMetricsA(2) + io.brMetricsB(3) // BBAB S3
    brMetricA(2) := io.brMetricsA(0) + io.brMetricsB(1) + io.brMetricsB(2) + io.brMetricsB(3) // ABBB S5
    brMetricB(2) := io.brMetricsB(0) + io.brMetricsA(1) + io.brMetricsA(2) + io.brMetricsA(3) // BAAA S5
    brMetricA(3) := io.brMetricsA(0) + io.brMetricsB(1) + io.brMetricsA(2) + io.brMetricsA(3) // ABAA S7
    brMetricB(3) := io.brMetricsB(0) + io.brMetricsA(1) + io.brMetricsB(2) + io.brMetricsB(3) // BABB S7
  }
  io.brMetrics4D(0) := Mux(brMetricA(0) < brMetricB(0), brMetricA(0), brMetricB(0))
  io.brMetrics4D(1) := Mux(brMetricA(1) < brMetricB(1), brMetricA(1), brMetricB(1))
  io.brMetrics4D(2) := Mux(brMetricA(2) < brMetricB(2), brMetricA(2), brMetricB(2))
  io.brMetrics4D(3) := Mux(brMetricA(3) < brMetricB(3), brMetricA(3), brMetricB(3))
}
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
  val sumBrMetricA = Vec(4, SInt(symBitWidth.W))
  val sumBrMetricB = Vec(4, SInt(symBitWidth.W))

  if (isEvenState) {
    sumBrMetricA(0) := io.brMetricsA(0) + io.brMetricsA(1) + io.brMetricsA(2) + io.brMetricsA(3) // AAAA S0
    sumBrMetricB(0) := io.brMetricsB(0) + io.brMetricsB(1) + io.brMetricsB(2) + io.brMetricsB(3) // BBBB S0
    sumBrMetricA(1) := io.brMetricsA(0) + io.brMetricsA(1) + io.brMetricsB(2) + io.brMetricsB(3) // AABB S2
    sumBrMetricB(1) := io.brMetricsB(0) + io.brMetricsB(1) + io.brMetricsA(2) + io.brMetricsA(3) // BBAA S2
    sumBrMetricA(2) := io.brMetricsA(0) + io.brMetricsB(1) + io.brMetricsB(2) + io.brMetricsA(3) // ABBA S4
    sumBrMetricB(2) := io.brMetricsB(0) + io.brMetricsA(1) + io.brMetricsA(2) + io.brMetricsB(3) // BAAB S4
    sumBrMetricA(3) := io.brMetricsA(0) + io.brMetricsB(1) + io.brMetricsA(2) + io.brMetricsB(3) // ABAB S6
    sumBrMetricB(3) := io.brMetricsB(0) + io.brMetricsA(1) + io.brMetricsB(2) + io.brMetricsA(3) // BABA S6
  } else {
    sumBrMetricA(0) := io.brMetricsA(0) + io.brMetricsA(1) + io.brMetricsA(2) + io.brMetricsB(3) // AAAB S1
    sumBrMetricB(0) := io.brMetricsB(0) + io.brMetricsB(1) + io.brMetricsB(2) + io.brMetricsA(3) // BBBA S1
    sumBrMetricA(1) := io.brMetricsA(0) + io.brMetricsA(1) + io.brMetricsB(2) + io.brMetricsA(3) // AABA S3
    sumBrMetricB(1) := io.brMetricsB(0) + io.brMetricsB(1) + io.brMetricsA(2) + io.brMetricsB(3) // BBAB S3
    sumBrMetricA(2) := io.brMetricsA(0) + io.brMetricsB(1) + io.brMetricsB(2) + io.brMetricsB(3) // ABBB S5
    sumBrMetricB(2) := io.brMetricsB(0) + io.brMetricsA(1) + io.brMetricsA(2) + io.brMetricsA(3) // BAAA S5
    sumBrMetricA(3) := io.brMetricsA(0) + io.brMetricsB(1) + io.brMetricsA(2) + io.brMetricsA(3) // ABAA S7
    sumBrMetricB(3) := io.brMetricsB(0) + io.brMetricsA(1) + io.brMetricsB(2) + io.brMetricsB(3) // BABB S7
  }
  io.brMetrics4D(0) := Mux(sumBrMetricA(0) < sumBrMetricB(0), sumBrMetricA(0), sumBrMetricB(0))
  io.brMetrics4D(1) := Mux(sumBrMetricA(1) < sumBrMetricB(1), sumBrMetricA(1), sumBrMetricB(1))
  io.brMetrics4D(2) := Mux(sumBrMetricA(2) < sumBrMetricB(2), sumBrMetricA(2), sumBrMetricB(2))
  io.brMetrics4D(3) := Mux(sumBrMetricA(3) < sumBrMetricB(3), sumBrMetricA(3), sumBrMetricB(3))
}
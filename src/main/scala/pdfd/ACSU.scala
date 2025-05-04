package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** ACSU module that compute the path metrics for each active path
  *
  */
class ACSU(bmWidth: Int)
    extends Module {
  // local parameters
  val pmWidth = bmWidth + 2

  val io = IO(new Bundle {
    val brMetrics4D = Input(Vec(4, UInt(bmWidth.W)))
    val pathMetrics = Input(Vec(4, UInt(pmWidth.W)))
    val pathSelect = Output(UInt(2.W))
    val pathMetric = Output(UInt(pmWidth.W))
  })
  
  val pathMetricReg = RegInit(0.U(pmWidth.W))
  io.pathMetric := pathMetricReg

  // Sum the path metric and branch metric
  val sum0 = io.pathMetrics(0) + io.brMetrics4D(0)
  val sum1 = io.pathMetrics(1) + io.brMetrics4D(1)
  val sum2 = io.pathMetrics(2) + io.brMetrics4D(2)
  val sum3 = io.pathMetrics(3) + io.brMetrics4D(3)

  // Get the compare signs for the six comparisons
  val dist0 = sum2.asSInt - sum3.asSInt
  val dist1 = sum1.asSInt - sum3.asSInt
  val dist2 = sum0.asSInt - sum3.asSInt
  val dist3 = sum1.asSInt - sum2.asSInt
  val dist4 = sum0.asSInt - sum2.asSInt
  val dist5 = sum0.asSInt - sum1.asSInt

  val msb = dist0.getWidth - 1

  // Select which of the four sums is smallest
  when (dist2(msb) && dist4(msb) && dist5(msb)) {
    // sum0 < sum2, sum0 < sum1, !(sum3 < sum0)
    io.pathSelect := 0.U
    pathMetricReg := sum0
  } .elsewhen (dist1(msb) && dist3(msb) && !dist5(msb)) {
    // sum1 < sum3, sum1 < sum2, !(sum0 < sum1)
    io.pathSelect := 1.U
    pathMetricReg := sum1
  } .elsewhen (dist0(msb) && !dist3(msb) && !dist4(msb)) {
    // sum2 < sum3, !(sum1 < sum2), !(sum0 < sum2)
    io.pathSelect := 2.U
    pathMetricReg := sum2
  } .otherwise { // Assume this is when (!dist0 && !dist1 && !dist2)
    // !(sum2 < sum3), !(sum1 < sum3), !(sum0 < sum3)
    io.pathSelect := 3.U
    pathMetricReg := sum3
  }
}
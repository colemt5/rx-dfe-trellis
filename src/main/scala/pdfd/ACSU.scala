package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** ACSU module that compute the path metrics for each active path
  *
  * @param bmWidth the bit width of the input and output signals
  */
class ACSU(bmWidth: Int)
    extends Module {
  val io = IO(new Bundle {
    val brMetrics4D = Input(Vec(4, UInt(bmWidth.W)))
    val pathMetrics = Input(Vec(4, SInt(bmWidth.W)))
    val pathSelect = Output(UInt(2.W))
    val pathMetric = Output(SInt(bmWidth.W))
  })
  
  val pathMetricReg = RegInit(0.S(bmWidth.W))
  io.pathMetric := pathMetricReg

  // Modulo to prevent overflow
  // val mod_val = 1 << bmWidth // 2^bmWidth
  val pm0 = io.pathMetrics(0) // % mod_val.S
  val pm1 = io.pathMetrics(1) // % mod_val.S
  val pm2 = io.pathMetrics(2) // % mod_val.S
  val pm3 = io.pathMetrics(3) // % mod_val.S

  // Sum the path metric and branch metric
  val sum0 = pm0 + io.brMetrics4D(0).asSInt
  val sum1 = pm1 + io.brMetrics4D(1).asSInt
  val sum2 = pm2 + io.brMetrics4D(2).asSInt
  val sum3 = pm3 + io.brMetrics4D(3).asSInt

  // Get the compare signs for the six comparisons
  val cmp0 = sum2 < sum3
  val cmp1 = sum1 < sum3
  val cmp2 = sum0 < sum3
  val cmp3 = sum1 < sum2
  val cmp4 = sum0 < sum2
  val cmp5 = sum0 < sum1

  // Select which of the four sums is smallest
  when (cmp2 && cmp4 && cmp5) {
    // sum0 < sum3, sum0 < sum2, sum0 < sum1
    io.pathSelect := 0.U
    pathMetricReg := sum0
  } .elsewhen (cmp1 && cmp3 && !cmp5) {
    // sum1 < sum3, sum1 < sum2, !(sum0 < sum1)
    io.pathSelect := 1.U
    pathMetricReg := sum1
  } .elsewhen (cmp0 && !cmp3 && !cmp4) {
    // sum2 < sum3, !(sum1 < sum2), !(sum0 < sum2)
    io.pathSelect := 2.U
    pathMetricReg := sum2
  } .otherwise { // Assume this is when (!cmp0 && !cmp1 && !cmp2)
    // !(sum2 < sum3), !(sum1 < sum3), !(sum0 < sum3)
    io.pathSelect := 3.U
    pathMetricReg := sum3
  }
}
package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** ACSU module that compute the path metrics for each active path
  *
  * @param symBitWidth the bit width of the input and output signals
  */
class ACSU(symBitWidth: Int)
    extends Module {
  val io = IO(new Bundle {
    val brMetrics4D = Input(Vec(4, SInt(symBitWidth.W))) // todo need to verify if int or fixed point
    val pathMetrics = Input(Vec(4, SInt(symBitWidth.W))) // todo need to verify if int or fixed point
    val pathSelect = Output(UInt(2.W))
    val pathMetric = Output(SInt(symBitWidth.W)) // todo need to verify if int or fixed point
  })
  
  // Modulo to prevent overflow
  val mod_val = 1 << symBitWidth // 2^symBitWidth
  val pm0 = io.pathMetrics(0) % mod_val
  val pm1 = io.pathMetrics(1) % mod_val
  val pm2 = io.pathMetrics(2) % mod_val
  val pm3 = io.pathMetrics(3) % mod_val

  // Sum the path metric and branch metric
  // sum_k = PM_k + BM_(n-k)
  // PM starting from state_k + BM from state0 to state_(n-k)
  // n = 3, k starts from 0 and ends at 3
  val sum0 = pm0 + io.brMetrics4D(3)
  val sum1 = pm1 + io.brMetrics4D(2)
  val sum2 = pm2 + io.brMetrics4D(1)
  val sum3 = pm3 + io.brMetrics4D(0)

  // Get the compare signs for the six comparisons
  val cmp0 = sum2 < sum3
  val cmp1 = sum1 < sum3
  val cmp2 = sum0 < sum3
  val cmp3 = sum1 < sum2
  val cmp4 = sum0 < sum2
  val cmp5 = sum0 < sum1

  // Select which of the four sums is smallest
  if (cmp2 && cmp4 && cmp5) {
    // sum0 < sum3, sum0 < sum2, sum0 < sum1
    io.pathSelect := 0.U
    io.pathMetric := sum0
  } else if (cmp1 && cmp3 && !cmp5) {
    // sum1 < sum3, sum1 < sum2, !(sum0 < sum1)
    io.pathSelect := 1.U
    io.pathMetric := sum1
  } else if (cmp0 && !cmp3 && !cmp4) {
    // sum2 < sum3, !(sum1 < sum2), !(sum0 < sum2)
    io.pathSelect := 2.U
    io.pathMetric := sum2
  } else { // Assume this is when (!cmp0 && !cmp1 && !cmp2)
    // !(sum2 < sum3), !(sum1 < sum3), !(sum0 < sum3)
    io.pathSelect := 3.U
    io.pathMetric := sum3
  }
}
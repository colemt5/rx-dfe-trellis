package pdfd

import chisel3._
import chisel3.util.log2Ceil

/** MUXU module that take all possible branch metrics and selects one of them
  * based on the input selection signal.
  * 
  * @param bitWidth the bit width of the input and output signals
  */
class MUXU(bitWidth: Int) // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    val sel = Input(UInt(3.W))
    val inBranchMetricsA = Input(Vec(5, SInt(bitWidth.W))) // todo need to verify if int or fixed point
    val inBranchMetricsB = Input(Vec(5, SInt(bitWidth.W))) // todo need to verify if int or fixed point
    val branchMetricA = Output(SInt(bitWidth.W)) // todo need to verify if int or fixed point
    val branchMetricB = Output(SInt(bitWidth.W)) // todo need to verify if int or fixed point
  })
  
  // Default to zero
  io.branchMetricA := 0.S
  io.branchMetricB := 0.S

  switch(io.sel) {
    is(0.U) {
      io.branchMetricA := io.inBranchMetricsA(0)
      io.branchMetricB := io.inBranchMetricsB(0)
    }
    is(1.U) {
      io.branchMetricA := io.inBranchMetricsA(1)
      io.branchMetricB := io.inBranchMetricsB(1)
    }
    is(2.U) {
      io.branchMetricA := io.inBranchMetricsA(2)
      io.branchMetricB := io.inBranchMetricsB(2)
    }
    is(3.U) {
      io.branchMetricA := io.inBranchMetricsA(3)
      io.branchMetricB := io.inBranchMetricsB(3)
    }
    is(4.U) {
      io.branchMetricA := io.inBranchMetricsA(4)
      io.branchMetricB := io.inBranchMetricsB(4)
    }
  }
}
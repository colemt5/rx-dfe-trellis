package pdfd

import chisel3._
import chisel3.util.log2Ceil

/** 1D LaBMU module that computes the branch metrics for the 1D Lookahead
  * Branch Metric Unit (LaBMU) for each of the 4 channel symbols.
  *
  * @param bitWidth the bit width of the input and output signals
  */
class OneDimLaBMU(bitWidth: Int) // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    val preFilteredSymbol = Input(SInt(bitWidth.W)) // todo need to verify if int or fixed point
    val chanCoeff1 = Input(SInt(bitWidth.W)) // todo need to verify if int or fixed point
    val symMetricsA = output(Vec(5, SInt(bitWidth.W))) // todo need to verify if int or fixed point
    val symMetricsB = output(Vec(5, SInt(bitWidth.W))) // todo need to verify if int or fixed point
  })
  
  // todo add module implementation


}
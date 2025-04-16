package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** 1D LaBMU module that computes the branch metrics for the 1D Lookahead
  * Branch Metric Unit (LaBMU) for each of the 4 channel symbols.
  *
  * @param symBitWidth the bit width of the input and output signals
  */
class OneDimLaBMU(symBitWidth: Int) // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    val preFilteredSymbol = Input(SInt(symBitWidth.W)) // todo need to verify if int or fixed point
    val chanCoeff1  = Input(SInt(symBitWidth.W)) // todo need to verify if int or fixed point
    val symMetricsA = Output(Vec(5, SInt(symBitWidth.W))) // todo need to verify if int or fixed point
    val symMetricsB = Output(Vec(5, SInt(symBitWidth.W))) // todo need to verify if int or fixed point
  })
  
  // todo add module implementation


}
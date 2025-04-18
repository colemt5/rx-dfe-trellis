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
  
  // todo add module implementation


}
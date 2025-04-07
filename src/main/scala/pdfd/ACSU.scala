package pdfd

import chisel3._
import chisel3.util.log2Ceil

/** ACSU module that compute the path metrics for each active path
  *
  * @param bitWidth the bit width of the input and output signals
  */
class ACSU(bitWidth: Int)
    extends Module {
  val io = IO(new Bundle {
    val brMetric4D = Input(Vec(4, SInt(bitWidth.W))) // todo need to verify if int or fixed point
    val pathMetrics = Input(Vec(4, SInt(bitWidth.W))) // todo need to verify if int or fixed point
    val pathSelect = Output(Vec(2, UInt(2.W)))
    val pathMetric = Output(Vec(4, SInt(bitWidth.W))) // todo need to verify if int or fixed point
  })
  
  // todo add module implementation


}
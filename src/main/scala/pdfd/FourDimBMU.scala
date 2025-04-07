package pdfd

import chisel3._
import chisel3.util.log2Ceil
import javax.swing.InputMap
import javax.xml.transform.OutputKeys

/** 4d BMU module that computes the combined branch metric for all 4 channels
  * from the 1D LaBMU modules.
  *
  * @param bitWidth the bit width of the input and output signals
  */
class FourDimBMU(bitWidth: Int)
    extends Module {
  val io = IO(new Bundle { // [0] is Channel 1, [1] is Channel 2, [2] is Channel 3, [3] is Channel 4
    val symSelects = Input(Vec(4, UInt(3.W))) 
    val chanBrMetricsA = Input(Vec(4, SInt(bitWidth.W))) // todo need to verify if int or fixed point
    val chanBrMetricsB = Input(Vec(4, SInt(bitWidth.W))) // todo need to verify if int or fixed point
    val symSelects4D = Output(Vec(4, Vec(4, UInt(3.W))))
    val brMetrics4D = Output(Vec(4, SInt(bitWidth.W))) // todo need to verify if int or fixed point
  })
  
  // todo add module implementation


}
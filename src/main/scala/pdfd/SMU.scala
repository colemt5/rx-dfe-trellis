package pdfd

import chisel3._
import chisel3.util.log2Ceil

/** SMU module keeps track of the path metrics to reconstruct the most
 * likely bit sequence.
  *
  */
class SMU() // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    val pathSelect = Input(Vec(2, UInt(2.W)))
    val symSelects4d = Input(Vec(4, Vec(4, UInt(3.W))))
    val symSelects = Input(Vec(4, UInt(3.W)))
  })
  
  // todo add module implementation


}
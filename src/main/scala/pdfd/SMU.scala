package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** SMU module keeps track of the path metrics to reconstruct the most
 * likely bit sequence.
  *
  */
class SMU() // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    val pathSelect = Input(UInt(2.W))
    val stateSymSelects = Input(Vec(4, Vec(4, UInt(3.W))))
    val byteOut = Output(UInt(8.W))
    val symSelects = Output(Vec(4, UInt(3.W)))
  })
  
  
}
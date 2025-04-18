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
    val byteOut = Output(UInt(8.W)) // todo change to byteResult
    val symSelects = Output(Vec(4, UInt(3.W)))
  })

  // todo these should be inputs
  val byteChoices = VecInit(Seq.fill(14)(0.U(8.W)))
  val byteInputs  = VecInit(Seq.fill(3)(VecInit(Seq.fill(13)(0.U(8.W)))))

  val symSurvivor = RegInit(VecInit(Seq.fill(4)(0.U(3.W))))

  symSurvivor = MuxLookup(io.pathSelect, io.stateSymSelects(0))(Seq(
    0.U -> io.stateSymSelects(0),
    1.U -> io.stateSymSelects(1),
    2.U -> io.stateSymSelects(2),
    3.U -> io.stateSymSelects(3)))

  val shiftReg = RegInit(VecInit(Seq.fill(13)(0.U(8.W))))

  byteChoices(0) := 0.U(8.W) // todo create a function that decodes symSurvivor to byteChoices(0)

  for (i <- 0 until 12) {
    shiftReg(i) := MuxLookup(io.pathSelect, 0.U(8.W))(byteChoices(i), byteInputs(0)(i), byteInputs(1)(i), byteInputs(2)(i))
    byteChoices(i + 1) := shiftReg(i)
  }

  io.byteOut := byteChoices(13)
  io.symSelects := symSurvivor
}
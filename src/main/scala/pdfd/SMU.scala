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
    val stateSymSelects = Input(Vec(4, Vec(4, SInt(3.W))))
    val byteInputs = Input(Vec(4, Vec(13, UInt(8.W))))
    val byteChoices = Output(Vec(13, UInt(8.W)))
    val symSelects = Output(Vec(4, SInt(3.W)))
    val byteDecision = Output(UInt(8.W))
  })

  val symSurvivor = RegInit(VecInit(Seq.fill(4)(0.S(3.W))))

  symSurvivor := MuxLookup(io.pathSelect, io.stateSymSelects(0))(Seq(
    0.U -> io.stateSymSelects(0),
    1.U -> io.stateSymSelects(1),
    2.U -> io.stateSymSelects(2),
    3.U -> io.stateSymSelects(3)))

  val shiftReg = RegInit(VecInit(Seq.fill(13)(0.U(8.W))))

  for (i <- 0 until 13) {
    shiftReg(i) := MuxLookup(io.pathSelect, 0.U(8.W))(Seq(
      0.U -> io.byteInputs(0)(i), 
      1.U -> io.byteInputs(1)(i), 
      2.U -> io.byteInputs(2)(i), 
      3.U -> io.byteInputs(3)(i)))
  }

  io.byteChoices(0) := Cat(symSurvivor(0)(1,0), symSurvivor(1)(1,0), symSurvivor(2)(1,0), symSurvivor(3)(1,0)) // todo create a function that decodes symSurvivor to byteChoices(0)
  for (i <- 0 until 12) {
    io.byteChoices(i + 1) := shiftReg(i)
  }
  io.byteDecision := shiftReg(12)

  io.symSelects := symSurvivor
}
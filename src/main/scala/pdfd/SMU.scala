package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** SMU module keeps track of the path metrics to reconstruct the most
  * likely bit sequence.
  *
  */
class SMU(bitWidth: Int = 12) // width of 4 symbols (4 * 3)
    extends Module {
  val io = IO(new Bundle {
    val pathSelect = Input(UInt(2.W))
    val stateSymSelects = Input(Vec(4, Vec(4, SInt(3.W))))
    val byteInputs = Input(Vec(4, Vec(13, UInt(bitWidth.W)))) 
    val byteChoices = Output(Vec(13, UInt(bitWidth.W))) 
    val symSelects = Output(Vec(4, SInt(3.W)))
    val byteDecision = Output(UInt(bitWidth.W))
  })

  val symSurvivor = RegInit(VecInit(Seq.fill(4)(0.S(3.W))))

  symSurvivor := MuxLookup(io.pathSelect, io.stateSymSelects(0))(Seq(
    0.U -> io.stateSymSelects(0),
    1.U -> io.stateSymSelects(1),
    2.U -> io.stateSymSelects(2),
    3.U -> io.stateSymSelects(3)))

  val shiftReg = RegInit(VecInit(Seq.fill(13)(0.U(bitWidth.W))))

  for (i <- 0 until 13) {
    shiftReg(i) := MuxLookup(io.pathSelect, 0.U(bitWidth.W))(Seq(
      0.U -> io.byteInputs(0)(i), 
      1.U -> io.byteInputs(1)(i), 
      2.U -> io.byteInputs(2)(i), 
      3.U -> io.byteInputs(3)(i)))
  }

  io.byteChoices(0) := Cat(symSurvivor(0), symSurvivor(1), symSurvivor(2), symSurvivor(3)) 
  for (i <- 0 until bitWidth) {
    io.byteChoices(i + 1) := shiftReg(i)
  }
  io.byteDecision := shiftReg(bitWidth)

  io.symSelects := symSurvivor
}
package srambist

import chisel3._
import chisel3.util.log2Ceil

/** Access patterns that can be exercised by the BIST.
  *
  *   W0 - Write `data0` to each address sequentially, starting from address 0.
  *   W1 - Write `data1` to each address sequentially, starting from address 0.
  *   R0 - Reads each address sequentially and validates that it equals `data0`, 
  *        starting from address 0.
  *   R1 - Reads each address sequentially and validates that it equals `data1`, 
  *        starting from address 0.
  */
object Pattern extends Enumeration {
  type Type = Value
  val W0, W1, R0, R1 = Value
}

/** Runs a set of patterns against an SRAM to verify its behavior.
  *
  * Once the BIST is complete, `done` should be set high and remain high. If the BIST
  * failed, `fail` should be asserted on the same cycle and remain high.
  */
class SramBist(numWords: Int, dataWidth: Int, patterns: Seq[Pattern.Type])
    extends Module {
  val io = IO(new Bundle {
    // SRAM interface
    val we = Output(Bool())
    val addr = Output(UInt(log2Ceil(numWords).W))
    val din = Output(UInt(dataWidth.W))
    val dout = Input(UInt(dataWidth.W))

    // BIST interface
    val data0 = Input(UInt(dataWidth.W))
    val data1 = Input(UInt(dataWidth.W))
    val done = Output(Bool())
    val fail = Output(Bool())
  })
  
  // pattern register
  val ctr = RegInit(0.U(3.W))
  // address register
  val addrReg = RegInit(0.U(log2Ceil(numWords).W))
  val maxAddr = (numWords - 1).U
  // status registers
  val fail = RegInit(false.B)
  val done = RegInit(false.B)
  val complete = RegInit(false.B)

  io.fail := fail
  io.done := fail | (ctr === patterns.length.U)

  //Default values
  io.we := false.B
  io.addr := addrReg
  io.din := 0.U

  patterns.zipWithIndex.foreach { // A Scala `foreach` loop to iterate over the Scala sequence `patterns`.
  case (pattern, idx) => {
    when(ctr === idx.U) { // Chisel `when` statement to match against the hardware register `ctr`.
      pattern match { // Scala `match` statement to match against a specific Scala `Pattern` enumeration.
        case Pattern.W0 => {
          // Pattern W0 behavior
          io.we := true.B
          io.din := io.data0
          addrReg := addrReg + 1.U
          when(addrReg === maxAddr) {
            ctr := ctr + 1.U
          }
        }
        case Pattern.W1 => {
          // Pattern W1 behavior
          io.we := true.B
          io.din := io.data1
          addrReg := addrReg + 1.U
          when(addrReg === maxAddr) {
            ctr := ctr + 1.U
          }
        }
        case Pattern.R0 => {
          // Pattern R0 behavior
          io.we := false.B
          when (addrReg > 0.U) {
            fail := fail | (io.dout =/= io.data0)
          }
          when (complete) {
            addrReg := 0.U
            complete := false.B
            ctr := ctr + 1.U
          }.otherwise {
            when (addrReg === maxAddr) {
              complete := true.B
            }.otherwise {
              addrReg := addrReg + 1.U
            }
          }
        }
        case Pattern.R1 => {
          // Pattern R1 behavior
          io.we := false.B
          when (addrReg > 0.U) {
            fail := fail | (io.dout =/= io.data1)
          }
          when (complete) {
            addrReg := 0.U
            complete := false.B
            ctr := ctr + 1.U
          }.otherwise {
            when (addrReg === maxAddr) {
              complete := true.B
            }.otherwise {
              addrReg := addrReg + 1.U
            }
          }
        }
      }
    }
  }
};

}
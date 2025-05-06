import chisel3._
import chisel3.util._

/** Enum for condition values */
object Condition extends ChiselEnum {
  val Normal, xmt_err, CSExtend_Err, CSExtend, CSReset,
      SSD1, SSD2, ESD1, ESD2_Ext_0, ESD2_Ext_1, ESD2_Ext_2,
      ESD_Ext_Err, Idle_CarrExt = Value
}

class ReverseLookupTableModuleError extends Module {
  val io = IO(new Bundle {
    val condition = Input(Condition()) // Enum input for condition
    val sdn_6_8 = Output(UInt(3.W)) // 3-bit Sdn[6:8]
    val tA = Input(SInt(8.W)) // TAn
    val tB = Input(SInt(8.W)) // TBn
    val tC = Input(SInt(8.W)) // TCn
    val tD = Input(SInt(8.W)) // TDn
  })

  // Default output values
  io.sdn_6_8 := 0.U

val reverseLookup_xxxxxx = Seq(
  ((0.S,2.S,2.S,0.S), Condition.xmt_err) -> "b000".U,
  ((1.S,1.S,2.S,2.S), Condition.xmt_err) -> "b010".U,
  ((2.S,1.S,1.S,2.S), Condition.xmt_err) -> "b100".U,
  ((2.S,1.S,2.S,1.S), Condition.xmt_err) -> "b110".U,
  ((-2.S,2.S,2.S,-2.S), Condition.CSExtend_Err) -> "b000".U,
  ((-1.S,-1.S,2.S,2.S), Condition.CSExtend_Err) -> "b010".U,
  ((2.S,-1.S,-1.S,2.S), Condition.CSExtend_Err) -> "b100".U,
  ((2.S,-1.S,2.S,-1.S), Condition.CSExtend_Err) -> "b110".U,
  ((2.S,0.S,0.S,2.S), Condition.CSExtend) -> "b000".U,
  ((2.S,2.S,1.S,1.S), Condition.CSExtend) -> "b010".U,
  ((1.S,2.S,2.S,1.S), Condition.CSExtend) -> "b100".U,
  ((1.S,2.S,1.S,2.S), Condition.CSExtend) -> "b110".U,
  ((2.S,-2.S,-2.S,2.S), Condition.CSReset) -> "b000".U,
  ((2.S,2.S,-1.S,-1.S), Condition.CSReset) -> "b010".U,
  ((-1.S,2.S,2.S,-1.S), Condition.CSReset) -> "b100".U,
  ((-1.S,2.S,-1.S,2.S), Condition.CSReset) -> "b110".U,
  ((2.S,2.S,2.S,2.S), Condition.SSD1) -> "b000".U,
  ((2.S,2.S,2.S,2.S), Condition.SSD1) -> "b010".U,
  ((2.S,2.S,2.S,2.S), Condition.SSD1) -> "b100".U,
  ((2.S,2.S,2.S,2.S), Condition.SSD1) -> "b110".U,
  ((2.S,2.S,2.S,-2.S), Condition.SSD2) -> "b000".U,
  ((2.S,2.S,2.S,-2.S), Condition.SSD2) -> "b010".U,
  ((2.S,2.S,2.S,-2.S), Condition.SSD2) -> "b100".U,
  ((2.S,2.S,2.S,-2.S), Condition.SSD2) -> "b110".U,
  ((2.S,2.S,2.S,2.S), Condition.ESD1) -> "b000".U,
  ((2.S,2.S,2.S,2.S), Condition.ESD1) -> "b010".U,
  ((2.S,2.S,2.S,2.S), Condition.ESD1) -> "b100".U,
  ((2.S,2.S,2.S,2.S), Condition.ESD1) -> "b110".U,
  ((2.S,2.S,2.S,-2.S), Condition.ESD2_Ext_0) -> "b000".U,
  ((2.S,2.S,2.S,-2.S), Condition.ESD2_Ext_0) -> "b010".U,
  ((2.S,2.S,2.S,-2.S), Condition.ESD2_Ext_0) -> "b100".U,
  ((2.S,2.S,2.S,-2.S), Condition.ESD2_Ext_0) -> "b110".U,
  ((2.S,2.S,-2.S,2.S), Condition.ESD2_Ext_1) -> "b000".U,
  ((2.S,2.S,-2.S,2.S), Condition.ESD2_Ext_1) -> "b010".U,
  ((2.S,2.S,-2.S,2.S), Condition.ESD2_Ext_1) -> "b100".U,
  ((2.S,2.S,-2.S,2.S), Condition.ESD2_Ext_1) -> "b110".U,
  ((2.S,-2.S,2.S,2.S), Condition.ESD2_Ext_2) -> "b000".U,
  ((2.S,-2.S,2.S,2.S), Condition.ESD2_Ext_2) -> "b010".U,
  ((2.S,-2.S,2.S,2.S), Condition.ESD2_Ext_2) -> "b100".U,
  ((2.S,-2.S,2.S,2.S), Condition.ESD2_Ext_2) -> "b110".U,
  ((-2.S,2.S,2.S,2.S), Condition.ESD_Ext_Err) -> "b000".U,
  ((-2.S,2.S,2.S,2.S), Condition.ESD_Ext_Err) -> "b010".U,
  ((-2.S,2.S,2.S,2.S), Condition.ESD_Ext_Err) -> "b100".U,
  ((-2.S,2.S,2.S,2.S), Condition.ESD_Ext_Err) -> "b110".U,
  ((2.S,2.S,0.S,1.S), Condition.xmt_err) -> "b001".U,
  ((0.S,2.S,1.S,2.S), Condition.xmt_err) -> "b011".U,
  ((1.S,2.S,2.S,0.S), Condition.xmt_err) -> "b101".U,
  ((2.S,1.S,2.S,0.S), Condition.xmt_err) -> "b111".U,
  ((2.S,2.S,-2.S,-1.S), Condition.CSExtend_Err) -> "b001".U,
  ((-2.S,2.S,-1.S,2.S), Condition.CSExtend_Err) -> "b011".U,
  ((-1.S,2.S,2.S,-2.S), Condition.CSExtend_Err) -> "b101".U,
  ((2.S,-1.S,2.S,-2.S), Condition.CSExtend_Err) -> "b111".U,
  ((2.S,0.S,2.S,1.S), Condition.CSExtend) -> "b001".U,
  ((2.S,0.S,1.S,2.S), Condition.CSExtend) -> "b011".U,
  ((1.S,0.S,2.S,2.S), Condition.CSExtend) -> "b101".U,
  ((2.S,1.S,0.S,2.S), Condition.CSExtend) -> "b111".U,
  ((2.S,-2.S,2.S,-1.S), Condition.CSReset) -> "b001".U,
  ((2.S,-2.S,-1.S,2.S), Condition.CSReset) -> "b011".U,
  ((-1.S,-2.S,2.S,2.S), Condition.CSReset) -> "b101".U,
  ((2.S,-1.S,-2.S,2.S), Condition.CSReset) -> "b111".U
)

  // Second match in reverseLookup_xxxxxx (only 3-bit output)
  for (((tValues, cond), sdn6_8) <- reverseLookup_xxxxxx) {
    when(io.condition === cond &&
         io.tA === tValues._1 &&
         io.tB === tValues._2 &&
         io.tC === tValues._3 &&
         io.tD === tValues._4) {
      io.sdn_6_8 := sdn6_8
    }
  }
}
// object ReverseLookupTableModule extends App {
//   (new chisel3.stage.ChiselStage).emitVerilog(new ReverseLookupTableModule)
// }
// println(getVerilog(new ReverseLookupTableModule))
package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** SymMux helper module that takes in two sets of branch metrics and selects one
  * based on the input selection signal. Takes in all channels
  * 
  * @param symBitWidth the bit width of the input and output signals
  */

class SymMux(symBitWidth: Int) extends Module {
  val io = IO(new Bundle {
    val symSelect = Input(UInt(3.W))
    val symMetricA = Input(Vec(5, SInt(symBitWidth.W)))
    val symMetricB = Input(Vec(5, SInt(symBitWidth.W)))
    val brMetricA = Output(SInt(symBitWidth.W))
    val brMetricB = Output(SInt(symBitWidth.W))
  })

  when (io.symSelect === 0.U) {
    io.brMetricA := io.symMetricA(0)
    io.brMetricB := io.symMetricB(0)
  }
  .elsewhen (io.symSelect === 1.U) {
    io.brMetricA := io.symMetricA(1)
    io.brMetricB := io.symMetricB(1)
  }
  .elsewhen (io.symSelect === 2.U) {
    io.brMetricA := io.symMetricA(2)
    io.brMetricB := io.symMetricB(2)
  }
  .elsewhen (io.symSelect === 3.U) {
    io.brMetricA := io.symMetricA(3)
    io.brMetricB := io.symMetricB(3)
  }
  .elsewhen (io.symSelect === 4.U) {
    io.brMetricA := io.symMetricA(4)
    io.brMetricB := io.symMetricB(4)
  }
  .otherwise {
    io.brMetricA := 0.S
    io.brMetricB := 0.S
  }
}

/** MUXU module that take all possible branch metrics and selects one of them
  * based on the input selection signal. Takes in all channels
  * 
  * @param symBitWidth the bit width of the input and output signals
  */

class MUXU(symBitWidth: Int)
    extends Module {
  val io = IO(new Bundle {
    val symSelects = Input(Vec(4, UInt(3.W)))
    val symMetricsA = Input(Vec(4, Vec(5, SInt(symBitWidth.W))))
    val symMetricsB = Input(Vec(4, Vec(5, SInt(symBitWidth.W))))
    val brMetricsA = Output(Vec(4, SInt(symBitWidth.W)))
    val brMetricsB = Output(Vec(4, SInt(symBitWidth.W)))
})

val mux = Seq.fill(4)(Module(new SymMux(symBitWidth)))

for (i <- 0 until 4) {
  mux(i).io.symSelect := io.symSelects(i)
  mux(i).io.symMetricA := io.symMetricsA(i)
  mux(i).io.symMetricB := io.symMetricsB(i)
  io.brMetricsA(i) := mux(i).io.brMetricA
  io.brMetricsB(i) := mux(i).io.brMetricB
}
}
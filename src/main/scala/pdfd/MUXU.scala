package pdfd

import chisel3._
import chisel3.util.log2Ceil

/** SymMux helper module that takes in two sets of branch metrics and selects one
  * based on the input selection signal. Takes in all channels
  * 
  * @param bitWidth the bit width of the input and output signals
  */

class SymMux(bitWidth: Int) extends Module {
  val io = IO(new Bundle {
    val symSelect = Input(UInt(3.W))
    val symMetricsA = Input(Vec(5, SInt(bitWidth.W)))
    val symMetricsB = Input(Vec(5, SInt(bitWidth.W)))
    val brMetricA = Output(SInt(bitWidth.W))
    val brMetricB = Output(SInt(bitWidth.W))
  })

  io.brMetricA := 0.S
  io.brMetricB := 0.S

  switch(io.symSelect) {
    is(0.U) {
      io.brMetricA := io.symMetricsA(0)
      io.brMetricB := io.symMetricsB(0)
    }
    is(1.U) {
      io.brMetricA := io.symMetricsA(1)
      io.brMetricB := io.symMetricsB(1)
    }
    is(2.U) {
      io.brMetricA := io.symMetricsA(2)
      io.brMetricB := io.symMetricsB(2)
    }
    is(3.U) {
      io.brMetricA := io.symMetricsA(3)
      io.brMetricB := io.symMetricsB(3)
    }
    is(4.U) {
      io.brMetricA := io.brMetricsA(4)
      io.brMetricB := io.brMetricsB(4)
    }
  }
}

/** MUXU module that take all possible branch metrics and selects one of them
  * based on the input selection signal. Takes in all channels
  * 
  * @param bitWidth the bit width of the input and output signals
  */

class MUXU(bitWidth: Int)
    extends Module {
  val io = IO(new Bundle {
    val symSelects = Input(Vec(4, UInt(3.W)))
    val brMetricsA = Input(Vec(4, Vec(5, SInt(bitWidth.W))))
    val brMetricsB = Input(Vec(4, Vec(5, SInt(bitWidth.W))))
    val chanBrMetricsA = Output(Vec(4, SInt(bitWidth.W)))
    val chanBrMetricsB = Output(Vec(4, SInt(bitWidth.W)))
})

val mux = Seq.fill(4)(Module(new SymMux(bitWidth)))

for (i <- 0 until 4) {
  mux(i).io.symSelect := io.symSelects(i)
  mux(i).io.brMetricsA := io.brMetricsA(i)
  mux(i).io.brMetricsB := io.brMetricsB(i)
  io.brMetricA(i) := mux(i).io.brMetricA
  io.brMetricB(i) := mux(i).io.brMetricB
}
}
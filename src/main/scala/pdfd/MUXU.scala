package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** MUXU module that take all possible branch metrics and selects one of them
  * based on the input selection signal. Computes for all 4 channels.
  * 
  */

class MUXU(symBitWidth: Int)
    extends Module {
  val io = IO(new Bundle {
    val symSelects = Input(Vec(4, SInt(3.W)))
    val symsA = Input(Vec(4, Vec(5, SInt(3.W))))
    val symsB = Input(Vec(4, Vec(5, SInt(3.W))))
    val symMetricsA = Input(Vec(4, Vec(5, UInt(symBitWidth.W))))
    val symMetricsB = Input(Vec(4, Vec(5, UInt(symBitWidth.W))))
    val brMetricsA = Output(Vec(4, UInt(symBitWidth.W)))
    val brMetricsB = Output(Vec(4, UInt(symBitWidth.W)))
    val brSymsA = Output(Vec(4, SInt(3.W)))
    val brSymsB = Output(Vec(4, SInt(3.W)))
})

val mux = Seq.fill(4)(Module(new SymMux(symBitWidth)))

for (i <- 0 until 4) {
  mux(i).io.symsA := io.symsA(i)
  mux(i).io.symsB := io.symsB(i)
  mux(i).io.symSelect := io.symSelects(i)
  mux(i).io.symMetricA := io.symMetricsA(i)
  mux(i).io.symMetricB := io.symMetricsB(i)
  io.brMetricsA(i) := mux(i).io.brMetricA
  io.brMetricsB(i) := mux(i).io.brMetricB
  io.brSymsA(i) := mux(i).io.brSymA
  io.brSymsB(i) := mux(i).io.brSymB
}
}

/** SymMux helper module that takes in two sets of branch metrics and selects one
  * based on the input selection signal
  * 
  */

class SymMux(symBitWidth: Int) extends Module {
  val io = IO(new Bundle {
    val symSelect = Input(SInt(3.W))
    val symMetricA = Input(Vec(5, UInt(symBitWidth.W)))
    val symMetricB = Input(Vec(5, UInt(symBitWidth.W)))
    val symsA = Input(Vec(5, SInt(3.W)))
    val symsB = Input(Vec(5, SInt(3.W)))
    val brMetricA = Output(UInt(symBitWidth.W))
    val brMetricB = Output(UInt(symBitWidth.W))
    val brSymA = Output(SInt(3.W))
    val brSymB = Output(SInt(3.W))
  })

  when (io.symSelect === -2.S) {
    io.brMetricA := io.symMetricA(0)
    io.brMetricB := io.symMetricB(0)
    io.brSymA := io.symsA(0)
    io.brSymB := io.symsB(0)
  }
  .elsewhen (io.symSelect === -1.S) {
    io.brMetricA := io.symMetricA(1)
    io.brMetricB := io.symMetricB(1)
    io.brSymA := io.symsA(1)
    io.brSymB := io.symsB(1)
  }
  .elsewhen (io.symSelect === 0.S) {
    io.brMetricA := io.symMetricA(2)
    io.brMetricB := io.symMetricB(2)
    io.brSymA := io.symsA(2)
    io.brSymB := io.symsB(2)
  }
  .elsewhen (io.symSelect === 1.S) {
    io.brMetricA := io.symMetricA(3)
    io.brMetricB := io.symMetricB(3)
    io.brSymA := io.symsA(3)
    io.brSymB := io.symsB(3)
  }
  .elsewhen (io.symSelect === 2.S) {
    io.brMetricA := io.symMetricA(4)
    io.brMetricB := io.symMetricB(4)
    io.brSymA := io.symsA(4)
    io.brSymB := io.symsB(4)
  }
  .otherwise {
    io.brMetricA := 0.U
    io.brMetricB := 0.U
    io.brSymA := 0.S
    io.brSymB := 0.S
  }
}
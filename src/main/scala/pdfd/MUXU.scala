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
  
  val index = (io.symSelect + 2.S).asUInt

  io.brMetricA := MuxLookup(index, 0.U)(Seq(
    0.U -> io.symMetricA(0),
    1.U -> io.symMetricA(1),
    2.U -> io.symMetricA(2),
    3.U -> io.symMetricA(3),
    4.U -> io.symMetricA(4),
  ))

  io.brMetricB := MuxLookup(index, 0.U)(Seq(
    0.U -> io.symMetricB(0),
    1.U -> io.symMetricB(1),
    2.U -> io.symMetricB(2),
    3.U -> io.symMetricB(3),
    4.U -> io.symMetricB(4),
  ))

  io.brSymA := MuxLookup(index, 0.S)(Seq(
    0.U -> io.symsA(0),
    1.U -> io.symsA(1),
    2.U -> io.symsA(2),
    3.U -> io.symsA(3),
    4.U -> io.symsA(4),
  ))

  io.brSymB := MuxLookup(index, 0.S)(Seq(
    0.U -> io.symsB(0),
    1.U -> io.symsB(1),
    2.U -> io.symsB(2),
    3.U -> io.symsB(3),
    4.U -> io.symsB(4),
  ))
}
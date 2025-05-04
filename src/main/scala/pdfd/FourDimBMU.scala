package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** 4d BMU module that computes the combined branch metric for all 4 channels
  * from the 1D LaBMU modules.
  *
  */
class FourDimBMU(sampleWidth: Int, bmWidth: Int, isEvenState: Boolean)
    extends Module {
  val io = IO(new Bundle {
    val brMetricsA = Input(Vec(4, UInt(sampleWidth.W)))
    val brMetricsB = Input(Vec(4, UInt(sampleWidth.W)))
    val brSymsA = Input(Vec(4, SInt(3.W)))
    val brSymsB = Input(Vec(4, SInt(3.W)))
    val brMetrics4D = Output(Vec(4, UInt((bmWidth).W)))
    val brSyms4D = Output(Vec(4, Vec(4, SInt(3.W))))
  })
  val sumBrMetricA = Wire(Vec(4, UInt(bmWidth.W)))
  val sumBrMetricB = Wire(Vec(4, UInt(bmWidth.W)))
  val brSymsA = Wire(Vec(4, Vec(4, SInt(3.W))))
  val brSymsB = Wire(Vec(4, Vec(4, SInt(3.W))))

  // compute different branch metrics depending on even or odd state
  // there can only be even symbol combinations in even states and odd symbol combinations in odd states
  if (isEvenState) {
    sumBrMetricA(0) := io.brMetricsA(0) +& io.brMetricsA(1) +& io.brMetricsA(2) +& io.brMetricsA(3) // AAAA S0
    sumBrMetricB(0) := io.brMetricsB(0) +& io.brMetricsB(1) +& io.brMetricsB(2) +& io.brMetricsB(3) // BBBB S0
    sumBrMetricA(1) := io.brMetricsA(0) +& io.brMetricsA(1) +& io.brMetricsB(2) +& io.brMetricsB(3) // AABB S2
    sumBrMetricB(1) := io.brMetricsB(0) +& io.brMetricsB(1) +& io.brMetricsA(2) +& io.brMetricsA(3) // BBAA S2
    sumBrMetricA(2) := io.brMetricsA(0) +& io.brMetricsB(1) +& io.brMetricsB(2) +& io.brMetricsA(3) // ABBA S4
    sumBrMetricB(2) := io.brMetricsB(0) +& io.brMetricsA(1) +& io.brMetricsA(2) +& io.brMetricsB(3) // BAAB S4
    sumBrMetricA(3) := io.brMetricsA(0) +& io.brMetricsB(1) +& io.brMetricsA(2) +& io.brMetricsB(3) // ABAB S6
    sumBrMetricB(3) := io.brMetricsB(0) +& io.brMetricsA(1) +& io.brMetricsB(2) +& io.brMetricsA(3) // BABA S6
    brSymsA(0) := VecInit(Seq(io.brSymsA(0), io.brSymsA(1), io.brSymsA(2), io.brSymsA(3))) // AAAA S0
    brSymsB(0) := VecInit(Seq(io.brSymsB(0), io.brSymsB(1), io.brSymsB(2), io.brSymsB(3))) // BBBB S0
    brSymsA(1) := VecInit(Seq(io.brSymsA(0), io.brSymsA(1), io.brSymsB(2), io.brSymsB(3))) // AABB S2
    brSymsB(1) := VecInit(Seq(io.brSymsB(0), io.brSymsB(1), io.brSymsA(2), io.brSymsA(3))) // BBAA S2
    brSymsA(2) := VecInit(Seq(io.brSymsA(0), io.brSymsB(1), io.brSymsB(2), io.brSymsA(3))) // ABBA S4
    brSymsB(2) := VecInit(Seq(io.brSymsB(0), io.brSymsA(1), io.brSymsA(2), io.brSymsB(3))) // BAAB S4
    brSymsA(3) := VecInit(Seq(io.brSymsA(0), io.brSymsB(1), io.brSymsA(2), io.brSymsB(3))) // ABAB S6
    brSymsB(3) := VecInit(Seq(io.brSymsB(0), io.brSymsA(1), io.brSymsB(2), io.brSymsA(3))) // BABA S6
  } else {
    sumBrMetricA(0) := io.brMetricsA(0) +& io.brMetricsA(1) +& io.brMetricsA(2) +& io.brMetricsB(3) // AAAB S1
    sumBrMetricB(0) := io.brMetricsB(0) +& io.brMetricsB(1) +& io.brMetricsB(2) +& io.brMetricsA(3) // BBBA S1
    sumBrMetricA(1) := io.brMetricsA(0) +& io.brMetricsA(1) +& io.brMetricsB(2) +& io.brMetricsA(3) // AABA S3
    sumBrMetricB(1) := io.brMetricsB(0) +& io.brMetricsB(1) +& io.brMetricsA(2) +& io.brMetricsB(3) // BBAB S3
    sumBrMetricA(2) := io.brMetricsA(0) +& io.brMetricsB(1) +& io.brMetricsB(2) +& io.brMetricsB(3) // ABBB S5
    sumBrMetricB(2) := io.brMetricsB(0) +& io.brMetricsA(1) +& io.brMetricsA(2) +& io.brMetricsA(3) // BAAA S5
    sumBrMetricA(3) := io.brMetricsA(0) +& io.brMetricsB(1) +& io.brMetricsA(2) +& io.brMetricsA(3) // ABAA S7
    sumBrMetricB(3) := io.brMetricsB(0) +& io.brMetricsA(1) +& io.brMetricsB(2) +& io.brMetricsB(3) // BABB S7
    brSymsA(0) := VecInit(Seq(io.brSymsA(0), io.brSymsA(1), io.brSymsA(2), io.brSymsB(3))) // AAAB S1
    brSymsB(0) := VecInit(Seq(io.brSymsB(0), io.brSymsB(1), io.brSymsB(2), io.brSymsA(3))) // BBBA S1
    brSymsA(1) := VecInit(Seq(io.brSymsA(0), io.brSymsA(1), io.brSymsB(2), io.brSymsA(3))) // AABA S3
    brSymsB(1) := VecInit(Seq(io.brSymsB(0), io.brSymsB(1), io.brSymsA(2), io.brSymsB(3))) // BBAB S3
    brSymsA(2) := VecInit(Seq(io.brSymsA(0), io.brSymsB(1), io.brSymsB(2), io.brSymsB(3))) // ABBB S5
    brSymsB(2) := VecInit(Seq(io.brSymsB(0), io.brSymsA(1), io.brSymsA(2), io.brSymsA(3))) // BAAA S5
    brSymsA(3) := VecInit(Seq(io.brSymsA(0), io.brSymsB(1), io.brSymsA(2), io.brSymsA(3))) // ABAA S7
    brSymsB(3) := VecInit(Seq(io.brSymsB(0), io.brSymsA(1), io.brSymsB(2), io.brSymsB(3))) // BABB S7
  }
  for (i <- 0 until 4) {
    io.brMetrics4D(i) := Mux(sumBrMetricA(i) < sumBrMetricB(i), sumBrMetricA(i), sumBrMetricB(i))
    io.brSyms4D(i) := Mux(sumBrMetricA(i) < sumBrMetricB(i), brSymsA(i), brSymsB(i))
  }
}
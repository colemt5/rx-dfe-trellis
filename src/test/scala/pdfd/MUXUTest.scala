package pdfd

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MUXUTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "MUXU"

  it should "correctly select the branch metrics and symbols based on the selection signal" in {
    test(new MUXU(symBitWidth = 8)) { c =>
      // Sample input values (ensure all values fit within the valid range for SInt(3.W) [-4, 3])
      val symSelects = Seq(-2, -1, 0, 1) // Selection indices (range from -2 to 2)
      val symsA = Seq(
        Seq(-4, -3, -2, -1, 0),
        Seq(1, 2, 3, -4, -3),
        Seq(-2, 0, 1, 2, -1),
        Seq(0, -1, -3, 2, 1)
      ) // Example symbols for A (within the range of -4 to 3)
      val symsB = Seq(
        Seq(3, 2, 1, 0, -1),
        Seq(-2, -3, -4, 0, 1),
        Seq(2, -1, 0, -3, 3),
        Seq(-4, -2, 1, 2, 0)
      ) // Example symbols for B (within the range of -4 to 3)
      val symMetricsA = Seq(
        Seq(1, 2, 3, 4, 5),
        Seq(6, 7, 8, 9, 10),
        Seq(11, 12, 13, 14, 15),
        Seq(16, 17, 18, 19, 20)
      ) // Example metrics for A
      val symMetricsB = Seq(
        Seq(20, 19, 18, 17, 16),
        Seq(15, 14, 13, 12, 11),
        Seq(10, 9, 8, 7, 6),
        Seq(5, 4, 3, 2, 1)
      ) // Example metrics for B

      // Poke inputs
      for (i <- 0 until 4) {
        c.io.symSelects(i).poke(symSelects(i).S)
        for (j <- 0 until 5) {
          c.io.symsA(i)(j).poke(symsA(i)(j).S)
          c.io.symsB(i)(j).poke(symsB(i)(j).S)
          c.io.symMetricsA(i)(j).poke(symMetricsA(i)(j).S)
          c.io.symMetricsB(i)(j).poke(symMetricsB(i)(j).S)
        }
      }

      c.clock.step()

      // Check outputs (expect the correct branch metrics and symbols)
      for (i <- 0 until 4) {
        c.io.brMetricsA(i).expect(symMetricsA(i)(symSelects(i) + 2).S) // Adjust for selection range
        c.io.brMetricsB(i).expect(symMetricsB(i)(symSelects(i) + 2).S) // Adjust for selection range
        c.io.brSymsA(i).expect(symsA(i)(symSelects(i) + 2).S)         // Adjust for selection range
        c.io.brSymsB(i).expect(symsB(i)(symSelects(i) + 2).S)         // Adjust for selection range
      }
    }
  }
}

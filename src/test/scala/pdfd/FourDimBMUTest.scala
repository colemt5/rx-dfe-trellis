package pdfd

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FourDimBMUTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "FourDimBMU"

  it should "compute combined 4D branch metrics and symbol vectors" in {
    test(new FourDimBMU(bmWidth = 8, isEvenState = true)) { c =>
      // Test input data (arbitrary but controlled)
      val brMetricsA = Seq(5, 10, 15, 20)
      val brMetricsB = Seq(2, 4, 6, 8)

      val brSymsA = Seq(1, 2, 3, -1)
      val brSymsB = Seq(0, -2, 1, -3)

      // Poke inputs
      for (i <- 0 until 4) {
        c.io.brMetricsA(i).poke(brMetricsA(i).S)
        c.io.brMetricsB(i).poke(brMetricsB(i).S)
        c.io.brSymsA(i).poke(brSymsA(i).S)
        c.io.brSymsB(i).poke(brSymsB(i).S)
      }

      c.clock.step()

      val expectedBrMetrics = Seq(20, 29, 35, 32)
      val expectedBrSyms = Seq(
        Seq(0, -2, 1, -3),
        Seq(1, 2, 1, -3),
        Seq(0, 2, 3, -3),
        Seq(1, -2, 3, -3)
      )

      for (i <- 0 until 4) {
        c.io.brMetrics4D(i).expect(expectedBrMetrics(i).S)
        for (j <- 0 until 4) {
          c.io.brSyms4D(i)(j).expect(expectedBrSyms(i)(j).S)
        }
      }
    }
  }

  it should "compute 4D branch metrics and symbols for odd state (isEvenState = false)" in {
  test(new FourDimBMU(bmWidth = 8, isEvenState = false)) { c =>
    val brMetricsA = Seq(5, 10, 15, 20)
    val brMetricsB = Seq(2, 4, 6, 8)
    val brSymsA = Seq(1, 2, 3, -1)
    val brSymsB = Seq(0, -2, 1, -3)

    for (i <- 0 until 4) {
      c.io.brMetricsA(i).poke(brMetricsA(i).S)
      c.io.brMetricsB(i).poke(brMetricsB(i).S)
      c.io.brSymsA(i).poke(brSymsA(i).S)
      c.io.brSymsB(i).poke(brSymsB(i).S)
    }

    c.clock.step()

    val expectedBrMetrics = Seq(32, 29, 23, 26)
    val expectedBrSyms = Seq(
      Seq(0, -2, 1, -1),
      Seq(0, -2, 3, -3),
      Seq(1, -2, 1, -3),
      Seq(0, 2, 1, -3)
    )

    for (i <- 0 until 4) {
      c.io.brMetrics4D(i).expect(expectedBrMetrics(i).S)
      for (j <- 0 until 4) {
        c.io.brSyms4D(i)(j).expect(expectedBrSyms(i)(j).S)
      }
    }
  }
}

}

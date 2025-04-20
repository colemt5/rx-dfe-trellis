package pdfd

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ACSUTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ACSU"

  it should "correctly compute pathMetric and pathSelect" in {
    test(new ACSU(bmWidth = 8)) { c =>
      // Sample input values
      val brMetrics = Seq(3, 6, 2, 7) // Branch metrics
      val pathMetrics = Seq(10, 5, 8, 9) // Previous path metrics

      // Calculate expected results in software
      val accMetrics = brMetrics.zip(pathMetrics).map { case (br, pm) => br + pm }
      val minVal = accMetrics.min
      val minIdx = accMetrics.indexOf(minVal)

      // Poke inputs
      for (i <- 0 until 4) {
        c.io.brMetrics4D(i).poke(brMetrics(i).S)
        c.io.pathMetrics(i).poke(pathMetrics(i).S)
      }

      c.clock.step()

      // Check outputs
      c.io.pathSelect.expect(minIdx.U)
      c.io.pathMetric.expect(minVal.S)
    }
  }
}

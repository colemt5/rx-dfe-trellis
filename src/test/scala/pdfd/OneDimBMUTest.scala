package pdfd

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class OneDimLaBMUTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "OneDimLaBMU"

  it should "compute symMetrics and syms correctly" in {
    test(new OneDimLaBMU(tapWidth = 8, sampleWidth = 16, upSizeWidth = 16, pam5 = Seq(0, 1, -1, 2, -2))) { c =>
      // Sample input values
      val rxFilterVal = 10
      val tapOneVal = 3

      // Expected output values based on branch metric calculations
      val expectedSymMetricsA = Seq(0, 0, 0, 1, 0) // Calculated branch metrics for A
      val expectedSymMetricsB = Seq(0, 0, 0, 1, 0) // Calculated branch metrics for B
      val expectedSymsA = Seq(1, 2, -1, 3, -2) // Expected PAM5 symbols for A
      val expectedSymsB = Seq(1, 2, -1, 3, -2) // Expected PAM5 symbols for B

      // Poke inputs
      c.io.rxFilter.poke(rxFilterVal.S)
      c.io.tapOne.poke(tapOneVal.S)

      c.clock.step()

      // Check outputs for symMetricsA and symMetricsB
      c.io.symMetricsA.zip(expectedSymMetricsA).foreach { case (out, expected) =>
        out.expect(expected.S)
      }

      c.io.symMetricsB.zip(expectedSymMetricsB).foreach { case (out, expected) =>
        out.expect(expected.S)
      }

      // Check outputs for symsA and symsB
      c.io.symsA.zip(expectedSymsA).foreach { case (out, expected) =>
        out.expect(expected.S)
      }

      c.io.symsB.zip(expectedSymsB).foreach { case (out, expected) =>
        out.expect(expected.S)
      }
    }
  }
}

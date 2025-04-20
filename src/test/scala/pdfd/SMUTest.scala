package pdfd

import chisel3._
import chisel3.experimental.BundleLiterals._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SMUTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SMU"

  it should "correctly update survivor symbols and output byte choices" in {
    test(new SMU) { c =>
      val stateSymbols = Seq(
        Seq(-1, 0, 1, 2),
        Seq(2, 1, 0, -1),
        Seq(1, 1, 1, 1),
        Seq(0, 0, 0, 0)
      )

      val byteInputs = Seq.tabulate(4, 13)((i, j) => (i * 10 + j).U(8.W))

      for (cycle <- 0 until 4) {
        c.io.pathSelect.poke(cycle.U)

        // Apply state symbols and bytes
        for (i <- 0 until 4) {
          for (j <- 0 until 4) {
            val v = stateSymbols(i)(j).S(3.W)
            c.io.stateSymSelects(i)(j).poke(v)
          }
        }

        for (i <- 0 until 4) {
          for (j <- 0 until 13) {
            c.io.byteInputs(i)(j).poke(byteInputs(i)(j))
          }
        }

        c.clock.step(1)

        // Check that symSelects match selected state
        for (j <- 0 until 4) {
          c.io.symSelects(j).expect(stateSymbols(cycle)(j).S(3.W))
        }

        // Byte choices: byteChoices(0) is constructed from symSelects(1,0)
        val catByte = (
          (stateSymbols(cycle)(0) & 0x3) << 6 |
          (stateSymbols(cycle)(1) & 0x3) << 4 |
          (stateSymbols(cycle)(2) & 0x3) << 2 |
          (stateSymbols(cycle)(3) & 0x3)
        ).U(8.W)
        c.io.byteChoices(0).expect(catByte)

        // Check that the rest of byteChoices match input bytes
        for (i <- 1 until 13) {
          c.io.byteChoices(i).expect(byteInputs(cycle)(i - 1))
        }

        // Final byte decision is last of selected byteInputs
        c.io.byteDecision.expect(byteInputs(cycle)(12))
      }
    }
  }
}

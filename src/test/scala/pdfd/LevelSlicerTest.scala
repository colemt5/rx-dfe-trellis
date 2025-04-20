package pdfd

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LevelSlicerTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "levelSlicer"

  it should "correctly quantize the input based on thresholds" in {
    test(new LevelSlicerTestModule) { c =>
      val testCases = Seq(
        -20 -> -4,  // way below lowest threshold
        -4  -> -4,
        -3  -> -4,
        -2  -> -2,  // between -3 and -1
        -1  -> -2,
        0   -> 0,   // between -1 and 1
        1   -> 0,
        2   -> 2,   // between 1 and 3
        3   -> 2,
        4   -> 4,   // above highest threshold
        10  -> 4,

        // Boundary sensitivity
        -3 -> -4,   // on threshold
        -3 + 1 -> -2,
        -1 -> -2,
        -1 + 1 -> 0,
        1 -> 0,
        1 + 1 -> 2,
        3 -> 2,
        3 + 1 -> 4,

        // Fine-grained around transitions
        -3 - 1 -> -4,
        -3 + 1 -> -2,
        -1 - 1 -> -2,
        -1 + 1 -> 0,
        1 - 1 -> 0,
        1 + 1 -> 2,
        3 - 1 -> 2,
        3 + 1 -> 4
        )

      for ((in, expected) <- testCases) {
        c.io.data.poke(in.S)
        c.clock.step()
        c.io.out.expect(expected.S, s"Input $in should slice to $expected")
      }
    }
  }
}

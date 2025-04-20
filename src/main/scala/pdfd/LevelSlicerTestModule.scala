package pdfd

import chisel3._
import pdfd.Utils._

class LevelSlicerTestModule extends Module {
  val io = IO(new Bundle {
    val data = Input(SInt(8.W))
    val out = Output(SInt(8.W))
  })

  // Example 5-level slicer
  val levels = Seq(-4.S, -2.S, 0.S, 2.S, 4.S)
  val thresholds = Seq(-3.S, -1.S, 1.S, 3.S)

  io.out := levelSlicer(io.data, levels, thresholds)
}

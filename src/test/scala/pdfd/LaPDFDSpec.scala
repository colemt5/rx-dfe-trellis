
package pdfd

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

  
    // todo add test cases here

/** This is a trivial example of how to run this Specification From within sbt
  * use:
  * {{{
  * testOnly pdfd.PDFDSpec
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly pdfd.PDFDSpec'
  * }}}
  */
class LaPDFDSpec extends AnyFreeSpec with ChiselScalatestTester {

    "Check if Seq makes it through" in {
    test(
      new LaPDFD()
    ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for (i <- 0 until 17) {
        dut.io.rxSamples(0).poke(120.S(8.W))
        dut.io.rxSamples(1).poke(0.S(8.W))
        dut.io.rxSamples(2).poke(50.S(8.W))
        dut.io.rxSamples(3).poke(-70.S(8.W))
        dut.clock.step()
      }
      val outVal = dut.io.rxData.peek().litValue
      println(s"Output is: $outVal")
    }
  }
}

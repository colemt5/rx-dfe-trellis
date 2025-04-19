
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

    "Standard SRAM behavioral model should pass BIST" in {
    test(
      new LaPDFD()
    ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.rxSamples(0).poke(0.S(8.W))
      dut.io.rxSamples(1).poke(0.S(8.W))
      dut.io.rxSamples(2).poke(0.S(8.W))
      dut.io.rxSamples(3).poke(0.S(8.W))
    }
  }
}

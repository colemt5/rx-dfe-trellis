
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
      new LaPDFD(
        18,
        14
      )
    ).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.inSymbols(0).poke(0.S(18.W))
      dut.io.inSymbols(1).poke(0.S(18.W))
      dut.io.inSymbols(2).poke(0.S(18.W))
      dut.io.inSymbols(3).poke(0.S(18.W))
    }
  }
}

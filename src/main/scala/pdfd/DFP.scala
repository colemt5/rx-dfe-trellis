package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** DFP module that computes the pre-filtered symbol for each channel
  *
  * @param bitWidth the bit width of the input and output signals
  */
class DFP(bitWidth: Int, numTaps: Int = 14) // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    val inSymbol = Input(SInt(bitWidth.W)) // todo need to verify if int or fixed point
    val chanCoeffs = Input(Vec(numTaps, SInt(bitWidth.W))) // todo need to verify if int or fixed point
    val preFilteredSymbol = Output(SInt(bitWidth.W)) // todo need to verify if int or fixed point
  })

  /* 
  // Slice input symbol
  val a = RegInit(0.S(bitWidth.W))

  // Register chain 
  val y = RegInit(VecInit(Seq.fill(numTaps-1)(0.S(bitWidth.W))))

  // Compute each stage
  y(0) := -io.chanCoeffs(13) * a // f14
  for (i <- 1 until 11) { 
    y(i) := y(i - 1) + (io.chanCoeffs(13-i) * a) // f13 to f3
  }
  y(12) := io.inSymbol + y(11) + (io.chanCoeffs(1) * a) // f2

  // Compute a
  a := pam5Slice(y(12) + (io.chanCoeffs(0) * a)) // f1

  // Set output symbol
  io.preFilteredSymbol := y(12)
  */

}
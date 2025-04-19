package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** DFP module that computes the pre-filtered symbol for each channel
  *
  * @param bitWidth the bit width of the input and output signals
  */
class DFP(numTaps: Int = 14, tapWidth: Int, sampleWidth: Int, upSizeWidth: Int, pam5: Seq[SInt], pam5Thresholds: Seq[SInt])
    extends Module {
  val io = IO(new Bundle {
    val rxSample = Input(SInt(sampleWidth.W)) // todo need to verify if int or fixed point
    val taps = Input(Vec(numTaps, SInt(tapWidth.W))) // todo need to verify if int or fixed point
    val rxFilter = Output(SInt(upSizeWidth.W)) // todo need to verify if int or fixed point
  })
  /* Assumptions
    *  - The input symbol is 8 bits with DC at 0
    *  - The channel coefficients are signed 8 bits
    *  - The output symbol is 
    */
  val filtSample = RegInit(0.S(upSizeWidth.W))
  val softSym = RegInit(0.S(3.W))
  val feedbackPath = RegInit(VecInit(Seq.fill(numTaps - 2)(0.S(upSizeWidth.W)))) // 13 bits to hold

  softSym := levelSlicer(decData, pam5, pam5Thresholds)
  // f14
  feedbackPath(0) := softSym * -io.taps(numTaps - 1)
  // f13 to f3
  for (i <- 1 until numTaps - 3) {
    feedbackPath(i) := feedbackPath(i - 1) + (softSym * -io.taps(numTaps - 1 - i))
  }
  // f2
  filtSample := io.rxSample + feedbackPath(numTaps - 2) + (softSym * -io.taps(1))

  // f1
  val decData = filtSample + softSym * -io.taps(0)

  io.rxFilter := filtSample
}
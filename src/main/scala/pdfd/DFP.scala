package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** DFP module that computes the pre-filtered symbol for each channel
  *
  * @param bitWidth the bit width of the input and output signals
  */
class DFP(numTaps: Int = 14, tapWidth: Int, sampleWidth: Int, upSizeWidth: Int, pam5: Seq[Int], pam5Thresholds: Seq[Int])
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
  val pam5Vals = pam5.map(_.S(sampleWidth.W))
  val pam5Thresh = pam5Thresholds.map(_.S(sampleWidth.W))
  
  val filtSample = RegInit(0.S(upSizeWidth.W)) // y
  val softSym = RegInit(0.S(3.W)) // a
  val feedbackPath = RegInit(VecInit(Seq.fill(numTaps - 2)(0.S(upSizeWidth.W)))) // hold f3 to f14 math
  
  val decSample = filtSample + softSym * -io.taps(0) // y - (f1 * a)

  softSym := levelSlicer(decSample, pam5Vals, pam5Thresh) // d sliced to a
  
  feedbackPath(0) := softSym * -io.taps(numTaps - 1) // -f14 * a
  for (i <- 1 until numTaps - 3) {
    feedbackPath(i) := feedbackPath(i - 1) + (softSym * -io.taps(numTaps - 1 - i)) // f13 to f3 accumulate
  }
  filtSample := io.rxSample + feedbackPath(numTaps - 3) + (softSym * -io.taps(1)) // z - sum(fi * a) (i=2 to 14)

  io.rxFilter := filtSample
}
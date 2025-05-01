package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** DFP module that computes the pre-filtered symbol for each channel
  *
  * @param bitWidth the bit width of the input and output signals
  */
class DFP(numTaps: Int, tapWidth: Int, tapScale: Int, sampleWidth: Int, upSizeWidth: Int, pam5: Seq[Int], pam5Thresholds: Seq[Int])
    extends Module {
  val io = IO(new Bundle {
    val rxSample = Input(SInt(sampleWidth.W)) 
    val taps = Input(Vec(numTaps, SInt(tapWidth.W)))
    val rxFilter = Output(SInt(upSizeWidth.W)) 
  })
  /* Assumptions
    *  - The input symbol is 8 bits with DC at 0
    *  - The channel coefficients are signed 8 bits
    *  - The output symbol is 
    */
  val pam5Vals = pam5.map(_.S(sampleWidth.W))
  val pam5Thresh = pam5Thresholds.map(_.S(sampleWidth.W))
  
  val filtSample = RegInit(0.S(upSizeWidth.W)) // y
  val softSym = RegInit(0.S(sampleWidth.W)) // a
  val feedbackPath = RegInit(VecInit(Seq.fill(numTaps - 2)(0.S(20.W)))) // hold f3 to f14 math
  
  val decSample = filtSample + ((softSym * -io.taps(0)) >> tapScale) // y - (f1 * a)

  softSym := levelSlicer(Cat(decSample(sampleWidth-1), decSample(6,0)).asSInt, pam5Vals, pam5Thresh) // d sliced to a
  
  feedbackPath(0) := softSym * -io.taps(13) // -f14 * a
  feedbackPath(1) := feedbackPath(0) + (softSym * -io.taps(12))
  feedbackPath(2) := feedbackPath(1) + (softSym * -io.taps(11))
  feedbackPath(3) := feedbackPath(2) + (softSym * -io.taps(10))
  feedbackPath(4) := feedbackPath(3) + (softSym * -io.taps(9)) 
  feedbackPath(5) := feedbackPath(4) + (softSym * -io.taps(8))
  feedbackPath(6) := feedbackPath(5) + (softSym * -io.taps(7))
  feedbackPath(7) := feedbackPath(6) + (softSym * -io.taps(6))
  feedbackPath(8) := feedbackPath(7) + (softSym * -io.taps(5))
  feedbackPath(9) := feedbackPath(8) + (softSym * -io.taps(4))
  feedbackPath(10) := feedbackPath(9) + (softSym * -io.taps(3))
  feedbackPath(11) := feedbackPath(10) + (softSym * -io.taps(2))

  val alignedSum = (feedbackPath(11) + (softSym * -io.taps(1))) >> tapScale

  filtSample := io.rxSample + Cat(alignedSum(sampleWidth-1), alignedSum(6,0)).asSInt // z - sum(fi * a) (i=2 to 14)

  io.rxFilter := filtSample
}
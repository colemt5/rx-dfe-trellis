package pdfd

import chisel3._
import chisel3.util._
import pdfd.Utils._

/** DFP module that computes the pre-filtered symbol for each channel
  */
class DFP(numTaps: Int, tapWidth: Int, fracWidth: Int, sampleWidth: Int, pam5: Seq[Int], pam5Thresholds: Seq[Int])
    extends Module {
  val io = IO(new Bundle {
    val rxSample = Input(SInt(sampleWidth.W)) 
    val taps = Input(Vec(numTaps, SInt(tapWidth.W)))
    val rxFilter = Output(SInt((sampleWidth + fracWidth).W)) 
  })

  // convert to Chisel
  val pam5Vals = pam5.map(_.S(sampleWidth.W))
  val pam5Thresh = pam5Thresholds.map(_.S(sampleWidth.W))
  
  val filtSample = RegInit(0.S(18.W))
  val softSym = RegInit(0.S(sampleWidth.W)) 
  val feedbackPath = RegInit(VecInit(Seq.fill(numTaps - 2)(0.S(18.W)))) // hold f3 to f14 math

  // filtSample + softSym * -f1 scaled back to normal by dividing by 128
  val decSample = (filtSample + (softSym * -io.taps(0))) >> fracWidth 

  softSym := levelSlicer(decSample, pam5Vals, pam5Thresh)

  // sum(softSym * tapScale * tap)
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

  // sample * tapScale
  val scaledSample = Cat(io.rxSample, 0.U(fracWidth.W)).asSInt
  
  // tapScale * sample + tapScale * sum(softSym * tap)
  filtSample := scaledSample + (feedbackPath(11) + (softSym * -io.taps(1)))

  // output is still in fixed point
  io.rxFilter := filtSample 
}
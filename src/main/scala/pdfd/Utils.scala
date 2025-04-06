package pdfd

import chisel3._

object Utils {
  /** 
   * Function to slice the input symbol into 5 levels
   * 
   * @param input SInt input symbol //todo need to verify if int or fixed point
   * @param output SInt output symbol // todo need to verify if int or fixed point
   */
  def pam5Slice(input: SInt): SInt = {
    val out = Wire(SInt(input.getWidth.W))
    // todo need to understand what levels to slice at
    when (input <= (-1.5).S) {
      out := (-2).S
    } .elsewhen (input <= (-0.5).S) {
      out := (-1).S
    } .elsewhen (input <= 0.5.S) {
      out := 0.S
    } .elsewhen (input <= 1.S) {
      out := 1.S
    } .otherwise {
      out := 2.S
    }
    out
  }
  
  /**
   * Approximate squaring using log-based estimation.
   * Approximates x^2 ≈ 2^(2 * log2(x)) using bit length.
   *
   * @param input UInt value to be squared (must be > 0)
   * @param output Approximate square as UInt
   */
  def logApproxSquare(input: UInt): UInt = {
    val width = input.getWidth
    val out = Wire(UInt((2 * width).W))
    when (input === 0.U) {
      out := 0.U
    } .otherwise {
      val approxExp = (Log2(input) << 1).asUInt  // multiply log2 by 2
      out := (1.U << approxExp).asUInt
    }
    out
  }

  /**
   * Approximate squaring for signed inputs using log-based estimation.
   * Approximates x^2 ≈ 2^(2 * log2(|x|)) using bit length.
   *
   * @param input SInt value to be squared
   * @param output Approximate square as UInt
   */
  def logApproxSquare(input: SInt): UInt = {
    val absInput = Wire(UInt(input.getWidth.W))
    absInput := Mux(input < 0.S, (-input).asUInt, input.asUInt)
    logApproxSquare(absInput)
  }

}
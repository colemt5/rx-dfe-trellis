package pdfd

import chisel3._
import chisel3.util._

object Utils {
  /** 
   * Function to slice the input symbol into 5 levels
   * 
   * @param input SInt input symbol //todo need to verify if int or fixed point
   * @return output SInt output symbol // todo need to verify if int or fixed point
   */
  def pam5Slice(input: SInt): SInt = {
    val out = input 
    out
    // todo need to understand what levels to slice at
  }
  
  /**
   * Approximate squaring using log-based estimation.
   * Approximates x^2 ≈ 2^(2 * log2(x)) using bit length.
   *
   * @param input UInt value to be squared (must be > 0)
   * @return output Approximate square as UInt
   */
  def logApproxSquare(input: UInt): UInt = {
    val width = input.getWidth
    val out = Wire(UInt((2 * width).W))
    // when (input === 0.U) {
    //   out := 0.U
    // } .otherwise {
    //   val approxExp = (log2Ceil(input) << 1).asUInt  // multiply log2 by 2
    //   out := (1.U << approxExp).asUInt
    // }
    out
  }

  /**
   * Approximate squaring for signed inputs using log-based estimation.
   * Approximates x^2 ≈ 2^(2 * log2(|x|)) using bit length.
   *
   * @param input SInt value to be squared
   * @return output Approximate square as UInt
   */
  def logApproxSquare(input: SInt): UInt = {
    val absInput = Wire(UInt(input.getWidth.W))
    absInput := Mux(input < 0.S, (-input).asUInt, input.asUInt)
    logApproxSquare(absInput)
  }

  /**
   * Returns the index of the minimum value in a list of UInts.
   * Designed to synthesize efficiently as a comparator tree.
   *
   * @param values Vector of UInt values
   * @return Index (as UInt) of the minimum value
   */
  def minIndex(values: Seq[SInt]): UInt = {
    // require(values.nonEmpty, "Input sequence must not be empty")

    // val pairs = values.zipWithIndex.map { case (v, i) => (v, i.U(log2Ceil(values.length).W)) }
    // val (minVal, minIdx) = pairs.reduce { case ((v1, i1), (v2, i2)) =>
    //   (Mux(v1 < v2, v1, v2), Mux(v1 < v2, i1, i2))
    // }
    val minIdx = 0.U
    minIdx
  }
}
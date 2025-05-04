package pdfd

import chisel3._
import chisel3.util._

object Utils {
  /** 
   * Function to slice the input symbol into 5 levels
   * 
   */
  def levelSlicer(data: SInt, levels: Seq[SInt], thresholds: Seq[SInt]): SInt = {
    require(levels.length >= 2, "Need at least 2 levels for slicing.")
    require(thresholds.length == levels.length - 1, s"Expected ${levels.length - 1} thresholds, got ${thresholds.length}")

    val result = WireDefault(levels.head) // Default to the lowest level

    for (i <- thresholds.indices) {
      when(data > thresholds(i)) {
        result := levels(i + 1)
      }
    }

    result
  }

  /**
   * Function to square a signed number and return a saturated unsigned result with specified bitwidth.
   *
   * @param data      The signed input data to be squared.
   * @param outWidth  The output bit width for the saturated unsigned result.
   * @return          The saturated squared result as UInt with width `outWidth`.
   */
  def saturatingSquare(data: SInt, outWidth: Int): UInt = {
    require(outWidth > 0, "Output width must be positive.")

    val fullSquare = Wire(UInt((2 * data.getWidth).W))
    fullSquare := (data * data).asUInt

    val maxVal = ((1 << outWidth) - 1).U

    val saturated = Wire(UInt(outWidth.W))
    when(fullSquare > maxVal) {
      saturated := maxVal
    } .otherwise {
      saturated := fullSquare(outWidth-1, 0)
    }

    saturated
  }

  /**
   * Function to perform saturating addition of two unsigned integers.
   *
   * If the result exceeds the maximum representable value of the output width,
   * it is clamped to the max value.
   *
   * @param a         First unsigned operand.
   * @param b         Second unsigned operand.
   * @param outWidth  Output width of the result.
   * @return          Saturated sum as UInt with width `outWidth`.
   */
  def saturatingAdd(a: UInt, b: UInt, outWidth: Int): UInt = {
    require(outWidth > 0, "Output width must be positive.")

    val sumFull = Wire(UInt((a.getWidth max b.getWidth + 1).W))
    sumFull := a +& b

    val maxVal = ((BigInt(1) << outWidth) - 1).U

    val saturated = Wire(UInt(outWidth.W))
    when(sumFull > maxVal) {
      saturated := maxVal
    }.otherwise {
      saturated := sumFull(outWidth - 1, 0)
    }

    saturated
  }

  /**
   * Function to perform saturating addition of two signed integers.
   *
   * If the result exceeds the representable range of the given output width,
   * it is clamped to the max or min signed value.
   *
   * @param a         First signed operand.
   * @param b         Second signed operand.
   * @param outWidth  Output width of the result.
   * @return          Saturated sum as SInt with width `outWidth`.
   */
  def saturatingAddSigned(a: SInt, b: SInt, outWidth: Int): SInt = {
    require(outWidth > 0, "Output width must be positive.")

    val sumFull = Wire(SInt((a.getWidth max b.getWidth + 1).W))
    sumFull := a +& b

    val maxVal = ((BigInt(1) << (outWidth - 1)) - 1).S
    val minVal = (-(BigInt(1) << (outWidth - 1))).S

    val saturated = Wire(SInt(outWidth.W))
    when(sumFull > maxVal) {
      saturated := maxVal
    }.elsewhen(sumFull < minVal) {
      saturated := minVal
    }.otherwise {
      saturated := sumFull(outWidth - 1, 0).asSInt
    }

    saturated
  }
}
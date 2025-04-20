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

    // Fold in reverse so the highest threshold has the highest priority
    for (i <- thresholds.indices.reverse) {
      // Set the result only if the threshold condition holds true
      when(data > thresholds(i)) {
        result := levels(i + 1)  // Set result to the corresponding level
      }
    }

    result
  }
}
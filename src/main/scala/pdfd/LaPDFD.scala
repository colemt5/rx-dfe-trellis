package pdfd

import chisel3._
import chisel3.util.log2Ceil

/** Top module for the PDFD (Parallel Decision Feedback Decoder) project.
  *
  * todo add a description of the project.
  */

// * inputs: 4 18-bit channel symbols, 14 channel coefficients
// * outputs: 4 decoded channel bits (maybe 8-bits per channel)
// todo need to verify what the output looks like
// todo need to understand how to get 14 channel coefficients

class LaPDFD() // todo add parameters 
    extends Module {
  val io = IO(new Bundle {
    // todo add port declarations
  })
  
  // todo add module implementation


}
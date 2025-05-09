# LaPDFD combined DFE trellis decoder
This project implements a Decision Feedback Equalizer (DFE) and trellis decoder in the Receiver (RX) Digital Physical Medium Attachment (PMA) of a 1000BASE-T Gigabit Ethernet PHY. Our combined DFE and trellis decoder block sits between the feed-forward equalizer (FFE) and physical coding sublayer (PCS), as indicated below. Our design is based on the paper "A 1-Gb/s Joint Equalizer and Trellis Decoder for 1000BASE-T Gigabit Ethernet" by Haratsch et al. for use in a 1000BASE-T PHY.

![alt text](/figures/integration.png "Block diagram showing the combination DFE/trellis decoder block between the FFE and PCS blocks. The DFE/trellis decoder takes in inputs from the FFE and NEXT/Echo blocks and outputs decoded symbols to the PCS.")

# Implementation Details
Our design consists of several smaller functional blocks, as shown in the block diagram below (connections shown for state 0).

![alt text](/figures/full_diagram.png "Block diagram showing small blocks labeled DFP, 1D-LaBMU, MUXU, 4D-BMU, ACSU, and SMU connected with arrows and flip-flops.")

- **DFP**: The primary function of the decision-feedback prefilter (DFP) is to cancel most of the post-cursor ISI, reducing the residual ISI to a single tap for processing in subsequent blocks. 
- **1D-LaBMU**: The primary function of the 1D lookahead branch metric unit (1D-LaBMU) is to cancel the remaining post-cursor ISI and compute the distance to the closest A or B symbol set in a lookahead fashion. This results in five type-A symMetrics and five type-B symMetrics.
- **MUXU**: The multiplexor unit (MUXU) takes in the branch metric outputs from the 1D-LA-BMU and selects a desired branch metric based on the previous survivor symbol. Its inputs are five type-A branch metrics, five type-B branch metrics, and one symbol. The outputs are one type-A branch metric and one type-B branch metric.
- **4D-BMU**: The 4D branch metric unit (4D-BMU) combines the 1D branch metrics from the MUXU into 4D branch metrics.
- **ACSU**: The add compare select unit (ACSU) computes the minimum path metric to reach the next state N where N is state 0 to state 7.
- **SMU**: The survivor memory unit (SMU) uses a merge depth of 14 to ensure that the survivor paths have converged and we can make a decision with minimal risk of errors.

# IO and parameters
## Inputs
- `rxSamples`: A vector of four signed 8-bit symbols passed from the prior stage of filtering blocks.
- `taps`: A vector of fourteen signed 8-bit tap values scaled by 128 that characterize the channel.

## Outputs
- `rxSymbols`: A packet 12-bit set of four signed 3-bit decoded symbols from the set {-2, -1, 0, 1, 2}
- `rxValid`: A valid signal for `rxSymbols`

## Parameters
- `level`: Sets the level at which symbols decisions are sliced
- `tapScale`: Sets the scaling factor for taps (128)
- `tapWidth`: Sets the width of taps (8)
- `sampleWidth`: Sets the width of the input sample (8)

# Testing framework
## Generate SystemVerilog design
The first step is to generate SystemVerilog from the Chisel implementation. Running the following command will generate `LaPDFD.sv` into the base `rx-dfe-trellis` directory:
```
make verilog
```
## Generate test vectors
The second step is to generate seqeunce of PAM5 symbols to test the decoder. The following command uses the Encoder.sv block to generate a sequence of allowed 4D symbols and saves it into a file `ref_vectors.txt`:
```
make data
```
## Run test
The third step is to run the testbench. The `ref_vectors.txt` file is parsed by a Python script, `test_seq_generator.py`, to add ISI and Gaussian noise. This Python script outputs a set of input symbols, 'test_vectors.txt', and a set of tap vectors, 'tap_vector.txt'. These files are then used to run the testbench, `lapdfd_tb.sv`. Run the testbench with
```
make run
```
### Extras
If you want to just generate test vectors, you can run the following command:
```
make seq-gen-only
```
If you want to just run the testbench without generating test vectors, you can run the following command:
```
make run-test-only
```

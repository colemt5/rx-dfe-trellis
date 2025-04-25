
`timescale 1ns/1ps
`include "LaPDFD.sv"

module lapdfd_tb;

parameter CLOCK_PERIOD = 10;

reg clock;
reg reset;

// Inputs
reg signed [7:0] rxSamples[3:0];
reg signed [7:0] taps[13:0];

// Wires to connect to DUT
wire [7:0] rxData;
wire       rxValid;

// DUT instance
LaPDFD dut (
    .clock(clock),
    .reset(reset),
    .io_rxSamples_0(rxSamples[0]),
    .io_rxSamples_1(rxSamples[1]),
    .io_rxSamples_2(rxSamples[2]),
    .io_rxSamples_3(rxSamples[3]),
    .io_taps_0(taps[0]),
    .io_taps_1(taps[1]),
    .io_taps_2(taps[2]),
    .io_taps_3(taps[3]),
    .io_taps_4(taps[4]),
    .io_taps_5(taps[5]),
    .io_taps_6(taps[6]),
    .io_taps_7(taps[7]),
    .io_taps_8(taps[8]),
    .io_taps_9(taps[9]),
    .io_taps_10(taps[10]),
    .io_taps_11(taps[11]),
    .io_taps_12(taps[12]),
    .io_taps_13(taps[13]),
    .io_rxData(rxData),
    .io_rxValid(rxValid)
);

// Clock generation
initial clock = 0;
always #(CLOCK_PERIOD / 2) clock = ~clock;

// Reset task
task reset_DUT();
    reset <= 1;
    @(posedge clock);
    @(posedge clock);
    reset <= 0;
endtask

// Reading test vectors from a file
integer vector_file, status, line;
reg signed [7:0] sample_in [3:0];
reg signed [7:0] tap_in [13:0];

initial begin
    $dumpfile("lapdfd.vcd");
    $dumpvars(0, lapdfd_tb);
    $vcdplusfile("lapdfd.vpd");
    $vcdpluson(0, lapdfd_tb);

    // Initialize
    reset = 1;
    repeat(4) @(posedge clock);
    reset = 0;

    // Open test vector file
    vector_file = $fopen("test_vectors.txt", "r");
    if (!vector_file) begin
        $display("ERROR: Cannot open input file!");
        $finish;
    end

    // Read line by line: 4 samples, 14 taps per line
    while (!$feof(vector_file)) begin
        status = $fscanf(vector_file, "%d %d %d %d", 
            sample_in[0], sample_in[1], sample_in[2], sample_in[3]);
        for (int i = 0; i < 14; i++) begin
            status = status + $fscanf(vector_file, "%d", tap_in[i]);
        end

        // Apply inputs
        for (int i = 0; i < 4; i++) rxSamples[i] = sample_in[i];
        for (int i = 0; i < 14; i++) taps[i] = tap_in[i];

        // Wait and observe output
        @(posedge clock);

        if (rxValid)
            $display("Output: 0x%02x", rxData);
        else
            $display("Output not valid");

        @(posedge clock);
    end

    $fclose(vector_file);
    $vcdplusoff;
    $finish;
end

endmodule

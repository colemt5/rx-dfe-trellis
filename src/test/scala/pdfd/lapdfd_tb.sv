`timescale 1ns/1ps
`include "LaPDFD.sv"

module lapdfd_tb;

parameter CLOCK_PERIOD = 10;

reg clock;
reg reset;

// Inputs
logic signed [7:0] rxSamples[3:0];
logic signed [7:0] taps[13:0];
logic signed [2:0] sym0, sym1, sym2, sym3;

// Wires to connect to DUT
wire [11:0] rxData;
wire       rxValid;

typedef struct {
    int sym0;
    int sym1;
    int sym2;
    int sym3;
} SymbolEntry;

SymbolEntry expected_syms [$];
SymbolEntry exp;

int lookup_table[5] = '{-103, -52, 0, 51, 101};
int symbol_table[5] = '{-2, -1, 0, 1, 2};

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
integer tap_file, vector_file, status, line;
integer cycle_count = 0;
int exp_syms [3:0];
reg signed [7:0] sample_in [3:0];

initial begin
    $dumpfile("lapdfd.vcd");
    $dumpvars(0, lapdfd_tb);
    $vcdplusfile("lapdfd.vpd");
    $vcdpluson(0, lapdfd_tb);

    // Initialize
    reset = 1;
    repeat(4) @(posedge clock);
    reset = 0;


    // Read taps from tap_vector.txt
    tap_file = $fopen("tap_vector.txt", "r");
    if (!tap_file) begin
        $display("ERROR: Cannot open tap_vector.txt!");
        $finish;
    end
    for (int i = 0; i < 14; i++) begin
        status = $fscanf(tap_file, "%d", taps[i]);
    end
    $fclose(tap_file);

    // Open test vector file
    vector_file = $fopen("test_vectors.txt", "r");
    if (!vector_file) begin
        $display("ERROR: Cannot open input file!");
        $finish;
    end

    // Read line by line: 4 samples per line
    while (!$feof(vector_file)) begin
        cycle_count += 1;
        status = $fscanf(vector_file, "%d %d %d %d", 
            sample_in[0], sample_in[1], sample_in[2], sample_in[3]);

        // Apply inputs
        for (int i = 0; i < 4; i++) rxSamples[i] = sample_in[i];
        
        // Buffer input samples
        for (int j = 0; j < 4; j++) begin
            exp_syms[j] = 1;
            for (int k = 0; k < 5; k++) begin
                if (sample_in[j] == lookup_table[k]) begin
                    exp_syms[j] = symbol_table[k];
                end
            end
        end
        expected_syms.push_back('{exp_syms[0], exp_syms[1], exp_syms[2], exp_syms[3]});

        sym0 = rxData[2:0];
        sym1 = rxData[5:3];
        sym2 = rxData[8:6];
        sym3 = rxData[11:9];
        if (rxValid)
            $display("Cycle %0d: Symbols: %0d %0d %0d %0d", cycle_count, sym3, sym2, sym1, sym0);
        else
            $display("Cycle %0d: Output not valid", cycle_count);
        
        // Check output against expected values
        if (cycle_count > 15) begin
            exp = expected_syms.pop_front();
            if (rxValid) begin
                if (sym0 !== exp.sym0 || sym1 !== exp.sym1 || sym2 !== exp.sym2 || sym3 !== exp.sym3) begin
                    $display("ERROR at Cycle %0d: Expected %0d %0d %0d %0d, Got %0d %0d %0d %0d",
                             cycle_count, exp.sym3, exp.sym2, exp.sym1, exp.sym0, sym3, sym2, sym1, sym0);
                end
            end
        end

        @(posedge clock);
    end

    // Wait for a few cycles to observe the remaining output
    for (int n = 0; n < 14; n++) begin
        cycle_count += 1;
        for (int i = 0; i < 4; i++) rxSamples[i] = 0;
        sym0 = rxData[2:0];
        sym1 = rxData[5:3];
        sym2 = rxData[8:6];
        sym3 = rxData[11:9];
        if (rxValid)
            $display("Cycle %0d: Symbols: %0d %0d %0d %0d", cycle_count, sym3, sym2, sym1, sym0);
        else
            $display("Cycle %0d: Output not valid", cycle_count);
        
        // Check output against expected values
        if (cycle_count > 15) begin
            exp = expected_syms.pop_front();
            if (rxValid) begin
                if (sym0 !== exp.sym0 || sym1 !== exp.sym1 || sym2 !== exp.sym2 || sym3 !== exp.sym3) begin
                    $display("ERROR at Cycle %0d: Expected %0d %0d %0d %0d, Got %0d %0d %0d %0d",
                             cycle_count, exp.sym3, exp.sym2, exp.sym1, exp.sym0, sym3, sym2, sym1, sym0);
                end
            end
        end

        @(posedge clock);
    end

    $fclose(vector_file);
    $vcdplusoff;
    $finish;
end

endmodule

`timescale 1ns/1ps
`include "LaPDFD.sv"

module lapdfd_tb;

parameter CLOCK_PERIOD = 10;

reg clock;
reg reset;

// Inputs
logic signed [7:0] rx_syms[3:0];
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

int sym_table[5] = '{-103, -52, 0, 51, 101};
int ref_table[5] = '{-2, -1, 0, 1, 2};

// DUT instance
LaPDFD dut (
    .clock(clock),
    .reset(reset),
    .io_rxSamples_0(rx_syms[0]),
    .io_rxSamples_1(rx_syms[1]),
    .io_rxSamples_2(rx_syms[2]),
    .io_rxSamples_3(rx_syms[3]),
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
    .io_rxSymbols(rxData),
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

function automatic signed [7:0] lookup_table(input signed [2:0] symbol);
    case (symbol)
        ref_table[0]: lookup_table = sym_table[0];
        ref_table[1]: lookup_table = sym_table[1];
        ref_table[2]: lookup_table = sym_table[2];
        ref_table[3]: lookup_table = sym_table[3];
        ref_table[4]: lookup_table = sym_table[4];
        default: lookup_table = 0;
    endcase
endfunction

reg signed [7:0] sample_in [3:0];
reg signed [7:0] ref_in [3:0];

// map standard PAM5 to sampled values
always_comb begin
    for (int i = 0; i < 4; i++) begin
        rx_syms[i] = sample_in[i];
    end
end

// Reading test vectors from a file
integer tap_file, vector_file, ref_file, line, ref_line;
integer cycle_count = 0;
integer error_count = 0;

initial begin
    if ($test$plusargs("debug")) begin
        $dumpfile("lapdfd.vcd");
        $dumpvars(0, lapdfd_tb);
        $vcdplusfile("lapdfd.vpd");
        $vcdpluson(0, lapdfd_tb);
    end

    // Read taps from tap_vector.txt
    tap_file = $fopen("tap_vector.txt", "r");
    if (!tap_file) begin
        $display("ERROR: Cannot open tap_vector.txt!");
        $finish;
    end
    for (int i = 0; i < 14; i++) begin
        line = $fscanf(tap_file, "%d", taps[i]);
    end
    $fclose(tap_file);

    // Open test vector file
    vector_file = $fopen("test_vectors.txt", "r");
    if (!vector_file) begin
        $display("ERROR: Cannot open input file!");
        $finish;
    end
    // Open reference vector file
    ref_file = $fopen("ref_vectors.txt", "r");
    if (!ref_file) begin
        $display("ERROR: Cannot open input file!");
        $finish;
    end

    for (int i = 0; i < 4; i++) begin
        sample_in[i] = 0;
    end
    
    // Initialize
    reset = 1;
    repeat(4) @(posedge clock);
    reset = 0;

    // Read line by line: 4 samples per line
    while (!$feof(vector_file)) begin
        cycle_count += 1;
        line = $fscanf(vector_file, "%d %d %d %d", 
            sample_in[0], sample_in[1], sample_in[2], sample_in[3]);

        ref_line = $fscanf(ref_file, "%d %d %d %d", 
            ref_in[0], ref_in[1], ref_in[2], ref_in[3]);
        
        // Buffer input samples
        if (line == 4 && ref_line == 4) begin
            expected_syms.push_back('{ref_in[0], ref_in[1], ref_in[2], ref_in[3]});
        end

        sym3 = rxData[2:0];
        sym2 = rxData[5:3];
        sym1 = rxData[8:6];
        sym0 = rxData[11:9];
        
        // Check output against expected values
        if (cycle_count > 17) begin
            exp = expected_syms.pop_front();
            if (rxValid) begin
                if (sym0 !== exp.sym0 || sym1 !== exp.sym1 || sym2 !== exp.sym2 || sym3 !== exp.sym3) begin
                    // $display("ERROR at Cycle %0d: Expected %0d %0d %0d %0d, Got %0d %0d %0d %0d",
                    //          cycle_count, exp.sym0, exp.sym1, exp.sym2, exp.sym3, sym0, sym1, sym2, sym3);
                    if (sym0 !== exp.sym0) error_count += 1;
                    if (sym1 !== exp.sym1) error_count += 1;
                    if (sym2 !== exp.sym2) error_count += 1;
                    if (sym3 !== exp.sym3) error_count += 1;
                end
                // else begin
                //     $display("Cycle %0d: Symbols: %0d %0d %0d %0d", cycle_count, sym0, sym1, sym2, sym3);
                // end
            end
        end 
        else begin
        end

        @(posedge clock);
    end

    // Wait for a few cycles to observe the remaining output
    while (expected_syms.size() > 0) begin
        cycle_count += 1;

        sym3 = rxData[2:0];
        sym2 = rxData[5:3];
        sym1 = rxData[8:6];
        sym0 = rxData[11:9];

        // Check output against expected values
        exp = expected_syms.pop_front();
        if (rxValid) begin
            if (sym0 !== exp.sym0 || sym1 !== exp.sym1 || sym2 !== exp.sym2 || sym3 !== exp.sym3) begin
                // $display("ERROR at Cycle %0d: Expected %0d %0d %0d %0d, Got %0d %0d %0d %0d",
                //             cycle_count, exp.sym0, exp.sym1, exp.sym2, exp.sym3, sym0, sym1, sym2, sym3);
                if (sym0 !== exp.sym0) error_count += 1;
                if (sym1 !== exp.sym1) error_count += 1;
                if (sym2 !== exp.sym2) error_count += 1;
                if (sym3 !== exp.sym3) error_count += 1;
            end
            // else begin
            //     $display("Cycle %0d: Symbols: %0d %0d %0d %0d", cycle_count, sym0, sym1, sym2, sym3);
            // end
        end

        @(posedge clock);
    end
    $display("Simulation finished. Total error count: %0d out of %0d", error_count, (cycle_count - 17) * 4);

    $fclose(vector_file);
    $fclose(ref_file);
    if ($test$plusargs("debug")) begin
        $vcdplusoff;
    end
    $finish;
end

endmodule

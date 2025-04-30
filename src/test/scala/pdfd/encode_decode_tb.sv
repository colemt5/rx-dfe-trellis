`timescale 1ns/1ps
`include "LaPDFD.sv"
`include "Encoder.sv"

module lapdfd_tb;

parameter CLOCK_PERIOD = 10;

reg clock;
reg reset;

// Input random data
logic [7:0] random_data;
logic mode;
logic tx_enable;

// Connect Encoder to DUT
logic signed [7:0] rx_syms[3:0];
logic signed [2:0] tx_syms[3:0];
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

// Buffer encoded symbols for comparison
SymbolEntry expected_syms [$];
SymbolEntry exp;

int sym_table[5] = '{-103, -52, 0, 51, 101};
int ref_table[5] = '{-2, -1, 0, 1, 2};


// Encoder instance
Encoder encoder (
    .clock(clock),
    .reset(reset),
    .io_tx_enable(tx_enable),
    .io_tx_mode(0),
    .io_tx_error(0),
    .io_tx_data(random_data),
    .io_n(0),
    .io_n0(0),
    .io_loc_rcvr_status(1),
    .io_A(tx_syms[0]),
    .io_B(tx_syms[1]),
    .io_C(tx_syms[2]),
    .io_D(tx_syms[3])
);

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


// map standard PAM5 to sampled values
always_comb begin
    for (int i = 0; i < 4; i++) begin
        rx_syms[i] = lookup_table(tx_syms[i]);
    end
end

// Used for reading taps from file
integer tap_file, status;
integer cycle_count = 0;

initial begin
    $dumpfile("lapdfd.vcd");
    $dumpvars(0, lapdfd_tb);
    $vcdplusfile("lapdfd.vpd");
    $vcdpluson(0, lapdfd_tb);


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

    tx_enable = 0;
    // Initialize
    reset_DUT();
    @(posedge clock);
    tx_enable = 1;
    @(posedge clock);

    // Training sequence
    // for (int i = 0; i < 14; i++) begin
    //     random_data = 0;
    //     mode = 0;
    //     $display("Symbols: %0d %0d %0d %0d", tx_syms[3], tx_syms[2], tx_syms[1], tx_syms[0]);
    //     @(posedge clock);
    // end


    for (int i = 0; i < 100; i++) begin
        cycle_count += 1;
        mode = 0;

        // Generate random data
        random_data = $urandom_range(0, 255); // 240
        
        // Buffer input samples
        expected_syms.push_back('{tx_syms[0], tx_syms[1], tx_syms[2], tx_syms[3]});

        sym3 = rxData[2:0];
        sym2 = rxData[5:3];
        sym1 = rxData[8:6];
        sym0 = rxData[11:9];
        
        // Check output against expected values
        if (cycle_count > 15) begin
            exp = expected_syms.pop_front();
            if (rxValid) begin
                if (sym0 !== exp.sym0 || sym1 !== exp.sym1 || sym2 !== exp.sym2 || sym3 !== exp.sym3) begin
                    $display("ERROR at Cycle %0d: Expected %0d %0d %0d %0d, Got %0d %0d %0d %0d",
                             cycle_count, exp.sym0, exp.sym1, exp.sym2, exp.sym3, sym0, sym1, sym2, sym3);
                end
                else begin
                    $display("Cycle %0d: Symbols: %0d %0d %0d %0d", cycle_count, sym0, sym1, sym2, sym3);
                end
            end
        end
        else begin
            $display("Cycle %0d: Input %0d %0d %0d %0d, Output %0d %0d %0d %0d",
                     cycle_count, tx_syms[0], tx_syms[1], tx_syms[2], tx_syms[3], sym0, sym1, sym2, sym3);
        end

        @(posedge clock);
    end

    // Wait for a few cycles to observe the remaining output
    for (int n = 0; n < 14; n++) begin
        cycle_count += 1;
        mode = 0;

        sym3 = rxData[2:0];
        sym2 = rxData[5:3];
        sym1 = rxData[8:6];
        sym0 = rxData[11:9];

        // Check output against expected values
        exp = expected_syms.pop_front();
        if (rxValid) begin
            if (sym0 !== exp.sym0 || sym1 !== exp.sym1 || sym2 !== exp.sym2 || sym3 !== exp.sym3) begin
                $display("ERROR at Cycle %0d: Expected %0d %0d %0d %0d, Got %0d %0d %0d %0d",
                            cycle_count, exp.sym0, exp.sym1, exp.sym2, exp.sym3, sym0, sym1, sym2, sym3);
            end
            else begin
                $display("Cycle %0d: Symbols: %0d %0d %0d %0d", cycle_count, sym0, sym1, sym2, sym3);
            end
        end

        @(posedge clock);
    end

    $vcdplusoff;
    $finish;
end

endmodule

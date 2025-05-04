`timescale 1ns/1ps
`include "Encoder.sv"

module encode_data;

parameter CLOCK_PERIOD = 10;
`ifdef NUM_DATA
    parameter NUM_DATA = `NUM_DATA;
`else
    parameter NUM_DATA = 64;
`endif

reg clock;
reg reset;

// Input random data
logic [7:0] random_data;
logic mode;
logic tx_enable;

// Connect Encoder to DUT
logic signed [2:0] tx_syms[3:0];

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

// Used for reading taps from file
integer cycle_count = 0;

integer ref_fd;
initial ref_fd = $fopen("ref_vectors.txt", "w");

initial begin
    tx_enable = 0;
    // Initialize
    reset_DUT();
    @(posedge clock);
    tx_enable = 1;
    random_data = $urandom_range(0, 255); // 240
    mode = 0;

    for (int i = 0; i < NUM_DATA; i++) begin
        $fwrite(ref_fd, "%0d %0d %0d %0d\n",
                tx_syms[0], tx_syms[1], tx_syms[2], tx_syms[3]);
        @(posedge clock);
        random_data = $urandom_range(0, 255); // 240
    end

    $fclose(ref_fd);
    $finish;
end

endmodule

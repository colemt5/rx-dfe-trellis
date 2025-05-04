SBT=sbt
VCS=vcs -full64
VCS_FLAGS=-sverilog +incdir+. -R +vcs+vcdpluson -debug_access+all +vcs+dumpvars+all
#TB_PATH = src/test/scala/pdfd/encode_decode_tb.sv

# Default target
all:
	@echo "Usage: make verilog | make run-test"

# Run Chisel to generate Verilog
verilog:
	$(SBT) "runMain pdfd.LaPDFD"

# Run simulation testbench
run:
	python test_seq_generator.py
	$(VCS) src/test/scala/pdfd/lapdfd_tb.sv $(VCS_FLAGS)

data:
	$(VCS) src/test/scala/pdfd/encode_data.sv -sverilog +incdir+. +define+NUM_DATA=$(NUM_DATA) -R
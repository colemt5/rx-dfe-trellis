# To run module tests
In Terminal, run
```
sbt
test
```
Once you hit Enter after `sbt`, it'll enter sbt in the Terminal. Type `exit` to exit.

I needed to run the following command to build
```
sbt "runMain pdfd.LaPDFD"
```

# To run end-to-end tests
In Terminal, run
```
vcs -full64 src/test/scala/pdfd/lapdfd_tb.sv -sverilog +incdir+. -R

vcs -full64 src/test/scala/pdfd/lapdfd_tb.sv -sverilog +incdir+. -R +vcs+vcdpluson -debug_access+all +vcs+dumpvars+all
```
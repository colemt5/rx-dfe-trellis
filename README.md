# To run module tests
In Terminal, run
```
sbt
test
```
Once you hit Enter after `sbt`, it'll enter sbt in the Terminal. Type `exit` to exit.

# To run end-to-end tests
In Terminal, run
```
vcs -full64 src/test/scala/pdfd/lapdfd_tb.sv -sverilog +incdir+. -R
```
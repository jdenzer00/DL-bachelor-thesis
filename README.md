The folder "TESTS-CEL" contains all the test files, shell scripts, and a .txt file with the test results. 
The tests were dont on the full ontologies GO, Galen, and nciOntology 
which can be found at https://sourceforge.net/projects/latitude/files/cel/

The folder "TESTS-SPASS" also contains test files and shell scripts for 
the SPASS test cases as well as a .txt file similar to the one mentioned before.
The tests were done on drastically reduced modules of the before mentioned ontologies. 
Those modules where constructed using reachability based modules, all computed MinAs and random
axioms from the original ontologies. Complete translations of the original ontologies and the 
example ontology of the thesis are also found in this folder.

The folder "CEL-Vergleich" is analogue to the "TESTS-CEL" folder but uses the modules of the SPASS tests

The folder "TranslateCelToDfg" contains the parser prototype. It is not yet a jar file or fleshed out programm but rather a script
which I execute within my IDE.

all:
	mkdir -p bin;javac -cp "lib/*" -sourcepath src -d bin/ src/generator/CNFGenerator.java
	mkdir -p bin;javac -cp "lib/*" -sourcepath src -d bin/ src/parser_test/CYKParserTest.java
	mkdir -p bin;javac -cp "lib/*" -sourcepath src -d bin/ src/parser/CYKParser.java
clean:
	rm -r bin

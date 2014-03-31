all:
	mkdir -p bin;javac -cp lib/junit-4.11.jar -sourcepath src -d bin/ src/generator/CNFGenerator.java
clean:
	rm -r bin

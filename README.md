# CYKaveat

The implementation of CYK algorithm for context-free grammars in **Java 8**.
This project aims to show the sweetness of functional-ish programming in Java 8
by taking a specific example, CYK parsing.

## Context-Free Grammar Format
For simplicity, it is assumed that the input file is ascii-recognizable in
Chomsky normal form, and that all variables are of length one, captialized, and
that all terminals are in lower case.

## What's Included
- a parser, the most important one
- a generator, derive strings in finite steps based on the grammar for the sake
of testing the parser
- unit-test class, callables for testing

## How to Call CYKParser API

`````````````
CYKParser parser = new CYKParser("english.txt");
parser.parse("don't accept this"); // which returns a boolean value
`````````````

## License

CYKaveat is released under the [MIT License](http://www.opensource.org/licenses/MIT).


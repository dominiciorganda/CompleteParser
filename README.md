# CompleteParser
A recursive decent parser built in Java, based on an LL(1) grammar defined in EBNF. The parser works together with an scanner and has  following features: 1. Reporting syntax errors, E.g. “Syntax error in line X”, or “Invalid token Yin line X”. 2. Builds a rich symbol table containing identifiers (literals, variables, method and class declarations). Also the scanner and parser both continue working on the next line after reporting an error.
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

enum Type {
    Identifier, Keyword, StringLiteral, NumberLiteral, Operator, Separator;
}

class Token {
    private String value;
    private Type type;
    private int line;

    public Token(String value, Type type, int line) {
        this.value = value;
        this.type = type;
        this.line = line;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public int getLine() {
        return line;
    }
}

class Symbol {
    private String name, kind, type, access, scope;
    private int line;

    public Symbol(String name, String kind, String type, int line, String access, String scope) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.access = access;
        this.scope = scope;
        this.line = line;
    }

    @Override
    public String toString() {
        return name + ", " + kind + ", " + type + ", " + line + ", " + access + ", " + scope;
    }
}

class SyntaxError extends Exception {
    private String token;
    private int position;

    public SyntaxError(String token, int position) {
        this.token = token;
        this.position = position;
    }

    public void showMessage() {
        System.out.println("Syntax error not allowed token: " + token + " at line " + position);
    }
}

public class CompleteParser {

    static List<Character> alphabet = new ArrayList<>();
    static List<Character> numericAlphabet = new ArrayList<>();
    static List<Character> identifiersAlphabet = new ArrayList<>();
    static List<Character> literalsAlphabet = new ArrayList<>();
    static List<Character> operators = new ArrayList<>();
    static List<Character> separators = new ArrayList<>();
    static List<String> keywords;
    static String currentString;
    static List<Token> tokens = new ArrayList<>();
    static List<String> types = new ArrayList<>();
    static List<Symbol> symbolTable = new ArrayList<>();
    static String className;
    static String methodName;
    static int position = 0;

    private static void generateAlphabet() {
        for (char c = 'a'; c != 'z'; c++)
            identifiersAlphabet.add(c);
        identifiersAlphabet.add('z');
        for (char c = 'A'; c != 'Z'; c++)
            identifiersAlphabet.add(c);
        identifiersAlphabet.add('Z');


        for (char c = '0'; c != '9'; c++)
            numericAlphabet.add(c);
        numericAlphabet.add('9');
        identifiersAlphabet.addAll(numericAlphabet);

        literalsAlphabet.addAll(numericAlphabet);
        literalsAlphabet.add(' ');
        literalsAlphabet.addAll(identifiersAlphabet);
        literalsAlphabet.add(' ');
        literalsAlphabet.add('\"');

        operators.add('=');
        operators.add('!');
        operators.add('>');
        operators.add('+');


        separators.add('(');
        separators.add(')');
        separators.add('{');
        separators.add('}');
        separators.add('[');
        separators.add(']');
        separators.add('.');
        separators.add(',');
        separators.add(';');
        separators.add(':');

        alphabet.addAll(identifiersAlphabet);
        alphabet.addAll(numericAlphabet);
        alphabet.addAll(literalsAlphabet);
        alphabet.addAll(operators);
        alphabet.addAll(separators);
        alphabet.add('/');
    }

    private static String transition(String currentState, char symbol) {
        if (!alphabet.contains(symbol))
            return "error";
        if (currentState.equals("state0")) {
            if (identifiersAlphabet.contains(symbol) && !numericAlphabet.contains(symbol))
                return "state1";
            if (numericAlphabet.contains(symbol) && symbol != '0')
                return "state2";
            if (symbol == '0')
                return "state9";
            if (symbol == '"')
                return "state3";
            if (symbol == '/')
                return "state7";
            if (operators.contains(symbol))
                return "state5";
            if (separators.contains(symbol))
                return "state6";
            return "error";
        }
        if (currentState.equals("state1")) {
            if (identifiersAlphabet.contains(symbol))
                return "state1";
            return "error";
        }
        if (currentState.equals("state2")) {
            if (numericAlphabet.contains(symbol))
                return "state2";
            return "error";
        }
        if (currentState.equals("state3")) {
            if (symbol == '"')
                return "state4";
            if (literalsAlphabet.contains(symbol))
                return "state3";
            return "error";
        }
        if (currentState.equals("state5")) {
            return "error";
        }
        if (currentState.equals("state6"))
            return "error";
        if (currentState.equals("state7")) {
            if (symbol == '/')
                return "state8";
            return "error";
        }
        if (currentState.equals("state8")) {
            if (literalsAlphabet.contains(symbol))
                return "state8";
            return "error";
        }
        if (currentState.equals("state9"))
            return "error";
        if (symbol == ' ')
            return "error";
        return "error";

    }

    public static void scanCode(String fileName) {
        try {
//            File f = new File("Main.java");
            File f = new File(fileName);
            Scanner scanner = new Scanner(f);
            List<String> states = Arrays.asList("state0", "state1", "state2", "state3", "state4", "state5", "state6", "state7", "state8", "state9");
            List<String> finalStates = Arrays.asList("state1", "state2", "state4", "state5", "state6", "state8", "state9");
            keywords = Arrays.asList("import", "public", "class", "private", "int", "this", "static", "void", "new", "while", "if");
            types = Arrays.asList("int", "void", "String", "Scanner", "Student");
            generateAlphabet();
            int row = 1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String currentState = states.get(0);
                currentString = "";
                char[] input = line.toCharArray();
                for (int i = 0; i < line.length(); i++) {
                    char symbol = input[i];

                    if (transition(currentState, symbol).equals("error")) {
                        checkTokenOrError(finalStates, currentState, row);
                        currentState = states.get(0);
                        currentString = "";
                    }

                    currentState = transition(currentState, symbol);
                    currentString += String.valueOf(symbol);

                }
                checkTokenOrError(finalStates, currentState, row);
                row++;
            }
            scanner.close();
//            for (Token token : tokens)
//                System.out.println(token.getValue() + " " + token.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkTokenOrError(List<String> finalStates, String currentState, int position) {
        if (finalStates.contains(currentState)) {
            if (currentState.equals("state1")) {
                if (keywords.contains(currentString))
                    tokens.add(new Token(currentString, Type.Keyword, position));
                else
                    tokens.add(new Token(currentString, Type.Identifier, position));
            }
            if (currentState.equals("state2"))
                tokens.add(new Token(currentString, Type.NumberLiteral, position));
            if (currentState.equals("state9"))
                tokens.add(new Token(currentString, Type.NumberLiteral, position));
            if (currentState.equals("state4"))
                tokens.add(new Token(currentString, Type.StringLiteral, position));
            if (currentState.equals("state5"))
                tokens.add(new Token(currentString, Type.Operator, position));
            if (currentState.equals("state6"))
                tokens.add(new Token(currentString, Type.Separator, position));

        } else {
            //lexical error
            if (!currentString.equals(" ") && !currentString.isEmpty() && !currentString.equals("\t")) {
                System.out.println("Lexical Error at line " + position + " illegal character: " + currentString);
            }
        }
    }

    public static void main(String[] args) {
        scanCode(args[0]);

        try {
            Program();
            System.out.println("Parsing successful!");
            writeSymboltable();

        } catch (SyntaxError syntaxError) {
            syntaxError.showMessage();

        } catch (IOException e) {
            e.printStackTrace();

        }
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        scanner.close();

    }

    private static void writeSymboltable() throws IOException {
        FileWriter fileWriter = new FileWriter("symboltable.csv");
        for(Symbol symbol:symbolTable)
            fileWriter.write(symbol.toString()+"\n");
        fileWriter.close();
    }

    public static void Program() throws SyntaxError {
        // PROGRAMM = IMPORT, CLASS;
//        System.out.println("Programm");
        Import();
        Classs();
    }

    public static void Import() throws SyntaxError {
        // IMPORT = "import", IDENTIFIER, ".", IDENTIFIER, ".", IDENTIFIER, END;
//        System.out.println("Import");
        Token current = tokens.get(position);
        if (current.getValue().equals("import")) {
            position++;
            Identfier();
            Punct();
            Identfier();
            Punct();
            Identfier();
            End();
        } else
            throw new SyntaxError(current.getValue(), current.getLine());

    }

    public static void End() throws SyntaxError {
        // END = " ; ";
//        System.out.println("END");
        Token current = tokens.get(position);
        if (current.getValue().equals(";")) {
            position++;
        } else
            throw new SyntaxError(current.getValue(), current.getLine());
    }

    public static void Punct() throws SyntaxError {
//        System.out.println("Punct");
        Token current = tokens.get(position);
        if (current.getValue().equals(".")) {
            position++;
        } else
            throw new SyntaxError(current.getValue(), current.getLine());

    }

    public static void Identfier() throws SyntaxError {
        // IDENTIFIER = LETTER, { LETTER | DIGIT};
//        System.out.println("Ident");
        Token current = tokens.get(position);
        if (current.getType() == Type.Identifier) {
            position++;
        } else
            throw new SyntaxError(current.getValue(), current.getLine());
    }

    public static void NumberLiteral() throws SyntaxError {
        // NUMBER = "0" | (DIGITWITHOUTNULL, { DIGIT }) ;
//        System.out.println("Number");
        Token current = tokens.get(position);
        if (current.getType() == Type.NumberLiteral) {
            position++;
            symbolTable.add(new Symbol(current.getValue(), "literal", "int", current.getLine(), "--", "global"));

        } else
            throw new SyntaxError(current.getValue(), current.getLine());
    }

    public static void Type() throws SyntaxError {
        // TYPE = "int" | "String[]" | "void" | "String" | "Student" | "Scanner" ;
//        System.out.println("Type");
        Token current = tokens.get(position);
        if (types.contains(current.getValue()) && !current.getValue().equals("String")) {
            position++;
        } else if (current.getValue().equals("String")) {
            position++;
            SquareParanthesis();
        } else
            throw new SyntaxError(current.getValue(), current.getLine());
    }

    private static void SquareParanthesis() {
//        System.out.println("[]");
        Token current = tokens.get(position);
        Token next = tokens.get(position + 1);
        if (current.getValue().equals("[") && next.getValue().equals("]")) {
            position += 2;
        }

    }

    public static void Classs() throws SyntaxError {
        // CLASS = "public", "class", TYPE, "{", {ATTRIBUTE}, {METHOD}, "}";
//        System.out.println("Class");
        Public();
        Token current = tokens.get(position);
        if (current.getValue().equals("class")) {
            position++;
            Type();
            className = tokens.get(position - 1).getValue();
            symbolTable.add(new Symbol(tokens.get(position - 1).getValue(), "class", "--", tokens.get(position - 1).getLine(), "public", "global"));
            OCP();
            current = tokens.get(position);
            while (current.getValue().equals("private")) {
                Attribute();
                current = tokens.get(position);
            }
            while (current.getValue().equals("public")) {
                Method();
                current = tokens.get(position);
            }
            CCP();
        } else
            throw new SyntaxError(current.getValue(), current.getLine());
    }

    private static void Method() throws SyntaxError {
        // METHOD = "public" ["static", TYPE], IDENTIFIER, "(", TYPE, IDENTFIER {",", TYPE, IDENTFIER}, ")"
        // ,"{" {VARIABLE}, {STATEMENT}, "}";
//        System.out.println("Method");
        Public();
        Token current = tokens.get(position);
        String type = "--";
        if (current.getValue().equals("static")) {
            position++;
            Type();
            type = tokens.get(position-1).getValue();
            if(type.equals("]"))
                type = tokens.get(position-3).getValue()+"[]";
        }
        Identfier();
        methodName = tokens.get(position - 1).getValue();
        symbolTable.add(new Symbol(methodName, "method", type, tokens.get(position - 1).getLine(), "public", className));
        ORP();
        Type();
        Identfier();
        type = tokens.get(position-2).getValue();
        if(type.equals("]"))
            type = tokens.get(position-4).getValue()+"[]";
        symbolTable.add(new Symbol(tokens.get(position - 1).getValue(), "parameter", type, tokens.get(position - 1).getLine(), "--", className + "." + methodName));
        current = tokens.get(position);
        while (current.getValue().equals(",")) {
            position++;
            Type();
            Identfier();
            type = tokens.get(position-2).getValue();
            if(type.equals("]"))
                type = tokens.get(position-4).getValue()+"[]";
            symbolTable.add(new Symbol(tokens.get(position - 1).getValue(), "parameter", type, tokens.get(position - 1).getLine(), "--", className + "." + methodName));
            current = tokens.get(position);
        }
        CRP();
        OCP();
        current = tokens.get(position);
        while (types.contains(current.getValue())) {
            Variable();
            current = tokens.get(position);
        }
        current = tokens.get(position);
        while (current.getType() == Type.Identifier || current.getType() == Type.Keyword || current.getValue().equals("{")) {
            Statement();
            current = tokens.get(position);
        }
        CCP();
    }

    private static void Attribute() throws SyntaxError {
        // ATTRIBUTE = "private", TYPE, IDENTIFIER, END;
//        System.out.println("Attribute");
        Private();
        Type();
        Identfier();
        String type = tokens.get(position-2).getValue();
        if(type.equals("]"))
            type = tokens.get(position-4).getValue()+"[]";
        symbolTable.add(new Symbol(tokens.get(position - 1).getValue(), "attribute", type, tokens.get(position - 1).getLine(), "private", className));
        End();
    }

    private static void Variable() throws SyntaxError {
        // VARIABLE = TYPE, IDENTIFIER, END;
//        System.out.println("Variable");
        Type();
        Identfier();
        String type = tokens.get(position-2).getValue();
        if(type.equals("]"))
            type = tokens.get(position-4).getValue()+"[]";
        symbolTable.add(new Symbol(tokens.get(position - 1).getValue(), "variable", type, tokens.get(position - 1).getLine(), "--", className + "." + methodName));
        End();
    }

    private static void Statement() throws SyntaxError {
        // STATEMENT = IDENTIFIER, "=" , EXPRESSION, END
        //              | "this.", IDENTIFIER, "=", IDENTIFIER, END
        //              | OUTPUT
        //              | "if (", EXPRESSION, ")", STATEMENT
        //              | "while (", EXPRESSION, ")" , STATEMENT
        //              | "{", {STATEMENT}, "}";
//        System.out.println("Statement");
        Token current = tokens.get(position);
        if (current.getType() == Type.Keyword) {
            switch (current.getValue()) {
                case "this":
//                    System.out.println("THIS");
                    position++;
                    Punct();
                    Identfier();
                    Equals();
                    Identfier();
                    End();
                    break;
                case "if":
//                    System.out.println("IF");
                    position++;
                    ORP();
                    Expression();
                    CRP();
                    Statement();
                    break;
                case "while":
//                    System.out.println("WHILE");
                    position++;
                    ORP();
                    Expression();
                    CRP();
                    Statement();
                    break;
                default:
                    throw new SyntaxError(current.getValue(), current.getLine());
            }

        } else if (current.getType() == Type.Identifier) {
            if (current.getValue().equals("System"))
                Output();
            else {
                Identfier();
                Equals();
                Expression();
                End();
            }
        } else if (current.getType() == Type.Separator) {
            OCP();
            current = tokens.get(position);
            while (current.getType() == Type.Identifier || current.getType() == Type.Keyword || current.getValue().equals("{")) {
                Statement();
                current = tokens.get(position);
            }
            CCP();
        }

    }

    private static void Expression() throws SyntaxError {
        // EXPRESSION =  IDENTIFIER, [ASSIGNREST], [EXPRREST]
//              | NUMBER
//              | "new", IDENTIFIER, "(", IDENTIFIER [IDENTIFIERREST] {"," IDENTFIER} ")";
//        System.out.println("Expression");
        Token current = tokens.get(position);
        if (current.getType() == Type.NumberLiteral) {
            NumberLiteral();
        } else if (current.getType() == Type.Keyword) {
            if (current.getValue().equals("new")) {
                position++;
                Identfier();
                ORP();
                Identfier();
                current = tokens.get(position);
                if (current.getValue().equals(".")) {
                    IdentifierRest();
                }
                current = tokens.get(position);
                while (current.getValue().equals(",")) {
                    position++;
                    Identfier();
                    current = tokens.get(position);
                }
                CRP();

            } else throw new SyntaxError(current.getValue(), current.getLine());
        } else if (current.getType() == Type.Identifier) {
            Identfier();
            current = tokens.get(position);
            if (current.getType() == Type.Separator || current.getValue().equals("+"))
                AssignRest();
            current = tokens.get(position);
            if (current.getType() == Type.Operator)
                ExprRest();
        }
    }

    private static void ExprRest() throws SyntaxError {
        // EXPRREST = ("!=" | ">"), (IDENTIFIER | NUMBER);
//        System.out.println("ExprRest");
        Token current = tokens.get(position);
        if (current.getType() == Type.Operator) {
            if (current.getValue().equals(">")) {
//                System.out.println("Greater");
                position++;
            } else
                Different();
            current = tokens.get(position);
            if (current.getType() == Type.Identifier)
                Identfier();
            else
                NumberLiteral();

        } else throw new SyntaxError(current.getValue(), current.getLine());

    }

    private static void Different() throws SyntaxError {
//        System.out.println("Different");
        Token current = tokens.get(position);
        if (current.getValue().equals("!")) {
            position++;
            Equals();
        } else throw new SyntaxError(current.getValue(), current.getLine());
    }

    private static void AssignRest() throws SyntaxError {
        // ASSIGNREST = (".", IDENTIFIER, "()" | "+", NUMBER);
//        System.out.println("AssignRest");
        Token current = tokens.get(position);
        if (current.getType() == Type.Separator) {
            Punct();
            Identfier();
            ORP();
            CRP();
        } else if (current.getType() == Type.Operator) {
            Plus();
            NumberLiteral();
        } else throw new SyntaxError(current.getValue(), current.getLine());

    }

    private static void Plus() throws SyntaxError {
//        System.out.println("Plus");
        Token current = tokens.get(position);
        if (current.getValue().equals("+"))
            position++;
        else
            throw new SyntaxError(tokens.get(position).getValue(), tokens.get(position).getLine());

    }

    private static void Equals() throws SyntaxError {
//        System.out.println("Equals");
        Token current = tokens.get(position);
        if (current.getValue().equals("="))
            position++;
        else
            throw new SyntaxError(tokens.get(position).getValue(), tokens.get(position).getLine());

    }

    private static void Output() throws SyntaxError {
//         OUTPUT = "System.out.println", "(", PRINT , ")", END;
//        System.out.println("Output");
        Token current = tokens.get(position);
        if (current.getValue().equals("System")) {
            Identfier();
            Punct();
            current = tokens.get(position);
            if (current.getValue().equals("out")) {
                Identfier();
                Punct();
                current = tokens.get(position);
                if (current.getValue().equals("println")) {
                    Identfier();
                    ORP();
                    Print();
                    CRP();
                    End();
                } else
                    throw new SyntaxError(current.getValue(), current.getLine());
            } else
                throw new SyntaxError(current.getValue(), current.getLine());
        } else
            throw new SyntaxError(current.getValue(), current.getLine());

    }

    public static void Print() throws SyntaxError {
        // PRINT = TEXT | IDENTIFIER, [IDENTIFIERREST];
//        System.out.println("Print");
        Token current = tokens.get(position);
        if (current.getType() == Type.StringLiteral)
            Text();
        else if (current.getType() == Type.Identifier) {
            Identfier();
            current = tokens.get(position);
            if (current.getValue().equals("."))
                IdentifierRest();

        } else
            throw new SyntaxError(tokens.get(position).getValue(), tokens.get(position).getLine());

    }

    private static void IdentifierRest() throws SyntaxError {
        // IDENTIFIERREST = ".", IDENTIFIER;
//        System.out.println("IdentifierRest");
        Punct();
        Identfier();
    }

    public static void Text() throws SyntaxError {
        // TEXT = """, (LETTER | DIGIT | SPACE), {LETTER | DIGIT | SPACE}, """ ;
//        System.out.println("Text");
        Token current = tokens.get(position);
        if (current.getType() == Type.StringLiteral) {
            position++;
            symbolTable.add(new Symbol(current.getValue(), "literal", "String", current.getLine(), "--", "global"));

        }
        else
            throw new SyntaxError(tokens.get(position).getValue(), tokens.get(position).getLine());
    }

    public static void OCP() throws SyntaxError {
//        System.out.println("{");
        if (tokens.get(position).getValue().equals("{"))
            position++;
        else
            throw new SyntaxError(tokens.get(position).getValue(), tokens.get(position).getLine());
    }

    public static void CCP() throws SyntaxError {
//        System.out.println("}");
        if (tokens.get(position).getValue().equals("}"))
            position++;
        else
            throw new SyntaxError(tokens.get(position).getValue(), tokens.get(position).getLine());
    }

    public static void ORP() throws SyntaxError {
//        System.out.println("(");
        if (tokens.get(position).getValue().equals("("))
            position++;
        else
            throw new SyntaxError(tokens.get(position).getValue(), tokens.get(position).getLine());
    }

    public static void CRP() throws SyntaxError {
//        System.out.println(")");
        if (tokens.get(position).getValue().equals(")"))
            position++;
        else
            throw new SyntaxError(tokens.get(position).getValue(), tokens.get(position).getLine());
    }

    public static void Public() throws SyntaxError {
//        System.out.println("Public");
        Token current = tokens.get(position);
        if (current.getValue().equals("public"))
            position++;
        else
            throw new SyntaxError(current.getValue(), current.getLine());
    }

    public static void Private() throws SyntaxError {
//        System.out.println("Private");
        Token current = tokens.get(position);
        if (current.getValue().equals("private"))
            position++;
        else
            throw new SyntaxError(current.getValue(), current.getLine());
    }
}


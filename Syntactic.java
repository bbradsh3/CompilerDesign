package ADT;

import java.io.IOException;

public class Syntactic {

    private String filein;
    private SymbolTable symbolList;
    private QuadTable quads;
    private Interpreter interp;
    private Lexical lex;
    private Lexical.token token;
    private boolean traceon;
    private int level = 0;
    private boolean anyErrors;

    private final int symbolSize = 250;
    private final int quadSize = 1000;
    private int Minus1Index;
    private int Plus1Index;

    public Syntactic(String filename, boolean traceOn) {
        filein = filename;
        traceon = traceOn;
        symbolList = new SymbolTable(symbolSize);
        Minus1Index = symbolList.AddSymbol("-1", symbolList.constantkind, -1);
        Plus1Index = symbolList.AddSymbol("1", symbolList.constantkind, 1);

        quads = new QuadTable(quadSize);
        interp = new Interpreter();
        lex = new Lexical(filein, symbolList, true);
        lex.setPrintToken(traceOn);
        anyErrors = false;
    }

    public void parse() throws IOException {
        String filenameBase = filein.substring(0, filein.length() - 4);
        System.out.println(filenameBase);
        int recur = 0;
        token = lex.GetNextToken(false);
        recur = Program();
        quads.AddQuad(interp.opcodeFor("STOP"), 0, 0, 0);

        symbolList.PrintSymbolTable(filenameBase + "ST-before.txt");
        quads.PrintQuadTable(filenameBase + "QUADS.txt");
        if (!anyErrors) {
            interp.InterpretQuads(quads, symbolList, false, filenameBase + "TRACE.txt");
        } else {
            System.out.println("Errors, unable to run program.");
        }
        symbolList.PrintSymbolTable(filenameBase + "ST-after.txt");
    }

    private int ProgIdentifier() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        if (token.code == lex.codeFor("IDNT")) {
            symbolList.UpdateSymbol(symbolList.LookupSymbol(token.lexeme), 'P', 0);
            token = lex.GetNextToken(false);
        }
        return recur;
    }

    private int Program() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("Program", true);
        if (token.code == lex.codeFor("UNIT")) {
            token = lex.GetNextToken(true);
            recur = ProgIdentifier();
            if (token.code == lex.codeFor("SEMI")) {
                token = lex.GetNextToken(false);
                recur = Block();
                if (token.code == lex.codeFor("PERD")) {
                    if (!anyErrors) {
                        System.out.println("Success.");
                    } else {
                        System.out.println("Compilation failed.");
                    }
                } else {
                    error(lex.reserveFor("PERD"), token.lexeme);
                }
            } else {
                error(lex.reserveFor("SEMI"), token.lexeme);
            }
        } else {
            error(lex.reserveFor("UNIT"), token.lexeme);
        }
        trace("Program", false);
        return recur;
    }

    private int Block() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("Block", true);
        while (token.code == lex.codeFor("VAR_")) {
            recur = VariableDecSec();
        }
        if (token.code == lex.codeFor("BEG_")) {
            recur = BlockBody();
            while ((token.code == lex.codeFor("SEMI")) && (!lex.EOF()) && (!anyErrors)) {
                token = lex.GetNextToken(false);
                recur = Statement();
            }
            if (token.code == lex.codeFor("END_")) {
                token = lex.GetNextToken(false);
            } else {
                if (token.code == lex.codeFor("PERD")) {
                    trace("Block", false);
                    return recur;
                } else {
                    error(lex.reserveFor("END_"), token.lexeme);
                }
            }
        } else {
            error(lex.reserveFor("BEG_"), token.lexeme);
        }
        trace("Block", false);
        return recur;
    }

    private int BlockBody() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("Block Body", true);
        token = lex.GetNextToken(false);
        while (token.code != lex.codeFor("END_") && token.code != lex.codeFor("SEMI")
                && token.code != lex.codeFor("ELSE") && token.code != lex.codeFor("PERD")) {
            recur = Statement();
            if (recur == -1) {
                trace("Block Body", false);
                return recur;
            }
            token = lex.GetNextToken(false);
        }
        trace("Block Body", false);
        return recur;
    }

    private int VariableDecSec() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("Variable-Dec-Sec", true);
        while (token.code != lex.codeFor("SEMI")) {
            token = lex.GetNextToken(true);
        }
        recur = Simpletype();
        token = lex.GetNextToken(true);
        trace("Variable-Dec-Sec", false);
        return recur;
    }

    private int Simpletype() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("Simpletype", true);
        while (token.code != lex.codeFor("SEMI")) {
            token = lex.GetNextToken(false);
        }
        trace("Simpletype", false);
        return recur;
    }

private int SimpleExpression() {
    int recur = 0;
    if (anyErrors) {
        return ErrorRecovery();
    }
    trace("SimpleExpression", true);

    recur = Term(); // Start with the first term
    while (token.code == lex.codeFor("ADD_") || token.code == lex.codeFor("SUB_")) {
        int operatorCode = token.code; // Save the operator code
        token = lex.GetNextToken(false); // Move past the operator
        int rightTerm = Term(); // Get the next term
        
        // Perform the operation directly, without creating a temporary entry
        // Assuming that the result can overwrite one of the operands, typically the left one
        int opCode = (operatorCode == lex.codeFor("ADD_")) ? interp.opcodeFor("ADD") : interp.opcodeFor("SUB");
        quads.AddQuad(opCode, recur, rightTerm, recur); // Update recur directly
    }

    trace("SimpleExpression", false);
    return recur; // Return the last computed result index
}

    private int Sign() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("Sign", true);
        token = lex.GetNextToken(false);
        trace("Sign", false);
        return recur;
    }

private int Term() {
    int recur = 0;
    if (anyErrors) {
        return ErrorRecovery();
    }
    trace("Term", true);
    recur = Factor(); // Start with the first factor
    while (token.code == lex.codeFor("MULT") || token.code == lex.codeFor("DIV_")) {
        int operatorCode = token.code;
        token = lex.GetNextToken(false); // Move past the operator
        int rightFactor = Factor(); // Get the next factor
        quads.AddQuad(operatorCode == lex.codeFor("MULT") ? interp.opcodeFor("MUL") : interp.opcodeFor("DIV"), recur, rightFactor, recur); // Generate the quad, updating recur directly
    }
    trace("Term", false);
    return recur;
}

private int Factor() {
    int recur = 0;
    if (anyErrors) {
        return ErrorRecovery();
    }
    trace("Factor", true);
    if (token.code == lex.codeFor("LPAR")) {
        token = lex.GetNextToken(false); // Move past '('
        recur = SimpleExpression(); // Recurse into a full expression within the parentheses
        if (token.code != lex.codeFor("RPAR")) {
            error("Right parenthesis", token.lexeme); // Expecting a ')'
            anyErrors = true;
        } else {
            token = lex.GetNextToken(false); // Move past ')'
        }
    } else if (token.code == lex.codeFor("ICST") || token.code == lex.codeFor("FLOT")) {
        recur = UnsignedNumber(); // Directly use the number
    } else if (token.code == lex.codeFor("IDNT")) {
        recur = Variable(); // Handle identifiers (variables)
    } else {
        error("Integer or Floating Point Number or Identifier", token.lexeme);
        anyErrors = true;
    }
    trace("Factor", false);
    return recur;
}

    private int UnsignedConstant() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("UnsignedConstant", true);
        recur = UnsignedNumber();
        trace("UnsignedConstant", false);
        return recur;
    }

    private int UnsignedNumber() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("UnsignedNumber", true);
        if (token.code == lex.codeFor("ICST") || token.code == lex.codeFor("FLOT")) {
            recur = symbolList.LookupSymbol(token.lexeme);
            token = lex.GetNextToken(false);
        } else {
            error("Integer or Floating Point Number", token.lexeme);
        }
        trace("UnsignedNumber", false);
        return recur;
    }

    private int AddOp() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("AddOp", true);
        token = lex.GetNextToken(false);
        if (token.code == lex.codeFor("ADD_") || token.code == lex.codeFor("SUB_")) {
            recur = symbolList.LookupSymbol(token.lexeme);
        }
        trace("AddOp", false);
        return recur;
    }

    private int MulOp() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("MulOp", true);
        if (token.code == lex.codeFor("MULT") || token.code == lex.codeFor("DIV_")) {
            token = lex.GetNextToken(false);
            if (token.code == lex.codeFor("LPAR") || token.code == lex.codeFor("RPAR")) {
                recur = SimpleExpression();
            }
            quads.AddQuad(interp.opcodeFor("MUL"), recur, Minus1Index, Plus1Index);
            Plus1Index++;
        }
        trace("MulOp", false);
        return recur;
    }

    private int Statement() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("Statement", true);
        if (token.code == lex.codeFor("BEG_")) {
            recur = BlockBody();
        }
        if (token.code == lex.codeFor("REP_")) {
            recur = HandleRepeat();
        }
        if (token.code == lex.codeFor("ELSE")) {
            trace("Statement", false);
            return recur;
        }
        if (token.code == lex.codeFor("IDNT")) {
            recur = handleAssignment();
        } else {
            if (token.code == lex.codeFor("_IF_")) {
                recur = HandleIf();
            } else {
                if (token.code == lex.codeFor("READ")) {
                    recur = ReadLN();
                } else {
                    if (token.code == lex.codeFor("FOR_")) {
                        recur = HandleFor();
                    } else {
                        if (token.code == lex.codeFor("WHIL")) {
                            recur = HandleWhile();
                        } else {
                            if (token.code == lex.codeFor("WRIT")) {
                                recur = HandleWriteln();
                            } else {
                                if (token.code == lex.codeFor("SEMI")) {
                                    trace("Statement", false);
                                    return recur;
                                } else {
                                    error("Statement start", token.lexeme);
                                    recur = ErrorRecovery();
                                    anyErrors = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        trace("Statement", false);
        return recur;
    }

    private int ErrorRecovery() {
        trace("Error Recovery", true);
        trace("Error Recovery", false);
        return -1;
    }

    private int ReadLN() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("ReadLN", true);
        if (token.code == lex.codeFor("READ")) {
            token = lex.GetNextToken(false);
            if (token.code == lex.codeFor("LPAR")) {
                token = lex.GetNextToken(false);
                recur = Variable();
            }
            quads.AddQuad(interp.opcodeFor("READ"), 0, 0, recur);
            token = lex.GetNextToken(false);
        }
        trace("ReadLN", false);
        return recur;
    }

    private int handleAssignment() {
        int recur = 0;
        int varIndex = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("handleAssignment", true);
        varIndex = Variable();
        if (token.code == lex.codeFor("SET_")) {
            token = lex.GetNextToken(false);
            recur = SimpleExpression();
            quads.AddQuad(interp.opcodeFor("MOV"), recur, 0, varIndex);
        } else {
            error(lex.reserveFor("SET_"), token.lexeme);
        }
        trace("handleAssignment", false);
        return recur;
    }

    private int HandleWriteln() {
        int recur = 0;
        int toprint = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleWriteln", true);
        token = lex.GetNextToken(false);
        if (token.code == lex.codeFor("LPAR")) {
            token = lex.GetNextToken(false);
            if (token.code == lex.codeFor("STRN") || token.code == lex.codeFor("IDNT")) {
                toprint = symbolList.LookupSymbol(token.lexeme);
                token = lex.GetNextToken(false);
            } else {
                toprint = SimpleExpression();
            }
            quads.AddQuad(interp.opcodeFor("PRINT"), 0, 0, toprint);
            if (token.code == lex.codeFor("RPAR")) {
                token = lex.GetNextToken(false);
            } else {
                error(lex.reserveFor("RPAR"), token.lexeme);
            }
        } else {
            error(lex.reserveFor("LPAR"), token.lexeme);
        }
        trace("handlePrintn", false);
        return recur;
    }

    private int HandleFor() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("HandleFor", true);
        if (token.code == lex.codeFor("FOR_")) {
            token = lex.GetNextToken(false);
            if (token.code == lex.codeFor("IDNT")) {
                recur = Variable();
            }
            token = lex.GetNextToken(false);
            recur = SimpleExpression();
            if (token.code == lex.codeFor("_TO_")) {
                token = lex.GetNextToken(false);
                recur = SimpleExpression();
                if (token.code == lex.codeFor("_DO_")) {
                    token = lex.GetNextToken(false);
                    recur = Statement();
                }
            }
        }
        trace("HandleFor", false);
        return recur;
    }

    private int HandleIf() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("HandleIf", true);
        if (token.code == lex.codeFor("_IF_")) {
            token = lex.GetNextToken(false);
            if (token.code == lex.codeFor("IDNT")) {
                recur = RelExpression();
            }
            token = lex.GetNextToken(false);
            if (token.code == lex.codeFor("BEG_")) {
                recur = Statement();
                token = lex.GetNextToken(false);
                if (token.code == lex.codeFor("_IF_")) {
                    recur = Statement();
                }
            }
            if (token.code == lex.codeFor("_TO_")) {
                token = lex.GetNextToken(false);
                recur = SimpleExpression();
                if (token.code == lex.codeFor("_DO_")) {
                    token = lex.GetNextToken(false);
                    recur = Statement();
                }
            }
            if (token.code == lex.codeFor("WRIT")) {
                recur = Statement();
            }
        }
        trace("HandleIf", false);
        return recur;
    }

private int HandleWhile() {
    if (anyErrors) {
        return ErrorRecovery();
    }
    trace("HandleWhile", true);

    // Expecting 'WHIL' token already identified to get here
    token = lex.GetNextToken(false);  // Move past 'WHIL'

    int conditionIndex = RelExpression(); // Evaluate the condition part of the while

    if (token.code != lex.codeFor("_DO_")) {
        error("Expected 'do'", token.lexeme);
        anyErrors = true;
    } else {
        token = lex.GetNextToken(false);  // Move past 'DO_'
        if (token.code == lex.codeFor("BEG_")) {
            int resultIndex = Statement(); // Handle the loop body

            if (token.code != lex.codeFor("END_") && token.code != lex.codeFor("SEMI")) {
                error("Expected 'end'", token.lexeme);
                anyErrors = true;
            } else {
                token = lex.GetNextToken(false); // Move past 'END_'
            }

            trace("HandleWhile", false);
            return resultIndex;  // Return the result of processing the loop body
        } else {
            error("Expected 'begin' after 'do'", token.lexeme);
            anyErrors = true;
        }
    }

    trace("HandleWhile", false);
    return -1; // Return error condition
}


    private int HandleRepeat() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("HandleRepeat", true);
        if (token.code == lex.codeFor("REP_")) {
            token = lex.GetNextToken(false);
            if (token.code == lex.codeFor("IDNT")) {
                recur = Statement();
            }
            token = lex.GetNextToken(false);
            if (token.code == lex.codeFor("IDNT")) {
                recur = RelExpression();
            }
            if (token.code != lex.codeFor("SEMI")) {
                recur = SimpleExpression();
            }
            if (token.code == lex.codeFor("_TO_")) {
                token = lex.GetNextToken(false);
                recur = SimpleExpression();
                if (token.code == lex.codeFor("_DO_")) {
                    token = lex.GetNextToken(false);
                    recur = Statement();
                }
            }
            if (token.code == lex.codeFor("LSEQ")) {
                recur = Relop();
                if (token.code == lex.codeFor("IDNT") || token.code == lex.codeFor("ICST")) {
                    recur = SimpleExpression();
                }
            }
        }
        trace("HandleRepeat", false);
        return recur;
    }

    private int Relop() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("Relop", true);
        if (token.code == lex.codeFor("LSEQ") || token.code == lex.codeFor("GREQ")
                || token.code == lex.codeFor("GRTH") || token.code == lex.codeFor("LETH")) {
            token = lex.GetNextToken(false);
        }
        trace("Relop", false);
        return recur;
    }

    private int RelExpression() {
        int recur = 0;
        if (anyErrors) {
            return ErrorRecovery();
        }
        trace("RelExpression", true);
        if (token.code == lex.codeFor("IDNT") || token.code == lex.codeFor("ICST")) {
            recur = SimpleExpression();
            if (token.code == lex.codeFor("LSEQ") || token.code == lex.codeFor("GREQ")
                    || token.code == lex.codeFor("GRTH") || token.code == lex.codeFor("LETH")) {
                recur = Relop();
                if (token.code == lex.codeFor("IDNT") || token.code == lex.codeFor("ICST")) {
                    recur = SimpleExpression();
                }
            }
        }
        trace("RelExpression", false);
        return recur;
    }

    private int Variable() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        trace("Variable", true);
        if ((token.code == lex.codeFor("IDNT"))) {
            recur = symbolList.LookupSymbol(token.lexeme);
            token = lex.GetNextToken(false);
        } else {
            error("Variable", token.lexeme);
        }

        trace("Variable", false);
        return recur;

    }

    private void error(String wanted, String got) {
        anyErrors = true;
        System.out.println("ERROR: Expected " + wanted + " but found " + got);
    }

    private void trace(String proc, boolean enter) {
        String tabs = "";
        if (!traceon) {
            return;
        }
        if (enter) {
            tabs = repeatChar(" ", level);
            System.out.print(tabs);
            System.out.println("--> Entering " + proc);
            level++;
        } else {
            if (level > 0) {
                level--;
            }
            tabs = repeatChar(" ", level);
            System.out.print(tabs);
            System.out.println("<-- Exiting " + proc);
        }
    }

    private String repeatChar(String s, int x) {
        String result = "";
        for (int i = 1; i <= x; i++) {
            result += s;
        }
        return result;
    }
}

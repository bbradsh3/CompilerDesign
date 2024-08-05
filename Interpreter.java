

package ADT;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


/**
 *
 * @author Brandon
 */
public class Interpreter {
    //variables for maximum values of tables and quads
    static int maxTableLength = 25;
    static int maxQuad = 1000;
    //initialize opTable
    ReserveTable optable = new ReserveTable(maxTableLength);
    
    public Interpreter(){
        //cast opTable to initReserve to fill opTable
        initReserve(optable);
    }
    
    private String makeTraceString(int pc, int opcode,int op1,int op2,int op3 ){
        String result = "";
        result = "PC = "+String.format("%04d", pc)+": "+(optable.LookupCode(opcode)+"     ").substring(0,6)+String.format("%02d",op1)+
                                ", "+String.format("%02d",op2)+", "+String.format("%02d",op3);
        return result;
    }
    

    //HERE is a free opcode table initialization for a created ReserveTable
    private void initReserve(ReserveTable optable){
      optable.Add("STOP", 0);
      optable.Add("DIV", 1);
      optable.Add("MUL", 2);
      optable.Add("SUB", 3);
      optable.Add("ADD", 4);
      optable.Add("MOV", 5);
      optable.Add("PRINT", 6);
      optable.Add("READ", 7);
      optable.Add("JMP", 8);
      optable.Add("JZ", 9);
      optable.Add("JP", 10);
      optable.Add("JN", 11);
      optable.Add("JNZ", 12);
      optable.Add("JNP", 13);
      optable.Add("JNN", 14);
      optable.Add("JINDR", 15);
    }

    //returns the opcode for the give opstring
    public int opcodeFor(String opString){
        
        return optable.LookupName(opString);
        
    }

    public boolean initializeFactorialTest(SymbolTable stable, QuadTable qtable) {
        InitSTF(stable);
        InitQTforFactorial(qtable);
        return true;
    }
    
    public boolean initializeSummationTest(SymbolTable stable, QuadTable qtable){
        InitSTF(stable);
        InitQTforSummation(qtable);
        return true;
    }

    //factorial Symbols  
    public static void InitSTF(SymbolTable st) {
        st.AddSymbol("n", 'V', 10);
        st.AddSymbol("i", 'V', 0);
        st.AddSymbol("product", 'V', 0);
        st.AddSymbol("1", 'C', 1);
        st.AddSymbol("$temp", 'V', 0);

    }

    //factorial Quads 
    public void InitQTforFactorial(QuadTable qt) {
        qt.AddQuad(5, 3, 0, 2); //MOV
        qt.AddQuad(5, 3, 0, 1); //MOV
        qt.AddQuad(3, 1, 0, 4); //SUB
        qt.AddQuad(10, 4, 0, 7); //JP
        qt.AddQuad(2, 2, 1, 2); //MUL
        qt.AddQuad(4, 1, 3, 1); //ADD
        qt.AddQuad(8, 0, 0, 2); //JMP
        qt.AddQuad(6, 2, 0, 0); //Print   

    }
    
    //summation quads
    public void InitQTforSummation(QuadTable qt){
        
        qt.AddQuad(5, 3, 0, 2); //MOV
        qt.AddQuad(5, 3, 0, 1); //MOV
        qt.AddQuad(3, 1, 0, 4); //SUB
        qt.AddQuad(10, 4, 0, 7); //JP
        qt.AddQuad(4, 2, 1, 2); //ADD
        qt.AddQuad(4, 1, 3, 1); //ADD
        qt.AddQuad(8, 0, 0, 2); //JMP
        qt.AddQuad(6, 2, 0, 0); //Print 
    }
    
    public void InterpretQuads(QuadTable Q, SymbolTable S, boolean TraceOn, String filename) throws IOException{
        //initialize vars for program counter and quad data
        int pc, opcode, op1, op2, op3;
        //array to store data from quad index
        int[] quad = new int[4];
        
        //program counter = 0
        pc=0;
        while(pc < maxQuad){
            //store quad data from index pc in quad array 
            quad = Q.GetQuad(pc);
            //store quad data in corresponding vars
            opcode = quad[0];
            op1 = quad[1];
            op2 = quad[2];
            op3 = quad[3];
            

            
            //checks to make sure opcode is valid
            if(optable.IsValidOp(opcode)){
                
                //if trace mode is on print program counter, quad data, operands with symbol table data
                if(TraceOn){
                    //creates string for output with structure from makeTraceString method
                    String outputString = makeTraceString(pc, opcode, op1, op2, op3) + "\n";
                    System.out.print(outputString);
                    //Prints to the named file with the required error catching
                    appendToFile(filename, outputString); 
                  
                }

                switch(opcode){
                    //STOP
                    case 0:
                        System.out.println("Execution terminated by program STOP.");
                        return;
                    //DIV
                    case 1:
                        /*
                            updates the value in symbolTable S for index defined in op3
                            with results of op1 / op2
                        */
                        S.UpdateSymbol(op3, S.GetUsage(op3), (S.GetInteger(op1)/S.GetInteger(op2)));
                        //increment program counter
                        pc++;
                        break;
                        
                    //MUL
                    case 2:
                        /*
                            updates the value in symbolTable S for index defined in op3
                            with results of op1 * op2
                        */
                        S.UpdateSymbol(op3, S.GetUsage(op3), (S.GetInteger(op1)*S.GetInteger(op2)));
                        //increment program counter
                        pc++;
                        break;
                        
                    //SUB
                    case 3:
                        /*
                            updates the value in symbolTable S for index defined in op3
                            with results of op1 - op2
                        */
                        S.UpdateSymbol(op3, S.GetUsage(op3), (S.GetInteger(op1)-S.GetInteger(op2)));
                        //increment program counter
                        pc++;
                        break;
                        
                    //ADD
                    case 4:
                        /*
                            updates the value in symbolTable S for index defined in op3
                            with results of op1 + op2
                        */
                        S.UpdateSymbol(op3, S.GetUsage(op3), (S.GetInteger(op1)+S.GetInteger(op2)));
                        //increment program counter
                        pc++;
                        break;
                        
                    //MOV
                    case 5:
                        /*
                            updates the value in symbolTable S for index defined in op3 with value in index op1
                        */
                        S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1));
                        //increment program counter
                        pc++;
                        break;
                        
                    //PRINT
                    case 6:
                        
                            if(S.GetDataType(op3) == 'I'){
                                System.out.println(S.GetInteger(op3));
                                pc++;
                                break;
                            }
                            //prints the symbol name and value at index defined in op3
                            //System.out.print("PRINT RESULTS: Symbol Name:[" + S.GetSymbol(op3) + "] Symbol Value:[" + S.GetInteger(op3) + "] \n");
                            System.out.println(S.GetSymbol(op3));
                            //increment program counter
                            pc++;
                            break;
                            
                    //READ
                    case 7:
                        // Make a scanner to read from CONSOLE
                        Scanner sc = new Scanner(System.in);
                        // Put out a prompt to the user
                        System.out.print('>');
                        // Read one integer only   
                        int readval = sc.nextInt();    
                        // Op3 has the SymbolTable index we need, update its value
			S.UpdateSymbol(op3,'I',readval);
                        // Deallocate the scanner
                        sc = null;
                        // Increment Program Counter
			pc++;
                        break;
                        
                    //JMP
                    case 8:
                        //set program counter to op3 (quadTable index)
                        pc = op3;
                    //JZ
                    case 9:
                        //if integer value at op1 == 0 then set program counter to op3 value
                        if(S.GetInteger(op1) == 0){
                            pc = op3;
                        }
                        break;
                        
                        
                    //JP
                    case 10:
                        //if integer value at op1 > 0 then set program counter to op3 value
                        if(S.GetInteger(op1) > 0){
                            pc = op3;
                        }else{
                            pc++;
                        }
                        break;
                        
                    //JN
                    case 11:
                        //if integer value at op1 < 0 then set program counter to op3 value
                        if(S.GetInteger(op1) < 0){
                            pc = op3;
                        }else{
                            pc++;
                        }
                        break;
                        
                    //JNZ
                    case 12:
                        //if integer value at op1 != 0 then set program counter to op3 value
                        if(S.GetInteger(op1) != 0){
                            pc = op3;
                        }else{
                            pc++;
                        }
                        break;
                        
                    //JNP
                    case 13:
                        //if integer value at op1 <= 0 then set program counter to op3 value
                        if(S.GetInteger(op1) <= 0){
                            pc = op3;
                        }else{
                            pc++;
                        }
                        break;
                        
                    //JNN
                    case 14:
                        //if integer value at op1 >= 0 then set program counter to op3 value
                        if(S.GetInteger(op1) >= 0){
                            pc = op3;
                        }else{
                            pc++;
                        }
                        break;
                        
                    //JINDR
                    case 15:
                        //set program counter to op3 symbolTable value contents
                       pc = S.GetInteger(op3);
                       break;
 
                    default:
                        System.out.print("Invalid opcode");
                        break;
                        
                }//end switch
                
            }//end if valid opcode
            
             
        }//end while
        
        
    }//end InterpretQuads
    
    
    private static void appendToFile(String filename, String content) throws IOException{
        
        try(FileOutputStream fileOutputStream = new FileOutputStream(filename, true)){
            fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
        
    }
    
}//end interpreter class

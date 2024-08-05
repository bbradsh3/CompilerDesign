
package ADT;
import ADT.*;
import java.io.IOException;

/**
 *
 * @author abrouill FALL 2023
 */
public class main {

    public static void main(String[] args) throws IOException {
        String filePath = args[0];
        //String filePath = "C:\\Users\\Brandon\\Desktop\\CodeGenBASIC_SP24.txt";
        System.out.println("Code Generation FA2024, by Brandon Bradshaw");
        System.out.println("Parsing "+ filePath);
        boolean traceon = false; //false;
        Syntactic parser = new Syntactic(filePath, traceon);
        parser.parse();
        
        System.out.println("Done.");
    }

}

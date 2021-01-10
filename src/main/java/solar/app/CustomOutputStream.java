package solar.app;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
 
 
/**
 * This class extends from OutputStream to redirect output to a JTextArrea
 * @author www.codejava.net
 *
 */
public class CustomOutputStream extends OutputStream {
    private TextArea textArea;
     
    public CustomOutputStream(TextArea textArea) {
        this.textArea = textArea;
    }
     
   @Override
   public void write(int b) throws IOException {
     textArea.appendText("" + ((char)b));
   }
   @Override
   public void write(byte[] b) throws IOException {
     textArea.appendText(new String(b));
   }
   @Override
   public void write(byte[] b, int off, int len) throws IOException {
     textArea.appendText(new String(b, off, len));
   }
   
   public void println(String s)
   {
        try {
            write(s.getBytes());
            write('\n');
        } catch (IOException ex) {
            Logger.getLogger(CustomOutputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
}
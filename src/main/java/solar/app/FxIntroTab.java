package solar.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.web.WebView;

/**
 *
 * @author rocky
 */
public class FxIntroTab extends FxHtmlTab {

    private WebView webView;

    public FxIntroTab() {
        super();

        String html = "<p>Error</p>";
        try {
            InputStream in = getClass().getResourceAsStream("/README.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder textBuilder = new StringBuilder();
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
            reader.close();
            html = textBuilder.toString();
        } catch (IOException ex) {
            Logger.getLogger(FxIntroTab.class.getName()).log(Level.SEVERE, null, ex);
        }
        setText(html);
    }
}

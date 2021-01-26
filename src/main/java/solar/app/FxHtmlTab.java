package solar.app;

import java.net.URL;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;


/**
 * Simple tabpane to display HTML in a vertically scrolling window.
 * 
 * @author rocky
 */
public class FxHtmlTab extends BorderPane {

    private WebView webView;

    public FxHtmlTab() {
        super();

        webView = new WebView();
        ScrollPane sp = new ScrollPane();
        webView.setPrefSize(10000, 10000);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox p = new VBox();
        sp.setContent(p);
        p.getChildren().add(webView);
        VBox.setVgrow(sp, Priority.ALWAYS);
        p.setPadding(FxMainAnalysis.INSETS);
        setText("<p>Error</p>");
        setCenter(p);

        URL css = getClass().getResource("style.css");
        webView.getEngine().setUserStyleSheetLocation(css.toString());
    }
    
    protected final void setText(String html)
    {
        webView.getEngine().loadContent(html);
    }
}

package solar.app;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FxMainTest extends Application {
    
    public static final Insets INSETS = new Insets(20, 20, 20, 20);
    public static int SPACING = 20;
    
    public FxMainTest() {
        super();
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public void start(Stage stage) {
        
        BorderPane p = new BorderPane();
        FxMonthControl c = new FxMonthControl(1);
        
        TextArea t = new TextArea();
        
        t.setPrefSize(2000, 2000);
        
        c.getProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
                //t.appendText(arg2.toString()+"  ");
                t.appendText(c.getValue()+":"+c.getMonthName()+"  ");
            }
        });
        p.setCenter(t);
        p.setTop(c);
        Scene scene = new Scene(p);
        stage.setScene(scene);
        stage.setTitle("Solar PV Analysis Tool");
        
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

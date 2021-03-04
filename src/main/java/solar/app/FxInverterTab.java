package solar.app;

import java.util.Collection;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import solar.model.Components;
import solar.model.Inverter;
import solar.model.Listener;
import solar.model.Record;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.WATT;

/**
 * Graph inverter efficiencies for interest
 * 
 * @author rocky
 */
public class FxInverterTab extends BorderPane implements Listener{

    private static final double MAXPOWER = 5000.0;
    LineChart<Number, Number> sc;

    private final TextArea t = new TextArea();
    private Components components;

    public FxInverterTab() {

        super();

        t.setPrefSize(1000, 200);
        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        setTop(p);

        final NumberAxis xAxis = new NumberAxis(0.0, MAXPOWER/1000.0, 1.0);
        final NumberAxis yAxis = new NumberAxis(80.0, 100.0, 1.0);
        sc = new LineChart<>(xAxis, yAxis);
        sc.setPrefSize(10000, 10000);
        xAxis.setLabel("Input power kW");
        yAxis.setLabel("Efficiency %");
        sc.setCreateSymbols(false);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(sc);
        setCenter(p);

        ScrollPane sp = new ScrollPane();
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox box = new VBox();
        sp.setContent(box);
        box.getChildren().add(t);
        VBox.setVgrow(sp, Priority.ALWAYS);
        box.setPadding(FxMainAnalysis.INSETS);
        setBottom(box);

        analyse();
    }

    private void analyse() {

 //        importPanel.getOs().println("System Data");
//        importPanel.getOs().println("-----------");
//        importPanel.getOs().println("latitude=" + SystemData.latitude);
//        importPanel.getOs().println("longitude=" + SystemData.longitude);
//        importPanel.getOs().println(String.format(SystemData.south.toString()));
//        importPanel.getOs().println(String.format(SystemData.east.toString()));
//        importPanel.getOs().println(String.format(SystemData.west.toString()));
//        importPanel.getOs().println();
//        importPanel.getOs().println();
//        importPanel.getOs().println("Number of batteries=" + SystemData.NBATTERIES);
//        importPanel.getOs().println("Capacity of each battery=" + SystemData.BATTERY_CAPACITY);

        for (Inverter inv : components.getInverters().values()) {
            t.appendText(inv.toString() + "\n");
            XYChart.Series data = new XYChart.Series();
            data.setName(inv.name);
            for (double pin = 100.0; pin < MAXPOWER; pin += 50.0) {
                double pout = inv.pout(getQuantity(pin,WATT)).getValue().doubleValue();
                data.getData().add(new XYChart.Data(pin / 1000, 100 * pout / pin));
            }
            sc.getData().add(data);
        }
    }

    @Override
    public void changed(Collection<Record> records, Components componentsList) {
        this.components=componentsList;
    }
}

package solar.app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import solar.model.Calculator;

/**
 * SUN POSITION
 *
 * @author rocky
 */
public class SolarTab extends BorderPane {

    private final FxMonthControl monthControl = new FxMonthControl();
    private final Calculator calculator = new Calculator();
    protected final XYChart.Series elevationTrace = new XYChart.Series();
    protected final XYChart.Series azimuthTrace = new XYChart.Series();

    protected final NumberAxis xAxis = new NumberAxis(0, 24, 1);
    protected final NumberAxis yAxis = new NumberAxis(0, 50, 10);
    protected final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);

    private final FxLatitudeControl latitudeSlider = new FxLatitudeControl();

    public SolarTab() {

        super();

        this.getChildren().add(latitudeSlider);

        yAxis.setLabel("Angle");
        yAxis.setAutoRanging(true);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(50);
        yAxis.setTickUnit(10);

        xAxis.setLabel("Hour");
        //xAxis.setAutoRanging(true);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(24);
        xAxis.setTickUnit(1);

        latitudeSlider.getLatitudeProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                analyse();
                plot();
            }
        });

        monthControl.getMonthProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                analyse();
                plot();
            }
        });

        sc.setPrefSize(10000, 10000);
        sc.setAnimated(false);
        sc.setCreateSymbols(false);
        sc.getData().add(azimuthTrace);
        sc.getData().add(elevationTrace);
        azimuthTrace.setName("Azimuth");
        elevationTrace.setName("Elevation");

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Text("Month"), monthControl, new Text("Latitude"), latitudeSlider);
        setTop(p);
        
        plot();
    }

    protected void analyse() {
    }

    protected void plot() {
        int month = monthControl.getMonth();
        double latitude = latitudeSlider.getLatitudeValue();
        elevationTrace.getData().clear();
        azimuthTrace.getData().clear();
        for (double hour = 0.0; hour < 24.0; hour += 0.2) {
            int daynumber = Calculator.dayNumber(month, 15);
            double elevation = calculator.getSunElevation(latitude, daynumber, hour);
            elevationTrace.getData().add(new XYChart.Data(hour, elevation));
            double azimuth = calculator.getSunAzimuth(latitude, daynumber, hour);
            azimuthTrace.getData().add(new XYChart.Data(hour, azimuth));
        }
    }

    @Override
    public String toString() {
        return "Solar statistics";
    }
}

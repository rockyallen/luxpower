package solar.app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import solar.model.Calculator;

/**
 * INSOLATION
 *
 * @author rocky
 */
public class Insolation extends BorderPane {

    private final Calculator calculator =  Calculator.getInstance();
    protected final XYChart.Series extraterrestial = new XYChart.Series();
    protected final XYChart.Series beamNormalAtSeaLevel = new XYChart.Series();
    protected final XYChart.Series direct = new XYChart.Series();
    protected final XYChart.Series diffuse = new XYChart.Series();
    protected final XYChart.Series reflected = new XYChart.Series();
    protected final XYChart.Series total = new XYChart.Series();

    protected final NumberAxis xAxis = new NumberAxis(0, 364, 30);
    protected final NumberAxis yAxis = new NumberAxis(0, 1, 0.05);
    protected final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);

    protected FxLatitudeControl lat = new FxLatitudeControl(50);
    protected FxTiltControl tilt = new FxTiltControl(35);

    public Insolation() {

        super();

        yAxis.setLabel("Power, kW/m2");
        yAxis.setAutoRanging(true);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(50);
        yAxis.setTickUnit(10);

        xAxis.setLabel("Day number");
        //xAxis.setAutoRanging(true);

        sc.setPrefSize(10000, 10000);
        sc.setAnimated(false);
        sc.setCreateSymbols(false);
        sc.getData().add(extraterrestial);
        sc.getData().add(beamNormalAtSeaLevel);
        sc.getData().add(diffuse);
        sc.getData().add(direct);
        sc.getData().add(reflected);
        sc.getData().add(total);
        extraterrestial.setName("Extra terrestial");
        diffuse.setName("Diffuse (midday)");
        beamNormalAtSeaLevel.setName("Sea level (midday)");
        direct.setName("Direct (midday)");
        reflected.setName("Ground reflection (midday)");
        total.setName("Total received (midday)");

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(lat, tilt);        
        setTop(p);

        lat.getProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
                plot();
            }
        });
        tilt.getProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
                plot();
            }
        });
        plot();
    }

    public void init()
    {
        
    }
    
    protected void analyse() {
    }

    protected void plot() {
        beamNormalAtSeaLevel.getData().clear();
        diffuse.getData().clear();
        extraterrestial.getData().clear();
        direct.getData().clear();
        total.getData().clear();
        reflected.getData().clear();

        for (int dayNumber = 0; dayNumber < 365; dayNumber++) {
            double et = calculator.solarIrradiance(dayNumber);
            extraterrestial.getData().add(new XYChart.Data(dayNumber + 1, et));

            double cn = 1.0;
            double k = calculator.getAttenuation(dayNumber);
            double latitude = lat.getValue();
            // elevation at midday (12h solar time)
            double sunElevation = calculator.getSunElevation(latitude, dayNumber, 12.0);
            // pointing straight at the sun
            double directIrradiance = calculator.beamNormalIrradiance(cn, et, k, sunElevation);
            beamNormalAtSeaLevel.getData().add(new XYChart.Data(dayNumber + 1, directIrradiance));
            
            double panelTilt = tilt.getValue();
            
            double angle = Math.toRadians(sunElevation-(90-panelTilt));
            
            // component of normal received on a tilted panel
            double directRecieved = directIrradiance*Math.cos(angle);
            direct.getData().add(new XYChart.Data(dayNumber + 1, directRecieved));

            // factor 0-1
            double skyDiffusionFactor = calculator.getDiffusion(dayNumber);

            // W/m2
            double skyDiffuse = calculator.diffuseIrradiance(skyDiffusionFactor, directIrradiance, panelTilt);
            diffuse.getData().add(new XYChart.Data(dayNumber + 1, skyDiffuse));

            // W/m2
            double groundReflected = calculator.groundReflected(Calculator.SURFACE_REFLECTIVITY, directIrradiance, sunElevation, skyDiffusionFactor, panelTilt);
            reflected.getData().add(new XYChart.Data(dayNumber + 1, groundReflected));

            // W/m2
            double received = directRecieved + skyDiffuse + groundReflected;
            total.getData().add(new XYChart.Data(dayNumber + 1, received));
        }
    }

    @Override
    public String toString() {
        return "Irradiance";
    }
}

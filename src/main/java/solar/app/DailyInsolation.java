package solar.app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import solar.model.Calculator;

/**
 * DAILY INSOLATION
 *
 * @author rocky
 */
public class DailyInsolation extends BorderPane {

    private final Calculator calculator =  Calculator.getInstance();
    protected final XYChart.Series extraterrestial = new XYChart.Series();
    protected final XYChart.Series beamNormalAtSeaLevel = new XYChart.Series();
    protected final XYChart.Series direct = new XYChart.Series();
    protected final XYChart.Series diffuse = new XYChart.Series();
    protected final XYChart.Series reflected = new XYChart.Series();
    protected final XYChart.Series total = new XYChart.Series();

    protected final NumberAxis xAxis = new NumberAxis();
    protected final NumberAxis yAxis = new NumberAxis();
    protected final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);

    protected FxLatitudeControl lat = new FxLatitudeControl(50);
    protected FxTiltControl tilt = new FxTiltControl(35);
    //protected FxTiltControl azimuth = new FxTiltControl(0);
    protected FxMonthControl month = new FxMonthControl(1);

    protected Label kwh = new Label();
    
    public DailyInsolation() {

        super();

        yAxis.setLabel("Power, kW/m2");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1500);
        yAxis.setTickUnit(100);
        yAxis.setForceZeroInRange(false);

        xAxis.setLabel("Day number");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(4);
        xAxis.setUpperBound(20);
        xAxis.setTickUnit(1);
        xAxis.setForceZeroInRange(false);

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
        diffuse.setName("Diffuse");
        beamNormalAtSeaLevel.setName("Beam (Sea level)");
        direct.setName("Direct");
        reflected.setName("Ground reflection");
        total.setName("Total received");

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(lat, tilt, month, new Label("Energy "), kwh, new Label("kWh"));
        setTop(p);

        month.getProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
                plot();
            }
        });
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

    public void init() {

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

        double cn = 1.0;
        int dayNumber = Calculator.dayNumber(month.getValue(), 15);
        double k = calculator.getAttenuation(dayNumber);
        double latitude = lat.getValue();
        // factor 0-1
        double skyDiffusionFactor = calculator.getDiffusion(dayNumber);

        double STEP = 0.2;
        double energy=0.0;
        for (double hour = 4; hour < 20; hour += STEP) {
            double et = calculator.solarIrradiance(dayNumber);
            extraterrestial.getData().add(new XYChart.Data(hour, et));

            // elevation at midday (12h solar time)
            double sunElevation = calculator.getSunElevation(latitude, dayNumber, hour);
            // pointing straight at the sun
            double directIrradiance = calculator.beamNormalIrradiance(cn, et, k, sunElevation);
            beamNormalAtSeaLevel.getData().add(new XYChart.Data(hour, directIrradiance));

            double panelTilt = tilt.getValue();

            double angle = Math.toRadians(sunElevation - (90 - panelTilt));

            // component of normal received on a tilted panel
            double directRecieved = directIrradiance * Math.cos(angle);
            direct.getData().add(new XYChart.Data(hour, directRecieved));

            // W/m2
            double skyDiffuse = calculator.diffuseIrradiance(skyDiffusionFactor, directIrradiance, panelTilt);
            diffuse.getData().add(new XYChart.Data(hour, skyDiffuse));

            // W/m2
            double groundReflected = calculator.groundReflected(Calculator.SURFACE_REFLECTIVITY, directIrradiance, sunElevation, skyDiffusionFactor, panelTilt);
            reflected.getData().add(new XYChart.Data(hour, groundReflected));

            // W/m2
            double received = directRecieved + skyDiffuse + groundReflected;
            total.getData().add(new XYChart.Data(hour, received));
            
            energy+=received*STEP;
        }
        kwh.setText(String.format("%1.1f", energy/1000.0));
    }

    @Override
    public String toString() {
        return "Irradiance";
    }
}

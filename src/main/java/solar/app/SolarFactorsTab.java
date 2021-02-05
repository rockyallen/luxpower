package solar.app;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import solar.model.Calculator;

/**
 * SUN POSITION
 *
 * @author rocky
 */
public class SolarFactorsTab extends BorderPane {

    private final Calculator calculator = new Calculator();
    protected final XYChart.Series attenuationTrace = new XYChart.Series();
    protected final XYChart.Series weatherTrace = new XYChart.Series();
    protected final XYChart.Series diffusionTrace = new XYChart.Series();

    protected final NumberAxis xAxis = new NumberAxis(0, 364, 30);
    protected final NumberAxis yAxis = new NumberAxis(0, 1, 0.05);
    protected final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);

    public SolarFactorsTab() {

        super();

        yAxis.setLabel("Factor");
        yAxis.setAutoRanging(true);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(50);
        yAxis.setTickUnit(10);

        xAxis.setLabel("Day number");
        //xAxis.setAutoRanging(true);

        sc.setPrefSize(10000, 10000);
        sc.setAnimated(false);
        sc.setCreateSymbols(false);
        sc.getData().add(attenuationTrace);
        sc.getData().add(diffusionTrace);
        sc.getData().add(weatherTrace);
        diffusionTrace.setName("Diffusion");
        weatherTrace.setName("Weather (sunny days)");
        attenuationTrace.setName("Attenuation");

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);
        
        plot();
    }

    protected void analyse() {
    }

    protected void plot() {
        weatherTrace.getData().clear();
        diffusionTrace.getData().clear();
        attenuationTrace.getData().clear();
        for (int i = 0; i < 365; i++) {
            weatherTrace.getData().add(new XYChart.Data(i, calculator.getWeatherFactorSmoothed(i)));
            diffusionTrace.getData().add(new XYChart.Data(i, calculator.getDiffusion(i)));
            attenuationTrace.getData().add(new XYChart.Data(i, calculator.getAttenuation(i)));
        }
    }

    @Override
    public String toString() {
        return "Irradiance reduction factors";
    }
}

package solar.app;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import solar.model.Calculator;
import static solar.model.Calculator.Weather;

/**
 * SUN POSITION
 *
 * @author rocky
 */
public class SolarFactorsTab extends BorderPane {

    private final Calculator calculator =  Calculator.getInstance();
    protected final XYChart.Series attenuationTrace = new XYChart.Series();
    protected final XYChart.Series smoothedWeatherTrace = new XYChart.Series();
    protected final XYChart.Series rawWeatherTrace = new XYChart.Series();
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
        sc.getData().add(smoothedWeatherTrace);
        sc.getData().add(rawWeatherTrace);
        diffusionTrace.setName("Diffusion");
        smoothedWeatherTrace.setName("Weather (smoothed)");
        rawWeatherTrace.setName("Weather (raw)");
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
        smoothedWeatherTrace.getData().clear();
        diffusionTrace.getData().clear();
        attenuationTrace.getData().clear();
        for (int dayNumber = 0; dayNumber < 365; dayNumber++) {
            smoothedWeatherTrace.getData().add(new XYChart.Data(dayNumber + 1, Weather.SMOOTHEDWEATHER.getWeatherFactor(dayNumber)));
            rawWeatherTrace.getData().add(new XYChart.Data(dayNumber + 1, Weather.RAWWEATHER.getWeatherFactor(dayNumber)));
            diffusionTrace.getData().add(new XYChart.Data(dayNumber + 1, calculator.getDiffusion(dayNumber)));
            attenuationTrace.getData().add(new XYChart.Data(dayNumber + 1, calculator.getAttenuation(dayNumber)));
        }
    }

    @Override
    public String toString() {
        return "Irradiance reduction factors";
    }
}

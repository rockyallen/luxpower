package solar.app;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import solar.model.Calculator;
import solar.model.SolarArray;
import solar.model.SystemData;

/**
 *
 * @author rocky
 */
public class FxAnnualModelTab extends BorderPane {

    private final XYChart.Series traceSouth = new XYChart.Series();
    private final XYChart.Series traceEast = new XYChart.Series();
    private final XYChart.Series traceWest = new XYChart.Series();
    private final XYChart.Series traceCombined = new XYChart.Series();
    private final XYChart.Series traceLimited = new XYChart.Series();
    private final XYChart.Series traceWeather = new XYChart.Series();
    private final Text ratedPowerBox = new Text();
    private final Text ratedCapacityBox = new Text();
    private final Text yield1Box = new Text();
    private final Text yield2Box = new Text();
    private final Text yield3Box = new Text();

    public FxAnnualModelTab() {
//        chart.setToolTipText("Simulation at 15th day of the month");
//        eastBox.setToolTipText("Maximum possible yield before the inverter");
//        westBox.setToolTipText("Maximum possible yield before the inverter");
//        southBox.setToolTipText("Maximum possible yield before the inverter");
//        combinedBox.setToolTipText("Maximum possible yield before the inverter");
//        limitedBox.setToolTipText("Maximum possible yield after the inverter(s)");
//        inverterBox.setToolTipText("Total inverter losses, including parasitic loss, I2R loss and output limiting");

        HBox p = new HBox();
//        p.setPadding(FxMain.INSETS);
//        p.setSpacing(FxMain.SPACING);
//        p.getChildren().addAll(new Text("Month"), monthControl);
//        setTop(p);

        final NumberAxis xAxis = new NumberAxis(1, 365, 30);
        final NumberAxis yAxis = new NumberAxis(0.0, 80.0, 10.0);
        final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);
        sc.setPrefSize(10000, 10000);
        xAxis.setLabel("Day");
        yAxis.setLabel("kWh");
        traceSouth.setName("South");
        traceEast.setName("East");
        traceWest.setName("West");
        traceCombined.setName("Combined");
        traceLimited.setName("After inverter");
        traceWeather.setName("Including weather");
        sc.getData().addAll(traceSouth, traceEast, traceWest, traceCombined, traceLimited, traceWeather);
        sc.setCreateSymbols(false);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("Rated power"), size(ratedPowerBox));
        p.getChildren().addAll(new Label("Rated capacity"), size(ratedCapacityBox));
        p.getChildren().addAll(new Label("Maximum yield"), size(yield1Box));
        p.getChildren().addAll(new Label("Inverters"), size(yield2Box));
        p.getChildren().addAll(new Label("Weather"), size(yield3Box));
        setBottom(p);

        modelAndPlot();
    }

    private Text size(Text t) {
        return t;
    }

    private double power(int dday, double hour, SolarArray array) {
        double insolation = Calculator.insSolarRadiation(hour, SystemData.latitude, SystemData.longitude, 0, array.tilt, array.azimuth, dday, Calculator.CN, Calculator.SURFACE_REFLECTIVITY);
        return Math.max(0, array.efficiency * array.area * insolation);
    }

    private void modelAndPlot() {
        traceSouth.getData().clear();
        traceEast.getData().clear();
        traceWest.getData().clear();
        traceCombined.getData().clear();
        traceLimited.getData().clear();
        traceWeather.getData().clear();

        // run simulation to get sunny envelope
        double perfectYearTotal = 0.0; // kWh
        double limitedYearTotal = 0.0; // kWh
        double weatherYearTotal = 0.0; // kWh
        for (int month = 0; month < 12; month++) {
            for (int day = 1; day <= Calculator.daysPerMonth[month]; day += 1) {
                int dday = Calculator.dayNumber(month, day);
                // accumulators for each array
                double southDayTotal = 0;
                double eastDayTotal = 0;
                double westDayTotal = 0;
                // accumulator for inverter output
                double inverterDayTotal = 0;
                // accumulator for inverter output including effect of weather
                double weatherDayTotal = 0;
                for (int solarHour = 4; solarHour < 19; solarHour++) {
                    // instantaneous power for each array
                    double southPower = power(dday, solarHour, SystemData.south);
                    double eastPower = power(dday, solarHour, SystemData.east);
                    double westPower = power(dday, solarHour, SystemData.west);
                    // inverter output without weather
                    double inverterPower = SystemData.LuxPower.pout(eastPower + westPower) + SystemData.SunnyBoy.pout(southPower);
                    // inverter output with weather
                    double weatherFactor = SystemData.sunnyDays[month];
                    double weatherPower = SystemData.LuxPower.pout(weatherFactor * eastPower + weatherFactor * westPower) + SystemData.SunnyBoy.pout(weatherFactor * southPower);
                    // accumulate over day
                    southDayTotal += southPower;
                    eastDayTotal += eastPower;
                    westDayTotal += westPower;
                    inverterDayTotal += inverterPower;
                    weatherDayTotal += weatherPower;
                }

                traceSouth.getData().add(new XYChart.Data(dday, southDayTotal / 1000.0));
                traceEast.getData().add(new XYChart.Data(dday, eastDayTotal / 1000.0));
                traceWest.getData().add(new XYChart.Data(dday, westDayTotal / 1000.0));
                double combined = (southDayTotal + eastDayTotal + westDayTotal);
                traceCombined.getData().add(new XYChart.Data(dday, combined / 1000.0));
                traceLimited.getData().add(new XYChart.Data(dday, inverterDayTotal / 1000.0));
                traceWeather.getData().add(new XYChart.Data(dday, weatherDayTotal / 1000.0));

                // accumulate over year
                perfectYearTotal += combined;
                limitedYearTotal += inverterDayTotal;
                weatherYearTotal += weatherDayTotal;
            }

            ratedPowerBox.setText(String.format("%3.1f kW", SystemData.ratedPower));
            ratedCapacityBox.setText(String.format("%3.1f kWh", SystemData.ratedCapacity));
            yield1Box.setText(String.format("%3.1f kWh (%2.1f%%)", perfectYearTotal / 1000.0, perfectYearTotal / (10 * SystemData.ratedCapacity)));
            yield2Box.setText(String.format("%3.1f kWh (%2.1f%%)", limitedYearTotal / 1000.0, limitedYearTotal / (10 * SystemData.ratedCapacity)));
            yield3Box.setText(String.format("%3.1f kWh (%2.1f%%)", weatherYearTotal / 1000.0, weatherYearTotal / (10 * SystemData.ratedCapacity)));
        }
    }
}

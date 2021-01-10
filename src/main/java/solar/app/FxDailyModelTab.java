package solar.app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
public class FxDailyModelTab extends BorderPane {

    private final XYChart.Series traceSouth = new XYChart.Series();
    private final XYChart.Series traceEast = new XYChart.Series();
    private final XYChart.Series traceWest = new XYChart.Series();
    private final XYChart.Series traceCombined = new XYChart.Series();
    private final XYChart.Series traceLimited = new XYChart.Series();
    private final Text eastBox = new Text();
    private final Text westBox = new Text();
    private final Text southBox = new Text();
    private final Text combinedBox = new Text();
    private final Text limitedBox = new Text();

    private final Text inverterBox = new Text();

    private final FxMonthControl monthControl = new FxMonthControl();

    public FxDailyModelTab() {
//        chart.setToolTipText("Simulation at 15th day of the month");
//        eastBox.setToolTipText("Maximum possible yield before the inverter");
//        westBox.setToolTipText("Maximum possible yield before the inverter");
//        southBox.setToolTipText("Maximum possible yield before the inverter");
//        combinedBox.setToolTipText("Maximum possible yield before the inverter");
//        limitedBox.setToolTipText("Maximum possible yield after the inverter(s)");
//        inverterBox.setToolTipText("Total inverter losses, including parasitic loss, I2R loss and output limiting");

        monthControl.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
                //  if (!monthControl.isValueChanging()) {
                modelAndPlot();
                //  }
            }
        });

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Text("Month"), monthControl);
        setTop(p);

        final NumberAxis xAxis = new NumberAxis(4, 21, 1);
        final NumberAxis yAxis = new NumberAxis(0.0, 8.0, 1.0);
        final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);
        sc.setPrefSize(10000, 10000);
        xAxis.setLabel("Day");
        yAxis.setLabel("kWh");
        traceSouth.setName("South");
        traceEast.setName("East");
        traceWest.setName("West");
        traceCombined.setName("Combined");
        traceLimited.setName("After inverter");
        sc.getData().addAll(traceSouth, traceEast, traceWest, traceCombined, traceLimited);
        sc.setCreateSymbols(false);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("East"), size(eastBox));
        p.getChildren().addAll(new Label("West"), size(westBox));
        p.getChildren().addAll(new Label("South"), size(southBox));
        p.getChildren().addAll(new Label("Combined"), size(combinedBox));
        p.getChildren().addAll(new Label("Limited"), size(limitedBox));
        p.getChildren().addAll(new Label("Inverter loss"), size(inverterBox));
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

        // run simulation to get sunny envelope
        int month = monthControl.getMonth();
        // accumulators for each array
        double ss = 0;
        double ee = 0;
        double ww = 0;
        // accumulator for inverter output
        double tt = 0;
        // time resolution, hours
        double step = 0.20;
        for (double solarHour = 4; solarHour < 21; solarHour += step) {
            // maximum power for each array
            double s = 0;
            double e = 0;
            double w = 0;
            s = power(month * 30 + 15, solarHour, SystemData.south);
            e = power(month * 30 + 15, solarHour, SystemData.east);
            w = power(month * 30 + 15, solarHour, SystemData.west);
            // inverter output
            double t = 0;
            t = SystemData.LuxPower.pout(e + w) + SystemData.SunnyBoy.pout(s);
            // accumulate over day
            ss += s * step;
            ee += e * step;
            ww += w * step;
            tt += t * step;

            traceSouth.getData().add(new XYChart.Data(solarHour, s / 1000.0));
            traceEast.getData().add(new XYChart.Data(solarHour, e / 1000.0));
            traceWest.getData().add(new XYChart.Data(solarHour, w / 1000.0));
            traceCombined.getData().add(new XYChart.Data(solarHour, (s + e + w) / 1000.0));
            traceLimited.getData().add(new XYChart.Data(solarHour, t / 1000));
        }

        eastBox.setText(String.format("%3.1f kWh", ee / 1000));
        westBox.setText(String.format("%3.1f kWh", ww / 1000));
        southBox.setText(String.format("%3.1f kWh", ss / 1000));
        combinedBox.setText(String.format("%3.1f kWh", (ee + ww + ss) / 1000));
        limitedBox.setText(String.format("%3.1f kWh", tt / 1000));
        double loss = 100 * (1 - tt / (ee + ss + ww));
        inverterBox.setText(String.format("%3.1f%%", loss));
    }

}

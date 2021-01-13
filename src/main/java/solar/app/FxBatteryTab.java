package solar.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Listener;
import solar.model.Period;
import solar.model.DataStore;
import solar.model.Record;
import solar.model.RecordFilter;
import solar.model.SystemData;

/**
 *
 * @author rocky
 */
public class FxBatteryTab extends BorderPane implements Listener {

    private final FxSmoothingControl smoothing = new FxSmoothingControl();

    private final Text nominalCapacityBox = new Text();
    private final Text chargeBox = new Text();
    private final Text dischargeBox = new Text();
    private final Text dailyBox = new Text();
    private final Text utilisationBox = new Text();
    private final Text efficiencyBox = new Text();

    // Collectors used to transfer data from the simulation ot the chart
    private final XYChart.Series traceChg = new XYChart.Series();
    private final XYChart.Series traceDischg = new XYChart.Series();

    private Collection<Record> records = null;

    public FxBatteryTab() {

        super();

//        nominalCapacityBox.setTooltip(new Tooltip("Nominal capacity of the battries. (Not the same as the effective capacity which allows for a safe DOD)"));
//        chargeBox.setTooltip(new Tooltip("Daily charge"));
//        dischargeBox.setTooltip(new Tooltip("Daily discharge"));
//        dailyBox.setTooltip(new Tooltip("Mean discharge"));
//        utilisationBox.setTooltip(new Tooltip("Ratio of mean discharge to capacity"));
//        efficiencyBox.setTooltip(new Tooltip("Efficiency of the combined charge/discharge cycle"));
        smoothing.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
                if (!smoothing.isValueChanging()) {
                    analyse();
                    plot();
                }
            }
        });

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Text("Smoothing"), smoothing);
        setTop(p);

        final NumberAxis xAxis = new NumberAxis(1, 365, 7);
        final NumberAxis yAxis = new NumberAxis(0.0, 8.0, 1.0);
        final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);
        sc.setPrefSize(10000, 10000);
        xAxis.setLabel("Day");
        yAxis.setLabel("kWh");
        traceChg.setName("Charge");
        traceDischg.setName("Discharge");
        sc.getData().addAll(traceChg, traceDischg);
        sc.setCreateSymbols(false);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("Capacity"), size(nominalCapacityBox));
        p.getChildren().addAll(new Label("Charge"), size(chargeBox));
        p.getChildren().addAll(new Label("Discharge"), size(dischargeBox));
        p.getChildren().addAll(new Label("Mean discharge"), size(dailyBox));
        p.getChildren().addAll(new Label("Utilisation"), size(utilisationBox));
        p.getChildren().addAll(new Label("Efficiency"), size(efficiencyBox));
        setBottom(p);
    }

    List<DatedValue> totalDischarge = new ArrayList<>();
    List<DatedValue> totalCharge = new ArrayList<>();

    private void analyse() {
        totalDischarge.clear();
        totalCharge.clear();
//            System.out.println("gr="+ds.getDataStore.Records().size());
        List<Record> endOfDays = new RecordFilter<Record>(records).endOfPeriod(Period.DAY).result();
//            System.out.println(endOfDays.size());
        for (Record e : endOfDays) {
            //          System.out.println(e);
            totalCharge.add(new DatedValue(e.getDate(), e.geteChgDay()));
            totalDischarge.add(new DatedValue(e.getDate(), e.geteDisChgDay()));
        }

        double capacity = SystemData.battery.getActualCapacity();
        double chg = new DatedValueFilter(totalCharge).total();
        double dis = new DatedValueFilter(totalDischarge).total();
        nominalCapacityBox.setText(String.format("%3.1f kWh", capacity));
        chargeBox.setText(String.format("%3.1f kWh", chg));
        dischargeBox.setText(String.format("%3.1f kWh", dis));
        if (records.size() > 0) {
            double mean = dis / totalDischarge.size();
            dailyBox.setText(String.format("%3.1f kWh", mean));
            utilisationBox.setText(String.format("%3.1f%%", 100 * mean / capacity));
            efficiencyBox.setText(String.format("%3.1f%%", 100 * dis / chg));
        }
    }

    private void plot() {
        int sm = smoothing.getSmoothingValue();

        plot(traceChg, totalCharge, sm);
        plot(traceDischg, totalDischarge, sm);
    }

    private void plot(XYChart.Series trace, List<DatedValue> accumulator, int smoothing) {
        trace.getData().clear();
        DatedValueFilter filter = new DatedValueFilter(accumulator);
        Map<Integer, Double> m = filter.slidingMean(smoothing);
        for (Map.Entry<Integer, Double> e : m.entrySet()) {
            trace.getData().add(new XYChart.Data(e.getKey(), e.getValue()));
        }
    }

    @Override
    public void changed(Collection<Record> records, String description) {
        this.records = records;
        analyse();
        plot();
    }

    private Text size(Text t) {
//        t.setFill(new Paint(Color.red));
//        setPrefSize(80, 12);
        return t;
    }
}

package solar.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Listener;
import solar.model.Period;
import solar.model.Record;
import solar.model.RecordFilter;
import solar.model.SystemData;

/**
 * Daily performance averaged per month, with smoothing
 *
 * @author rocky
 */
public class FxAnalysisDailyTab extends BorderPane implements Listener {

    private final FxMonthControl monthControl = new FxMonthControl();
    private final CheckBox arraysCheckBox = new CheckBox("Inputs (otherwise outputs)");
    private final XYChart.Series traceGeneration = new XYChart.Series();
    private final XYChart.Series traceConsumption = new XYChart.Series();
    private final XYChart.Series traceExported = new XYChart.Series();
    private final XYChart.Series traceImported = new XYChart.Series();
    private final XYChart.Series traceSelfUse = new XYChart.Series();
    private final XYChart.Series tracePv1 = new XYChart.Series();
    private final XYChart.Series tracePv2 = new XYChart.Series();
    private final XYChart.Series tracePv3 = new XYChart.Series();

    private final NumberAxis xAxis = new NumberAxis(1, 24, 1);
    // Auto scale?
    private final NumberAxis yAxis = new NumberAxis(0.0, 5.0, 1.0);
    private final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);

    private Collection<Record> records;

    public FxAnalysisDailyTab() {

        super();

        monthControl.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
                analyse();
                plot();
            }
        });

        arraysCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_val, Boolean new_val) {
                show(tracePv1, new_val);
                show(tracePv2, new_val);
                show(tracePv3, new_val);
                show(traceConsumption, !new_val);
                show(traceImported, !new_val);
                show(traceExported, !new_val);
                show(traceSelfUse, !new_val);
            }

            private void show(XYChart.Series trace, boolean show) {
                if (show && !sc.getData().contains(trace)) {
                    sc.getData().add(trace);
                } else if (!show && sc.getData().contains(trace)) {
                    sc.getData().remove(trace);
                }
            }
        });
        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Text("Month"), monthControl);
        p.getChildren().addAll(arraysCheckBox);

        setTop(p);

        sc.setPrefSize(10000, 10000);
        xAxis.setLabel("Hour");
        yAxis.setLabel("kW");
        traceGeneration.setName("Generation");
        traceConsumption.setName("Consumption");
        traceImported.setName("Imported");
        traceExported.setName("Exported");
        traceSelfUse.setName("Self Use");
        tracePv1.setName(SystemData.pv1.name);
        tracePv2.setName(SystemData.pv2.name);
        tracePv3.setName(SystemData.pv3.name);
        sc.setCreateSymbols(false);
        sc.getData().addAll(traceGeneration);

        arraysCheckBox.setSelected(true);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        setBottom(p);
    }

    private void analyse() {

         Collection<Record> thisMonthRecords = new RecordFilter<Record>(records).period(monthControl.getMonth(), Period.MONTH).result();

        tracePv1.getData().clear();
        tracePv2.getData().clear();
        tracePv3.getData().clear();
        traceGeneration.getData().clear();
        traceImported.getData().clear();
        traceExported.getData().clear();
        traceConsumption.getData().clear();
        traceSelfUse.getData().clear();

        for (int hour = 0; hour < 24; hour++) {
            List<DatedValue> totalPv1 = new ArrayList<>();
            List<DatedValue> totalPv2 = new ArrayList<>();
            List<DatedValue> totalPv3 = new ArrayList<>();
            List<DatedValue> totalGen = new ArrayList<>();
            List<DatedValue> totalImport = new ArrayList<>();
            List<DatedValue> totalExport = new ArrayList<>();
            List<DatedValue> totalConsumption = new ArrayList<>();
            List<DatedValue> totalSelfUse = new ArrayList<>();

            RecordFilter<Record> filter = new RecordFilter<Record>(thisMonthRecords);
            List<Record> thisHour = filter.period(hour, Period.HOUR).result();
            for (Record r : thisHour) {

                totalPv1.add(new DatedValue(r.getDate(), r.getPpv1()));
                totalPv2.add(new DatedValue(r.getDate(), r.getPpv2()));
                totalPv3.add(new DatedValue(r.getDate(), r.getPpv3()));
                
                double generated = r.getPinv();
                double exported = r.getpToGrid();
                double selfUse = generated - exported;
                double imported = r.getpToUser();
                double consumption = imported + selfUse;
                
                totalGen.add(new DatedValue(r.getDate(), generated));
                totalSelfUse.add(new DatedValue(r.getDate(), selfUse));
                totalExport.add(new DatedValue(r.getDate(), exported));
                totalImport.add(new DatedValue(r.getDate(), imported));
                totalConsumption.add(new DatedValue(r.getDate(), consumption));
            }
            addPoint(tracePv1, totalPv1, hour, 0.001);
            addPoint(tracePv2, totalPv2, hour, 0.001);
            addPoint(tracePv3, totalPv3, hour, 0.001);
            addPoint(traceGeneration, totalGen, hour, 0.001);
            addPoint(traceExported, totalExport, hour, 0.001);
            addPoint(traceImported, totalImport, hour, 0.001);
            addPoint(traceSelfUse, totalSelfUse, hour, 0.001);
            addPoint(traceConsumption, totalConsumption, hour, 0.001);
        }
    }

    private void plot() {
//        int sm = smoothing.getSmoothingValue();
    }

    @Override
    public void changed(Collection<Record> records, String description) {
        this.records = records;
        analyse();
        plot();
    }

    private Text size(Text t) {
        return t;
    }

    private void addPoint(XYChart.Series trace, List<DatedValue> accumulator, int hour, double scale) {
        int points = accumulator.size();
        if (points > 0) {
            DatedValueFilter filter = new DatedValueFilter(accumulator);
            double mean = filter.total() / points;
            // + 0.5 to plot in the middle of the hour
            trace.getData().add(new XYChart.Data(hour+0.5, mean * scale));
        }
    }
}

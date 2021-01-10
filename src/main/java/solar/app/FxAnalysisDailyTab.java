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
import solar.model.DataSource;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Listener;
import solar.model.Period;
import solar.model.Record;
import solar.model.RecordFilter;

/**
 * Daily performance averaged per month, with smoothing
 *
 * @author rocky
 */
public class FxAnalysisDailyTab extends BorderPane implements Listener {

//    FxSmoothingControl smoothing = new FxSmoothingControl();
    private final FxMonthControl monthControl = new FxMonthControl();
    private CheckBox arraysCheckBox = new CheckBox("Show arrays");
//    private final Text yieldBox = new Text();
//    private final Text selfUseBox = new Text();
//    private final Text selfUseRatioBox = new Text();
//    private final Text consumptionBox = new Text();
//    private final Text importBox = new Text();
//    private final Text exportBox = new Text();
//    private final Text capacityFactorBox = new Text();

    // Collectors used to transfer data from the simulation ot the chart
//    private final List<DatedValue> totalEast = new ArrayList<>();
//    private final List<DatedValue> totalWest = new ArrayList<>();
//    private final List<DatedValue> totalSouth = new ArrayList<>();
//    private final List<DatedValue> totalGen = new ArrayList<>();
//    private final List<DatedValue> totalImport = new ArrayList<>();
//    private final List<DatedValue> totalExport = new ArrayList<>();
//    private final List<DatedValue> totalConsumption = new ArrayList<>();
//    private final List<DatedValue> totalSelfUse = new ArrayList<>();
    private final XYChart.Series traceGeneration = new XYChart.Series();
    private final XYChart.Series traceConsumption = new XYChart.Series();
    private final XYChart.Series traceExported = new XYChart.Series();
    private final XYChart.Series traceImported = new XYChart.Series();
    private final XYChart.Series traceSelfUse = new XYChart.Series();
    private final XYChart.Series traceSouth = new XYChart.Series();
    private final XYChart.Series traceEast = new XYChart.Series();
    private final XYChart.Series traceWest = new XYChart.Series();

    private final NumberAxis xAxis = new NumberAxis(1, 24, 1);
    // Auto scale?
    private final NumberAxis yAxis = new NumberAxis(0.0, 5.0, 1.0);
    private final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);

    private final DataSource ds;

    public FxAnalysisDailyTab(DataSource ds) {

        super();
        this.ds = ds;

        ds.addListener(this);

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
                show(traceEast, new_val);
                show(traceWest, new_val);
                show(traceSouth, new_val);
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
//        p.getChildren().addAll(new Text("Smoothing"), smoothing);
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
        traceEast.setName("East");
        traceWest.setName("West");
        traceSouth.setName("South");
        sc.setCreateSymbols(false);
        sc.getData().addAll(traceGeneration);

        arraysCheckBox.setSelected(true);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        ds.addListener(this);
        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
//        p.getChildren().addAll(new Label("Consumption"), size(consumptionBox));
//        p.getChildren().addAll(new Label("Generation"), size(yieldBox));
//        p.getChildren().addAll(new Label("Export"), size(exportBox));
//        p.getChildren().addAll(new Label("Import"), size(importBox));
//        p.getChildren().addAll(new Label("Self use"), size(selfUseBox));
//        p.getChildren().addAll(new Label("Self use ratio"), size(selfUseRatioBox));
//        p.getChildren().addAll(new Label("Capacity factor"), size(capacityFactorBox));
        setBottom(p);
    }

    private void analyse() {

        Collection<Record> input = ds.getRecords();
//        System.out.println("all records=" + input.size());
//        Collection<Record> thisMonthRecords = new RecordFilter<Record>(input).period(monthControl.getMonth(), Period.MONTH).result();
        Collection<Record> thisMonthRecords = new RecordFilter<Record>(input).period(monthControl.getMonth(), Period.MONTH).result();

//        System.out.println("month records=" + thisMonthRecords.size());
        traceEast.getData().clear();
        traceWest.getData().clear();
        traceSouth.getData().clear();
        traceGeneration.getData().clear();
        traceImported.getData().clear();
        traceExported.getData().clear();
        traceConsumption.getData().clear();
        traceSelfUse.getData().clear();

        for (int hour = 0; hour < 24; hour++) {
            List<DatedValue> totalEast = new ArrayList<>();
            List<DatedValue> totalWest = new ArrayList<>();
            List<DatedValue> totalSouth = new ArrayList<>();
            List<DatedValue> totalGen = new ArrayList<>();
            List<DatedValue> totalImport = new ArrayList<>();
            List<DatedValue> totalExport = new ArrayList<>();
            List<DatedValue> totalConsumption = new ArrayList<>();
            List<DatedValue> totalSelfUse = new ArrayList<>();

            RecordFilter<Record> filter = new RecordFilter<Record>(thisMonthRecords);
            List<Record> thisHour = filter.period(hour, Period.HOUR).result();
//            System.out.println("hour " + hour + " points=" + thisHour.size());
            for (Record r : thisHour) {

                totalWest.add(new DatedValue(r.getDate(), r.getPpv1()));
                totalEast.add(new DatedValue(r.getDate(), r.getPpv2()));
                totalSouth.add(new DatedValue(r.getDate(), r.getPpv3()));
                
                double generated = r.getPpv1() + r.getPpv2() + r.getPpv3();
                double exported = r.getpToGrid();
                double selfUse = generated - exported;
                double imported = r.getpToUser(); // fixme
                double consumption = imported + selfUse;
                
                totalGen.add(new DatedValue(r.getDate(), generated));
                totalSelfUse.add(new DatedValue(r.getDate(), selfUse));
                totalExport.add(new DatedValue(r.getDate(), exported));
                totalImport.add(new DatedValue(r.getDate(), imported));
                totalConsumption.add(new DatedValue(r.getDate(), consumption));
            }
            addPoint(traceEast, totalEast, hour, 0.001);
            addPoint(traceWest, totalWest, hour, 0.001);
            addPoint(traceSouth, totalSouth, hour, 0.001);
            addPoint(traceGeneration, totalGen, hour, 0.001);
            addPoint(traceExported, totalExport, hour, 0.001);
            addPoint(traceImported, totalImport, hour, 0.001);
            addPoint(traceSelfUse, totalSelfUse, hour, 0.001);
            addPoint(traceConsumption, totalConsumption, hour, 0.001);
        }
//        yieldBox.setText(String.format("%3.0f kWh", totalGen.total()));
//        importBox.setText(String.format("%3.0f kWh", totalImport.total()));
//        exportBox.setText(String.format("%3.0f kWh", totalExport.total()));
//        consumptionBox.setText(String.format("%3.0f kWh", totalConsumption.total()));
//        selfUseBox.setText(String.format("%3.0f kWh", totalSelfUse.total()));
//        selfUseRatioBox.setText(String.format("%3.1f%%", 100 * totalSelfUse.total() / totalGen.total()));
//        capacityFactorBox.setText(String.format("%3.1f%%", 100 * totalGen.total() / SystemData.ratedCapacity));
    }

    private void plot() {
//        int sm = smoothing.getSmoothingValue();
    }

    @Override
    public void changed() {
        analyse();
        plot();
    }

    private Text size(Text t) {
//        t.setFill(new Paint(Color.red));
//        setPrefSize(80, 12);
        return t;
    }

    private void addPoint(XYChart.Series trace, List<DatedValue> accumulator, int hour, double scale) {
        int points = accumulator.size();
        if (points > 0) {
            DatedValueFilter filter = new DatedValueFilter(accumulator);
            double mean = filter.total() / points;
            //System.out.println("hour=" + hour + " records=" + points + " mean=" + mean);
            trace.getData().add(new XYChart.Data(hour+1, mean * scale));
        }
    }
}

package solar.app;

import java.util.ArrayList;
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
import solar.model.DataSource;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Listener;
import solar.model.Period;
import solar.model.Record;
import solar.model.RecordFilter;
import solar.model.SystemData;

/**
 *
 * @author rocky
 */
public class FxAnalysisTab extends BorderPane implements Listener {

    FxSmoothingControl smoothing = new FxSmoothingControl();

    private final Text yieldBox = new Text();
    private final Text selfUseBox = new Text();
    private final Text selfUseRatioBox = new Text();
    private final Text consumptionBox = new Text();
    private final Text importBox = new Text();
    private final Text exportBox = new Text();
    private final Text capacityFactorBox = new Text();

    private final List<DatedValue> totalSelfUse = new ArrayList<>();
    private final List<DatedValue> totalGen = new ArrayList<>();
    private final List<DatedValue> totalImport = new ArrayList<>();
    private final List<DatedValue> totalExport = new ArrayList<>();
    private final List<DatedValue> totalConsumption = new ArrayList<>();

    // Collectors used to transfer data from the simulation ot the chart
    private final XYChart.Series traceGeneration = new XYChart.Series();
    private final XYChart.Series traceConsumption = new XYChart.Series();
    private final XYChart.Series traceExported = new XYChart.Series();
    private final XYChart.Series traceImported = new XYChart.Series();
    private final XYChart.Series traceSelfUse = new XYChart.Series();
    private final DataSource ds;
    private FxSummaryTab summaryTab;

    public FxAnalysisTab(DataSource ds, FxSummaryTab summaryTab) {

        super();
        this.ds = ds;
        this.summaryTab = summaryTab;

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
        final NumberAxis yAxis = new NumberAxis(0, 50, 10);
        final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);
        sc.setPrefSize(10000, 10000);
        xAxis.setLabel("Day");
        yAxis.setLabel("kWh");
        traceGeneration.setName("Generation");
        traceConsumption.setName("Consumption");
        traceImported.setName("Imported");
        traceExported.setName("Exported");
        traceSelfUse.setName("Self Use");
        sc.getData().addAll(traceGeneration, traceConsumption, traceImported, traceExported, traceSelfUse);
        sc.setCreateSymbols(false);

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        ds.addListener(this);
        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("Consumption"), size(consumptionBox));
        p.getChildren().addAll(new Label("Generation"), size(yieldBox));
        p.getChildren().addAll(new Label("Export"), size(exportBox));
        p.getChildren().addAll(new Label("Import"), size(importBox));
        p.getChildren().addAll(new Label("Self use"), size(selfUseBox));
        p.getChildren().addAll(new Label("Self use ratio"), size(selfUseRatioBox));
        p.getChildren().addAll(new Label("Capacity factor"), size(capacityFactorBox));
        setBottom(p);
    }

    private void analyse() {

        totalGen.clear();
        totalSelfUse.clear();
        totalExport.clear();
        totalImport.clear();
        totalConsumption.clear();

        List<Record> endOfDays = new RecordFilter<Record>(ds.getRecords()).endOfPeriod(Period.DAY).result();
        for (Record r : endOfDays) {
            double generated = r.getePv1Day() + r.getePv2Day() + r.getePv3Day();
            double exported = r.geteToGridDay();
            double selfUse = generated - exported;
            double imported = r.geteToUserDay();

            totalGen.add(new DatedValue(r.getDate(), generated));
            totalSelfUse.add(new DatedValue(r.getDate(), selfUse));
            totalExport.add(new DatedValue(r.getDate(), exported));
            totalImport.add(new DatedValue(r.getDate(), imported));
            totalConsumption.add(new DatedValue(r.getDate(), imported + selfUse));
        }

        double totalGenTotal = new DatedValueFilter(totalGen).total();
        double totalSelfUseTotal = new DatedValueFilter(totalSelfUse).total();
        yieldBox.setText(String.format("%3.0f kWh", totalGenTotal));
        importBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalImport).total()));
        exportBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalExport).total()));
        consumptionBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalConsumption).total()));
        selfUseBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalSelfUse).total()));
        selfUseRatioBox.setText(String.format("%3.1f%%", 100 * totalSelfUseTotal / totalGenTotal));
        capacityFactorBox.setText(String.format("%3.1f%%", 100 * totalGenTotal / SystemData.ratedCapacity));
        
        summaryTab.populate(totalGen, totalSelfUse, totalExport, totalImport, totalConsumption);
    }

    private void plot() {
        int sm = smoothing.getSmoothingValue();

        plot(traceGeneration, totalGen, sm);
        plot(traceImported, totalImport, sm);
        plot(traceExported, totalExport, sm);
        plot(traceConsumption, totalConsumption, sm);
        plot(traceSelfUse, totalSelfUse, sm);
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
    public void changed() {
        analyse();
        plot();
    }

    private Text size(Text t) {
//        t.setFill(new Paint(Color.red));
//        setPrefSize(80, 12);
        return t;
    }
}

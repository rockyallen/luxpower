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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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
import static solar.model.SystemData.pv1;
import static solar.model.SystemData.pv2;
import static solar.model.SystemData.pv3;

/**
 *
 * @author rocky
 */
public class FxAnalysisTab extends BorderPane implements Listener {

    private final FxSmoothingControl smoothing = new FxSmoothingControl();
    private final CheckBox arraysCheckBox = new CheckBox("Inputs (otherwise outputs)");

    private final Text yieldBox = new Text();
    private final Text selfUseBox = new Text();
    private final Text selfUseRatioBox = new Text();
    private final Text consumptionBox = new Text();
    private final Text importBox = new Text();
    private final Text exportBox = new Text();
    private final Text capacityFactorBox = new Text();

    private final List<DatedValue> totalPv1 = new ArrayList<>();
    private final List<DatedValue> totalPv2 = new ArrayList<>();
    private final List<DatedValue> totalPv3 = new ArrayList<>();
    private final List<DatedValue> totalGen = new ArrayList<>();
    private final List<DatedValue> totalImport = new ArrayList<>();
    private final List<DatedValue> totalExport = new ArrayList<>();
    private final List<DatedValue> totalConsumption = new ArrayList<>();
    private final List<DatedValue> totalSelfUse = new ArrayList<>();

    // Collectors used to transfer data from the simulation ot the chart
    private final XYChart.Series tracePv1 = new XYChart.Series();
    private final XYChart.Series tracePv2 = new XYChart.Series();
    private final XYChart.Series tracePv3 = new XYChart.Series();
    private final XYChart.Series traceGeneration = new XYChart.Series();
    private final XYChart.Series traceConsumption = new XYChart.Series();
    private final XYChart.Series traceExported = new XYChart.Series();
    private final XYChart.Series traceImported = new XYChart.Series();
    private final XYChart.Series traceSelfUse = new XYChart.Series();
    private FxSummaryTab summaryTab;
    private Collection<Record> records;

    public FxAnalysisTab(FxSummaryTab summaryTab) {

        super();
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
        p.getChildren().addAll(new Text("Smoothing"), smoothing, arraysCheckBox);
        setTop(p);

        final NumberAxis xAxis = new NumberAxis(1, 365, 7);
        final NumberAxis yAxis = new NumberAxis(0, 50, 10);
        final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);
        sc.setPrefSize(10000, 10000);
        xAxis.setLabel("Day");
        yAxis.setLabel("kWh");
        tracePv1.setName(SystemData.pv1.name);
        tracePv2.setName(SystemData.pv2.name);
        tracePv3.setName(SystemData.pv3.name);
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

        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("Consumption"), size(consumptionBox));
        p.getChildren().addAll(new Label("Inverter"), size(yieldBox));
        p.getChildren().addAll(new Label("Export"), size(exportBox));
        p.getChildren().addAll(new Label("Import"), size(importBox));
        p.getChildren().addAll(new Label("Self use"), size(selfUseBox));
        p.getChildren().addAll(new Label("Self use ratio"), size(selfUseRatioBox));
        p.getChildren().addAll(new Label("Capacity factor"), size(capacityFactorBox));
        setBottom(p);
    }

    private void analyse() {

        totalPv1.clear();
        totalPv2.clear();
        totalPv3.clear();
        totalGen.clear();
        totalImport.clear();
        totalExport.clear();
        totalConsumption.clear();
        totalSelfUse.clear();

        totalGen.clear();
        totalSelfUse.clear();
        totalExport.clear();
        totalImport.clear();
        totalConsumption.clear();

        //System.out.println("input="+records.size());
        List<Record> endOfDays = new RecordFilter<>(records).endOfPeriod(Period.DAY).result();
        //System.out.println("end of day="+endOfDays.size());
        for (Record r : endOfDays) {
            totalPv1.add(new DatedValue(r.getDate(), r.getePv1Day()));
            totalPv2.add(new DatedValue(r.getDate(), r.getePv2Day()));
            totalPv3.add(new DatedValue(r.getDate(), r.getePv3Day()));

            double generated = r.geteInvDay();
            double exported = r.geteToGridDay();
            double selfUse = generated - exported;
            double imported = r.geteToUserDay();

            totalGen.add(new DatedValue(r.getDate(), r.geteInvDay()));
            totalSelfUse.add(new DatedValue(r.getDate(), selfUse));
            totalExport.add(new DatedValue(r.getDate(), exported));
            totalImport.add(new DatedValue(r.getDate(), imported));
            totalConsumption.add(new DatedValue(r.getDate(), imported + selfUse));
        }

        final double ratedPower = (pv3.power + pv2.power + pv1.power) / 1000.0; // kW
        final double ratedCapacity = ratedPower * 365 * 24; // kW

        double totalGenTotal = new DatedValueFilter(totalGen).total();
        double totalSelfUseTotal = new DatedValueFilter(totalSelfUse).total();
        yieldBox.setText(String.format("%3.0f kWh", totalGenTotal));
        importBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalImport).total()));
        exportBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalExport).total()));
        consumptionBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalConsumption).total()));
        selfUseBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalSelfUse).total()));
        selfUseRatioBox.setText(String.format("%3.1f%%", 100 * totalSelfUseTotal / totalGenTotal));
        capacityFactorBox.setText(String.format("%3.1f%%", 100 * totalGenTotal / ratedCapacity));

        summaryTab.populate(totalPv1, totalPv2, totalPv3, totalGen, totalSelfUse, totalExport, totalImport, totalConsumption);
    }

    private void plot() {
        int sm = smoothing.getSmoothingValue();

        plot(traceGeneration, totalGen, sm);

        if (arraysCheckBox.isSelected()) {
            plot(tracePv1, totalPv1, sm);
            plot(tracePv2, totalPv2, sm);
            plot(tracePv3, totalPv3, sm);

            traceImported.getData().clear();
            traceExported.getData().clear();
            traceConsumption.getData().clear();
            traceSelfUse.getData().clear();
        } else {
            plot(traceImported, totalImport, sm);
            plot(traceExported, totalExport, sm);
            plot(traceConsumption, totalConsumption, sm);
            plot(traceSelfUse, totalSelfUse, sm);

            tracePv1.getData().clear();
            tracePv2.getData().clear();
            tracePv3.getData().clear();
        }
    }

    private void plot(XYChart.Series trace, List<DatedValue> accumulator, int smoothing) {
        trace.getData().clear();
        DatedValueFilter filter = new DatedValueFilter(accumulator);
        Map<Integer, Double> m = filter.slidingMean(smoothing);
        for (Map.Entry<Integer, Double> e : m.entrySet()) {
            // + 0.5 to plot in the middle of the period
            trace.getData().add(new XYChart.Data(e.getKey() + 0.5, e.getValue()));
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

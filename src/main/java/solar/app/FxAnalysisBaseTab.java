package solar.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import solar.model.DatedValue;
import solar.model.Listener;
import solar.model.Record;

/**
 * Common stuff for energy and power graphs. Saves duplication of code.
 *
 * @author rocky
 */
public abstract class FxAnalysisBaseTab extends BorderPane implements Listener {

    protected final CheckBox allCheckBox = new CheckBox("All");

    protected final Text yieldBox = new Text();
    protected final Text selfUseBox = new Text();
    protected final Text selfUseRatioBox = new Text();
    protected final Text consumptionBox = new Text();
    protected final Text importBox = new Text();
    protected final Text exportBox = new Text();
    protected final Text capacityFactorBox = new Text();

    protected final Text nominalCapacityBox = new Text();
    protected final Text chargeBox = new Text();
    protected final Text dischargeBox = new Text();
    protected final Text dailyBox = new Text();
    protected final Text utilisationBox = new Text();
    protected final Text efficiencyBox = new Text();

    protected final List<DatedValue> totalPv1 = new ArrayList<>();
    protected final List<DatedValue> totalPv2 = new ArrayList<>();
    protected final List<DatedValue> totalPv3 = new ArrayList<>();
    protected final List<DatedValue> totalCombined = new ArrayList<>();
    protected final List<DatedValue> totalGen = new ArrayList<>();
    protected final List<DatedValue> totalImport = new ArrayList<>();
    protected final List<DatedValue> totalExport = new ArrayList<>();
    protected final List<DatedValue> totalConsumption = new ArrayList<>();
    protected final List<DatedValue> totalSelfUse = new ArrayList<>();
    protected final List<DatedValue> totalDischarge = new ArrayList<>();
    protected final List<DatedValue> totalCharge = new ArrayList<>();

    // Collectors used to transfer data from the simulation ot the chart
    protected final XYChart.Series tracePv1 = new XYChart.Series();
    protected final XYChart.Series tracePv2 = new XYChart.Series();
    protected final XYChart.Series tracePv3 = new XYChart.Series();
    protected final XYChart.Series traceCombined = new XYChart.Series();
    protected final XYChart.Series traceGeneration = new XYChart.Series();
    protected final XYChart.Series traceConsumption = new XYChart.Series();
    protected final XYChart.Series traceExported = new XYChart.Series();
    protected final XYChart.Series traceImported = new XYChart.Series();
    protected final XYChart.Series traceSelfUse = new XYChart.Series();
    protected final XYChart.Series traceCharge = new XYChart.Series();
    protected final XYChart.Series traceDischarge = new XYChart.Series();

    protected Collection<Record> records = Collections.EMPTY_LIST;

    protected final NumberAxis xAxis = new NumberAxis(1, 365, 7);
    protected final NumberAxis yAxis = new NumberAxis(0, 50, 10);
    protected final LineChart<Number, Number> sc = new LineChart<>(xAxis, yAxis);

    protected final Map<CheckBox, XYChart.Series> traces = new LinkedHashMap<>();
    protected final Collection<List<DatedValue>> accumulators = new ArrayList<>();

    public FxAnalysisBaseTab() {

        super();

        traces.put(new CheckBox("PV1"), tracePv1);
        traces.put(new CheckBox("PV2"), tracePv2);
        traces.put(new CheckBox("PV3"), tracePv3);
        traces.put(new CheckBox("Combined"), traceCombined);
        traces.put(new CheckBox("Generation"), traceGeneration);
        traces.put(new CheckBox("Consumed"), traceConsumption);
        traces.put(new CheckBox("Imported"), traceImported);
        traces.put(new CheckBox("Exported"), traceExported);
        traces.put(new CheckBox("Charge"), traceCharge);
        traces.put(new CheckBox("Discharge"), traceDischarge);
        traces.put(new CheckBox("Self use"), traceSelfUse);

        accumulators.add(totalPv1);
        accumulators.add(totalPv2);
        accumulators.add(totalPv3);
        accumulators.add(totalCombined);
        accumulators.add(totalGen);
        accumulators.add(totalImport);
        accumulators.add(totalExport);
        accumulators.add(totalConsumption);
        accumulators.add(totalSelfUse);
        accumulators.add(totalDischarge);
        accumulators.add(totalCharge);

        for (CheckBox b : traces.keySet()) {
            b.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    plot();
                }
            });
        }
        allCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                for (CheckBox b : traces.keySet()) {
                    b.setSelected(new_val);
                }
            }
        });

        sc.setPrefSize(10000, 10000);
        sc.setAnimated(false);
        sc.setCreateSymbols(false);

        tracePv1.setName("pv1");
        tracePv2.setName("pv2");
        tracePv3.setName("pv3");
        traceCombined.setName("Combined");
        traceGeneration.setName("Generation");
        traceConsumption.setName("Consumption");
        traceImported.setName("Imported");
        traceExported.setName("Exported");
        traceSelfUse.setName("Self Use");
        traceCharge.setName("Charge");
        traceDischarge.setName("Discharge");

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.getChildren().addAll(sc);
        setCenter(p);

        VBox v = new VBox();
        v.setPadding(FxMainAnalysis.INSETS);
        v.setSpacing(FxMainAnalysis.SPACING);
        v.getChildren().add(allCheckBox);
        for (CheckBox box : traces.keySet()) {
            v.getChildren().add(box);
        }
        this.setRight(v);
    }

    protected abstract void analyse();

    protected abstract void plot();

    @Override
    public void changed(Collection<Record> records, String description) {
        this.records = records;
        analyse();
        plot();
    }

    protected Text size(Text t) {
        return t;
    }
}

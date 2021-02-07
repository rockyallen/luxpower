package solar.app;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Listener;
import solar.model.Period;
import solar.model.Record;
import solar.model.RecordFilter;

/**
 * Daily performance averaged by month
 *
 * @author rocky
 */
public class FxAnalysisDailyTab extends FxAnalysisBaseTab implements Listener {

    private final FxMonthControl monthControl = new FxMonthControl();

    public FxAnalysisDailyTab() {

        super();
        monthControl.getMonthProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
                analyse();
                plot();
            }
        });

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Text("Month"), monthControl);
        setTop(p);

        yAxis.setLabel("kW");
        yAxis.setAutoRanging(true);
        yAxis.setLowerBound(0.0);
        yAxis.setUpperBound(5.0);
        yAxis.setTickUnit(1.0);

        xAxis.setLabel("Hour");
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(24);
        xAxis.setTickUnit(1);
        //sc.setCreateSymbols(true);
    }

    @Override
    protected void analyse() {

        Collection<Record> thisMonthRecords = new RecordFilter<>(records).period(monthControl.getMonth(), Period.Month).result();

        for (XYChart.Series s : traces.values()) {
            s.getData().clear();
        }

        for (int hour = 0; hour < 24; hour++) {
            for (Collection<DatedValue> ll : accumulators) {
                ll.clear();
            }

            RecordFilter<Record> filter = new RecordFilter<>(thisMonthRecords);
            List<Record> thisHour = filter.period(hour, Period.Hour).result();
            for (Record r : thisHour) {

                totalPv1.add(new DatedValue(r.getDate(), r.getPpv1()));
                totalPv2.add(new DatedValue(r.getDate(), r.getPpv2()));
                totalPv3.add(new DatedValue(r.getDate(), r.getPpv3()));
                totalCombined.add(new DatedValue(r.getDate(), r.getPpv1() + r.getPpv2() + r.getPpv3()));
                totalInverter.add(new DatedValue(r.getDate(), r.getPinv()));
                totalSelfUse.add(new DatedValue(r.getDate(), r.getpLoad() - r.getpToUser()));
                totalExport.add(new DatedValue(r.getDate(), r.getpToGrid()));
                totalImport.add(new DatedValue(r.getDate(), r.getpToUser()));
                totalConsumption.add(new DatedValue(r.getDate(), r.getpLoad()));
                totalCharge.add(new DatedValue(r.getDate(), r.getpCharge()));
                totalDischarge.add(new DatedValue(r.getDate(), r.getpDisCharge()));
            }
            addPoint(tracePv1, totalPv1, hour, 0.001);
            addPoint(tracePv2, totalPv2, hour, 0.001);
            addPoint(tracePv3, totalPv3, hour, 0.001);
            addPoint(traceCombined, totalCombined, hour, 0.001);
            addPoint(traceInverter, totalInverter, hour, 0.001);
            addPoint(traceExported, totalExport, hour, 0.001);
            addPoint(traceImported, totalImport, hour, 0.001);
            addPoint(traceSelfUse, totalSelfUse, hour, 0.001);
            addPoint(traceConsumption, totalConsumption, hour, 0.001);
            addPoint(traceCharge, totalCharge, hour, 0.001);
            addPoint(traceDischarge, totalDischarge, hour, 0.001);
        }
    }

    @Override
    protected void plot() {
        for (Map.Entry<CheckBox, XYChart.Series> e : traces.entrySet()) {
            boolean show = e.getKey().isSelected();
            XYChart.Series trace = e.getValue();
            if (show && !sc.getData().contains(trace)) {
                sc.getData().add(trace);
            } else /*if (!show && sc.getData().contains(trace))*/ {
                sc.getData().remove(trace);
            }
        }
    }

    private void addPoint(XYChart.Series trace, List<DatedValue> accumulator, int hour, double scale) {
        int points = accumulator.size();
        if (points > 0) {
            DatedValueFilter filter = new DatedValueFilter(accumulator);
            double mean = filter.total() / points;
            // + 0.5 to plot in the middle of the hour
            trace.getData().add(new XYChart.Data(hour + 0.5, mean * scale));
        }
    }

    @Override
    public String toString() {
        return "Daily power graph";
    }
}

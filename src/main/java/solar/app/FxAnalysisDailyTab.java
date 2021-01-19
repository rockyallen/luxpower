package solar.app;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Listener;
import solar.model.Period;
import solar.model.Record;
import solar.model.RecordFilter;

/**
 * Daily performance averaged per month
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
        //xAxis.setAutoRanging(true);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(24);
        xAxis.setTickUnit(1);

        VBox v = new VBox();
        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("Usage:"));
        p.getChildren().addAll(new Label("Consumption"), size(consumptionBox));
        p.getChildren().addAll(new Label("Inverter"), size(yieldBox));
        p.getChildren().addAll(new Label("Export"), size(exportBox));
        p.getChildren().addAll(new Label("Import"), size(importBox));
        p.getChildren().addAll(new Label("Self use"), size(selfUseBox));
        p.getChildren().addAll(new Label("Self use ratio"), size(selfUseRatioBox));
        p.getChildren().addAll(new Label("Capacity factor"), size(capacityFactorBox));
        v.getChildren().add(p);
        p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(new Label("Battery:"));
        p.getChildren().addAll(new Label("Capacity"), size(nominalCapacityBox));
        p.getChildren().addAll(new Label("Charge"), size(chargeBox));
        p.getChildren().addAll(new Label("Discharge"), size(dischargeBox));
        p.getChildren().addAll(new Label("Mean discharge"), size(dailyBox));
        p.getChildren().addAll(new Label("Utilisation"), size(utilisationBox));
        p.getChildren().addAll(new Label("Efficiency"), size(efficiencyBox));
        v.getChildren().add(p);
        setBottom(v);
    }

    @Override
    protected void analyse() {

        Collection<Record> thisMonthRecords = new RecordFilter<>(records).period(monthControl.getMonth(), Period.MONTH).result();

        //System.out.println("Analysing month="+ monthControl.getMonth()+ " records="+thisMonthRecords.size());
        for (XYChart.Series s : traces.values()) {
            s.getData().clear();
        }

        for (int hour = 0; hour < 24; hour++) {
            for (Collection<DatedValue> ll : accumulators) {
                ll.clear();
            }

            RecordFilter<Record> filter = new RecordFilter<>(thisMonthRecords);
            List<Record> thisHour = filter.period(hour, Period.HOUR).result();
            for (Record r : thisHour) {

                totalPv1.add(new DatedValue(r.getDate(), r.getPpv1()));
                totalPv2.add(new DatedValue(r.getDate(), r.getPpv2()));
                totalPv3.add(new DatedValue(r.getDate(), r.getPpv3()));
                totalCombined.add(new DatedValue(r.getDate(), r.getPpv1() + r.getPpv2() + r.getPpv3()));

                double generated = r.getPinv() - r.getpDisCharge();
                double exported = r.getpToGrid();
                double imported = r.getpToUser();
                double charge = r.getpCharge();
                double disCharge = r.getpDisCharge();
                double selfUse = generated - exported - charge;
                double consumption = imported + selfUse + disCharge;

                totalGeneration.add(new DatedValue(r.getDate(), generated));
                totalInverter.add(new DatedValue(r.getDate(), r.getPinv()));
                totalSelfUse.add(new DatedValue(r.getDate(), selfUse));
                totalExport.add(new DatedValue(r.getDate(), exported));
                totalImport.add(new DatedValue(r.getDate(), imported));
                totalConsumption.add(new DatedValue(r.getDate(), consumption));
                totalCharge.add(new DatedValue(r.getDate(), charge));
                totalDischarge.add(new DatedValue(r.getDate(), r.getpDisCharge()));
            }
            addPoint(tracePv1, totalPv1, hour, 0.001);
            addPoint(tracePv2, totalPv2, hour, 0.001);
            addPoint(tracePv3, totalPv3, hour, 0.001);
            addPoint(traceCombined, totalCombined, hour, 0.001);
            addPoint(traceGeneration, totalGeneration, hour, 0.001);
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
            } else if (!show && sc.getData().contains(trace)) {
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
}

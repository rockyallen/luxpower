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
import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import solar.model.Components;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Listener;
import solar.model.Period;
import solar.model.Record;
import solar.model.RecordFilter;
import static solar.model.SolarUnitsAndConstants.KILO_WATT;
import static solar.model.SolarUnitsAndConstants.KILO_WATT_HOUR;
import tech.units.indriya.quantity.Quantities;
import static tech.units.indriya.unit.Units.YEAR;

/**
 * End of day energy performance with smoothing.
 *
 * @author rocky
 */
public class FxAnalysisTab extends FxAnalysisBaseTab implements Listener {

    private final FxSmoothingControl smoothing = new FxSmoothingControl(1);

    private FxSummaryTab summaryTab;

    public FxAnalysisTab(FxSummaryTab summaryTab) {

        super();
        this.summaryTab = summaryTab;

        yAxis.setLabel("kWh");
        yAxis.setAutoRanging(true);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(50);
        yAxis.setTickUnit(10);

        xAxis.setLabel("Day");
        //xAxis.setAutoRanging(true);
        xAxis.setLowerBound(1);
        xAxis.setUpperBound(365);
        xAxis.setTickUnit(30);

        smoothing.getProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                analyse();
                plot();
            }
        });

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);
        p.getChildren().addAll(smoothing);
        setTop(p);

//        VBox v = new VBox();
//        p = new HBox();
//        p.setPadding(FxMainAnalysis.INSETS);
//        p.setSpacing(FxMainAnalysis.SPACING);
//        p.getChildren().addAll(new Label("Usage:"));
//        p.getChildren().addAll(new Label("Consumption"), size(consumptionBox));
//        p.getChildren().addAll(new Label("Inverter"), size(yieldBox));
//        p.getChildren().addAll(new Label("Export"), size(exportBox));
//        p.getChildren().addAll(new Label("Import"), size(importBox));
//        p.getChildren().addAll(new Label("Self use"), size(selfUseBox));
//        p.getChildren().addAll(new Label("Self use ratio"), size(selfUseRatioBox));
//        p.getChildren().addAll(new Label("Capacity factor"), size(capacityFactorBox));
//        v.getChildren().add(p);
//
//        p = new HBox();
//        p.setPadding(FxMainAnalysis.INSETS);
//        p.setSpacing(FxMainAnalysis.SPACING);
//        p.getChildren().addAll(new Label("Battery:"));
//        p.getChildren().addAll(new Label("Capacity"), size(nominalCapacityBox));
//        p.getChildren().addAll(new Label("Charge"), size(chargeBox));
//        p.getChildren().addAll(new Label("Discharge"), size(dischargeBox));
//        p.getChildren().addAll(new Label("Mean discharge"), size(dailyBox));
//        p.getChildren().addAll(new Label("Utilisation"), size(utilisationBox));
//        p.getChildren().addAll(new Label("Efficiency"), size(efficiencyBox));
//        v.getChildren().add(p);
//        setBottom(v);
    }

    @Override
    protected void analyse() {

        for (Collection<DatedValue> ll : accumulators) {
            ll.clear();
        }
        List<Record> endOfDays = new RecordFilter<>(records).endOfPeriod(Period.Day).result();
        for (Record r : endOfDays) {

            totalCharge.add(new DatedValue(r.getDate(), r.geteChgDay().getValue().doubleValue()));
            totalDischarge.add(new DatedValue(r.getDate(), r.geteDisChgDay().getValue().doubleValue()));

            totalPv1.add(new DatedValue(r.getDate(), r.getePv1Day().getValue().doubleValue()));
            totalPv2.add(new DatedValue(r.getDate(), r.getePv2Day().getValue().doubleValue()));
            totalPv3.add(new DatedValue(r.getDate(), r.getePv3Day().getValue().doubleValue()));
            totalCombined.add(new DatedValue(r.getDate(), r.getePv1Day().add(r.getePv2Day()).add(r.getePv3Day()).getValue().doubleValue()));

            // there is a problem because load is only recorded as a power, not an energy.
            // have to reconstruct it
            Quantity<Energy> eload = (r.geteInvDay().add(r.geteToUserDay()).subtract(r.geteToGridDay()));

            totalInverter.add(new DatedValue(r.getDate(), r.geteInvDay().getValue().doubleValue()));
            totalSelfUse.add(new DatedValue(r.getDate(), eload.subtract(r.geteToUserDay()).getValue().doubleValue()));
            totalExport.add(new DatedValue(r.getDate(), r.geteToGridDay().getValue().doubleValue()));
            totalImport.add(new DatedValue(r.getDate(), r.geteToUserDay().getValue().doubleValue()));
            totalConsumption.add(new DatedValue(r.getDate(), eload.getValue().doubleValue()));
        }

        Quantity<Power> ratedPower = components.getPv1().getRatedPower().add(components.getPv2().getRatedPower()).add(components.getPv3().getRatedPower());

        Quantity<Energy> ratedCapacity = ratedPower.multiply(Quantities.getQuantity(1,YEAR)).asType(Energy.class).to(KILO_WATT_HOUR);

        double totalGenTotal = new DatedValueFilter(totalInverter).total();
        double totalSelfUseTotal = new DatedValueFilter(totalSelfUse).total();

        yieldBox.setText(String.format("%3.0f kWh", totalGenTotal));
        importBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalImport).total()));
        exportBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalExport).total()));
        consumptionBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalConsumption).total()));
        selfUseBox.setText(String.format("%3.0f kWh", new DatedValueFilter(totalSelfUse).total()));
        selfUseRatioBox.setText(String.format("%3.1f%%", 100 * totalSelfUseTotal / totalGenTotal));
        capacityFactorBox.setText(String.format("%3.1f%%", 100 * totalGenTotal / ratedCapacity.getValue().doubleValue()));

        Quantity<Energy> batteryCapacity = components.getBattery().getNominalCapacity().to(KILO_WATT_HOUR);
        double chg = new DatedValueFilter(totalCharge).total();
        double dis = new DatedValueFilter(totalDischarge).total();
        nominalCapacityBox.setText(batteryCapacity.toString());
        chargeBox.setText(String.format("%3.1f kWh", chg));
        dischargeBox.setText(String.format("%3.1f kWh", dis));
        if (records.size() > 0) {
            double mean = dis / totalDischarge.size();
            dailyBox.setText(String.format("%3.1f kWh", mean));
            utilisationBox.setText(String.format("%3.1f%%", 100 * mean / batteryCapacity.getValue().doubleValue()));
            efficiencyBox.setText(String.format("%3.1f%%", 100 * dis / chg));
        }
        summaryTab.populate(totalPv1, totalPv2, totalPv3, totalCombined, null, totalInverter, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge, components);
    }

    @Override
    protected void plot() {
        int sm = smoothing.getValue();

        // Inefficient: plots all traces before deciding whether or not to display them.
        plot(tracePv1, totalPv1, sm);
        plot(tracePv2, totalPv2, sm);
        plot(tracePv3, totalPv3, sm);
        plot(traceCombined, totalCombined, sm);
        //plot(traceGeneration, totalGeneration, sm);
        plot(traceInverter, totalInverter, sm);
        plot(traceExported, totalExport, sm);
        plot(traceConsumption, totalConsumption, sm);
        plot(traceSelfUse, totalSelfUse, sm);
        plot(traceImported, totalImport, sm);
        plot(traceCharge, totalCharge, sm);
        plot(traceDischarge, totalDischarge, sm);

        for (Map.Entry<CheckBox, XYChart.Series> e : traces.entrySet()) {
            boolean show = e.getKey().isSelected();
            XYChart.Series trace = e.getValue();
            if (show && !sc.getData().contains(trace)) {
                sc.getData().add(trace);

                //trace.getNode().setStyle("-fx-stroke: rgba(0,0,255, 0.15);");
            } else if (!show && sc.getData().contains(trace)) {
                sc.getData().remove(trace);
            }
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

    /**
     * @param costs the costs to set
     */
    public void setComponents(Components costs) {
        this.components = costs;
    }

    @Override
    public String toString() {
        return "Annual Energy graph";
    }
}

package solar.app;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import org.asciidoctor.Asciidoctor;
import solar.model.Components;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Period;
import solar.model.Record;

/**
 * Energy yield tabulated by month and summarized for the year,
 *
 * @author rocky
 */
public class FxSummaryTab extends FxHtmlTab {

    private Collection<Record> records;
    private String description;
    private Components components;
    private double ratedPower; // W
    private double ratedCapacity; // Wh

    RadioButton rb1 = new RadioButton("Total");
    RadioButton rb2 = new RadioButton("Daily mean");

    final ToggleGroup group = new ToggleGroup();

    public FxSummaryTab() {

        super();
        setText("Run analysis to see this table");

        rb1.setSelected(true);
        rb1.setToggleGroup(group);
        rb2.setToggleGroup(group);

        group.selectedToggleProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {

            }
        });
        HBox b = new HBox();
        b.setPadding(FxMainAnalysis.INSETS);
        b.setSpacing(FxMainAnalysis.SPACING);

        b.getChildren().addAll(rb1, rb2);
        setTop(b);
    }

    void populate(List<DatedValue> pv1, List<DatedValue> pv2, List<DatedValue> pv3, List<DatedValue> totalCombined, List<DatedValue> totalGen, List<DatedValue> totalInverter, List<DatedValue> totalSelfUse, List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption, List<DatedValue> totalCharge, List<DatedValue> totalDischarge, Components components) {

        this.components = components;

        ratedPower = (components.getPv1().getRatedPower() + components.getPv2().getRatedPower() + components.getPv3().getRatedPower()); // W
        ratedCapacity = ratedPower * 365 * 24; // Wh

        StringBuilder sb = new StringBuilder();

        sb.append("\n= Summary\n\n");
        sb.append("[cols=\"16*^\", options=\"header\"]\n");
        sb.append("|===\n");
        sb.append("|Month");
        sb.append("|PV1 kWh");
        sb.append("|PV2 kWh");
        sb.append("|PV3 kWh");
        sb.append("|Combined kWh");
        sb.append("|Yield kWh");
        sb.append("|Import kWh");
        sb.append("|Export kWh");
        sb.append("|Consumption kWh");
        sb.append("|Charge kWh");
        sb.append("|Discharge kWh");
        sb.append("|Battery utilisation");
        sb.append("|Self-use kWh");
        sb.append("|Self-use fraction");
        sb.append("|Capacity Factor");
        sb.append("|Bill\n\n");

        SimpleDateFormat f = new SimpleDateFormat("MMM");

        for (int month = 0; month < 12; month++) {
            sb.append(String.format("|%s\n", f.format(new Date(100, month, 1))));
            doRow(sb, month, Period.MONTH, pv1, pv2, pv3, totalCombined, totalGen, totalInverter, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);
        }
        sb.append("|Year\n");
        doRow(sb, 1, Period.ALL, pv1, pv2, pv3, totalCombined, totalGen, totalInverter, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);

        sb.append("|===\n");

        sb.append(String.format("Rated power: %3.1f kW\n\n", ratedPower / 1000.0));
        sb.append(String.format("Rated capacity: %3.1f kWh\n", ratedCapacity / 1000.0));

        sb.append("\n\n== Columns\n\n");

        sb.append("PV1:: Measured at the output of the array\n");
        sb.append("PV2:: Measured at the output of the array\n");
        sb.append("PV3:: Measured at the output of the array\n");
        sb.append("Combined:: Sum of PV1, PV2 and PV3 at the input to the inverter\n");
        sb.append("Yield:: Measured at the inverter output. **Includes battery discharge**\n");
        sb.append("Import:: Import from the grid\n");
        sb.append("Export:: Export to the grid\n");
        sb.append("Consumption:: User loads, calculated\n");
        sb.append("Charge:: Cumulative battery charge per day\n");
        sb.append("Discharge:: Cumulative battery discharge per day\n");
        sb.append("Battery utilisation:: Daily discharge divided by nominal capacity\n");
        sb.append("Self-use:: Portion of yield that is supplied to the user loads\n");
        sb.append("Self-use fraction:: Self-use divided by yield\n");
        sb.append("Capacity factor:: Yield divided by rated capacity\n");
        sb.append("Bill:: Standing charge + import - export - FITS\n\n");

        sb.append("== Additional Terms\n\n");
        sb.append("Rated power:: Sum of kWp of all arrays where kWp is the output measured at 1000 W/m2 sunlight at normal incidence\n");
        sb.append("Rated capacity:: Annual energy output assuming the system operates at rated power 24/7\n\n");

        sb.append("\n\n== Components\n\n");
        sb.append(components.getPv1().toString()).append("\n\n");
        sb.append(components.getPv2().toString()).append("\n\n");
        sb.append(components.getPv3().toString()).append("\n\n");
        sb.append(components.getBattery().toString()).append("\n\n");
        sb.append(components.getInv12().toString()).append("\n\n");

        String asciidoc = sb.toString();

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        String html = asciidoctor.convert(asciidoc, new HashMap<String, Object>());

        setText(html);
    }

    private void doRow(StringBuilder sb, int periodNumber, Period period, List<DatedValue> pv1, List<DatedValue> pv2, List<DatedValue> pv3, List<DatedValue> totalCombined, List<DatedValue> totalGen, List<DatedValue> totalInverter, List<DatedValue> totalSelfUse, List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption, List<DatedValue> totalCharge, List<DatedValue> totalDischarge) {
        {

            DatedValueFilter filter = new DatedValueFilter(totalInverter);
            filter.period(periodNumber, period);

            if (filter.size() > 0) {
                DecimalFormat df = new DecimalFormat("#,###");

                double totalGenTotal = filter.total(); // kWh

                sb.append(String.format("^|%3s\n", df.format(totalForPeriod(pv1, periodNumber, period))));

                sb.append(String.format("^|%3s\n", df.format(totalForPeriod(pv2, periodNumber, period))));

                double epv3 = totalForPeriod(pv3, periodNumber, period);
                sb.append(String.format("^|%3s\n", df.format(epv3)));

                sb.append(String.format("|%3s\n", df.format(totalForPeriod(totalCombined, periodNumber, period))));

                double eInverter = totalForPeriod(totalInverter, periodNumber, period);
                sb.append(String.format("|%3s\n", df.format(eInverter)));

                double eImport = totalForPeriod(totalImport, periodNumber, period);
                sb.append(String.format("|%3s\n", df.format(eImport)));

                double eExport = totalForPeriod(totalExport, periodNumber, period);
                sb.append(String.format("|%3s\n", df.format(eExport)));

                sb.append(String.format("|%3s\n", df.format(totalForPeriod(totalConsumption, periodNumber, period))));

                sb.append(String.format("|%3s\n", df.format(totalForPeriod(totalCharge, periodNumber, period))));

                double eDischarge = totalForPeriod(totalDischarge, periodNumber, period);
                sb.append(String.format("|%3s\n", df.format(eDischarge)));

                // annualise it
                double annualMultiplier = 365.0 / period.days();
                sb.append(String.format("|%3.1f%%\n", 100 * annualMultiplier * eDischarge / (components.getBattery().getNominalCapacity())));

                double eSelfUse = totalForPeriod(totalSelfUse, periodNumber, period); // kWh
                sb.append(String.format("|%3s\n", df.format(eSelfUse)));

                sb.append(String.format("|%3.1f%%\n", 100 * eSelfUse / eInverter));

                sb.append(String.format("|%3.1f%%\n", 100 * annualMultiplier * totalGenTotal / (ratedCapacity / 1000.0)));

                double bill
                        = components.getCost().getStandingCharge() * period.days()
                        + components.getCost().getImportPrice() * eImport
                        - components.getCost().getFits() * epv3
                        - components.getCost().getExportPrice() * eExport;
                sb.append(String.format("|£%3.2f\n", bill));

            } else {
                for (int i = 0; i < 15; i++) {
                    sb.append("|-\n");
                }
                sb.append("\n\n");
            }
        }
    }

    private double totalForPeriod(List<DatedValue> dv, int i, Period period) {
        DatedValueFilter filter = new DatedValueFilter(dv);
        filter.period(i, period);
        return filter.total();
    }

    @Override
    public String toString() {
        return "Monthly summary";
    }
}

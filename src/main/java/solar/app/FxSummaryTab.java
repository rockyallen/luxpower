package solar.app;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import solar.model.Listener;
import java.util.HashMap;
import java.util.List;
import org.asciidoctor.Asciidoctor;
import solar.model.Costs;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Period;
import solar.model.Record;
import solar.model.SystemData;

/**
 * Tabulated by month and summarized for the year,
 *
 * @author rocky
 */
public class FxSummaryTab extends FxHtmlTab implements Listener {

    private Collection<Record> records;
    private String description;
    private Costs costs = new Costs();

    public FxSummaryTab() {

        super();
        setText("Run analysis to see this table");
        costs.setExportPrice(SystemData.EXPORT_RATE);
        costs.setFits(SystemData.FITS_RATE);
        costs.setImportPrice(SystemData.IMPORT_RATE);
        costs.setStandingCharge(SystemData.STANDING_CHARGE);
    }

    @Override
    public void changed(Collection<Record> records, String description) {
        this.records = records;
        this.description = description;
    }

    void populate(List<DatedValue> pv1, List<DatedValue> pv2, List<DatedValue> pv3, List<DatedValue> totalCombined, List<DatedValue> totalGen, List<DatedValue> totalSelfUse, List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption, List<DatedValue> totalCharge, List<DatedValue> totalDischarge) {

        StringBuilder sb = new StringBuilder();

        sb.append("= " + description + "\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("[cols=\"^^^^^^^^^^^^^^^\", options=\"header\"]\n");
        sb.append("|===\n");
        sb.append("|Month");
        sb.append("|PV1 kWh");
        sb.append("|PV2 kWh");
        sb.append("|PV3 kWh");
        sb.append("|Combined kWh");
        sb.append("|Inverter kWh");
        sb.append("|Import kWh");
        sb.append("|Export kWh");
        sb.append("|Consumption kWh");
        sb.append("|Charge kWh");
        sb.append("|Discharge kWh");
        sb.append("|Self-use kWh");
        sb.append("|Self-use fraction");
        sb.append("|Capacity Factor");
        sb.append("|Bill\n\n");
        SimpleDateFormat f = new SimpleDateFormat("MMM");

        for (int month = 0; month < 12; month++) {
            sb.append(String.format("|%s\n", f.format(new Date(100, month, 1))));
            doRow(sb, month, Period.MONTH, pv1, pv2, pv3, totalCombined, totalGen, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);
        }
        sb.append("|Year\n");
        doRow(sb, 1, Period.ALL, pv1, pv2, pv3, totalCombined, totalGen, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);

        sb.append("|===\n");

        String asciidoc = sb.toString();

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        String html = asciidoctor.convert(asciidoc, new HashMap<String, Object>());

        setText(html);
    }

    private void doRow(StringBuilder sb, int month, Period period, List<DatedValue> pv1, List<DatedValue> pv2, List<DatedValue> pv3, List<DatedValue> totalCombined, List<DatedValue> totalGen, List<DatedValue> totalSelfUse, List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption, List<DatedValue> totalCharge, List<DatedValue> totalDischarge) {
        {
            final double ratedPower = (SystemData.garage.power + SystemData.east.power + SystemData.west.power) / 1000.0; // kW
            final double ratedCapacity = ratedPower * 365 * 24; // kW

            DatedValueFilter filter = new DatedValueFilter(totalGen);
            filter.period(month, period);
            int records = filter.size();

            if (records > 0) {
                DecimalFormat df = new DecimalFormat("#,###");

                double totalGenTotal = filter.total();

                filter = new DatedValueFilter(pv1);
                filter.period(month, period);
                double epv1 = filter.total();
                sb.append(String.format("|%3s\n", df.format(epv1)));

                filter = new DatedValueFilter(pv2);
                filter.period(month, period);
                double epv2 = filter.total();
                sb.append(String.format("|%3s\n", df.format(epv2)));

                filter = new DatedValueFilter(pv3);
                filter.period(month, period);
                double epv3 = filter.total();
                sb.append(String.format("|%3s\n", df.format(epv3)));

                filter = new DatedValueFilter(totalCombined);
                filter.period(month, period);
                double combined = filter.total();
                sb.append(String.format("|%3s\n", df.format(combined)));

                sb.append(String.format("|%3s\n", df.format(totalGenTotal)));

                filter = new DatedValueFilter(totalImport);
                filter.period(month, period);
                double eImport = filter.total();
                sb.append(String.format("|%3s\n", df.format(eImport)));

                filter = new DatedValueFilter(totalExport);
                filter.period(month, period);
                double eExport = filter.total();
                sb.append(String.format("|%3s\n", df.format(eExport)));

                filter = new DatedValueFilter(totalConsumption);
                filter.period(month, period);
                double eConsumption = filter.total();
                sb.append(String.format("|%3s\n", df.format(eConsumption)));

                filter = new DatedValueFilter(totalCharge);
                filter.period(month, period);
                double eCharge = filter.total();
                sb.append(String.format("|%3s\n", df.format(eCharge)));

                filter = new DatedValueFilter(totalDischarge);
                filter.period(month, period);
                double eDischarge = filter.total();
                sb.append(String.format("|%3s\n", df.format(eDischarge)));

                filter = new DatedValueFilter(totalSelfUse);
                filter.period(month, period);
                double eSelfUse = filter.total();
                sb.append(String.format("|%3s\n", df.format(eSelfUse)));

                sb.append(String.format("|%3.1f%%\n", 100 * eSelfUse / totalGenTotal));

                sb.append(String.format("|%3.1f%%\n", 100 * totalGenTotal / ratedCapacity));

                double bill
                        = SystemData.STANDING_CHARGE * period.days()
                        + SystemData.IMPORT_RATE * eImport
                        - epv3 * SystemData.FITS_RATE
                        - SystemData.EXPORT_RATE * eExport;
                sb.append(String.format("|£%3.2f\n", bill));
            } else {
                sb.append("|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n");
            }
        }
    }

    /**
     * @return the costs
     */
    public Costs getCosts() {
        return costs;
    }

    /**
     * @param costs the costs to set
     */
    public void setCosts(Costs costs) {
        this.costs = costs;
    }
}

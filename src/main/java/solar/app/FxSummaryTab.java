package solar.app;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import solar.model.Listener;
import java.util.HashMap;
import java.util.List;
import org.asciidoctor.Asciidoctor;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Period;
import solar.model.DataStore;
import solar.model.Record;
import solar.model.SystemData;
import static solar.model.SystemData.west;
import static solar.model.SystemData.east;
import static solar.model.SystemData.garage;

/**
 *
 * @author rocky
 */
public class FxSummaryTab extends FxHtmlTab implements Listener {

    private Collection<Record> records;
    private String description;

    public FxSummaryTab() {

        super();
        setText("Run analysis to see this table");
    }

    @Override
    public void changed(Collection<Record> records, String description) {
        this.records = records;
        this.description = description;
    }

    void populate(List<DatedValue> pv1, List<DatedValue> pv2, List<DatedValue> pv3, List<DatedValue> totalGen, List<DatedValue> totalSelfUse, List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption, List<DatedValue> totalCharge, List<DatedValue> totalDischarge) {

        StringBuilder sb = new StringBuilder();

        sb.append("= " + description + "\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("[cols=\"^^^^^^^^\", options=\"header\"]\n");
        sb.append("|===\n");
        sb.append("|Month");
        sb.append("|").append(SystemData.west.name).append(" kWh");
        sb.append("|").append(SystemData.east.name).append(" kWh");
        sb.append("|").append(SystemData.garage.name).append(" kWh");
        sb.append("|Inverter kWh |Import kWh |Export kWh |Consumption kWh | Self-use kWh |Self-use |Capacity Factor\n");
        sb.append("\n");
        SimpleDateFormat f = new SimpleDateFormat("MMM");

        for (int month = 0; month < 12; month++) {
            sb.append(String.format("|%s\n", f.format(new Date(100, month, 1))));
            doRow(sb, month, Period.MONTH, pv2, pv1, pv3, totalGen, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);
        }
        sb.append("|Year\n");
        doRow(sb, 1, Period.ALL, pv1, pv2, pv3, totalGen, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);

        sb.append("|===\n");

        String asciidoc = sb.toString();

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        String html = asciidoctor.convert(asciidoc, new HashMap<String, Object>());

        setText(html);
    }

    private void doRow(StringBuilder sb, int month, Period period, List<DatedValue> pv1, List<DatedValue> pv2, List<DatedValue> pv3, List<DatedValue> totalGen, List<DatedValue> totalSelfUse, List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption, List<DatedValue> totalCharge, List<DatedValue> totalDischarge) {
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
                sb.append(String.format("|%3s\n", df.format(filter.total())));

                filter = new DatedValueFilter(pv2);
                filter.period(month, period);
                sb.append(String.format("|%3s\n", df.format(filter.total())));

                filter = new DatedValueFilter(pv3);
                filter.period(month, period);
                sb.append(String.format("|%3s\n", df.format(filter.total())));

                sb.append(String.format("|%3s\n", df.format(totalGenTotal)));

                filter = new DatedValueFilter(totalImport);
                filter.period(month, period);
                sb.append(String.format("|%3s\n", df.format(filter.total())));

                filter = new DatedValueFilter(totalExport);
                filter.period(month, period);
                sb.append(String.format("|%3s\n", df.format(filter.total())));

                filter = new DatedValueFilter(totalConsumption);
                filter.period(month, period);
                sb.append(String.format("|%3s\n", df.format(filter.total())));

                filter = new DatedValueFilter(totalSelfUse);
                filter.period(month, period);
                double totalSelfUseTotal = filter.total();
                sb.append(String.format("|%3s\n", df.format(totalSelfUseTotal)));

                sb.append(String.format("|%3.1f%%\n", 100 * totalSelfUseTotal / totalGenTotal));

                sb.append(String.format("|%3.1f%%\n", 100 * totalGenTotal / ratedCapacity));
            } else {
                sb.append("|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n|-\n");
            }
        }
    }
}

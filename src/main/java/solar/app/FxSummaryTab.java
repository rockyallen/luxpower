package solar.app;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import solar.model.DataSource;
import solar.model.Listener;
import java.util.HashMap;
import java.util.List;
import org.asciidoctor.Asciidoctor;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.Period;
import solar.model.SystemData;

/**
 *
 * @author rocky
 */
public class FxSummaryTab extends FxHtmlTab implements Listener {

    private final DataSource ds;

    public FxSummaryTab(DataSource ds) {

        super();
        this.ds = ds;
        ds.addListener(this);
        setText("Run analysis to see this table");
    }

    @Override
    public void changed() {

    }

    void populate(List<DatedValue> totalGen, List<DatedValue> totalSelfUse, List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption) {

        StringBuilder sb = new StringBuilder();

        sb.append("[cols=\"^^^^^^^^\", options=\"header\"]\n");
        sb.append("|===\n");
        sb.append("|Month |Generation kWh |Import kWh |Export kWh |Consumption kWh | Self-use kWh |Self-use |Capacity Factor\n");
        sb.append("\n");
        SimpleDateFormat f = new SimpleDateFormat("MMM");

        for (int month = 0; month < 12; month++) {
            sb.append(String.format("|%s\n", f.format(new Date(100, month, 1))));
            doRow(sb, month, Period.MONTH, totalGen, totalSelfUse, totalExport, totalImport, totalConsumption);
        }
        sb.append("|Year\n");
        doRow(sb, 1, Period.ALL, totalGen, totalSelfUse, totalExport, totalImport, totalConsumption);

        sb.append("|===\n");

        String asciidoc = sb.toString();

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        String html = asciidoctor.convert(asciidoc, new HashMap<String, Object>());

        setText(html);
    }

    private void doRow(StringBuilder sb, int month, Period period, List<DatedValue> totalGen, List<DatedValue> totalSelfUse, List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption) {
        {
            DatedValueFilter filterGen = new DatedValueFilter(totalGen);
            filterGen.period(month, period);
            int records = filterGen.size();

            if (records > 0) {
                DecimalFormat df = new DecimalFormat("#,###");
                
                double totalGenTotal = filterGen.total();
                sb.append(String.format("|%3s\n", df.format(totalGenTotal)));

                DatedValueFilter filterImport = new DatedValueFilter(totalImport);
                filterImport.period(month, period);
                sb.append(String.format("|%3s\n", df.format(filterImport.total())));

                DatedValueFilter filterExport = new DatedValueFilter(totalExport);
                filterExport.period(month, period);
                sb.append(String.format("|%3s\n", df.format(filterExport.total())));

                DatedValueFilter filterConsumption = new DatedValueFilter(totalConsumption);
                filterConsumption.period(month, period);
                sb.append(String.format("|%3s\n", df.format(filterConsumption.total())));

                DatedValueFilter filterSelfUse = new DatedValueFilter(totalSelfUse);
                filterSelfUse.period(month, period);
                double totalSelfUseTotal = filterSelfUse.total();
                sb.append(String.format("|%3s\n", df.format(totalSelfUseTotal)));

                sb.append(String.format("|%3.1f%%\n", 100 * totalSelfUseTotal / totalGenTotal));

                sb.append(String.format("|%3.1f%%\n", 100 * totalGenTotal / SystemData.ratedCapacity));
            } else {
                sb.append("|-\n|-\n|-\n|-\n|-\n|-\n|-\n");
            }
        }
    }
}

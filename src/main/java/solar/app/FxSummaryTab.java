package solar.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.asciidoctor.Asciidoctor;
import solar.model.Components;
import solar.model.DatedValue;
import solar.model.DatedValueFilter;
import solar.model.EnergyStore;
import solar.model.Inverter;
import solar.model.Period;
import solar.model.Record;
import solar.model.SolarArray;

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
    private String csv = "Not generated";

    RadioButton rb1 = new RadioButton("Total");
    RadioButton rb2 = new RadioButton("Mean");

    RadioButton rb3 = new RadioButton("Day");
    RadioButton rb4 = new RadioButton("Week");
    RadioButton rb5 = new RadioButton("Month");

    Button export = new Button("Export");
    Button show = new Button("Show");
    final ToggleGroup group1 = new ToggleGroup();
    final ToggleGroup group2 = new ToggleGroup();
    private List<DatedValue> pv1;
    private List<DatedValue> pv2;
    private List<DatedValue> pv3;
    private List<DatedValue> totalCombined;
    private List<DatedValue> totalGen;
    private List<DatedValue> totalInverter;
    private List<DatedValue> totalSelfUse;
    private List<DatedValue> totalExport;
    private List<DatedValue> totalImport;
    private List<DatedValue> totalConsumption;
    private List<DatedValue> totalCharge;
    private List<DatedValue> totalDischarge;

    public FxSummaryTab() {

        super();
        setText("Run analysis to see this table");

        rb1.setSelected(true);
        rb1.setToggleGroup(group1);
        rb2.setToggleGroup(group1);

        rb5.setSelected(true);
        rb3.setToggleGroup(group2);
        rb4.setToggleGroup(group2);
        rb5.setToggleGroup(group2);

        HBox p = new HBox();
        p.setPadding(FxMainAnalysis.INSETS);
        p.setSpacing(FxMainAnalysis.SPACING);

        VBox vb = new VBox();
        vb.setPadding(FxMainAnalysis.INSETS);
        vb.setSpacing(FxMainAnalysis.SPACING);
        TitledPane tpane = new TitledPane("Values", vb);
        tpane.setCollapsible(false);
        vb.getChildren().addAll(rb1, rb2);
        p.getChildren().add(tpane);

        vb = new VBox();
        vb.setPadding(FxMainAnalysis.INSETS);
        vb.setSpacing(FxMainAnalysis.SPACING);
        tpane = new TitledPane("Group by", vb);
        tpane.setCollapsible(false);
        vb.getChildren().addAll(rb5, rb4, rb3);
        p.getChildren().add(tpane);

        p.getChildren().add(show);

        p.getChildren().add(export);

        setTop(p);

        show.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent value) {
                if (components != null) {
                    analyse();
                }
            }
        });

        export.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent value) {
                FileWriter fw = null;
                try {
                    File f = new File("export.csv");
                    fw = new FileWriter(f);
                    fw.append(csv);
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(FxSummaryTab.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        fw.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FxSummaryTab.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    void populate(List<DatedValue> pv1, List<DatedValue> pv2, List<DatedValue> pv3, List<DatedValue> totalCombined,
            List<DatedValue> totalGen, List<DatedValue> totalInverter, List<DatedValue> totalSelfUse,
            List<DatedValue> totalExport, List<DatedValue> totalImport, List<DatedValue> totalConsumption, List<DatedValue> totalCharge, List<DatedValue> totalDischarge, Components components) {

        this.components = components;
        this.pv1 = pv1;
        this.pv2 = pv2;
        this.pv3 = pv3;
        this.totalCombined = totalCombined;
        this.totalGen = totalGen;
        this.totalInverter = totalInverter;
        this.totalSelfUse = totalSelfUse;
        this.totalExport = totalExport;
        this.totalImport = totalImport;
        this.totalConsumption = totalConsumption;
        this.totalCharge = totalCharge;
        this.totalDischarge = totalDischarge;
        analyse();
    }

    void analyse() {

        ratedPower = (components.getPv1().getRatedPower() + components.getPv2().getRatedPower() + components.getPv3().getRatedPower()); // W
        ratedCapacity = ratedPower * 365 * 24; // Wh

        Period p = rb3.isSelected() ? Period.Day : (rb4.isSelected() ? Period.Week : Period.Month);

        StringBuilder sb = new StringBuilder();

        sb.append("\n= Summary\n\n");
        sb.append("[cols=\"16*^\", options=\"header\", format=\"csv\"]\n");
        sb.append("|===\n");
        csv = makeCsv(p);
        sb.append(csv);
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

        sb.append("\n\n== Modelled components\n\n");
        sb.append("pv1: ").append(components.getPv1().toString()).append("\n\n");
        sb.append("pv2: ").append(components.getPv2().toString()).append("\n\n");
        sb.append("pv3: ").append(components.getPv3().toString()).append("\n\n");
        sb.append("Battery: ").append(components.getBattery().toString()).append("\n\n");
        sb.append("Inverter: ").append(components.getInv12().toString()).append("\n\n");

        sb.append("\n\n== Database components\n\n");
        sb.append("\n\n=== Arrays\n\n");
        for (SolarArray array : components.getArrays().values()) {
            sb.append(array.toString()).append("\n\n");
        }
        sb.append("\n\n=== Batteries\n\n");
        for (EnergyStore es : components.getBatteries().values()) {
            sb.append(es.toString()).append("\n\n");
        }
        sb.append("\n\n=== Inverters\n\n");
        for (Inverter inv : components.getInverters().values()) {
            sb.append(inv.toString()).append("\n\n");
        }

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
                DecimalFormat df = new DecimalFormat("#,##0.0");

                double totalGenTotal = filter.total(); // kWh

                sb.append(String.format("\"%3s\",", df.format(totalForPeriod(pv1, periodNumber, period))));

                sb.append(String.format("\"%3s\",", df.format(totalForPeriod(pv2, periodNumber, period))));

                double epv3 = totalForPeriod(pv3, periodNumber, period);
                sb.append(String.format("\"%3s\",", df.format(epv3)));

                sb.append(String.format("\"%3s\",", df.format(totalForPeriod(totalCombined, periodNumber, period))));

                double eInverter = totalForPeriod(totalInverter, periodNumber, period);
                sb.append(String.format("\"%3s\",", df.format(eInverter)));

                double eImport = totalForPeriod(totalImport, periodNumber, period);
                sb.append(String.format("\"%3s\",", df.format(eImport)));

                double eExport = totalForPeriod(totalExport, periodNumber, period);
                sb.append(String.format("\"%3s\",", df.format(eExport)));

                sb.append(String.format("\"%3s\",", df.format(totalForPeriod(totalConsumption, periodNumber, period))));

                sb.append(String.format("\"%3s\",", df.format(totalForPeriod(totalCharge, periodNumber, period))));

                double eDischarge = totalForPeriod(totalDischarge, periodNumber, period);
                sb.append(String.format("\"%3s\",", df.format(eDischarge)));

                // annualise it
                double annualMultiplier = 365.0 / period.days();
                sb.append(String.format("%3.1f%%,", 100 * annualMultiplier * eDischarge / (components.getBattery().getNominalCapacity())));

                double eSelfUse = totalForPeriod(totalSelfUse, periodNumber, period); // kWh
                sb.append(String.format("\"%3s\",", df.format(eSelfUse)));

                sb.append(String.format("\"%3.1f%%\",", 100 * eSelfUse / eInverter));

                sb.append(String.format("\"%3.1f%%\",", 100 * annualMultiplier * totalGenTotal / (ratedCapacity / 1000.0)));

                double bill
                        = components.getCost().getStandingCharge() * period.days()
                        + components.getCost().getImportPrice() * eImport
                        - components.getCost().getFits() * epv3
                        - components.getCost().getExportPrice() * eExport;
                sb.append(String.format("\"£%3.2f\"\n", bill));

            } else {
                for (int i = 0; i < 14; i++) {
                    sb.append("\"-\",");
                }
                sb.append("\"-\"\n");
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
        return "Summary table";
    }

    private String makeCsv(Period p) {
        StringBuilder sb = new StringBuilder();
        sb.append(p).append(",");
        sb.append("\"PV1 kWh\",");
        sb.append("\"PV2 kWh\",");
        sb.append("\"PV3 kWh\",");
        sb.append("\"Combined kWh\",");
        sb.append("\"Yield kWh\",");
        sb.append("\"Import kWh\",");
        sb.append("\"Export kWh\",");
        sb.append("\"Consumption kWh\",");
        sb.append("\"Charge kWh\",");
        sb.append("\"Discharge kWh\",");
        sb.append("\"Battery utilisation\",");
        sb.append("\"Self-use kWh\",");
        sb.append("\"Self-use fraction\",");
        sb.append("\"Capacity Factor\",");
        sb.append("\"Bill\"\n");

        SimpleDateFormat f = new SimpleDateFormat("MMM");

        if (p == Period.Month) {
            for (int i = 0; i < 12; i++) {
                sb.append(String.format("\"%s\",", f.format(new Date(100, i, 1))));
                doRow(sb, i, p, pv1, pv2, pv3, totalCombined, totalGen, totalInverter, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);
            }
        } else if (p == Period.Week) {
            for (int i = 0; i < 52; i++) {
                sb.append(i + 1).append(",");
                doRow(sb, i, p, pv1, pv2, pv3, totalCombined, totalGen, totalInverter, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);
            }
        } else if (p == Period.Day) {
            for (int i = 0; i < 365; i++) {
                sb.append(i + 1).append(",");
                doRow(sb, i, p, pv1, pv2, pv3, totalCombined, totalGen, totalInverter, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);
            }
        }
        sb.append("\"Year\",");
        doRow(sb, 1, Period.All, pv1, pv2, pv3, totalCombined, totalGen, totalInverter, totalSelfUse, totalExport, totalImport, totalConsumption, totalCharge, totalDischarge);
        return sb.toString();
    }
}

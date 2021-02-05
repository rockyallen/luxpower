package solar.model;

import java.util.List;

/**
 * WIP
 * @author rocky
 */
public class PowerAnalysis {

    public boolean doAnalysis(List<Record> input, ResultSet output) {

        for (int hour = 0; hour < 24; hour++) {

            output.clear();

            RecordFilter<Record> filter = new RecordFilter<>(input);
            List<Record> thisHour = filter.period(hour, Period.Hour).result();
            for (Record r : thisHour) {

                output.totalPv1.add(new DatedValue(r.getDate(), r.getPpv1()));
                output.totalPv2.add(new DatedValue(r.getDate(), r.getPpv2()));
                output.totalPv3.add(new DatedValue(r.getDate(), r.getPpv3()));
                output.totalCombined.add(new DatedValue(r.getDate(), r.getPpv1() + r.getPpv2() + r.getPpv3()));

                double generated = r.getPinv() - r.getpDisCharge();
                double exported = r.getpToGrid();
                double imported = r.getpToUser();
                double charge = r.getpCharge();
                double disCharge = r.getpDisCharge();
                double selfUse = generated - exported - charge;
                double consumption = imported + selfUse + disCharge;

                output.totalGeneration.add(new DatedValue(r.getDate(), generated));
                output.totalInverter.add(new DatedValue(r.getDate(), r.getPinv()));
                output.totalSelfUse.add(new DatedValue(r.getDate(), selfUse));
                output.totalExport.add(new DatedValue(r.getDate(), exported));
                output.totalImport.add(new DatedValue(r.getDate(), imported));
                output.totalConsumption.add(new DatedValue(r.getDate(), consumption));
                output.totalCharge.add(new DatedValue(r.getDate(), charge));
                output.totalDischarge.add(new DatedValue(r.getDate(), r.getpDisCharge()));
            }
        }
        return true;
    }
}

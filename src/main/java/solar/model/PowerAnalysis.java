package solar.model;

import java.util.List;
import javax.measure.Quantity;
import javax.measure.quantity.Power;
import static tech.units.indriya.unit.Units.WATT;

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

                output.totalPv1.add(new DatedValue(r.getDate(), r.getPpv1().getValue().doubleValue()));
                output.totalPv2.add(new DatedValue(r.getDate(), r.getPpv2().getValue().doubleValue()));
                output.totalPv3.add(new DatedValue(r.getDate(), r.getPpv3().getValue().doubleValue()));
                output.totalCombined.add(new DatedValue(r.getDate(), (r.getPpv1().add(r.getPpv2()).add(r.getPpv3())).getValue().doubleValue()));

                Quantity<Power> generated = r.getPinv().subtract(r.getpDisCharge());
                Quantity<Power>  exported = r.getpToGrid();
                Quantity<Power>  imported = r.getpToUser();
                Quantity<Power>  charge = r.getpCharge();
                Quantity<Power>  disCharge = r.getpDisCharge();
                Quantity<Power>  selfUse = generated.subtract(exported).subtract(charge);
                Quantity<Power>  consumption = imported.add(selfUse).add(disCharge);

                output.totalGeneration.add(new DatedValue(r.getDate(), generated.to(WATT).getValue().doubleValue()));
                output.totalInverter.add(new DatedValue(r.getDate(), r.getPinv().to(WATT).getValue().doubleValue()));
                output.totalSelfUse.add(new DatedValue(r.getDate(), selfUse.to(WATT).getValue().doubleValue()));
                output.totalExport.add(new DatedValue(r.getDate(), exported.to(WATT).getValue().doubleValue()));
                output.totalImport.add(new DatedValue(r.getDate(), imported.to(WATT).getValue().doubleValue()));
                output.totalConsumption.add(new DatedValue(r.getDate(), consumption.to(WATT).getValue().doubleValue()));
                output.totalCharge.add(new DatedValue(r.getDate(), charge.to(WATT).getValue().doubleValue()));
                output.totalDischarge.add(new DatedValue(r.getDate(), r.getpDisCharge().to(WATT).getValue().doubleValue()));
            }
        }
        return true;
    }
}

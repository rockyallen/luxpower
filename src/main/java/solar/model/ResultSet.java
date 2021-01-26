package solar.model;

import java.util.ArrayList;
import java.util.List;

/**
 * WIP
 * @author rocky
 */
public class ResultSet {
        public final List<DatedValue> totalPv1 = new ArrayList<>();
    public final List<DatedValue> totalPv2 = new ArrayList<>();
    public final List<DatedValue> totalPv3 = new ArrayList<>();
    public final List<DatedValue> totalCombined = new ArrayList<>();
    public final List<DatedValue> totalGeneration = new ArrayList<>();
    public final List<DatedValue> totalInverter = new ArrayList<>();
    public final List<DatedValue> totalImport = new ArrayList<>();
    public final List<DatedValue> totalExport = new ArrayList<>();
    public final List<DatedValue> totalConsumption = new ArrayList<>();
    public final List<DatedValue> totalSelfUse = new ArrayList<>();
    public final List<DatedValue> totalDischarge = new ArrayList<>();
    public final List<DatedValue> totalCharge = new ArrayList<>();

    void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

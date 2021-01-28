package solar.model;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.TreeMap;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Database of components that can be chosen from in the model. Also has some
 * static data so the model can do something reasonable even if it can't find
 * the external database file.
 *
 * You can also use the class as a carrier for the currently selected
 * components.
 *
 * @author rocky
 */
public class Components {

    // Installation location. Should this be part of array?
    private static final double defaultLatitude = 50.6; // degrees google maps
    private static final double defaultLongitude = -2.5; // degrees google maps

    private final Map<String, Inverter> inverters = new TreeMap<>();
    private final Map<String, EnergyStore> batteries = new TreeMap<>();
    private final Map<String, SolarArray> arrays = new TreeMap<>();
    private final Map<String, Costs> costs = new TreeMap<>();

    // Array of 10 JAM60S10 340/MR on house East and West
    // House angle measured from google maps as 2.0 degrees
    // Tilt estimated from H2 ECO site visit
    private SolarArray pv1 = new SolarArray("West", "10 off 340 W", 10 * 1.669 * 0.996, 32.0, 92.0, 20.0 / 100, defaultLatitude, defaultLongitude);
    private SolarArray pv2 = new SolarArray("East", "10 off 340 W", 10 * 1.669 * 0.996, 32.0, 272.0, 20.0 / 100,  defaultLatitude, defaultLongitude);
    // Array of 9 PV-TD185MF5 on garage
    // Angle measured from google maps as 2.0 degrees
    // Tilt from memory of design
    private SolarArray pv3 = new SolarArray("South", "9 off 185 W", 9 * 1.65 * 0.83, 35.0, 2.0, 13.4 / 100,  defaultLatitude, defaultLongitude);
    private Inverter inv12 = Inverter.valueOf("LUX Power", "", 3600.0, 0.96);
    //private Inverter inv3 = Components.SunnyBoy;
    // Jan 2021, Octopus
    private Costs cost = new Costs("Current", 0.20, 0.15, 0.055, 0.52);
    private EnergyStore battery = new EnergyStore("PylonTech", "", 7200.0, 5760.0, 0.80, 3600.0, 3600.0);

    public Components() {
    }

    public boolean load(File file) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFRow row;

            HSSFSheet sheet = wb.getSheet("Inverters");

            int rows = sheet.getPhysicalNumberOfRows();

            if (rows > 1) {
                for (int r = 1; r < rows && r < 20; r++) {
                    row = sheet.getRow(r);
                    String name = row.getCell(0).getStringCellValue();
                    if (!"".equals(name)) {
                        inverters.put(name,
                                Inverter.valueOf(
                                        name,
                                        row.getCell(1).getStringCellValue(),
                                        row.getCell(2).getNumericCellValue(),
                                        row.getCell(3).getNumericCellValue()
                                ));
                    }
                }
            }
            sheet = wb.getSheet("Batteries");

            rows = sheet.getPhysicalNumberOfRows();

            if (rows >= 2) {
                for (int r = 1; r < rows && r < 20; r++) {
                    row = sheet.getRow(r);
                    String name = row.getCell(0).getStringCellValue();
                    if (!"".equals(name)) {
                        batteries.put(name,
                                new EnergyStore(
                                        name,
                                        row.getCell(1).getStringCellValue(),
                                        row.getCell(2).getNumericCellValue(),
                                        row.getCell(3).getNumericCellValue(),
                                        row.getCell(4).getNumericCellValue(),
                                        row.getCell(5).getNumericCellValue(),
                                        row.getCell(6).getNumericCellValue()
                                ));
                    }
                }
            }
            sheet = wb.getSheet("Arrays");

            rows = sheet.getPhysicalNumberOfRows();

            if (rows >= 2) {
                for (int r = 1; r < rows && r < 20; r++) {
                    row = sheet.getRow(r);
                    String name = row.getCell(0).getStringCellValue();
                    if (!"".equals(name)) {
                        arrays.put(name,
                                new SolarArray(
                                        name,
                                        row.getCell(1).getStringCellValue(),
                                        row.getCell(2).getNumericCellValue(),
                                        row.getCell(3).getNumericCellValue(),
                                        row.getCell(4).getNumericCellValue(),
                                        row.getCell(5).getNumericCellValue(),
                                        row.getCell(6).getNumericCellValue(),
                                        row.getCell(7).getNumericCellValue()
                                ));
                    }
                }
            }
            sheet = wb.getSheet("Costs");

            rows = sheet.getPhysicalNumberOfRows();

            if (rows >= 2) {
                for (int r = 1; r < rows && r < 20; r++) {
                    row = sheet.getRow(r);
                    String name = row.getCell(0).getStringCellValue();
                    if (!"".equals(name)) {
                        Costs c = new Costs(
                                name,
                                row.getCell(1).getNumericCellValue(),
                                row.getCell(2).getNumericCellValue(),
                                row.getCell(3).getNumericCellValue(),
                                row.getCell(4).getNumericCellValue());
                        costs.put(name, c);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public Map<String, Inverter> getInverters() {
        return inverters;
    }

    public Map<String, EnergyStore> getBatteries() {
        return batteries;
    }

    public Map<String, SolarArray> getArrays() {
        return arrays;
    }

    @Override
    public String toString() {
        return "Components(" + inverters.size() + "," + batteries.size() + "," + arrays.size() + ")";
    }

    /**
     * @return the costs
     */
    public Map<String, Costs> getCosts() {
        return costs;
    }

    /**
     * @return the pv1
     */
    public SolarArray getPv1() {
        return pv1;
    }

    /**
     * @param pv1 the pv1 to set
     */
    public void setPv1(SolarArray pv1) {
        this.pv1 = pv1;
    }

    /**
     * @return the pv2
     */
    public SolarArray getPv2() {
        return pv2;
    }

    /**
     * @param pv2 the pv2 to set
     */
    public void setPv2(SolarArray pv2) {
        this.pv2 = pv2;
    }

    /**
     * @return the pv3
     */
    public SolarArray getPv3() {
        return pv3;
    }

    /**
     * @param pv3 the pv3 to set
     */
    public void setPv3(SolarArray pv3) {
        this.pv3 = pv3;
    }

    /**
     * @return the inv12
     */
    public Inverter getInv12() {
        return inv12;
    }

    /**
     * @param inv12 the inv12 to set
     */
    public void setInv12(Inverter inv12) {
        this.inv12 = inv12;
    }

    /**
     * @return the cost
     */
    public Costs getCost() {
        return cost;
    }

    /**
     * @param cost the cost to set
     */
    public void setCost(Costs cost) {
        this.cost = cost;
    }

    /**
     * @return the battery
     */
    public EnergyStore getBattery() {
        return battery;
    }

    /**
     * @param battery the battery to set
     */
    public void setBattery(EnergyStore battery) {
        this.battery = battery;
    }
}

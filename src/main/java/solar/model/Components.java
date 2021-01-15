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
 *
 * @author rocky
 */
public class Components {

    private final Map<String, Inverter> inverters = new TreeMap<>();
    private final Map<String, EnergyStore> batteries = new TreeMap<>();
    private final Map<String, SolarArray> arrays = new TreeMap<>();
    private final Map<String, Costs> costs = new TreeMap<>();

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
                                        row.getCell(5).getNumericCellValue()
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
                        Costs c= new Costs();
                        c.setStandingCharge(row.getCell(1).getNumericCellValue());
                        c.setImportPrice(row.getCell(2).getNumericCellValue());
                        c.setExportPrice(row.getCell(3).getNumericCellValue());
                        c.setFits(row.getCell(4).getNumericCellValue());
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
}

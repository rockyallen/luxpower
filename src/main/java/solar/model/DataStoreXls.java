package solar.model;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javafx.concurrent.Task;
import javax.measure.Quantity;
import static javax.measure.Unit.*;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
//import tech.units.indriya.quantity.Quantities;
import static tech.units.indriya.unit.Units.*;
import static tech.units.indriya.quantity.Quantities.*;
import static solar.model.SolarUnitsAndConstants.*;

/**
 * Imports data from Excel files exported from the LuxPower website. All files
 * are assumed to be in a single folder and subfolders are not searched. Later
 * analysis assumes that only a single years data is in each folder.
 *
 * Read only.
 *
 * @author rocky
 */
public class DataStoreXls extends Task {

    // If true, pretend that PV3 is the south array and set its energy values to 0.27 times the sum of the other 2.
    // Appplies the same factor to the power, but this does not affect the analysis just the graphs so I dont cate how accuracte it is.
    // This is a fudge because I don't have logged data for the SunnyBoy, but from the data I do have, its daily energy is 27% +/- 4%  of the sum of the others
    private boolean fudge = true;

    private Collection<Record> records;
    //  final CustomOutputStream os;

    private static final String DS = "datasource";
    Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
    String source = "";

    private File folder = null;

    public DataStoreXls() {
        super();
    }

    @Override
    protected Collection<Record> call() {
        records = new TreeSet<>();
        try {
            records.clear();
            importFolder(getFolder());
            updateMessage("Done import");

            boolean result = new DataStoreCache().put(records);
        } catch (Exception ex) {
            records = null;
            ex.printStackTrace();
        }

        return records;
    }

    private void importFolder(File folder) throws Exception {
        Objects.nonNull(folder);
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (listOfFile.getAbsolutePath().endsWith(".xls")) {
                    importFile(listOfFile);
                }
            }
        }
    }

    private void importFile(File file) throws Exception {
        updateMessage(file.getAbsolutePath());

        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFRow row;

        SimpleDateFormat sfsheet = new SimpleDateFormat("yyyy-MM-dd");

        int nRecords = 0;
        for (int sheets = 0; sheets < wb.getNumberOfSheets(); sheets++) {
            HSSFSheet sheet = wb.getSheetAt(sheets);

            int rows = sheet.getPhysicalNumberOfRows();

            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (rows > 1) {
                for (int r = 1; r < rows; r++) {
                    row = sheet.getRow(r);
                    if (row != null) {

                        Record record = new Record();

                        record.setSerial_number(row.getCell(0).getStringCellValue());

                        record.setDate(sf.parse(row.getCell(1).getStringCellValue()));

                        record.setVpv1(getQuantity(row.getCell(3).getStringCellValue(), VOLT));
                        record.setVpv2(getQuantity(row.getCell(4).getStringCellValue(), VOLT));
                        record.setVpv3(getQuantity(row.getCell(5).getStringCellValue(), VOLT));

                        String s = row.getCell(7).getStringCellValue();
                        record.setSoc((float) Float.valueOf(s.substring(0, s.length() - 1)));

                        record.setPpv1(getQuantity(row.getCell(8).getNumericCellValue(), WATT));
                        record.setPpv2(getQuantity(row.getCell(9).getNumericCellValue(), WATT));
                        record.setPpv3(getQuantity(row.getCell(10).getNumericCellValue(), WATT));

                        record.setpCharge(getQuantity(row.getCell(11).getNumericCellValue(), WATT));
                        record.setpDisCharge(getQuantity(row.getCell(12).getNumericCellValue(), WATT));

                        record.setPinv(getQuantity(row.getCell(17).getNumericCellValue(), WATT));
                        record.setpToGrid(getQuantity(row.getCell(26).getNumericCellValue(), WATT));
                        record.setpToUser(getQuantity(row.getCell(27).getNumericCellValue(), WATT));

                        record.seteInvDay(getQuantity(row.getCell(32).getStringCellValue(), KILO_WATT_HOUR));
                        record.seteChgDay(getQuantity(row.getCell(34).getStringCellValue(), KILO_WATT_HOUR));
                        record.seteDisChgDay(getQuantity(row.getCell(35).getStringCellValue(), KILO_WATT_HOUR));
                        record.seteChgAll(getQuantity(row.getCell(44).getStringCellValue(), KILO_WATT_HOUR));
                        record.seteDisChgAll(getQuantity(row.getCell(45).getStringCellValue(), KILO_WATT_HOUR));
                        record.setePv1Day(getQuantity(row.getCell(29).getStringCellValue(), KILO_WATT_HOUR));
                        record.setePv2Day(getQuantity(row.getCell(30).getStringCellValue(), KILO_WATT_HOUR));
                        record.setePv3Day(getQuantity(row.getCell(31).getStringCellValue(), KILO_WATT_HOUR));
                        record.seteToGridDay(getQuantity(row.getCell(37).getStringCellValue(), KILO_WATT_HOUR));
                        record.seteToUserDay(getQuantity(row.getCell(38).getStringCellValue(), KILO_WATT_HOUR));

//To represent this, if "Model PV3" is selected when you import the data:
//
//- *Ppv3* is calculated as 27% of **Ppv1 + Ppv2**
//
//- *epv3day* is calculated as 27% of **epv1day + epv2day**
//
//- *PInv* is increased by *Ppv3*
//
//- *eInvday* is increased by 27%
//
//- *Pload* is recalculated as **Pinv + PToSser - PToGrid**
                        if (isFudge()) {
                            final float factor = 0.27f;

                            Quantity<Power> ppv3 = record.getPpv1().add(record.getPpv2()).multiply(factor);
                            record.setPpv3(ppv3);
                            record.setPinv(record.getPinv().add(ppv3));

                            Quantity<Energy> epv3 = (record.getePv1Day().add(record.getePv2Day())).multiply(factor);
                            record.setePv3Day(epv3);
                            record.seteInvDay(record.geteInvDay().multiply((1 + factor)));
                            record.setpLoad(record.getPinv().add(record.getpToUser()).add(record.getpToGrid()));
                        }
                        record.validate();
                        records.add(record);
                        nRecords++;
                    }
                }
            }
        }
        updateMessage(nRecords + " records created");
    }

    @Override
    public String toString() {
        return "Imported data";
    }

    /**
     * @return the fudge
     */
    public boolean isFudge() {
        return fudge;
    }

    /**
     * @param fudge the fudge to set
     */
    public void setFudge(boolean fudge) {
        this.fudge = fudge;
    }

    public void setFolder(File from) {
        Objects.nonNull(from);
        folder = from;
        prefs.put(DS, from.getAbsolutePath());
    }

    public File getFolder() {
        String f = prefs.get(DS, new File(".").getAbsolutePath());
        if (f == null) {
            return null;
        }
        File ff = new File(f);
        return ff;
    }
}

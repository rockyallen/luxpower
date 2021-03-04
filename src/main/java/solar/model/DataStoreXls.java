package solar.model;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javafx.concurrent.Task;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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

                        record.setVpv1(Float.valueOf(row.getCell(3).getStringCellValue()));
                        record.setVpv2(Float.valueOf(row.getCell(4).getStringCellValue()));
                        record.setVpv3(Float.valueOf(row.getCell(5).getStringCellValue()));

                        String s = row.getCell(7).getStringCellValue();
                        record.setSoc((float) Float.valueOf(s.substring(0, s.length() - 1)));

                        record.setPpv1((float) row.getCell(8).getNumericCellValue());
                        record.setPpv2((float) row.getCell(9).getNumericCellValue());
                        record.setPpv3((float) row.getCell(10).getNumericCellValue());

                        record.setpCharge((float) row.getCell(11).getNumericCellValue());
                        record.setpDisCharge((float) row.getCell(12).getNumericCellValue());

                        record.setPinv((float) row.getCell(17).getNumericCellValue());
                        record.setpToGrid((float) row.getCell(26).getNumericCellValue());
                        record.setpToUser((float) row.getCell(27).getNumericCellValue());

                        record.seteInvDay(Float.valueOf(row.getCell(32).getStringCellValue()));
                        record.seteChgDay(Float.valueOf(row.getCell(34).getStringCellValue()));
                        record.seteDisChgDay(Float.valueOf(row.getCell(35).getStringCellValue()));
                        record.seteChgAll(Float.valueOf(row.getCell(44).getStringCellValue()));
                        record.seteDisChgAll(Float.valueOf(row.getCell(45).getStringCellValue()));
                        record.setePv1Day(Float.valueOf(row.getCell(29).getStringCellValue()));
                        record.setePv2Day(Float.valueOf(row.getCell(30).getStringCellValue()));
                        record.setePv3Day(Float.valueOf(row.getCell(31).getStringCellValue()));
                        record.seteToGridDay(Float.valueOf(row.getCell(37).getStringCellValue()));
                        record.seteToUserDay(Float.valueOf(row.getCell(38).getStringCellValue()));

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
                            record.setPpv3(factor * (record.getPpv1() + record.getPpv2()));
                            float epv3 = factor * (record.getePv1Day() + record.getePv2Day());
                            record.setePv3Day(epv3);
                            record.setPinv(record.getPinv() + epv3);
                            record.seteInvDay((1 + factor) * record.geteInvDay());
                            record.setpLoad(record.getPinv() + record.getpToUser() - record.getpToGrid());
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

package solar.model;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
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
public class DataStoreXls<T> extends Task {

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

    /**
     *
     * @param records Destination for read records
     * @param os message printer
     */
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

    public void importFolder(File folder) throws Exception {
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

            // Uncomment "rows-" to only store the final record each day. 
            if (rows > 1) {
                for (int r = /* rows- */ 1; r < rows; r++) {
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

                        if (isFudge()) {
                            record.setPpv3(0.27f * (record.getPpv1() + record.getPpv2()));
                            float e3 = 0.27f * (record.getePv1Day() + record.getePv2Day());
                            record.setePv3Day(e3);
                            record.seteInvDay(record.geteInvDay() + e3);
                        }
                        record.validate();
                        records.add(record);
                        nRecords++;
                    }
                }
            }
        }
        //os.println(nRecords + " records imported");
    }

    /**
     * Read only.
     *
     * @param records
     * @return
     *
     * @throws UnsupportedOperationException always
     */
//    @Override
//    public boolean put(Collection<Record> records) {
//        throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
//    }
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

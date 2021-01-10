package solar.model;

import solar.app.CustomOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Imports data from Excel files exported from the LuxPower website. All files
 * are assumed to be in a single folder and subfolders are not searched. If not
 * just call importFolder for each folder.
 *
 * @author rocky
 */
public class Importer {

    // If true, pretend that PV3 is the south array and set its values to 0.3 times the sum of the other 2.
    // This is a fudge because I don't have logged data for the SunnyBoy, but from the data I do have, its daily energy is 27% +/- 4%  of the sum of the others
    boolean fudge = true;

    final Collection<Record> records;
    final CustomOutputStream os;

    /**
     * 
     * @param records Destination for read records
     * @param os message printer
     */
    public Importer(Collection<Record> records, CustomOutputStream os) {
        this.records = records;
        this.os = os;
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
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
                os.println("file=" + file);
//            }
//        });

        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFRow row;

        SimpleDateFormat sfsheet = new SimpleDateFormat("yyyy-MM-dd");

        int nRecords = 0;
        for (int sheets = 0; sheets < wb.getNumberOfSheets(); sheets++) {
            HSSFSheet sheet = wb.getSheetAt(sheets);

            //Date d = sfsheet.parse(sheet.getSheetName());
            int rows = sheet.getPhysicalNumberOfRows();

            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Uncomment "rows-" to only store the final record each day. 
            // It makes the app much quicker, and none of the analysis I do uses the other data.
            if (rows > 1) {
                for (int r = /* rows- */ 1; r < rows; r++) {
                    row = sheet.getRow(r);
                    if (row != null) {

                        Record record = new Record();

                        record.setSerial_number(row.getCell(0).getStringCellValue());

                        record.setDate(sf.parse(row.getCell(1).getStringCellValue()));

                        String s = row.getCell(7).getStringCellValue();
                        record.setSoc((float) Float.valueOf(s.substring(0, s.length() - 1)));
                        record.setVpv1((float) Float.valueOf(row.getCell(3).getStringCellValue()));
                        record.setVpv2((float) Float.valueOf(row.getCell(4).getStringCellValue()));
                        record.setVpv3((float) Float.valueOf(row.getCell(5).getStringCellValue()));
                        record.setPpv1((float)row.getCell(8).getNumericCellValue());
                        record.setPpv2((float)row.getCell(9).getNumericCellValue());
                        record.setPpv3((float)row.getCell(10).getNumericCellValue());
                        record.setpToGrid((float)row.getCell(26).getNumericCellValue());
                        record.setpToUser((float) row.getCell(27).getNumericCellValue());
                        record.seteChgDay((float) Float.valueOf(row.getCell(34).getStringCellValue()));
                        record.seteDisChgDay((float) Float.valueOf(row.getCell(35).getStringCellValue()));
                        record.seteChgAll((float) Float.valueOf(row.getCell(44).getStringCellValue()));
                        record.seteDisChgAll((float) Float.valueOf(row.getCell(45).getStringCellValue()));
                        record.setePv1Day((float) Float.valueOf(row.getCell(29).getStringCellValue()));
                        record.setePv2Day((float) Float.valueOf(row.getCell(30).getStringCellValue()));
                        record.setePv3Day((float) Float.valueOf(row.getCell(31).getStringCellValue()));
                        record.seteToGridDay((float) Float.valueOf(row.getCell(37).getStringCellValue()));
                        record.seteToUserDay((float) Float.valueOf(row.getCell(38).getStringCellValue()));

                        if (fudge) {
                            record.setPpv3(0.27f * (record.getPpv1() + record.getPpv2()));
                            record.setePv3Day(0.27f * (record.getePv1Day() + record.getePv2Day()));
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
}

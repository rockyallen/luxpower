package solar.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Java database for the luxpower export files.
 *
 * NB Returned records are only guaranteed to be sorted by increasing date if the put list was.
 *
 * @author rocky
 */
public class DataStoreCache implements Serializable {

    private static final transient File f = new File("hashmap.ser");
    private static final long serialVersionUID = 36477397454380L;
    private transient Collection<Record> records;

    public DataStoreCache() {
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        records = new TreeSet<>();
        int count = (Integer)ois.readObject();
        for (int i = 0; i < count; i++) {
            records.add((Record) ois.readObject());
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(records.size());
        for (DateProvider r : records) {            
            oos.writeObject((Record)r);
        }
    }

    public Collection<Record> get() {
        
        if (f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                DataStoreCache data = (DataStoreCache) ois.readObject();
                return data.records;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else
        {
        }
        return null;
    }

//     @Override
    public boolean put(Collection<Record> records) {
        try {
            this.records = records;
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            fos.close();
            return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Cached data. Could be modelled, could be imported. Who knows? Bug? Feature?";
    }
}

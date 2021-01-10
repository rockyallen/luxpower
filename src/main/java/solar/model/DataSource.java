package solar.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for the actual data so the data can be made non final (easier to
 * serialize)
 *
 * @author rocky
 */
public class DataSource implements Changeable {

    public static final File f = new File("hashmap.ser");
    private DataSourceData data = new DataSourceData();
    private Set<Listener> listeners = new HashSet<>();

    public DataSource() {
    }

    public void addListener(Listener ll) {
        listeners.add(ll);
    }

    public void announceChanged() {
        for (Listener ll : listeners) {
            ll.changed();
        }
    }

    public boolean serialize() {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            fos.close();
            return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    public boolean deserialize() {

        if (f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                data = (DataSourceData) ois.readObject();
                return true;
            } catch (Exception ex) {
            }
        }
        return false;
    }

    public Collection<Record> getRecords() {
        return data.records;
    }
}

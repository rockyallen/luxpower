package solar.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Java database for the luxpower exports.
 *
 * Records are guaranteed to be sorted by increasing data.
 *
 * @author rocky
 */
class DataSourceData implements Serializable {

    private static final long serialVersionUID = 36477397454380L;
    public transient Collection<Record> records;

    public DataSourceData() {
        records = new TreeSet<>();
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
}

package solar.model;

import java.util.Collection;
import javafx.concurrent.Task;

/**
 *
 * @author rocky
 */
public class DataStoreCacheWriter extends Task {

    private Collection<Record> data;

    public void setData(Collection<Record> data) {
        this.data = data;
    }

    @Override
    public Void call() {

        new DataStoreCache().put(data);
        return null;
    }
}

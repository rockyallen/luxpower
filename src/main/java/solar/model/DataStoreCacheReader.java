package solar.model;

import java.util.Collection;
import javafx.concurrent.Task;

/**
 * A task to get the cached data.
 * 
 * @author rocky
 */
public class DataStoreCacheReader extends Task {

    @Override
    public Collection<Record> call() {

        return new DataStoreCache().get();
    }
}

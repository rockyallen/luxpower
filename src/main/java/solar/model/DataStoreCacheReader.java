package solar.model;

import java.util.Collection;
import javafx.concurrent.Task;

/**
 *
 * @author rocky
 */
public class DataStoreCacheReader<T> extends Task {

    @Override
    public Collection<Record> call() {

        return new DataStoreCache().get();
    }
}

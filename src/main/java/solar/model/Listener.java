package solar.model;

import java.util.Collection;

/**
 *
 * @author rocky
 */
public interface Listener {
public void changed(Collection<Record> records, String description);    
}

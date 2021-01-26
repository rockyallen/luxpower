package solar.model;

import java.util.Collection;

/**
 * My version.
 * 
 * @design Why not use a JavaFX one?
 * 
 * @author rocky
 */
public interface Listener {
public void changed(Collection<Record> records, Components componentsList);    
}

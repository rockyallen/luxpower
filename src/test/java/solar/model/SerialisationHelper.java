package solar.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Checks that something can be round-tripped.
 *
 * If your object implements equals:
 *
 * MyObject obj = new MyObject(); 
 * // populate instance if necessary 
 * MyObject result = (MyObject);
 * SerialisationHelper.testRoundTrip(instance); 
 * assertEquals("", instance, result);
 *
 * @author rocky
 */
public class SerialisationHelper {

    private static <T extends Serializable> byte[] tobytes(T obj)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return baos.toByteArray();
    }

    private static <T extends Serializable> T frombytes(byte[] b, Class<T> cl)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        return cl.cast(o);
    }

    public static <T extends Serializable> Object testRoundTrip(T obj) throws IOException, ClassNotFoundException {
        byte[] bytes = tobytes(obj);
        Object obj2 = frombytes(bytes, obj.getClass());
        return obj2;
    }
}

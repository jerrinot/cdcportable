package info.jerrinot.cdcportable.client.domain;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class CustomerFactory implements PortableFactory {
    @Override
    public Portable create(int i) {
        if (i == 1) {
            return new Customer();
        } else {
            throw new UnsupportedOperationException("unknown class id " + i);
        }
    }
}

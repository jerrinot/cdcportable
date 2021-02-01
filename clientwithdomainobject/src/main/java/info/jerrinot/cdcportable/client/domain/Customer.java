package info.jerrinot.cdcportable.client.domain;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;
import java.util.Objects;

public class Customer implements Portable {

    public int id;
    public String firstName;
    public String lastName;
    public String email;

    public Customer() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, firstName, id, lastName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Customer other = (Customer) obj;
        return id == other.id
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName)
                && Objects.equals(email, other.email);
    }

    @Override
    public String toString() {
        return "Customer {id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + '}';
    }

    @Override
    public int getFactoryId() {
        return 1;
    }

    @Override
    public int getClassId() {
        return 1;
    }

    @Override
    public void writePortable(PortableWriter w) throws IOException {
        w.writeInt("id", id);
        w.writeUTF("firstname", firstName);
        w.writeUTF("lastname", lastName);
        w.writeUTF("email", email);
    }

    @Override
    public void readPortable(PortableReader r) throws IOException {
        id = r.readInt("id");
        firstName = r.readUTF("firstname");
        lastName = r.readUTF("lastname");
        email = r.readUTF("email");
    }
}
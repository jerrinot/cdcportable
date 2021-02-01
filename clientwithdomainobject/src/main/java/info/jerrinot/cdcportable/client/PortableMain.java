package info.jerrinot.cdcportable.client;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import info.jerrinot.cdcportable.client.domain.Customer;
import info.jerrinot.cdcportable.client.domain.CustomerFactory;
import info.jerrinot.cdcportable.client.utils.Constants;
import info.jerrinot.cdcportable.client.utils.TableMaker;

import java.util.Collection;

public class PortableMain {
    private static final TableMaker<SqlRow> TABLE_MAKER = TableMaker.<SqlRow>newMaker(SqlRow::getObject)
            .addColumn("id", 10)
            .addColumn("firstname", 20)
            .addColumn("lastname", 20)
            .addColumn("email", 25)
            .build();

    public static void main(String[] args) {
        var clientConfig = new ClientConfig();
        clientConfig.getSerializationConfig().addPortableFactoryClass(Constants.CUSTOMER_FACTORY_CLASS_ID,
                CustomerFactory.class);
        clientConfig.setClusterName("jet");

        var hazelcast = Jet.newJetClient(clientConfig);
        IMap<Integer, Customer> map = hazelcast.getMap("customers");

        System.out.println("Let try regular IMap operations. As you can see - they return Customers");
        Collection<Customer> values = map.values();
        System.out.println("Current records: ");
        for(var record : values) {
            System.out.println(record);
        }

        System.out.println("\nSQL works nicely too:");
        SqlResult sqlResult = hazelcast.getSql().execute("select id, firstname, lastname, email from customers");
        System.out.println(TABLE_MAKER.format(sqlResult));

        System.out.println("\nSQL can get the domain object too:");
        sqlResult = hazelcast.getSql().execute("select this as customer from customers");
        for (var row : sqlResult) {
            Customer customer = row.getObject("customer");
            System.out.println(customer);
        }

        System.out.println("\nSubscribing to receive events: ");
        map.addEntryListener((EntryUpdatedListener<Integer, Customer>) entryEvent -> {
            String sb = "Key " + entryEvent.getKey() + " updated\nOld Value: "
                    + entryEvent.getOldValue()
                    + "\nNew Value: " + entryEvent.getValue();
            System.out.println(sb);
        }, true);
    }
}
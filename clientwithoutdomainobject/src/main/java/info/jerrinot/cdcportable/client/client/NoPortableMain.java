package info.jerrinot.cdcportable.client.client;

import com.hazelcast.jet.Jet;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.nio.serialization.GenericRecord;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import info.jerrinot.cdcportable.client.utils.TableMaker;

import java.util.Collection;

public class NoPortableMain {
    private static final TableMaker<SqlRow> TABLE_MAKER = TableMaker.<SqlRow>newMaker(SqlRow::getObject)
            .addColumn("id", 10)
            .addColumn("firstname", 20)
            .addColumn("lastname", 20)
            .addColumn("email", 25)
            .build();

    public static void main(String[] args) {
        var hazelcast = Jet.newJetClient();
        IMap<Integer, GenericRecord> map = hazelcast.getMap("customers");

        System.out.println("Let try regular IMap operations. As you can see - they return GenericRecord(s)");
        Collection<GenericRecord> values = map.values();
        System.out.println("Current records: ");
        for(var record : values) {
            System.out.println(record);
            System.out.println();
        }

        System.out.println("\nSQL Works nicely too:");
        SqlResult sqlResult = hazelcast.getSql().execute("select id, firstname, lastname, email from customers");
        System.out.println(TABLE_MAKER.format(sqlResult));

        System.out.println("\nSubscribing to receive events: ");
        map.addEntryListener((EntryUpdatedListener<Integer, GenericRecord>) entryEvent -> {
            String sb = "Key " + entryEvent.getKey() + " updated\nOld Value: "
                    + entryEvent.getOldValue()
                    + "\nNew Value:" + entryEvent.getValue();
            System.out.println(sb);
        }, true);
    }
}
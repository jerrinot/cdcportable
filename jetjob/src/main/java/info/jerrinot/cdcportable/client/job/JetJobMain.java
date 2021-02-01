package info.jerrinot.cdcportable.client.job;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.cdc.CdcSinks;
import com.hazelcast.jet.cdc.ChangeRecord;
import com.hazelcast.jet.cdc.postgres.PostgresCdcSources;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.StreamSource;
import info.jerrinot.cdcportable.client.job.mapping.ChangeRecordMapper;
import info.jerrinot.cdcportable.client.job.mapping.MappingDef;
import info.jerrinot.cdcportable.client.utils.Constants;

import static com.hazelcast.nio.serialization.FieldType.INT;
import static com.hazelcast.nio.serialization.FieldType.UTF;

public class JetJobMain {
    // define field mapping between ChangeRecord and the resulting Portable object
    private static final MappingDef MAPPING = MappingDef.mapFrom("first_name").to("firstname").ofType(UTF)
            .andFrom("last_name").to("lastname").ofType(UTF)
            .andIdentityField("email").ofType(UTF)
            .andIdentityField("id").ofType(INT).build();

    public static void main(String[] args) {
        StreamSource<ChangeRecord> source = PostgresCdcSources.postgres("source")
                .setDatabaseAddress("172.17.0.2")
                .setDatabasePort(5432)
                .setDatabaseUser("postgres")
                .setDatabasePassword("postgres")
                .setDatabaseName("postgres")
                .setTableWhitelist("inventory.customers")
                .build();

        Sink<ChangeRecord> sink = CdcSinks.map("customers",
                r -> r.key().toMap().get("id"),
                ChangeRecordMapper.valueToPortable(
                        Constants.CUSTOMER_FACTORY_CLASS_ID, Constants.CUSTOMER_CLASS_ID, MAPPING)
        );

        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(source)
                .withoutTimestamps()
                .writeTo(sink);

        JobConfig cfg = new JobConfig().setName("postgres-monitor");
        cfg.addClass(JetJobMain.class).addClass(ChangeRecordMapper.class).addClass(MappingDef.class);
        JetInstance jet = Jet.bootstrappedInstance();
//        JetInstance jet = Jet.newJetClient();
        jet.newJob(pipeline, cfg);
    }
}
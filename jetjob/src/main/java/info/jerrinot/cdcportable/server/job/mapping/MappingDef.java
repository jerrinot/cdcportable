package info.jerrinot.cdcportable.server.job.mapping;

import com.hazelcast.nio.serialization.FieldType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Define field mapping between 2 objects.
 *
 * Use this as an input for a declarative mapper.
 *
 * Currently source and target fields must use the same type.
 * It could be extended to include type conversions.
 *
 */
public final class MappingDef {
    private final List<SingleFieldMapping> fieldMappings;

    private MappingDef(List<SingleFieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    List<SingleFieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public static MappingBuilderStep1 mapFrom(String from) {
        return new MappingBuilderStep1(from, List.of());
    }

    public static MappingBuilderStep2 mapIdentity(String field) {
        return new MappingBuilderStep2(field, field, Collections.emptyList());
    }

    public static final class UnfinishedMapping  {
        private final List<SingleFieldMapping> fieldMappings;

        private UnfinishedMapping(List<SingleFieldMapping> fieldMappings) {
            this.fieldMappings = fieldMappings;
        }

        public MappingBuilderStep1 andFrom(String from) {
            return new MappingBuilderStep1(from, fieldMappings);
        }

        public MappingBuilderStep2 andIdentityField(String field) {
            return new MappingBuilderStep2(field, field, fieldMappings);
        }

        public MappingDef build() {
            return new MappingDef(fieldMappings);
        }

    }

    public static final class SingleFieldMapping {
        private final String from;
        private final String to;
        private final FieldType type;

        private SingleFieldMapping(String from, String to, FieldType type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }

        public FieldType getType() {
            return type;
        }

        public String getTo() {
            return to;
        }

        public String getFrom() {
            return from;
        }
    }

    public static final class MappingBuilderStep1 {
        private final List<SingleFieldMapping> existingMappings;
        private final String from;

        private MappingBuilderStep1(String from, List<SingleFieldMapping> existingMappings) {
            this.from = from;
            this.existingMappings = existingMappings;
        }

        public MappingBuilderStep2 to(String to) {
            return new MappingBuilderStep2(from, to, existingMappings);
        }
    }

    public static final class MappingBuilderStep2 {
        private final String from;
        private final String to;
        private final List<SingleFieldMapping> existingMappings;

        private MappingBuilderStep2(String from, String to, List<SingleFieldMapping> existingMappings) {
            this.from = from;
            this.to = to;
            this.existingMappings = existingMappings;
        }

        public UnfinishedMapping ofType(FieldType type) {
            List<SingleFieldMapping> newMappings = new ArrayList<>(existingMappings);
            newMappings.add(new SingleFieldMapping(from, to, type));
            return new UnfinishedMapping(unmodifiableList(newMappings));
        }
    }
}

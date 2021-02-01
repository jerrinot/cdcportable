package info.jerrinot.cdcportable.client.job.mapping;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.cdc.ChangeRecord;
import com.hazelcast.jet.cdc.RecordPart;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.FieldType;
import com.hazelcast.nio.serialization.GenericRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChangeRecordMapper {

    /**
     * Creates a function to convert a value from CDC ChangeRecord to a protable record.
     * The result function maps only fields explicitly configured via Mapping.
     *
     *
     * @param factoryId factory id to be encoded in the result portable
     * @param classId class id to be encoded in the resulting portable
     * @param mapping configures fields to map from the ChangeRecord to Portable
     * @return function to map ChangeRecord value to a portable object
     */
    public static FunctionEx<? super ChangeRecord, ? extends GenericRecord> valueToPortable(int factoryId,
                                                                                            int classId,
                                                                                            MappingDef mapping) {
        return new Mapper(factoryId, classId, mapping);
    }

    private static class Mapper implements FunctionEx<ChangeRecord, GenericRecord> {
        private transient ClassDefinition cd;
        private String[] froms;
        private String[] tos;
        private FieldType[] types;
        private final int factoryId;
        private final int classId;

        private Mapper(int factoryId, int classId, MappingDef mapping) {
            this.factoryId = factoryId;
            this.classId = classId;
            expandMappings(mapping);
            initializeClassDefinition();
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            initializeClassDefinition();
        }

        private void expandMappings(MappingDef mapping) {
            List<MappingDef.SingleFieldMapping> fieldMappings = mapping.getFieldMappings();
            int mappingSize = fieldMappings.size();
            froms = new String[mappingSize];
            tos = new String[mappingSize];
            types = new FieldType[mappingSize];
            for (int i = 0; i < mappingSize; i++) {
                MappingDef.SingleFieldMapping fieldMapping = fieldMappings.get(i);
                String from = fieldMapping.getFrom();
                String to = fieldMapping.getTo();
                FieldType type = fieldMapping.getType();
                froms[i] = from;
                tos[i] = to;
                types[i] = type;
            }
        }

        private void initializeClassDefinition() {
            ClassDefinitionBuilder cdBuilder = new ClassDefinitionBuilder(factoryId, classId);
            for (int i = 0; i < froms.length; i++) {
                String to = tos[i];
                FieldType type = types[i];
                switch (type) {
                    case PORTABLE:
                        throw new UnsupportedOperationException("nested portables not implemented");
                    case BYTE:
                        cdBuilder.addByteField(to);
                        break;
                    case BOOLEAN:
                        cdBuilder.addBooleanField(to);
                        break;
                    case CHAR:
                        cdBuilder.addCharField(to);
                        break;
                    case SHORT:
                        cdBuilder.addShortField(to);
                        break;
                    case INT:
                        cdBuilder.addIntField(to);
                        break;
                    case LONG:
                        cdBuilder.addLongField(to);
                        break;
                    case FLOAT:
                        cdBuilder.addFloatField(to);
                        break;
                    case DOUBLE:
                        cdBuilder.addDoubleField(to);
                        break;
                    case UTF:
                        cdBuilder.addUTFField(to);
                        break;
                    case PORTABLE_ARRAY:
                    case BYTE_ARRAY:
                    case BOOLEAN_ARRAY:
                    case CHAR_ARRAY:
                    case SHORT_ARRAY:
                    case INT_ARRAY:
                    case LONG_ARRAY:
                    case FLOAT_ARRAY:
                    case DOUBLE_ARRAY:
                    case UTF_ARRAY:
                        throw new UnsupportedOperationException("nested arrays not implemented");
                }
            }
            cd = cdBuilder.build();
        }

        @Override
        public GenericRecord applyEx(ChangeRecord changeRecord) throws Exception {
            RecordPart value = changeRecord.value();
            Map<String, Object> fieldMap = value.toMap();
            GenericRecord.Builder builder = GenericRecord.Builder.portable(cd);
            for (int i = 0; i < froms.length; i++) {
                String from = froms[i];
                Object o = fieldMap.get(from);
                if (o == null) {
                    continue;
                }
                FieldType fieldType = types[i];
                String to = tos[i];
                switch (fieldType) {
                    case PORTABLE:
                        throw new UnsupportedOperationException("nested portables not implemented");
                    case BYTE:
                        builder.writeByte(to, (Byte) o);
                        break;
                    case BOOLEAN:
                        builder.writeBoolean(to, (Boolean) o);
                        break;
                    case CHAR:
                        builder.writeChar(to, (Character) o);
                        break;
                    case SHORT:
                        builder.writeShort(to, (Short) o);
                        break;
                    case INT:
                        builder.writeInt(to, (Integer) o);
                        break;
                    case LONG:
                        builder.writeLong(to, (Long) o);
                        break;
                    case FLOAT:
                        builder.writeFloat(to, (Float) o);
                        break;
                    case DOUBLE:
                        builder.writeDouble(to, (Double) o);
                        break;
                    case UTF:
                        builder.writeUTF(to, (String) o);
                        break;
                    case PORTABLE_ARRAY:
                    case BYTE_ARRAY:
                    case BOOLEAN_ARRAY:
                    case CHAR_ARRAY:
                    case SHORT_ARRAY:
                    case INT_ARRAY:
                    case LONG_ARRAY:
                    case FLOAT_ARRAY:
                    case DOUBLE_ARRAY:
                    case UTF_ARRAY:
                        throw new UnsupportedOperationException("nested arrays not implemented");
                }
            }
            return builder.build();
        }
    }
}
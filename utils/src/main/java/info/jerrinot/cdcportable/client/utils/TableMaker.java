package info.jerrinot.cdcportable.client.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static java.lang.System.lineSeparator;

public final class TableMaker<T> {
    private static final char COLUMN_SEPARATOR = '|';
    private static final String EMPTY_SYMBOL = "null";

    private final String[] columns;
    private final int[] lengths;
    private final BiFunction<? super T, ? super String, ?> columnExtractor;

    public static <T> Builder<T> newMaker(BiFunction<? super T, ? super String, ?> extractor) {
        return new Builder<T>(extractor);
    }

    public String format(Iterable<? extends T> rows) {
        var sb = new StringBuilder();
        appendTableHeader(sb);
        appendDataRows(rows, sb);
        return sb.toString();
    }

    private TableMaker(BiFunction<? super T, ? super String, ?> columnExtractor, List<String> strings, List<Integer> lengths) {
        this.columnExtractor = columnExtractor;
        this.columns = strings.toArray(new String[0]);
        this.lengths = lengths.stream().mapToInt(i->i).toArray();
    }

    private void appendDataRows(Iterable<? extends T> rows, StringBuilder sb) {
        for (var row : rows) {
            sb.append(COLUMN_SEPARATOR);
            for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                String field = extractColumnAt(row, columnIndex);
                sb.append(field).append(COLUMN_SEPARATOR);
            }
            sb.append(lineSeparator());
        }
    }

    private String extractColumnAt(T row, int columnIndex) {
        var columnName = columns[columnIndex];
        Object o = columnExtractor.apply(row, columnName);
        var string = o == null ? EMPTY_SYMBOL : o.toString().trim();

        int length = lengths[columnIndex];
        return StringUtils.center(string, length);
    }

    private void appendTableHeader(StringBuilder sb) {
        sb.append(COLUMN_SEPARATOR);
        for (int i = 0; i < columns.length; i++) {
            var columnName = columns[i];
            int length = lengths[i];
            sb.append(StringUtils.center(columnName, length))
                    .append(COLUMN_SEPARATOR);
        }
        sb.append(lineSeparator());
    }

    public static final class Builder<T> {
        private final BiFunction<? super T, ? super String, ?> extractor;
        private final List<String> columns = new ArrayList<>();
        private final List<Integer> lengths = new ArrayList<>();

        public Builder(BiFunction<? super T, ? super String, ?> extractor) {
            this.extractor = extractor;
        }

        public Builder<T> addColumn(String columnName, int columnLength) {
            columns.add(columnName);
            lengths.add(columnLength);
            return this;
        }

        public TableMaker<T> build() {
            return new TableMaker<T>(extractor, columns, lengths);
        }
    }
}

package info.jerrinot.cdcportable.client.utils;

public final class StringUtils {
    private static final String PADDING_CHAR = " ";

    private StringUtils() { }

    public static String center(String input, int desiredLength) {
        int origLength = input.length();
        int totalPadding = desiredLength - origLength;
        int leftPadding = totalPadding / 2;
        String res = PADDING_CHAR.repeat(leftPadding).concat(input);
        int rightPadding = desiredLength - res.length();
        return res.concat(PADDING_CHAR.repeat(rightPadding));
    }
}

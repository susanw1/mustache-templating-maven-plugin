package net.zscript.ascii;

import java.util.Arrays;

public class TextRow {
    private final CharacterStyle[] styles;
    private final char[]           chars;

    public TextRow(char[] chars, CharacterStyle[] styles) {
        this.chars = chars;
        this.styles = styles;
    }

    public String toString(CharacterStylePrinter printer) {
        CharacterStyle prev    = CharacterStyle.standardStyle();
        StringBuilder  builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (!styles[i].equals(prev)) {
                builder.append(printer.applyDiff(prev, styles[i]));
                prev = styles[i];
            }
            builder.append(chars[i]);
        }
        builder.append(printer.cancel(prev));
        return builder.toString();
    }

    public char charAt(int i) {
        return chars[i];
    }

    public CharacterStyle styleAt(int i) {
        return styles[i];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TextRow textRow = (TextRow) o;
        return Arrays.equals(styles, textRow.styles) && Arrays.equals(chars, textRow.chars);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(styles);
        result = 31 * result + Arrays.hashCode(chars);
        return result;
    }
}

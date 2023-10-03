package net.zscript.ascii;

import java.util.Iterator;

public interface AsciiFrame extends Iterable<TextRow> {

    int getWidth();

    int getHeight();

    boolean setWidth(int width);

    default String generateString(CharacterStylePrinter printer) {
        StringBuilder b = new StringBuilder();
        for (TextRow row : this) {
            b.append(row.toString(printer));
            b.append("\n");
        }
        return b.toString();
    }

}

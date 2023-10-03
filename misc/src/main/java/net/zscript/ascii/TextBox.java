package net.zscript.ascii;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

public class TextBox implements AsciiFrame {

    static class TextLine {
        private final int                     startIndent;
        private final List<StyledTextSegment> segments = new ArrayList<>();

        TextLine(int startIndent) {
            this.startIndent = startIndent;
        }

        public Iterator<StyledTextSegment> iterator() {
            return segments.iterator();
        }
    }

    static class StyledTextSegment {
        private final CharacterStyle style;
        private final String         string;

        StyledTextSegment(CharacterStyle style, String string) {
            this.style = style;
            this.string = string;
        }

        public int getLength() {
            return string.length();
        }
    }

    private final List<TextLine> lines = new ArrayList<>();

    private String         indentString;
    private CharacterStyle indentStyle = CharacterStyle.standardStyle();

    private int indentOnWrap = 0;

    private CharacterStyle lastStyle = CharacterStyle.standardStyle();
    private StringBuilder  builder   = new StringBuilder();
    private int            width     = 80;

    public TextBox(String indentString) {
        this.indentString = indentString;
    }

    public void setIndentOnWrap(int indentOnWrap) {
        this.indentOnWrap = indentOnWrap;
    }

    public void setIndentString(String indentString) {
        this.indentString = indentString;
        this.indentStyle = CharacterStyle.standardStyle();
    }

    public void setIndentString(String indentString, CharacterStyle indentStyle) {
        this.indentString = indentString;
        this.indentStyle = indentStyle;
    }

    private TextLine lastLine() {
        if (lines.isEmpty()) {
            lines.add(new TextLine(0));
        }
        return lines.get(lines.size() - 1);
    }

    private void emptyBuilder() {
        if (builder.length() != 0) {
            lastLine().segments.add(new StyledTextSegment(lastStyle, builder.toString()));
        }
        builder = new StringBuilder();
    }

    public TextBox setStyle(CharacterStyle style) {
        emptyBuilder();
        lastStyle = style;
        return this;
    }

    public TextBox startNewLine(int indent) {
        emptyBuilder();
        lastStyle = CharacterStyle.standardStyle();
        lines.add(new TextLine(indent));
        return this;
    }

    public TextBox startNewLine() {
        emptyBuilder();
        lastStyle = CharacterStyle.standardStyle();
        if (lines.isEmpty()) {
            lines.add(new TextLine(0));
        } else {
            lines.add(new TextLine(lastLine().startIndent));
        }
        return this;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        emptyBuilder();
        int height = 0;
        for (TextRow ignored : this) {
            height++;
        }
        return height;
    }

    @Override
    public boolean setWidth(int width) {
        emptyBuilder();
        for (TextLine l : lines) {
            if (width - l.startIndent * indentString.length() < 10) {
                return false;
            }
        }
        this.width = width;
        return true;
    }

    @Override
    public Iterator<TextRow> iterator() {
        emptyBuilder();
        return new Iterator<TextRow>() {
            private final Iterator<TextLine> lineIter = lines.iterator();

            private int indent = 0;
            private Iterator<StyledTextSegment> styledSegments = null;

            private StyledTextSegment last = null;
            private int currentPos = 0;

            @Override
            public boolean hasNext() {
                while (last == null) {
                    while (styledSegments == null || !styledSegments.hasNext()) {
                        if (!lineIter.hasNext()) {
                            return false;
                        }
                        TextLine line = lineIter.next();
                        indent = line.startIndent;
                        styledSegments = line.iterator();

                    }
                    last = styledSegments.next();
                    currentPos = 0;
                }

                return true;
            }

            @Override
            public TextRow next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                char[]           dataResult  = new char[width];
                CharacterStyle[] styleResult = new CharacterStyle[width];
                Arrays.fill(dataResult, ' ');
                Arrays.fill(styleResult, CharacterStyle.standardStyle());

                int dataIndex = 0;

                int indentReq;
                if (currentPos != 0) {
                    indentReq = indent + indentOnWrap;
                } else {
                    indentReq = indent;
                }
                for (int i = 0; i < indentReq; i++) {
                    for (char c : indentString.toCharArray()) {
                        dataResult[dataIndex] = c;
                        styleResult[dataIndex] = indentStyle;
                        dataIndex++;
                    }
                }
                int remainingLineLen = width - dataIndex;

                while (true) {
                    if (currentPos != 0) {
                        while (currentPos < last.getLength() && Character.isWhitespace(last.string.charAt(currentPos))) {
                            currentPos++;
                        }
                    }
                    if (last.getLength() - currentPos <= remainingLineLen) {
                        for (char c : last.string.substring(currentPos).toCharArray()) {
                            dataResult[dataIndex] = c;
                            styleResult[dataIndex] = last.style;
                            dataIndex++;
                        }
                        remainingLineLen -= (last.getLength() - currentPos);
                    } else {
                        boolean needsHyphen = false;
                        int     nextPos     = currentPos + remainingLineLen;
                        while (nextPos > currentPos && !Character.isWhitespace(last.string.charAt(nextPos))) {
                            nextPos--;
                        }
                        if (nextPos <= currentPos) {
                            nextPos = currentPos + remainingLineLen - 1;
                            while (nextPos > currentPos && Character.isLetterOrDigit(last.string.charAt(nextPos))) {
                                nextPos--;
                            }
                            if (nextPos != currentPos) {
                                nextPos++;
                            }
                            if (nextPos == currentPos) {
                                needsHyphen = true;
                                nextPos = currentPos + remainingLineLen - 1;
                            }
                        }
                        for (char c : last.string.substring(currentPos, nextPos).toCharArray()) {
                            dataResult[dataIndex] = c;
                            styleResult[dataIndex] = last.style;
                            dataIndex++;
                        }
                        if (needsHyphen) {
                            dataResult[dataIndex] = '-';
                            styleResult[dataIndex] = last.style;
                            dataIndex++;
                        }
                        currentPos = nextPos;
                        break;
                    }
                    if (styledSegments.hasNext()) {
                        last = styledSegments.next();
                        currentPos = 0;
                    } else {
                        last = null;
                        currentPos = 0;
                        break;
                    }
                }
                return new TextRow(dataResult, styleResult);
            }
        };
    }

    private char toHexChar(int i) {
        if (i < 0 || i > 0x10) {
            throw new IllegalArgumentException("Not a single hex digit: " + i);
        }
        if (i < 10) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + i - 10);
        }
    }

    public TextBox appendHex(int value, int minimumLength) {
        for (int i = 8 - 1; i >= 0; i--) {
            if (minimumLength > i || value >= (1 << (4 * i))) {
                builder.append(toHexChar((value >> (4 * i)) & 0xF));
            }
        }
        return this;
    }

    // All the append methods from StringBuilder...

    public TextBox append(Object obj) {
        builder.append(obj);
        return this;
    }

    public TextBox append(String str) {
        builder.append(str);
        return this;
    }

    public TextBox append(StringBuffer sb) {
        builder.append(sb);
        return this;
    }

    public TextBox append(CharSequence s) {
        builder.append(s);
        return this;
    }

    public TextBox append(CharSequence s, int start, int end) {
        builder.append(s, start, end);
        return this;
    }

    public TextBox append(char[] str) {
        builder.append(str);
        return this;
    }

    public TextBox append(char[] str, int offset, int len) {
        builder.append(str, offset, len);
        return this;
    }

    public TextBox append(boolean b) {
        builder.append(b);
        return this;
    }

    public TextBox append(char c) {
        builder.append(c);
        return this;
    }

    public TextBox append(int i) {
        builder.append(i);
        return this;
    }

    public TextBox append(long lng) {
        builder.append(lng);
        return this;
    }

    public TextBox append(float f) {
        builder.append(f);
        return this;
    }

    public TextBox append(double d) {
        builder.append(d);
        return this;
    }
}

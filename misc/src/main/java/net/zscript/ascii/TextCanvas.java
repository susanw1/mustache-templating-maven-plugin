package net.zscript.ascii;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TextCanvas implements AsciiFrame {

    protected interface CanvasElement {
        void apply();
    }

    protected class AsciiFrameElement implements CanvasElement {
        private final AsciiFrame box;

        private final int leftHorizontalPos;
        private final int topVerticalPos;

        public AsciiFrameElement(AsciiFrame box, int leftHorizontalPos, int topVerticalPos) {
            this.box = box;
            this.leftHorizontalPos = leftHorizontalPos;
            this.topVerticalPos = topVerticalPos;
        }

        @Override
        public void apply() {
            int y = topVerticalPos;
            for (TextRow row : box) {
                int x = leftHorizontalPos;
                for (int i = 0; i < box.getWidth(); i++) {
                    data[y][x] = row.charAt(i);
                    styles[y][x] = row.styleAt(i);
                    x++;
                }
                y++;
            }
        }
    }

    public static class LineDrawingStrategy {

        public static class VerticalLineDrawingStrategy {
            public final boolean isHStart;
            public final boolean isHEnd;

            public VerticalLineDrawingStrategy(boolean isHStart, boolean isHEnd) {
                this.isHStart = isHStart;
                this.isHEnd = isHEnd;
            }
        }

        public static class HorizontalLineDrawingStrategy {
            public final boolean isTop;
            public final boolean isBottom;

            public HorizontalLineDrawingStrategy(boolean isTop, boolean isBottom) {
                this.isTop = isTop;
                this.isBottom = isBottom;
            }
        }

        public final VerticalLineDrawingStrategy   vertical;
        public final HorizontalLineDrawingStrategy horizontal;

        LineDrawingStrategy(VerticalLineDrawingStrategy vertical, HorizontalLineDrawingStrategy horizontal) {
            this.vertical = vertical;
            this.horizontal = horizontal;
        }

        public LineDrawingStrategy(boolean isHStart, boolean isHEnd, boolean isTop, boolean isBottom) {
            this.vertical = new VerticalLineDrawingStrategy(isHStart, isHEnd);
            this.horizontal = new HorizontalLineDrawingStrategy(isTop, isBottom);
        }
    }

    protected class LineElement implements CanvasElement {
        private final CharacterStyle style;

        private final int hStartPos;
        private final int vStartPos;

        private final int hEndPos;
        private final int vEndPos;

        private final LineDrawingStrategy strategy;

        public LineElement(CharacterStyle style, int hStartPos, int vStartPos, int hEndPos, int vEndPos, LineDrawingStrategy strategy) {
            this.style = style;
            if (vEndPos >= vStartPos) {
                this.hStartPos = hStartPos;
                this.vStartPos = vStartPos;
                this.hEndPos = hEndPos;
                this.vEndPos = vEndPos;
            } else {
                this.hStartPos = hEndPos;
                this.vStartPos = vEndPos;
                this.hEndPos = hStartPos;
                this.vEndPos = vStartPos;
            }
            this.strategy = strategy;
        }

        private void drawDiagonal(int vStart, int hStart, int length, boolean hPos) {
            for (int i = 1; i < length + 1; i++) {
                if (hPos) {
                    data[vStart + i][hStart + i] = '\\';
                    styles[vStart + i][hStart + i] = style;
                } else {
                    data[vStart + i][hStart - i] = '/';
                    styles[vStart + i][hStart - i] = style;
                }
            }
        }

        @Override
        public void apply() {
            if (vEndPos == vStartPos) {
                for (int i = Math.min(hStartPos, hEndPos) + 1; i < hEndPos || i < hStartPos; i++) {
                    data[vStartPos][i] = '-';
                    styles[vStartPos][i] = style;
                }
                return;
            } else if (hEndPos == hStartPos) {
                for (int i = vStartPos + 1; i < vEndPos; i++) {
                    data[i][hStartPos] = '|';
                    styles[i][hStartPos] = style;
                }
                return;
            } else if (vEndPos - vStartPos == Math.abs(hEndPos - hStartPos)) {
                // pure diagonals
                if (vEndPos - vStartPos == 1) {
                    data[vEndPos][hStartPos] = '\'';
                    styles[vEndPos][hStartPos] = style;
                } else {
                    drawDiagonal(vStartPos, hStartPos, vEndPos - vStartPos - 1, hEndPos > hStartPos);
                }
                return;
            }
            int vLength = vEndPos - vStartPos - 1;
            int hLength = Math.abs(hEndPos - hStartPos) - 1;
            if (vLength > hLength) {
                // very downwards lines...
                if (strategy.vertical.isHStart) {
                    int vertLen = vLength - hLength + 1;
                    if (hLength == 0) {
                        vertLen--;
                    }
                    for (int i = 1; i < vertLen; i++) {
                        data[vStartPos + i][hStartPos] = '|';
                        styles[vStartPos + i][hStartPos] = style;
                    }
                    if (hLength == 0) {
                        if (hEndPos > hStartPos) {
                            data[vStartPos + vertLen][hStartPos] = '\\';
                        } else {
                            data[vStartPos + vertLen][hStartPos] = '/';
                        }
                        styles[vStartPos + vertLen][hStartPos] = style;
                    }
                    drawDiagonal(vEndPos - hLength - 1, hStartPos, hLength, hEndPos > hStartPos);
                } else if (strategy.vertical.isHEnd) {
                    int vertLen = vLength - hLength;
                    if (hLength == 0) {
                        vertLen--;
                    }
                    for (int i = 0; i < vertLen; i++) {
                        data[vEndPos - i - 1][hEndPos] = '|';
                        styles[vEndPos - i - 1][hEndPos] = style;
                    }
                    if (hLength == 0) {
                        if (hEndPos > hStartPos) {
                            data[vEndPos - vertLen - 1][hEndPos] = '\\';
                        } else {
                            data[vEndPos - vertLen - 1][hEndPos] = '/';
                        }
                        styles[vEndPos - vertLen - 1][hEndPos] = style;
                    }
                    drawDiagonal(vStartPos, hStartPos, hLength, hEndPos > hStartPos);
                } else {
                    int middle = hStartPos + (hEndPos > hStartPos ? 1 : -1) * (hLength + 2) / 2;
                    drawDiagonal(vStartPos, hStartPos, (hLength + 1) / 2, hEndPos > hStartPos);
                    for (int i = vStartPos + (hLength + 1) / 2 + 1; i < vEndPos - (hLength + 1) / 2; i++) {
                        data[i][middle] = '|';
                        styles[i][middle] = style;
                    }
                    if (hLength == 0) {
                        if (hEndPos > hStartPos) {
                            data[vStartPos + 1][middle] = '\\';
                        } else {
                            data[vStartPos + 1][middle] = '/';
                        }
                        styles[vStartPos + 1][middle] = style;
                    }
                    drawDiagonal(vEndPos - (hLength + 1) / 2 - 1, middle + (hEndPos > hStartPos ? -1 : 1), (hLength + 1) / 2, hEndPos > hStartPos);
                }
            } else {
                if (strategy.horizontal.isTop) {
                    drawDiagonal(vStartPos, hEndPos + (vLength + 1) * (hEndPos > hStartPos ? -1 : 1), vLength, hEndPos > hStartPos);
                    for (int i = 1; i < hLength - vLength; i++) {
                        if (hEndPos > hStartPos) {
                            data[vStartPos][hStartPos + i] = '-';
                            styles[vStartPos][hStartPos + i] = style;
                        } else {
                            data[vStartPos][hStartPos - i] = '-';
                            styles[vStartPos][hStartPos - i] = style;
                        }
                    }
                    if (hEndPos > hStartPos) {
                        data[vStartPos][hStartPos + hLength - vLength] = '.';
                        styles[vStartPos][hStartPos + hLength - vLength] = style;
                    } else {
                        data[vStartPos][hStartPos - (hLength - vLength)] = '.';
                        styles[vStartPos][hStartPos - (hLength - vLength)] = style;
                    }
                    if (vLength == 0) {
                        if (hEndPos > hStartPos) {
                            data[vEndPos][hStartPos + hLength - vLength] = '`';
                            styles[vEndPos][hStartPos + hLength - vLength] = style;
                        } else {
                            data[vEndPos][hStartPos - (hLength - vLength)] = '`';
                            styles[vEndPos][hStartPos - (hLength - vLength)] = style;
                        }
                    }
                } else if (strategy.horizontal.isBottom) {
                    drawDiagonal(vStartPos, hStartPos, vLength, hEndPos > hStartPos);
                    for (int i = 1; i < hLength - vLength; i++) {
                        if (hEndPos < hStartPos) {
                            data[vEndPos][hEndPos + i] = '-';
                            styles[vEndPos][hEndPos + i] = style;
                        } else {
                            data[vEndPos][hEndPos - i] = '-';
                            styles[vEndPos][hEndPos - i] = style;
                        }
                    }
                    if (hEndPos < hStartPos) {
                        data[vEndPos][hEndPos + hLength - vLength] = '`';
                        styles[vEndPos][hEndPos + hLength - vLength] = style;
                    } else {
                        data[vEndPos][hEndPos - (hLength - vLength)] = '`';
                        styles[vEndPos][hEndPos - (hLength - vLength)] = style;
                    }
                } else {
                    drawDiagonal(vStartPos, hStartPos, vLength / 2, hEndPos > hStartPos);
                    drawDiagonal(vEndPos - vLength / 2 - 1, hEndPos + (hEndPos > hStartPos ? -1 : +1) * (vLength / 2 + 1), vLength / 2, hEndPos > hStartPos);
                    if (vLength % 2 == 1) {
                        int vMiddle = (vEndPos + vStartPos) / 2;
                        for (int i = vLength / 2 + 2; i < hLength - (vLength / 2 - 1); i++) {
                            if (hEndPos > hStartPos) {
                                data[vMiddle][hStartPos + i] = '-';
                                styles[vMiddle][hStartPos + i] = style;
                            } else {
                                data[vMiddle][hStartPos - i] = '-';
                                styles[vMiddle][hStartPos - i] = style;
                            }
                        }
                        if (hEndPos > hStartPos) {
                            data[vMiddle][hStartPos + vLength / 2 + 1] = '`';
                            styles[vMiddle][hStartPos + vLength / 2 + 1] = style;
                            data[vMiddle][hEndPos - vLength / 2 - 1] = '.';
                            styles[vMiddle][hEndPos - vLength / 2 - 1] = style;
                        } else {
                            data[vMiddle][hStartPos - vLength / 2 - 1] = '`';
                            styles[vMiddle][hStartPos - vLength / 2 - 1] = style;
                            data[vMiddle][hEndPos + vLength / 2 + 1] = '.';
                            styles[vMiddle][hEndPos + vLength / 2 + 1] = style;
                        }
                    } else {
                        int vMiddle = (vEndPos + vStartPos) / 2;
                        for (int i = vLength / 2 + 1; i < hLength - (vLength / 2 + 1) + 2; i++) {
                            if (hEndPos > hStartPos) {
                                data[vMiddle][hStartPos + i] = '_';
                                styles[vMiddle][hStartPos + i] = style;
                            } else {
                                data[vMiddle][hStartPos - i] = '_';
                                styles[vMiddle][hStartPos - i] = style;
                            }
                        }
                    }
                }
                return;
            }
        }
    }

    protected class CharacterElement implements CanvasElement {
        private final CharacterStyle style;

        private final char c;
        private final int  hPos;
        private final int  vPos;

        public CharacterElement(char c, CharacterStyle style, int hPos, int vPos) {
            this.c = c;
            this.style = style;
            this.hPos = hPos;
            this.vPos = vPos;
        }

        @Override
        public void apply() {
            data[vPos][hPos] = c;
            styles[vPos][hPos] = style;
        }
    }

    private char[][]           data;
    private CharacterStyle[][] styles;

    private int width;
    private int height;

    public TextCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        resetToSize(width, height);
    }

    protected void resetToSize(int width, int height) {
        this.height = height;
        this.width = width;
        data = new char[height][width];
        styles = new CharacterStyle[height][width];
        for (char[] row : data) {
            Arrays.fill(row, ' ');
        }
        CharacterStyle defStyle = CharacterStyle.standardStyle();
        for (CharacterStyle[] row : styles) {
            Arrays.fill(row, defStyle);
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean setWidth(int width) {
        return false;
    }

    public void addFrame(AsciiFrame box, int leftHorizontalPos, int topVerticalPos) {
        CanvasElement element = new AsciiFrameElement(box, leftHorizontalPos, topVerticalPos);
        element.apply();
    }

    public void addCharacter(CharacterStyle style, char c, int hPos, int vPos) {
        CanvasElement element = new CharacterElement(c, style, hPos, vPos);
        element.apply();
    }

    public void addLine(CharacterStyle style, int hStartPos, int vStartPos, int hEndPos, int vEndPos, LineDrawingStrategy strategy) {
        CanvasElement element = new LineElement(style, hStartPos, vStartPos, hEndPos, vEndPos, strategy);
        element.apply();
    }

    @Override
    public Iterator<TextRow> iterator() {
        return new Iterator<TextRow>() {
            private int vertPos = 0;

            @Override
            public boolean hasNext() {
                return vertPos < height;
            }

            @Override
            public TextRow next() {
                return new TextRow(data[vertPos], styles[vertPos++]);
            }
        };
    }
}

package net.zscript.ascii;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Iterator;

public class TextCanvasTest {

    void assertTheSame(AsciiFrame target, TextBox test) {
        assertThat(target.getHeight()).isEqualTo(test.getHeight());
        try {
            Iterator<TextRow> boxRows  = target.iterator();
            Iterator<TextRow> testRows = test.iterator();

            while (boxRows.hasNext()) {
                assertThat(testRows).withFailMessage(() -> "Box has too many rows").hasNext();
                TextRow boxRow  = boxRows.next();
                TextRow testRow = testRows.next();
                for (int i = 0; i < target.getWidth(); i++) {
                    assertThat(boxRow.charAt(i)).isEqualTo(testRow.charAt(i));
                    assertThat(boxRow.styleAt(i)).isEqualTo(testRow.styleAt(i));
                }
            }
            assertThat(testRows).withFailMessage(() -> "Box has too few rows").isExhausted();
        } catch (AssertionFailedError e) {
            StringBuilder b = new StringBuilder();
            b.append("\nexpected:\n");
            for (TextRow row : test) {
                b.append(row.toString(new AnsiCharacterStylePrinter()));
                b.append("\n");
            }
            b.append(" but was:\n");
            for (TextRow row : target) {
                b.append(row.toString(new AnsiCharacterStylePrinter()));
                b.append("\n");
            }
            throw new AssertionFailedError(b.toString(), e);
        }
    }

    @Test
    void shouldAddCharacter() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        TextCanvas     canvas = new TextCanvas(10, 10);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(CharacterStyle.standardStyle(), 'X', 5, 1);
        TextBox box = new TextBox("");
        box.setWidth(10);
        box.startNewLine().append("          ");
        box.startNewLine().append("     X    ");
        box.startNewLine().append("  ").setStyle(green).append('O');
        box.startNewLine().append(" ");
        box.startNewLine().append(" ");
        box.startNewLine().append(" ");
        box.startNewLine().append(" ");
        box.startNewLine().append(" ");
        box.startNewLine().append(" ");
        box.startNewLine().append(" ");
        assertThat(canvas.setWidth(10)).isFalse();
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddBox() {
        CharacterStyle green        = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        TextBox        contentToAdd = new TextBox(":");
        contentToAdd.setWidth(10);
        contentToAdd.append("AAAA");
        contentToAdd.startNewLine(1).append("BBB");
        contentToAdd.startNewLine(3).setStyle(green).append("LETS HAVE SOME WRAP!!!!!!!");

        TextCanvas canvas = new TextCanvas(12, 10);
        canvas.addFrame(contentToAdd, 2, 2);
        TextBox box = new TextBox("");
        box.setWidth(12);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  AAAA    ");
        box.startNewLine().append("  :BBB    ");
        box.startNewLine().append("  :::").setStyle(green).append("LETS");
        box.startNewLine().append("  :::").setStyle(green).append("HAVE");
        box.startNewLine().append("  :::").setStyle(green).append("SOME");
        box.startNewLine().append("  :::").setStyle(green).append("WRAP!!!");
        box.startNewLine().append("  :::").setStyle(green).append("!!!!");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddDiagonalLine() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        TextCanvas     canvas = new TextCanvas(10, 10);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 6, 6);
        canvas.addLine(green, 2, 2, 6, 6, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        TextBox box = new TextBox("");
        box.setWidth(10);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append('O');
        box.startNewLine().append("   ").setStyle(green).append('\\');
        box.startNewLine().append("    ").setStyle(green).append('\\');
        box.startNewLine().append("     ").setStyle(green).append('\\');
        box.startNewLine().append("      ").setStyle(green).append('O');
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddMinimalDiagonalLines() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        TextCanvas     canvas = new TextCanvas(10, 10);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 3, 3);
        canvas.addLine(green, 2, 2, 3, 3, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 1, 6);
        canvas.addLine(green, 2, 5, 1, 6, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        TextBox box = new TextBox("");
        box.setWidth(10);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append("'O");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append('O');
        box.startNewLine().append(" ").setStyle(green).append("O'");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddVerticalLines() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        TextCanvas     canvas = new TextCanvas(10, 10);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 2, 3);
        canvas.addLine(green, 2, 2, 2, 3, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 2, 8);
        canvas.addLine(green, 2, 8, 2, 5, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        TextBox box = new TextBox("");
        box.setWidth(10);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append("|");
        box.startNewLine().append("  ").setStyle(green).append("|");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddHorizontalLines() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        TextCanvas     canvas = new TextCanvas(10, 10);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 3, 2);
        canvas.addLine(green, 2, 2, 3, 2, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 6, 5);
        canvas.addLine(green, 2, 5, 6, 5, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        TextBox box = new TextBox("");
        box.setWidth(10);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("OO");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O---O");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddNearVerticalLinesHEnd() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        CharacterStyle none   = CharacterStyle.standardStyle();
        TextCanvas     canvas = new TextCanvas(15, 20);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 8, 2);
        canvas.addCharacter(green, 'O', 7, 4);
        canvas.addCharacter(green, 'O', 3, 4);
        canvas.addLine(green, 2, 2, 3, 4, new TextCanvas.LineDrawingStrategy(false, true, true, false));
        canvas.addLine(green, 8, 2, 7, 4, new TextCanvas.LineDrawingStrategy(false, true, true, false));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 6, 5);
        canvas.addCharacter(green, 'O', 4, 9);
        canvas.addLine(green, 2, 5, 4, 9, new TextCanvas.LineDrawingStrategy(false, true, true, false));
        canvas.addLine(green, 6, 5, 4, 9, new TextCanvas.LineDrawingStrategy(false, true, true, false));
        canvas.addCharacter(green, 'O', 2, 10);
        canvas.addCharacter(green, 'O', 10, 10);
        canvas.addCharacter(green, 'O', 6, 16);
        canvas.addLine(green, 2, 10, 6, 16, new TextCanvas.LineDrawingStrategy(false, true, true, false));
        canvas.addLine(green, 10, 10, 6, 16, new TextCanvas.LineDrawingStrategy(false, true, true, false));
        TextBox box = new TextBox("");
        box.setWidth(15);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("     ").setStyle(green).append('O');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append("   ").setStyle(green).append('/');
        box.startNewLine().append("   ").setStyle(green).append("O").setStyle(none).append("   ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("   ").setStyle(green).append('O');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append(" ").setStyle(green).append('/');
        box.startNewLine().append("    ").setStyle(green).append("|");
        box.startNewLine().append("    ").setStyle(green).append("|");
        box.startNewLine().append("    ").setStyle(green).append("O");
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("       ").setStyle(green).append('O');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append("     ").setStyle(green).append('/');
        box.startNewLine().append("    ").setStyle(green).append("\\").setStyle(none).append("   ").setStyle(green).append('/');
        box.startNewLine().append("     ").setStyle(green).append("\\").setStyle(none).append(" ").setStyle(green).append('/');
        box.startNewLine().append("      ").setStyle(green).append("|");
        box.startNewLine().append("      ").setStyle(green).append("|");
        box.startNewLine().append("      ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddNearVerticalLinesHMiddle() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        CharacterStyle none   = CharacterStyle.standardStyle();
        TextCanvas     canvas = new TextCanvas(15, 20);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 8, 2);
        canvas.addCharacter(green, 'O', 7, 4);
        canvas.addCharacter(green, 'O', 3, 4);
        canvas.addLine(green, 2, 2, 3, 4, new TextCanvas.LineDrawingStrategy(false, false, true, false));
        canvas.addLine(green, 8, 2, 7, 4, new TextCanvas.LineDrawingStrategy(false, false, true, false));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 6, 5);
        canvas.addCharacter(green, 'O', 4, 9);
        canvas.addLine(green, 2, 5, 4, 9, new TextCanvas.LineDrawingStrategy(false, false, true, false));
        canvas.addLine(green, 6, 5, 4, 9, new TextCanvas.LineDrawingStrategy(false, false, true, false));
        canvas.addCharacter(green, 'O', 2, 10);
        canvas.addCharacter(green, 'O', 12, 10);
        canvas.addCharacter(green, 'O', 7, 18);
        canvas.addLine(green, 2, 10, 7, 18, new TextCanvas.LineDrawingStrategy(false, false, true, false));
        canvas.addLine(green, 12, 10, 7, 18, new TextCanvas.LineDrawingStrategy(false, false, true, false));
        TextBox box = new TextBox("");
        box.setWidth(15);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("     ").setStyle(green).append('O');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append("   ").setStyle(green).append('/');
        box.startNewLine().append("   ").setStyle(green).append("O").setStyle(none).append("   ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("   ").setStyle(green).append('O');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append(" ").setStyle(green).append('/');
        box.startNewLine().append("   ").setStyle(green).append("|").setStyle(none).append(" ").setStyle(green).append('|');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append(" ").setStyle(green).append('/');
        box.startNewLine().append("    ").setStyle(green).append("O");
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("         ").setStyle(green).append('O');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append("       ").setStyle(green).append('/');
        box.startNewLine().append("    ").setStyle(green).append("\\").setStyle(none).append("     ").setStyle(green).append('/');
        box.startNewLine().append("     ").setStyle(green).append("|").setStyle(none).append("   ").setStyle(green).append('|');
        box.startNewLine().append("     ").setStyle(green).append("|").setStyle(none).append("   ").setStyle(green).append('|');
        box.startNewLine().append("     ").setStyle(green).append("|").setStyle(none).append("   ").setStyle(green).append('|');
        box.startNewLine().append("     ").setStyle(green).append("\\").setStyle(none).append("   ").setStyle(green).append('/');
        box.startNewLine().append("      ").setStyle(green).append("\\").setStyle(none).append(" ").setStyle(green).append('/');
        box.startNewLine().append("       ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddNearHorizontalLinesHStart() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        CharacterStyle none   = CharacterStyle.standardStyle();
        TextCanvas     canvas = new TextCanvas(15, 20);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 8, 2);
        canvas.addCharacter(green, 'O', 7, 4);
        canvas.addCharacter(green, 'O', 3, 4);
        canvas.addLine(green, 2, 2, 3, 4, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        canvas.addLine(green, 8, 2, 7, 4, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 6, 5);
        canvas.addCharacter(green, 'O', 4, 9);
        canvas.addLine(green, 2, 5, 4, 9, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        canvas.addLine(green, 6, 5, 4, 9, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        canvas.addCharacter(green, 'O', 2, 10);
        canvas.addCharacter(green, 'O', 10, 10);
        canvas.addCharacter(green, 'O', 6, 16);
        canvas.addLine(green, 2, 10, 6, 16, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        canvas.addLine(green, 10, 10, 6, 16, new TextCanvas.LineDrawingStrategy(true, false, true, false));
        TextBox box = new TextBox("");
        box.setWidth(15);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("     ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append("\\").setStyle(none).append("     ").setStyle(green).append('/');
        box.startNewLine().append("   ").setStyle(green).append("O").setStyle(none).append("   ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("   ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append("|").setStyle(none).append("   ").setStyle(green).append('|');
        box.startNewLine().append("  ").setStyle(green).append("|").setStyle(none).append("   ").setStyle(green).append('|');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append(" ").setStyle(green).append('/');
        box.startNewLine().append("    ").setStyle(green).append("O");
        box.startNewLine().append("  ").setStyle(green).append('O').setStyle(none).append("       ").setStyle(green).append('O');
        box.startNewLine().append("  ").setStyle(green).append("|").setStyle(none).append("       ").setStyle(green).append('|');
        box.startNewLine().append("  ").setStyle(green).append("|").setStyle(none).append("       ").setStyle(green).append('|');
        box.startNewLine().append("   ").setStyle(green).append("\\").setStyle(none).append("     ").setStyle(green).append('/');
        box.startNewLine().append("    ").setStyle(green).append("\\").setStyle(none).append("   ").setStyle(green).append('/');
        box.startNewLine().append("     ").setStyle(green).append("\\").setStyle(none).append(" ").setStyle(green).append('/');
        box.startNewLine().append("      ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddNearVerticalLinesTop() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        CharacterStyle none   = CharacterStyle.standardStyle();
        TextCanvas     canvas = new TextCanvas(15, 20);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 4, 3);
        canvas.addCharacter(green, 'O', 8, 2);
        canvas.addCharacter(green, 'O', 6, 3);
        canvas.addLine(green, 2, 2, 4, 3, new TextCanvas.LineDrawingStrategy(true, false, true, true));
        canvas.addLine(green, 8, 2, 6, 3, new TextCanvas.LineDrawingStrategy(true, false, true, true));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 8, 6);
        canvas.addCharacter(green, 'O', 8, 8);
        canvas.addCharacter(green, 'O', 2, 9);
        canvas.addLine(green, 2, 5, 8, 6, new TextCanvas.LineDrawingStrategy(true, false, true, true));
        canvas.addLine(green, 8, 8, 2, 9, new TextCanvas.LineDrawingStrategy(true, false, true, true));
        canvas.addCharacter(green, 'O', 2, 12);
        canvas.addCharacter(green, 'O', 8, 14);
        canvas.addCharacter(green, 'O', 8, 16);
        canvas.addCharacter(green, 'O', 2, 18);
        canvas.addLine(green, 2, 12, 8, 14, new TextCanvas.LineDrawingStrategy(true, false, true, true));
        canvas.addLine(green, 8, 16, 2, 18, new TextCanvas.LineDrawingStrategy(true, false, true, true));
        TextBox box = new TextBox("");
        box.setWidth(15);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O.").setStyle(none).append("   ").setStyle(green).append(".O");
        box.startNewLine().append("   ").setStyle(green).append("`O").setStyle(none).append(" ").setStyle(green).append("O`");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O----.");
        box.startNewLine().append("       ").setStyle(green).append("`O");
        box.startNewLine().append("          ");
        box.startNewLine().append("   ").setStyle(green).append(".----O");
        box.startNewLine().append("  ").setStyle(green).append("O`");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O---.");
        box.startNewLine().append("       ").setStyle(green).append("\\");
        box.startNewLine().append("        ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("    ").setStyle(green).append(".---O");
        box.startNewLine().append("   ").setStyle(green).append("/");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddNearVerticalLinesBottom() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        CharacterStyle none   = CharacterStyle.standardStyle();
        TextCanvas     canvas = new TextCanvas(15, 20);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 4, 3);
        canvas.addCharacter(green, 'O', 8, 2);
        canvas.addCharacter(green, 'O', 6, 3);
        canvas.addLine(green, 2, 2, 4, 3, new TextCanvas.LineDrawingStrategy(true, false, false, true));
        canvas.addLine(green, 8, 2, 6, 3, new TextCanvas.LineDrawingStrategy(true, false, false, true));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 8, 6);
        canvas.addCharacter(green, 'O', 8, 8);
        canvas.addCharacter(green, 'O', 2, 9);
        canvas.addLine(green, 2, 5, 8, 6, new TextCanvas.LineDrawingStrategy(true, false, false, true));
        canvas.addLine(green, 8, 8, 2, 9, new TextCanvas.LineDrawingStrategy(true, false, false, true));
        canvas.addCharacter(green, 'O', 2, 12);
        canvas.addCharacter(green, 'O', 8, 14);
        canvas.addCharacter(green, 'O', 8, 16);
        canvas.addCharacter(green, 'O', 2, 18);
        canvas.addLine(green, 2, 12, 8, 14, new TextCanvas.LineDrawingStrategy(true, false, false, true));
        canvas.addLine(green, 8, 16, 2, 18, new TextCanvas.LineDrawingStrategy(true, false, false, true));
        TextBox box = new TextBox("");
        box.setWidth(15);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O").setStyle(none).append("     ").setStyle(green).append("O");
        box.startNewLine().append("   ").setStyle(green).append("`O").setStyle(none).append(" ").setStyle(green).append("O`");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("   ").setStyle(green).append("`----O");
        box.startNewLine().append("          ");
        box.startNewLine().append("        ").setStyle(green).append("O");
        box.startNewLine().append("  ").setStyle(green).append("O----`");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("   ").setStyle(green).append("\\");
        box.startNewLine().append("    ").setStyle(green).append("`---O");
        box.startNewLine().append("          ");
        box.startNewLine().append("        ").setStyle(green).append("O");
        box.startNewLine().append("       ").setStyle(green).append("/");
        box.startNewLine().append("  ").setStyle(green).append("O---`");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

    @Test
    void shouldAddNearVerticalLinesMiddle() {
        CharacterStyle green  = new CharacterStyle(TextColor.GREEN, TextColor.RED, true);
        CharacterStyle none   = CharacterStyle.standardStyle();
        TextCanvas     canvas = new TextCanvas(15, 31);
        canvas.addCharacter(green, 'O', 2, 2);
        canvas.addCharacter(green, 'O', 4, 3);
        canvas.addCharacter(green, 'O', 8, 2);
        canvas.addCharacter(green, 'O', 6, 3);
        canvas.addLine(green, 2, 2, 4, 3, new TextCanvas.LineDrawingStrategy(true, false, false, false));
        canvas.addLine(green, 8, 2, 6, 3, new TextCanvas.LineDrawingStrategy(true, false, false, false));
        canvas.addCharacter(green, 'O', 2, 5);
        canvas.addCharacter(green, 'O', 8, 6);
        canvas.addCharacter(green, 'O', 8, 8);
        canvas.addCharacter(green, 'O', 2, 9);
        canvas.addLine(green, 2, 5, 8, 6, new TextCanvas.LineDrawingStrategy(true, false, false, false));
        canvas.addLine(green, 8, 8, 2, 9, new TextCanvas.LineDrawingStrategy(true, false, false, false));
        canvas.addCharacter(green, 'O', 2, 12);
        canvas.addCharacter(green, 'O', 8, 14);
        canvas.addCharacter(green, 'O', 8, 16);
        canvas.addCharacter(green, 'O', 2, 18);
        canvas.addLine(green, 2, 12, 8, 14, new TextCanvas.LineDrawingStrategy(true, false, false, false));
        canvas.addLine(green, 8, 16, 2, 18, new TextCanvas.LineDrawingStrategy(true, false, false, false));
        canvas.addCharacter(green, 'O', 2, 20);
        canvas.addCharacter(green, 'O', 8, 23);
        canvas.addCharacter(green, 'O', 8, 25);
        canvas.addCharacter(green, 'O', 2, 28);
        canvas.addLine(green, 2, 20, 8, 23, new TextCanvas.LineDrawingStrategy(true, false, false, false));
        canvas.addLine(green, 8, 25, 2, 28, new TextCanvas.LineDrawingStrategy(true, false, false, false));
        TextBox box = new TextBox("");
        box.setWidth(15);
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O_").setStyle(none).append("   ").setStyle(green).append("_O");
        box.startNewLine().append("    ").setStyle(green).append("O").setStyle(none).append(" ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O_____");
        box.startNewLine().append("        ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("   ").setStyle(green).append("_____O");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("   ").setStyle(green).append("`---.");
        box.startNewLine().append("        ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("        ").setStyle(green).append("O");
        box.startNewLine().append("   ").setStyle(green).append(".---`");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("   ").setStyle(green).append("\\___");
        box.startNewLine().append("       ").setStyle(green).append("\\");
        box.startNewLine().append("        ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("        ").setStyle(green).append("O");
        box.startNewLine().append("    ").setStyle(green).append("___/");
        box.startNewLine().append("   ").setStyle(green).append("/");
        box.startNewLine().append("  ").setStyle(green).append("O");
        box.startNewLine().append("          ");
        box.startNewLine().append("          ");
        assertTheSame(canvas, box);
    }

}

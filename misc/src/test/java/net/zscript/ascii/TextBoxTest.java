package net.zscript.ascii;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Iterator;

public class TextBoxTest {

    void assertTheSame(TextBox box, TextBox test) {
        assertThat(box.getHeight()).isEqualTo(test.getHeight());
        try {
            Iterator<TextRow> boxRows  = box.iterator();
            Iterator<TextRow> testRows = test.iterator();

            while (boxRows.hasNext()) {
                assertThat(testRows).withFailMessage(() -> "Box has too many rows").hasNext();
                TextRow boxRow  = boxRows.next();
                TextRow testRow = testRows.next();
                for (int i = 0; i < box.getWidth(); i++) {
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
            for (TextRow row : box) {
                b.append(row.toString(new AnsiCharacterStylePrinter()));
                b.append("\n");
            }
            throw new AssertionFailedError(b.toString(), e);
        }
    }

    @Test
    void shouldGenerateBasicCorrectly() {
        CharacterStyle blank = CharacterStyle.standardStyle();
        TextBox        box   = new TextBox("  ");
        box.setWidth(100);
        box.append("Test data");
        box.startNewLine(1);
        box.append("More data");
        box.startNewLine();
        box.append("Yet more data");
        box.startNewLine(0);
        CharacterStyle red = new CharacterStyle(TextColor.RED, TextColor.RED, true);
        box.append("non-red ");
        box.setStyle(red);
        box.append("Red-on-red data");
        Iterator<TextRow> data = box.iterator();

        TextRow current = data.next();

        int i = 0;
        for (char c : "Test data".toCharArray()) {
            assertThat(current.charAt(i)).isEqualTo(c);
            assertThat(current.styleAt(i)).isEqualTo(blank);
            i++;
        }
        for (; i < 100; i++) {
            assertThat(current.charAt(i)).isEqualTo(' ');
            assertThat(current.styleAt(i)).isEqualTo(blank);
        }

        current = data.next();
        i = 0;
        for (char c : "  More data".toCharArray()) {
            assertThat(current.charAt(i)).isEqualTo(c);
            assertThat(current.styleAt(i)).isEqualTo(blank);
            i++;
        }
        for (; i < 100; i++) {
            assertThat(current.charAt(i)).isEqualTo(' ');
            assertThat(current.styleAt(i)).isEqualTo(blank);
        }

        current = data.next();
        i = 0;
        for (char c : "  Yet more data".toCharArray()) {
            assertThat(current.charAt(i)).isEqualTo(c);
            assertThat(current.styleAt(i)).isEqualTo(blank);
            i++;
        }
        for (; i < 100; i++) {
            assertThat(current.charAt(i)).isEqualTo(' ');
            assertThat(current.styleAt(i)).isEqualTo(blank);
        }

        current = data.next();
        i = 0;
        for (char c : "non-red ".toCharArray()) {
            assertThat(current.charAt(i)).isEqualTo(c);
            assertThat(current.styleAt(i)).isEqualTo(blank);
            i++;
        }
        for (char c : "Red-on-red data".toCharArray()) {
            assertThat(current.charAt(i)).isEqualTo(c);
            assertThat(current.styleAt(i)).isEqualTo(red);
            i++;
        }
        for (; i < 100; i++) {
            assertThat(current.charAt(i)).isEqualTo(' ');
            assertThat(current.styleAt(i)).isEqualTo(blank);
        }
        assertThat(data).isExhausted();
    }

    @Test
    void shouldDoBasicWrapping() {
        TextBox box    = new TextBox("  ");
        TextBox tester = new TextBox("");

        box.setWidth(20);
        tester.setWidth(100);
        box.append("Some text which should wrap nicely");
        tester.append("Some text which").startNewLine();
        tester.append("should wrap nicely");
        assertTheSame(box, tester);
    }

    @Test
    void shouldDoBasicWrappingWithIndentOnWrap() {
        TextBox box    = new TextBox("");
        TextBox tester = new TextBox("");
        box.setIndentString("--");
        box.setIndentOnWrap(2);
        box.setWidth(20);
        tester.setWidth(100);
        box.append("Some text which should wrap nicely");
        tester.append("Some text which").startNewLine();
        tester.append("----should wrap").startNewLine();
        tester.append("----nicely");
        assertTheSame(box, tester);
    }

    @Test
    void shouldDoWrapOnNonLetterDigit() {
        TextBox box    = new TextBox("  ");
        TextBox tester = new TextBox("");
        box.setIndentOnWrap(2);
        box.setWidth(20);
        tester.setWidth(100);
        box.append("Some.text.which.should.wrap.nicely");
        tester.append("Some.text.which.").startNewLine();
        tester.append("    should.wrap.").startNewLine();
        tester.append("    nicely");
        assertTheSame(box, tester);
    }

    @Test
    void shouldHyphenate() {
        TextBox box    = new TextBox("  ");
        TextBox tester = new TextBox("");
        box.setIndentOnWrap(2);
        box.setWidth(20);
        tester.setWidth(100);
        box.append("SomeTextWhichShouldWrapNicely");
        tester.append("SomeTextWhichShould-").startNewLine();
        tester.append("    WrapNicely");
        assertTheSame(box, tester);
    }

    @Test
    void shouldPerformAllWrappingTypes() {
        TextBox box    = new TextBox("  ");
        TextBox tester = new TextBox("");
        box.setIndentOnWrap(1);
        box.setWidth(20);
        tester.setWidth(100);
        box.append("This should be easy to wrap, where.this.is.much.harder.but.still.possible.if.barely AndHereWeHaveToResortToHyphens");
        tester.append("This should be easy ").startNewLine();
        tester.append("  to wrap,").startNewLine();
        tester.append("  where.this.is.").startNewLine();
        tester.append("  much.harder.but.").startNewLine();
        tester.append("  still.possible.if.").startNewLine();
        tester.append("  barely").startNewLine();
        tester.append("  AndHereWeHaveToRe-").startNewLine();
        tester.append("  sortToHyphens");
        assertTheSame(box, tester);
    }

    @Test
    void shouldWrapColors() {
        CharacterStyle red    = new CharacterStyle(TextColor.RED, TextColor.BLUE, true);
        TextBox        box    = new TextBox("  ");
        TextBox        tester = new TextBox("");
        box.setIndentOnWrap(1);
        box.setWidth(20);
        tester.setWidth(100);
        box.setStyle(red);
        box.append("Some text which should wrap at least a little");
        tester.startNewLine().setStyle(red).append("Some text which");
        tester.startNewLine().append("  ").setStyle(red).append("should wrap at");
        tester.startNewLine().append("  ").setStyle(red).append("least a little");
        assertTheSame(box, tester);
    }

    @Test
    void shouldStyleIndent() {
        CharacterStyle red    = new CharacterStyle(TextColor.RED, TextColor.PURPLE, true);
        TextBox        box    = new TextBox("  ");
        TextBox        tester = new TextBox("");
        box.setIndentOnWrap(1);
        box.setWidth(20);
        tester.setWidth(100);
        box.setIndentString("  ", red);
        box.append("Some text which should wrap at least a little");
        tester.startNewLine().append("Some text which");
        tester.startNewLine().setStyle(red).append("  ").setStyle(CharacterStyle.standardStyle()).append("should wrap at");
        tester.startNewLine().setStyle(red).append("  ").setStyle(CharacterStyle.standardStyle()).append("least a little");
        assertTheSame(box, tester);
    }

    @Test
    void shouldChangeWidth() {
        TextBox box    = new TextBox("  ");
        TextBox tester = new TextBox("");
        box.setIndentOnWrap(1);
        box.setWidth(100);
        tester.setWidth(100);
        box.append("This should be easy to wrap, where.this.is.much.harder.but.still.possible.if.barely AndHereWeHaveToResortToHyphens");
        tester.append("This should be easy ").startNewLine();
        tester.append("  to wrap,").startNewLine();
        tester.append("  where.this.is.").startNewLine();
        tester.append("  much.harder.but.").startNewLine();
        tester.append("  still.possible.if.").startNewLine();
        tester.append("  barely").startNewLine();
        tester.append("  AndHereWeHaveToRe-").startNewLine();
        tester.append("  sortToHyphens");
        box.setWidth(20);
        assertThat(box.setWidth(5)).isFalse();
        assertTheSame(box, tester);
    }

    @Test
    void shouldHandleVariedAppends() {
        TextBox box    = new TextBox("::");
        TextBox tester = new TextBox("");
        box.setIndentOnWrap(1);
        box.setWidth(10);
        tester.setWidth(100);
        box.startNewLine().append(100000);
        tester.startNewLine().append("100000");
        box.startNewLine().append(1023.5);
        tester.startNewLine().append("1023.5");
        box.startNewLine().append(1023.5f);
        tester.startNewLine().append("1023.5");
        box.startNewLine().append(21474098543342L);
        tester.startNewLine().append("214740985-").startNewLine().append("::43342");
        box.startNewLine().append('A');
        tester.startNewLine().append("A");
        box.startNewLine().append(true);
        tester.startNewLine().append("true");
        box.startNewLine().append("Some text".toCharArray());
        tester.startNewLine().append("Some text");
        box.startNewLine().append("Some demo text".toCharArray(), 5, 9);
        tester.startNewLine().append("demo text");
        StringBuilder b = new StringBuilder();
        b.append("More text");
        box.startNewLine().append(b);
        tester.startNewLine().append("More text");
        box.startNewLine().append(b, 5, 9);
        tester.startNewLine().append("text");
        box.startNewLine().append(new StringBuffer("AAAAAAA"));
        tester.startNewLine().append("AAAAAAA");
        box.startNewLine().append(Integer.valueOf(27));
        tester.startNewLine().append("27");
        box.startNewLine().appendHex(0x24abdf, 0);
        tester.startNewLine().append("24abdf");
        box.startNewLine().appendHex(0x2, 0);
        tester.startNewLine().append("2");
        box.startNewLine().appendHex(0x0, 0);
        tester.startNewLine().append("");
        box.startNewLine().appendHex(0x2, 4);
        tester.startNewLine().append("0002");
        assertTheSame(box, tester);
    }

}

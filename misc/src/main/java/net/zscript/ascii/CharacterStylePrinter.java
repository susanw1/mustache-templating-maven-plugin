package net.zscript.ascii;

public interface CharacterStylePrinter {

    String apply(CharacterStyle style);

    String applyDiff(CharacterStyle prev, CharacterStyle next);

    String cancel(CharacterStyle style);
}

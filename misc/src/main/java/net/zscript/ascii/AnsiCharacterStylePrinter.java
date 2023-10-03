package net.zscript.ascii;

public class AnsiCharacterStylePrinter implements CharacterStylePrinter {
    public static final String ANSI_RESET = "\u001B[0";
    public static final String ANSI_BOLD  = "\u001B[1";
    public static final String ANSI_FG    = "\u001B[3";
    public static final String ANSI_BG    = "\u001B[4";

    public static final String ANSI_BLACK  = "0";
    public static final String ANSI_RED    = "1";
    public static final String ANSI_GREEN  = "2";
    public static final String ANSI_YELLOW = "3";
    public static final String ANSI_BLUE   = "4";
    public static final String ANSI_PURPLE = "5";
    public static final String ANSI_CYAN   = "6";
    public static final String ANSI_WHITE  = "7";

    public static final String ANSI_DEFAULT = "9";

    public static final String ANSI_END_ESCAPE = "m";

    private String getAnsiFromColor(TextColor color) {
        switch (color) {
        case BLACK:
            return ANSI_BLACK;
        case RED:
            return ANSI_RED;
        case GREEN:
            return ANSI_GREEN;
        case YELLOW:
            return ANSI_YELLOW;
        case BLUE:
            return ANSI_BLUE;
        case PURPLE:
            return ANSI_PURPLE;
        case CYAN:
            return ANSI_CYAN;
        case WHITE:
            return ANSI_WHITE;
        case DEFAULT:
            return ANSI_DEFAULT;
        default:
            throw new IllegalStateException("Unrecognised color: " + color.name());
        }
    }

    @Override
    public String apply(CharacterStyle style) {
        String result = "";
        if (style.getFGColor() != TextColor.DEFAULT) {
            result += ANSI_FG + getAnsiFromColor(style.getFGColor()) + ANSI_END_ESCAPE;
        }
        if (style.getBGColor() != TextColor.DEFAULT) {
            result += ANSI_BG + getAnsiFromColor(style.getBGColor()) + ANSI_END_ESCAPE;
        }
        if (style.isBold()) {
            result += ANSI_BOLD + ANSI_END_ESCAPE;
        }
        return result;
    }

    @Override
    public String applyDiff(CharacterStyle prev, CharacterStyle next) {
        if (prev.isBold() && !next.isBold()) {
            return ANSI_RESET + ANSI_END_ESCAPE + apply(next);
        }
        String result = "";
        if (next.isBold() && !prev.isBold()) {
            result += ANSI_BOLD + ANSI_END_ESCAPE;
        }
        if (next.getFGColor() != prev.getFGColor()) {
            result += ANSI_FG + getAnsiFromColor(next.getFGColor()) + ANSI_END_ESCAPE;
        }
        if (next.getBGColor() != prev.getBGColor()) {
            result += ANSI_BG + getAnsiFromColor(next.getBGColor()) + ANSI_END_ESCAPE;
        }
        return result;
    }

    @Override
    public String cancel(CharacterStyle style) {
        if (style.isBold()) {
            return ANSI_RESET + ANSI_END_ESCAPE;
        }
        String result = "";
        if (style.getFGColor() != TextColor.DEFAULT) {
            result += ANSI_FG + ANSI_DEFAULT + ANSI_END_ESCAPE;
        }
        if (style.getBGColor() != TextColor.DEFAULT) {
            result += ANSI_BG + ANSI_DEFAULT + ANSI_END_ESCAPE;
        }
        return result;
    }
}

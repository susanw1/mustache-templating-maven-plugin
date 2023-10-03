package net.zscript.ascii;

import java.util.Objects;

public class CharacterStyle {
    private final TextColor color;
    private final TextColor background;

    private final boolean isBold;

    public CharacterStyle(TextColor color, TextColor background, boolean isBold) {
        this.color = color;
        this.background = background;
        this.isBold = isBold;
    }

    public static CharacterStyle standardStyle() {
        return new CharacterStyle(TextColor.DEFAULT, TextColor.DEFAULT, false);
    }

    public TextColor getFGColor() {
        return color;
    }

    public TextColor getBGColor() {
        return background;
    }

    public boolean isBold() {
        return isBold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CharacterStyle that = (CharacterStyle) o;
        return isBold == that.isBold && color == that.color && background == that.background;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, background, isBold);
    }
}

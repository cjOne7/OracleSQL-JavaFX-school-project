package sample.enums;

public enum StylesEnum {
    EMPTY_STRING(""),
    ERROR_STYLE("-fx-border-color: red; -fx-border-radius: 5;"),
    FONT_STYLE("-fx-font: 15px \"Century Gothic\";"),
    COMBO_BOX_STYLE("-fx-font: 12px \"Castellar\"; -fx-background-color: #F5F5DC; -fx-border-color:  #50C878; -fx-border-radius: 5;");

    private String style;

    StylesEnum(final String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }
}

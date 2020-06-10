package sample.enums;

public enum CommentColumns {
    COMMENT_TEXT(1),
    COMMENT_ID(2),
    COMMENT_CREATER_ID(3);

    private int columnIndex;

    CommentColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() { return columnIndex; }
}

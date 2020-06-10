package sample.enums;

public enum CommentDiscussionColumns {
    DISCUSSION_DISCUSSION_ID(1),
    THE_COMMENT_COMMENT_ID(2);

    private int columnIndex;

    CommentDiscussionColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() { return columnIndex; }
}

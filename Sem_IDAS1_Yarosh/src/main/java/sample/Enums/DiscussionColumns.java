package sample.enums;

public enum DiscussionColumns {
    TITLE(1),
    DISCUSSION_ID(2),
    DISCUSSION_CREATER_ID(3),
    STY_MTRL_STUDY_MATERIAL_ID(4);

    private int columnIndex;

    DiscussionColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}

package sample.enums;

public enum UserSubjectColumns {
    USER_USER_ID(1),
    SUBJECT_SUBJECT_ID(2);

    private int columnIndex;

    UserSubjectColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() { return columnIndex; }
}

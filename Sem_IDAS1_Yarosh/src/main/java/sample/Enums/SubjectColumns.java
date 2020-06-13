package sample.enums;

public enum SubjectColumns {
    SUBJECT_NAME(1),
    ABBREVIATION(2),
    CREDITS(3),
    SEMESTER(4),
    DESCRIPTION(5),
    YEAR(6),
    SUBJECT_ID(7);

    private int columnIndex;

    SubjectColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}

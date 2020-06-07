package sample.enums;

public enum StudyMatColumns {
    FILE_NAME(1, "FILE_NAME"),
    THE_FILE(2, "THE_FILE"),
    FILE_TYPE(3, "FILE_TYPE"),
    DATE_OF_CREATION(4, "DATE_OF_CREATION"),
    CREATER(5, "CREATER"),
    NUMBER_OF_PAGES(6, "NUMBER_OF_PAGES"),
    DATE_OF_CHANGES(7, "DATE_OF_CHANGES"),
    CHANGER(8, "CHANGER"),
    DESCRIPTION(9, "DESCRIPTION"),
    SUBJECT_SUBJECT_ID(10, "SUBJECT_SUBJECT_ID"),
    STUDY_MATERIAL_ID(11, "STUDY_MATERIAL_ID");

    private int columnIndex;
    private String columnName;

    StudyMatColumns(final int columnIndex, final String columnName) {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getColumnName() {
        return columnName;
    }
}

package sample.enums;

public enum StudyMatCtgyColumns {
    CATEGORY_CATEGORY_ID(1),
    STY_MTRL_STY_MTRL_ID(2);

    private int columnIndex;

    StudyMatCtgyColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() { return columnIndex; }
}

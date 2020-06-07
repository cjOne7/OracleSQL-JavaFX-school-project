package sample.enums;

public enum CategoryColumns {
    CATEGORY_NAME(1),
    DESCRIPTION(2),
    CATEGORY_ID(3);

    private int columnIndex;

    CategoryColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() { return columnIndex; }
}

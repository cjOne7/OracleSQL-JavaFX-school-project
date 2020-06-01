package sample.enums;

public enum ElsaUserColumns {
    NAME(1),
    SURNAME(2),
    LOGIN(3),
    PASSWORD(4),
    EMAIL(5),
    TELEPHONE(6),
    ABOUT(7),
    IMAGE(8),
    ROLE_ID(9),
    USER_ID(10);

    private int columnIndex;

    ElsaUserColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}

package sample.enums;

public enum QuestionCatColumns {
    QUESTION_CAT_NAME(1),
    DESCRIPTION(2),
    QUESTIONS_CAT_ID(3);

    private int columnIndex;

    QuestionCatColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}

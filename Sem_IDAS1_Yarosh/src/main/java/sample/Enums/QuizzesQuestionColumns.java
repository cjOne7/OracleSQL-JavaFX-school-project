package sample.enums;

public enum QuizzesQuestionColumns {
    QUIZ_QUIZ_ID(1),
    QUESTION_QUESTION_ID(2);

    private int columnIndex;

    QuizzesQuestionColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() { return columnIndex; }
}

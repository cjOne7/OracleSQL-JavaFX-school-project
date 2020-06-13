package sample.enums;

public enum QuizColumns {
    QUIZ_NAME(1),
    NUMBER_OF_QUESTIONS(2),
    DESCRIPTION(3),
    STY_MTRL_STY_MTRL_ID(4),
    QUIZ_ID(5),
    USER_CREATER_ID(6);

    private int columnIndex;

    QuizColumns(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}

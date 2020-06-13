package sample.dbtableclasses;

import org.jetbrains.annotations.NotNull;
import sample.databasemanager.DbManager;
import sample.enums.QuestionCatColumns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Question {
    private int questionId;
    private int points;
    private String questionText;
    private int questionCatId;

    public Question(final int questionId,
                    final int points,
                    final String questionText,
                    final int questionCatId) {
        this.questionId = questionId;
        this.points = points;
        this.questionText = questionText;
        this.questionCatId = questionCatId;
    }

    public int getQuestionId() {
        return questionId;
    }

    @NotNull
    private String getCategoryName() {
        final DbManager dbManager = new DbManager();
        final String selectQuery = "SELECT QUESTION_CAT_NAME FROM ST58310.QUESTIONS_CTGY WHERE QUESTIONS_CAT_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, questionCatId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(QuestionCatColumns.QUESTION_CAT_NAME.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String toString() {
        return "Question's text: " + questionText +
                ", points: " + points +
                ", question category's name: " + getCategoryName();
    }
}

package sample.dbtableclasses;

import org.jetbrains.annotations.NotNull;
import sample.databasemanager.DbManager;
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.enums.StudyMatColumns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Quiz {
    private int quizId;
    private String quizName;
    private int numberOfQuestions;
    private String description;
    private int studyMatId;
    private int userCreaterId;

    public Quiz(final int quizId,
                final String quizName,
                final int numberOfQuestions,
                final String description,
                final int studyMatId,
                final int userCreaterId) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.numberOfQuestions = numberOfQuestions;
        this.description = description;
        this.studyMatId = studyMatId;
        this.userCreaterId = userCreaterId;
    }

    public Quiz(final int quizId,
                final String quizName,
                final int numberOfQuestions,
                final String description,
                final int studyMatId) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.numberOfQuestions = numberOfQuestions;
        this.description = description;
        this.studyMatId = studyMatId;
    }

    public Quiz(final int quizId,
                final String quizName,
                final int numberOfQuestions,
                final String description) {
        this.quizId = quizId;
        this.quizName = quizName;
        this.numberOfQuestions = numberOfQuestions;
        this.description = description;
    }

    public int getQuizId() {
        return quizId;
    }

    public String getDescription() {
        return description;
    }

    public int getStudyMatId() {
        return studyMatId;
    }

    public int getUserCreaterId() {
        return userCreaterId;
    }

    @NotNull
    private String getStudyMatName() {
        final DbManager dbManager = new DbManager();
        final String selectQuery = "SELECT FILE_NAME, FILE_TYPE FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, studyMatId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final String fileName = resultSet.getString(StudyMatColumns.FILE_NAME.getColumnName());
                final String fileType = resultSet.getString(StudyMatColumns.FILE_TYPE.getColumnName());
                return fileName + '.' + fileType;
            } else {
                return "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @NotNull
    private String getCreaterName() {
        final DbManager dbManager = new DbManager();
        final String selectQuery = "SELECT NAME, SURNAME, ROLE_ID FROM ST58310.ELSA_USER WHERE USER_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, userCreaterId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
                final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
                final String role = Role.getRole(resultSet.getInt(ElsaUserColumns.ROLE_ID.toString())).toString();
                return name + " " + surname + ", role: " + role;
            } else {
                return "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String toString() {
        return "Quiz's name: " + quizName +
                (numberOfQuestions == 0 ? "" : ", number of questions: " + numberOfQuestions) +
                (studyMatId == 0 ? "" : ", study material ID: " + getStudyMatName()) +
                (userCreaterId == 0 ? "" : ", user creater ID: " + getCreaterName()) +
                (description == null ? "" : ", description: " + description);
    }
}

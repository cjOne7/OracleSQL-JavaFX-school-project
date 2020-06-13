package sample.dbtableclasses;

import org.jetbrains.annotations.NotNull;
import sample.databasemanager.DbManager;
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.enums.StudyMatColumns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Discussion {
    private int discussionId;
    private String title;
    private int discussionCreaterId;
    private int studyMaterialId;

    public Discussion(final int discussionId, final String title, final int discussionCreaterId, final int studyMaterialId) {
        this.discussionId = discussionId;
        this.title = title;
        this.discussionCreaterId = discussionCreaterId;
        this.studyMaterialId = studyMaterialId;
    }

    public Discussion(final int commentId, final String title, final int studyMaterialId) {
        this.discussionId = commentId;
        this.title = title;
        this.studyMaterialId = studyMaterialId;
    }

    public int getDiscussionId() {
        return discussionId;
    }

    public String getTitle() {
        return title;
    }

    public int getDiscussionCreaterId() {
        return discussionCreaterId;
    }

    @NotNull
    private String getStudyMatName() {
        final DbManager dbManager = new DbManager();
        final String selectQuery = "SELECT FILE_NAME, FILE_TYPE FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, studyMaterialId);
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
            preparedStatement.setInt(1, discussionCreaterId);
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
        return "Title: " + title +
                (discussionCreaterId == 0 ? "" : ", creator: " + getCreaterName()) +
                (studyMaterialId == 0 ? "" : ", study material ID: " + getStudyMatName());
    }
}

package sample.dbtableclasses;

import org.jetbrains.annotations.NotNull;
import sample.controllers.MainWindowController;
import sample.controllers.userwindows.adminscontrollers.AdminController;
import sample.databasemanager.DbManager;
import sample.enums.ElsaUserColumns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Comment {
    private int commentId;
    private String commentText;
    private int commentCreaterId;

    public Comment(final int commentId, final String commentText, final int commentCreaterId) {
        this.commentId = commentId;
        this.commentText = commentText;
        this.commentCreaterId = commentCreaterId;
    }

    public Comment(final int commentId, final String commentText) {
        this.commentId = commentId;
        this.commentText = commentText;
    }

    public int getCommentId() {
        return commentId;
    }

    public String getCommentText() {
        return commentText;
    }

    public int getCommentCreaterId() {
        return commentCreaterId;
    }

    @NotNull
    private String getUserName() {
        final DbManager dbManager = new DbManager();
        final String selectQuery = "SELECT NAME, SURNAME, LOGIN FROM ST58310.ELSA_USER WHERE USER_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, commentCreaterId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
                final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
                final String login = resultSet.getString(ElsaUserColumns.LOGIN.toString());
                return name + " " + surname + " (" + login + ")";
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
        return getUserName() + "\n" + commentText;
    }
}

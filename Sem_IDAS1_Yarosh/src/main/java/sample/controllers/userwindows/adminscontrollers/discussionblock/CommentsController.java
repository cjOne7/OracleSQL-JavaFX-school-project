package sample.controllers.userwindows.adminscontrollers.discussionblock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.controllers.MainWindowController;
import sample.controllers.userwindows.adminscontrollers.AdminController;
import sample.controllers.userwindows.teacherscontrollers.TeacherWindowController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.*;
import sample.enums.*;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CommentsController implements Initializable {

    private static final int MAX_COMMENT_LENGTH = 500;

    private final DbManager dbManager = new DbManager();
    private ObservableList<Comment> comments = FXCollections.observableArrayList();

    @FXML
    private TextArea commentTextArea;
    @FXML
    private ListView<Comment> commentsListView;
    @FXML
    private Label leftCharactersLabel;
    @FXML
    private Label staticMessageLabel;
    @FXML
    private Button deleteCommentBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        commentsListView.setItems(comments);
        commentsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());

        commentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                leftCharactersLabel.setText("");
                staticMessageLabel.setVisible(false);
            } else {
                staticMessageLabel.setVisible(true);
                leftCharactersLabel.setText(500 - newValue.length() + "");
            }
            if (newValue.length() == MAX_COMMENT_LENGTH + 1) {
                commentTextArea.setText(oldValue);
            }
        });

        try {
            final String selectQuery = "SELECT * FROM ST58310.THE_COMMENT WHERE COMMENT_ID IN (SELECT THE_COMMENT_COMMENT_ID FROM ST58310.COMMENT_DISCUSSION WHERE DISCUSSION_DISCUSSION_ID = ?) ORDER BY COMMENT_ID";
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, DiscussionController.discussionId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int commentId = resultSet.getInt(CommentColumns.COMMENT_ID.toString());
                final String commentText = resultSet.getString(CommentColumns.COMMENT_TEXT.toString());
                final int commentCreaterId = resultSet.getInt(CommentColumns.COMMENT_CREATER_ID.toString());
                final Comment comment = new Comment(commentId, commentText, commentCreaterId);
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void sentComment(ActionEvent event) {
        final String commentText = commentTextArea.getText().trim();
        if (commentText.isEmpty()) {
            Main.callAlertWindow("Information", "Empty text can't be send!", Alert.AlertType.WARNING, "/images/information_icon.png");
        } else {
            try {
                final String insertQuery = "INSERT INTO ST58310.THE_COMMENT (COMMENT_TEXT, COMMENT_CREATER_ID) VALUES (?,?)";
                PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
                preparedStatement.setString(1, commentText);
                int userId = AdminController.curUserId;
                if (userId == 0) {
                    userId = MainWindowController.curUserId;
                }
                preparedStatement.setInt(2, userId);
                preparedStatement.execute();

                final String selectQuery = "SELECT MAX(COMMENT_ID) AS COMMENT_ID FROM ST58310.THE_COMMENT";
                preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
                final ResultSet resultSet = preparedStatement.executeQuery();
                int commentId = 0;
                if (resultSet.next()) {
                    commentId = resultSet.getInt(CommentColumns.COMMENT_ID.toString());
                    final Comment comment = new Comment(commentId, commentText, userId);
                    comments.add(comment);
                }

                final String insertQuery1 = "INSERT INTO ST58310.COMMENT_DISCUSSION VALUES (?,?)";
                preparedStatement = dbManager.getConnection().prepareStatement(insertQuery1);
                preparedStatement.setInt(1, DiscussionController.discussionId);
                preparedStatement.setInt(2, commentId);
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        commentTextArea.clear();
    }

    @FXML
    private void deleteComment(ActionEvent event) {
        final Comment comment = commentsListView.getSelectionModel().getSelectedItem();
        if (comment != null) {
            String deleteQuery = "DELETE FROM ST58310.THE_COMMENT WHERE COMMENT_ID = ?";
            try {
                PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, comment.getCommentId());
                preparedStatement.execute();

                deleteQuery = "DELETE FROM ST58310.COMMENT_DISCUSSION WHERE THE_COMMENT_COMMENT_ID = ?";
                preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, comment.getCommentId());
                preparedStatement.execute();

                commentsListView.getSelectionModel().clearSelection();
                comments.remove(comment);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        changeDisable();
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        changeDisable();
    }

    @FXML
    private void keyReleased(KeyEvent event) {
        changeDisable();
    }

    private void changeDisable() {
        final Comment comment = commentsListView.getSelectionModel().getSelectedItem();
        if (comment != null) {//if comment was chosen
            int userId = AdminController.curUserId;
            if (userId == 0) {
                userId = MainWindowController.curUserId;
            }
            final String selectRoleQuery = "SELECT ROLE_ID FROM ST58310.ELSA_USER WHERE USER_ID = ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectRoleQuery);
                preparedStatement.setInt(1, userId);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    final int role = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
                    switch (Role.getRole(role)) {
                        case STUDENT:
                            if (comment.getCommentCreaterId() == userId) {
                                deleteCommentBtn.setDisable(false);
                            } else {
                                deleteCommentBtn.setDisable(true);
                            }
                            break;
                        case TEACHER:
                            ObservableList<Integer> students = User.fillStudentsList().stream().map(userSubject -> userSubject.getUser().getUserId()).collect(Collectors.toCollection(FXCollections::observableArrayList));
                            if (comment.getCommentCreaterId() == userId || students.contains(comment.getCommentCreaterId())) {
                                deleteCommentBtn.setDisable(false);
                            } else {
                                deleteCommentBtn.setDisable(true);
                            }
                            break;
                        case ADMINISTRATOR:
                        case MAIN_ADMIN:
                            deleteCommentBtn.setDisable(false);
                            break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

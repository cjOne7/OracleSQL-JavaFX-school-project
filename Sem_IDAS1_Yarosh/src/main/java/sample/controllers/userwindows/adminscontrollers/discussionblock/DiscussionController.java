package sample.controllers.userwindows.adminscontrollers.discussionblock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.controllers.MainWindowController;
import sample.controllers.OpenNewWindow;
import sample.controllers.userwindows.adminscontrollers.AdminController;
import sample.controllers.userwindows.adminscontrollers.studymaterials.CreateStudyMaterialsController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Discussion;
import sample.enums.*;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class DiscussionController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private ObservableList<Discussion> discussions = FXCollections.observableArrayList();

    public static int discussionId;

    @FXML
    private ListView<Discussion> discussionsListView;
    @FXML
    private Button openCommentsBtn;
    @FXML
    private Button changeTitleOfDiscussionBtn;
    @FXML
    private Button deleteDiscussionBtn;
    @FXML
    private Button createNewDiscussionBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        discussionsListView.setItems(discussions);
        discussionsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());

        final String selectQuery = "SELECT DISCUSSION_ID, TITLE, DISCUSSION_CREATER_ID FROM ST58310.DISCUSSION WHERE STY_MTRL_STUDY_MATERIAL_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, CreateStudyMaterialsController.studyMatId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int discussionId = resultSet.getInt(DiscussionColumns.DISCUSSION_ID.toString());
                final String title = resultSet.getString(DiscussionColumns.TITLE.toString());
                final int discussionCreaterId = resultSet.getInt(DiscussionColumns.DISCUSSION_CREATER_ID.toString());
                final Discussion discussion = new Discussion(discussionId, title, discussionCreaterId, CreateStudyMaterialsController.studyMatId);
                discussions.add(discussion);
            }
            if (discussions.size() >= 1) {//enable buttons
                changeDisableAllBtn(false);
            }
            disableAccordingToRights();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disableAccordingToRights() throws SQLException {
        final String selectQuery = "SELECT ROLE_ID FROM ST58310.ELSA_USER WHERE USER_ID = ?";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        int userId = AdminController.curUserId;
        if (userId == 0) {
            userId = MainWindowController.curUserId;
        }
        preparedStatement.setInt(1, userId);
        final ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            final int roleId = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
            if (roleId == Role.STUDENT.getIndex()) {
                createNewDiscussionBtn.setVisible(false);
                changeTitleOfDiscussionBtn.setVisible(false);
                deleteDiscussionBtn.setVisible(false);
            }
        }
    }

    private void changeDisableAllBtn(final boolean state) {
        openCommentsBtn.setDisable(state);
        changeDisable(state);
    }

    private void changeDisable(final boolean state) {
        changeTitleOfDiscussionBtn.setDisable(state);
        deleteDiscussionBtn.setDisable(state);
    }

    @FXML
    private void createNewDiscussion(ActionEvent event) {
        final String title = openTextInputDialog();
        if (!title.isEmpty()) {
            try {
                if (checkForUnique(title)) {
                    final String insertQuery = "INSERT INTO ST58310.DISCUSSION (TITLE, STY_MTRL_STUDY_MATERIAL_ID, DISCUSSION_CREATER_ID) VALUES (?,?,?)";

                    PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
                    preparedStatement.setString(1, title);
                    preparedStatement.setInt(2, CreateStudyMaterialsController.studyMatId);
                    int userId = AdminController.curUserId;
                    if (userId == 0) {
                        userId = MainWindowController.curUserId;
                    }
                    preparedStatement.setInt(3, userId);
                    preparedStatement.execute();

                    final String selectQuery = "SELECT MAX(DISCUSSION_ID) AS DISCUSSION_ID FROM ST58310.DISCUSSION";
                    preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
                    final ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        discussionId = resultSet.getInt(DiscussionColumns.DISCUSSION_ID.toString());
                        final Discussion discussion = new Discussion(discussionId, title, userId, CreateStudyMaterialsController.studyMatId);
                        discussions.add(discussion);
                        discussionsListView.getSelectionModel().clearSelection();
                    }
                } else {
                    Main.callAlertWindow("Warning", "Discussion with that title had already been created!", Alert.AlertType.WARNING, "/images/warning_icon.png");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (discussions.size() >= 1) {
            changeDisableAllBtn(false);
        }
    }

    private boolean checkForUnique(final String title) throws SQLException {
        final String selectQuery = "SELECT TITLE FROM ST58310.DISCUSSION WHERE UPPER(TITLE) LIKE UPPER(?)";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        preparedStatement.setString(1, title);
        final ResultSet resultSet = preparedStatement.executeQuery();
        return !resultSet.next();
    }

    @FXML
    private void changeTitleOfDiscussion(ActionEvent event) {
        final Discussion discussion = discussionsListView.getSelectionModel().getSelectedItem();
        if (discussion == null) {
            Main.callAlertWindow("Warning", "Discussion is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String title = openTextInputDialog();
            if (title.isEmpty()) {
                Main.callAlertWindow("Information", "Adding a new discussion was canceled due to empty title name!", Alert.AlertType.INFORMATION, "/images/information_icon.png");
            } else {
                try {
                    if (discussion.getTitle().equals(title)) {
                        Main.callAlertWindow("Warning", "Discussion with that title had already been created!", Alert.AlertType.WARNING, "/images/warning_icon.png");
                    } else {
                        final String updateQuery = "UPDATE ST58310.DISCUSSION SET TITLE = ? WHERE DISCUSSION_ID = ?";
                        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);
                        preparedStatement.setString(1, title);
                        preparedStatement.setInt(2, discussion.getDiscussionId());
                        preparedStatement.execute();
                        updateList(discussion);
                        discussionsListView.getSelectionModel().clearSelection();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateList(@NotNull final Discussion discussion) throws SQLException {
        final String selectQuery = "SELECT * FROM ST58310.DISCUSSION WHERE DISCUSSION_ID = ?";//update list view
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        preparedStatement.setInt(1, discussion.getDiscussionId());
        final ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            discussions.removeAll((discussions.stream()
                    .filter(discussion1 -> discussion1.getDiscussionId() == discussion.getDiscussionId())
                    .collect(Collectors.toList())));
            discussions.add(new Discussion(
                    resultSet.getInt(DiscussionColumns.DISCUSSION_ID.toString()),
                    resultSet.getString(DiscussionColumns.TITLE.toString()),
                    resultSet.getInt(DiscussionColumns.DISCUSSION_CREATER_ID.toString()),
                    CreateStudyMaterialsController.studyMatId));
        }
    }

    private String openTextInputDialog() {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adding the discussion title");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter a title for new discussion:");

        final Optional<String> result = dialog.showAndWait();
        String title = "";
        if (result.isPresent()) {
            title = result.get().trim();
        }
        return title;
    }

    @FXML
    private void deleteDiscussion(ActionEvent event) {
        final Discussion discussion = discussionsListView.getSelectionModel().getSelectedItem();
        if (discussion == null) {
            Main.callAlertWindow("Warning", "Discussion is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                final String selectQuery = "SELECT THE_COMMENT_COMMENT_ID FROM ST58310.COMMENT_DISCUSSION WHERE DISCUSSION_DISCUSSION_ID = ?";
                final PreparedStatement selectStatement = dbManager.getConnection().prepareStatement(selectQuery);
                selectStatement.setInt(1, discussion.getDiscussionId());
                final ResultSet resultSet = selectStatement.executeQuery();
                while (resultSet.next()) {
                    final int commentId = resultSet.getInt(CommentDiscussionColumns.THE_COMMENT_COMMENT_ID.toString());
                    final String deleteComments = "DELETE FROM ST58310.THE_COMMENT WHERE COMMENT_ID = ?";
                    final PreparedStatement deleteStatement = dbManager.getConnection().prepareStatement(deleteComments);
                    deleteStatement.setInt(1, commentId);
                    deleteStatement.execute();
                }
                final String deleteQuery = "DELETE FROM ST58310.DISCUSSION WHERE DISCUSSION_ID = ?";
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, discussion.getDiscussionId());
                preparedStatement.execute();

                discussionsListView.getSelectionModel().clearSelection();
                discussions.removeAll(discussions.stream().filter(discussion1 -> discussion1.getDiscussionId() == discussion.getDiscussionId()).collect(Collectors.toList()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void openComments(ActionEvent event) {
        final Discussion discussion = discussionsListView.getSelectionModel().getSelectedItem();
        if (discussion == null) {
            Main.callAlertWindow("Warning", "Discussion is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            discussionId = discussion.getDiscussionId();
            OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/CommentsWindow.fxml", getClass(), false, "Comments window", new Image("/images/admin_icon.png"));
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
    private void keyRealesed(KeyEvent event) {
        changeDisable();
    }

    private void changeDisable() {
        final Discussion discussion = discussionsListView.getSelectionModel().getSelectedItem();
        if (discussion != null) {//if discussion  was chosen
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
                    if (role == Role.TEACHER.getIndex() && discussion.getDiscussionCreaterId() == userId) {
                        changeDisable(false);
                    } else {
                        changeDisable(true);
                    }
                    if (role == Role.ADMINISTRATOR.getIndex() || role == Role.MAIN_ADMIN.getIndex()) {
                        changeDisable(false);
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

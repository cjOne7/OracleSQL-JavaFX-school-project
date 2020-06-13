package sample.controllers.userwindows.adminscontrollers.quizmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import sample.controllers.Main;
import sample.controllers.MainWindowController;
import sample.controllers.OpenNewWindow;
import sample.controllers.userwindows.adminscontrollers.AdminController;
import sample.controllers.userwindows.adminscontrollers.studymaterials.CreateStudyMaterialsController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Quiz;
import sample.enums.*;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class QuizManagementController implements Initializable {

    private final DbManager dbManager = new DbManager();

    private final ObservableList<Quiz> quizzes = FXCollections.observableArrayList();
    private final ObservableList<Integer> teacherSubjectsIds = FXCollections.observableArrayList();

    public static int quizId;

    @FXML
    private ListView<Quiz> quizzesListView;
    @FXML
    private Button deleteQuizBtn;
    @FXML
    private Button changeQuizBtn;
    @FXML
    private Button createQuizBtn;
    @FXML
    private Button showQuestionsBtn;
    @FXML
    private Button editQuizsQuestionBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        quizzesListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        quizzesListView.setItems(quizzes);

        fillListView();
        disableAccordingToRights();
    }

    private void disableAccordingToRights() {
        final String selectRoleQuery = "SELECT ROLE_ID FROM ST58310.ELSA_USER WHERE USER_ID = ?";
        try {
            final PreparedStatement selectRoleStatemnt = dbManager.getConnection().prepareStatement(selectRoleQuery);
            int userId = AdminController.curUserId;
            if (userId == 0) {
                userId = MainWindowController.curUserId;
            }
            selectRoleStatemnt.setInt(1, userId);
            final ResultSet resultSet = selectRoleStatemnt.executeQuery();
            if (resultSet.next()) {
                final int roleId = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
                if (roleId == Role.STUDENT.getIndex()) {
                    changeVisible(false);
                } else if (roleId == Role.TEACHER.getIndex()) {
                    final String selectQuery1 = "SELECT SUBJECT_ID FROM ST58310.SUBJECT WHERE SUBJECT_ID " +
                            "IN (SELECT SUBJECT_SUBJECT_ID FROM ST58310.USER_SUBJECT WHERE USER_USER_ID = ?)";
                    final PreparedStatement preparedStatement1 = dbManager.getConnection().prepareStatement(selectQuery1);
                    preparedStatement1.setInt(1, userId);
                    final ResultSet resultSet1 = preparedStatement1.executeQuery();
                    while (resultSet1.next()) {
                        final int subjectId = resultSet1.getInt(SubjectColumns.SUBJECT_ID.toString());
                        teacherSubjectsIds.add(subjectId);
                    }
                    final String selectSubjectId = "SELECT SUBJECT_SUBJECT_ID FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";
                    final PreparedStatement preparedStatement2 = dbManager.getConnection().prepareStatement(selectSubjectId);
                    preparedStatement2.setInt(1, CreateStudyMaterialsController.studyMatId);
                    final ResultSet resultSet2 = preparedStatement2.executeQuery();
                    if (resultSet2.next()) {
                        final int subjectId = resultSet2.getInt(StudyMatColumns.SUBJECT_SUBJECT_ID.getColumnName());
                        if (teacherSubjectsIds.contains(subjectId)) {
                            changeVisible(true);
                        } else {
                            changeVisible(false);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void changeDisableAllBtn(final boolean state) {
        changeDisable(state);
        showQuestionsBtn.setDisable(state);
    }

    private void changeDisable(final boolean state) {
        changeQuizBtn.setDisable(state);
        deleteQuizBtn.setDisable(state);
        editQuizsQuestionBtn.setDisable(state);
    }

    private void changeVisible(final boolean state) {
        createQuizBtn.setVisible(state);
        deleteQuizBtn.setVisible(state);
        changeQuizBtn.setVisible(state);
        editQuizsQuestionBtn.setVisible(state);
    }

    private void fillListView() {
        final String selectQuery = "SELECT * FROM ST58310.QUIZ WHERE STY_MTRL_STY_MTRL_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, CreateStudyMaterialsController.studyMatId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int quizId = resultSet.getInt(QuizColumns.QUIZ_ID.toString());
                final String quizName = resultSet.getString(QuizColumns.QUIZ_NAME.toString());
                final int numberOfQuestions = resultSet.getInt(QuizColumns.NUMBER_OF_QUESTIONS.toString());
                final String description = resultSet.getString(QuizColumns.DESCRIPTION.toString());
                final int studyMatId = resultSet.getInt(QuizColumns.STY_MTRL_STY_MTRL_ID.toString());
                final int userCreaterId = resultSet.getInt(QuizColumns.USER_CREATER_ID.toString());
                final Quiz quiz = new Quiz(quizId, quizName, numberOfQuestions, description, studyMatId, userCreaterId);
                quizzes.add(quiz);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (quizzes.size() >= 1) {
            changeDisableAllBtn(false);
        }
    }

    @FXML
    private void createQuiz(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/quizmanagement/CreateQuizWindow.fxml", getClass(), false, "Create quiz window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void deleteQuiz(ActionEvent event) {
        final Quiz quiz = quizzesListView.getSelectionModel().getSelectedItem();
        if (quiz == null) {
            Main.callAlertWindow("Warning", "Quiz is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                final String selectQuery = "SELECT QUESTION_QUESTION_ID FROM ST58310.QUIZZES_QUESTION WHERE QUIZ_QUIZ_ID = ?";
                final PreparedStatement selectStatement = dbManager.getConnection().prepareStatement(selectQuery);
                selectStatement.setInt(1, quiz.getQuizId());
                final ResultSet resultSet = selectStatement.executeQuery();
                while (resultSet.next()) {
                    final int questionId = resultSet.getInt(QuizzesQuestionColumns.QUESTION_QUESTION_ID.toString());
                    final String deleteQuestions = "DELETE FROM ST58310.QUESTION WHERE QUESTION_ID = ?";
                    final PreparedStatement deleteStatement = dbManager.getConnection().prepareStatement(deleteQuestions);
                    deleteStatement.setInt(1, questionId);
                    deleteStatement.execute();
                }
                final String deleteQuery = "DELETE FROM ST58310.QUIZ WHERE QUIZ_ID = ?";
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, quiz.getQuizId());
                preparedStatement.execute();
                quizzes.remove(quiz);
                quizzesListView.getSelectionModel().clearSelection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (quizzes.isEmpty()) {
            changeDisableAllBtn(true);
        }
    }

    @FXML
    private void refreshList(ActionEvent event) {
        quizzes.clear();
        fillListView();
    }

    @FXML
    private void changeQuiz(ActionEvent event) {
        openWindow("/fxmlfiles/userwindows/adminsfxmls/quizmanagement/ChangeQuizWindow.fxml", "Change quiz window");
    }

    @FXML
    private void editQuizsQuestion(ActionEvent event) {
        openWindow("/fxmlfiles/userwindows/adminsfxmls/quizmanagement/EditQuizsQuestionsWindow.fxml", "Edit quiz's questions window");
    }

    @FXML
    private void showQuestions(ActionEvent event) {
        openWindow("/fxmlfiles/userwindows/adminsfxmls/quizmanagement/questionmanagement/ShowQuestionsWindow.fxml", "Open questions window");
    }

    private void openWindow(final String fxmlFilePath, final String title) {
        final Quiz quiz = quizzesListView.getSelectionModel().getSelectedItem();
        if (quiz == null) {
            Main.callAlertWindow("Warning", "Quiz is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            quizId = quiz.getQuizId();
            OpenNewWindow.openNewWindow(fxmlFilePath, getClass(), false, title, new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        changeDisable();
    }

    @FXML
    private void keyReleased(KeyEvent event) {
        changeDisable();
    }

    @FXML
    private void mouseClicked(MouseEvent event) {
        changeDisable();
    }

    private void changeDisable() {
        final Quiz quiz = quizzesListView.getSelectionModel().getSelectedItem();
        if (quiz != null) {//if quiz was chosen
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
                        case TEACHER:
                            if (quiz.getUserCreaterId() == userId) {
                                changeDisable(false);
                            } else {
                                changeDisable(true);
                            }
                            break;
                        case ADMINISTRATOR:
                        case MAIN_ADMIN:
                            changeDisable(false);
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

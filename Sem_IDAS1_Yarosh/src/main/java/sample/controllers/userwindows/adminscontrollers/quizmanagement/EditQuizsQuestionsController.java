package sample.controllers.userwindows.adminscontrollers.quizmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Question;
import sample.enums.QuestionColumns;
import sample.enums.QuizColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EditQuizsQuestionsController implements Initializable {
    private final DbManager dbManager = new DbManager();

    private ObservableList<Question> questions = FXCollections.observableArrayList();
    public static int questionId;
    public static int numberOfQuestions;

    @FXML
    private ListView<Question> questionsListView;
    @FXML
    private Button changeQuestionBtn;
    @FXML
    private Button deleteQuestionBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        questionsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        questionsListView.setItems(questions);

        fillListView();
    }

    private void changeDisable(final boolean state) {
        changeQuestionBtn.setDisable(state);
        deleteQuestionBtn.setDisable(state);
    }

    @FXML
    private void createQuestion(ActionEvent event) {
        final String checkSelection = "SELECT NUMBER_OF_QUESTIONS FROM ST58310.QUIZ WHERE QUIZ_ID = ?";
        final PreparedStatement checkStatement;
        try {
            checkStatement = dbManager.getConnection().prepareStatement(checkSelection);
            checkStatement.setInt(1, QuizManagementController.quizId);
            final ResultSet checkSet = checkStatement.executeQuery();
            if (checkSet.next()) {
                numberOfQuestions = checkSet.getInt(QuizColumns.NUMBER_OF_QUESTIONS.toString());
            }
            if (questions.size() >= numberOfQuestions) {
                Main.callAlertWindow("Warning", "You can't add one more question! Max size is " + numberOfQuestions, Alert.AlertType.WARNING, "/images/warning_icon.png");
            } else {
                OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/quizmanagement/questionmanagement/CreateQuestionWindow.fxml", getClass(), false, "Create question window", new Image("/images/admin_icon.png"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void changeQuestion(ActionEvent event) {
        final Question question = questionsListView.getSelectionModel().getSelectedItem();
        if (question == null) {
            Main.callAlertWindow("Warning", "Question is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            questionId = question.getQuestionId();
            OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/quizmanagement/questionmanagement/ChangeQuestionWindow.fxml", getClass(), false, "Change question window", new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void deleteQuestion(ActionEvent event) {
        final Question question = questionsListView.getSelectionModel().getSelectedItem();
        if (question != null) {
            String deleteQuery = "DELETE FROM ST58310.QUESTION WHERE QUESTION_ID = ?";
            try {
                PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, question.getQuestionId());
                preparedStatement.execute();

                deleteQuery = "DELETE FROM ST58310.QUIZZES_QUESTION WHERE QUESTION_QUESTION_ID = ?";
                preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, question.getQuestionId());
                preparedStatement.execute();

                questions.remove(question);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (questions.isEmpty()) {
            changeDisable(true);
        }
    }

    @FXML
    private void refreshList(ActionEvent event) {
        questions.clear();
        fillListView();
    }

    private void fillListView() {
        final String selectQuery = "SELECT * FROM ST58310.QUESTION WHERE QUESTION_ID IN (SELECT QUESTION_QUESTION_ID FROM ST58310.QUIZZES_QUESTION WHERE QUIZ_QUIZ_ID = ?) ORDER BY QUESTION_ID";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, QuizManagementController.quizId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int questionId = resultSet.getInt(QuestionColumns.QUESTION_ID.toString());
                final int points = resultSet.getInt(QuestionColumns.POINTS.toString());
                final String questionText = resultSet.getString(QuestionColumns.QUESTION_TEXT.toString());
                final int questionCatId = resultSet.getInt(QuestionColumns.QSTN_CTGY_QUESTIONS_CAT_ID.toString());
                final Question question = new Question(questionId, points, questionText, questionCatId);
                questions.add(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (questions.size() >= 1) {
            changeDisable(false);
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

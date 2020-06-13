package sample.controllers.userwindows.adminscontrollers.quizmanagement.questionmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.controllers.userwindows.adminscontrollers.quizmanagement.QuizManagementController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Question;
import sample.enums.QuestionColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ShowQuestionsController implements Initializable {

    private final DbManager dbManager = new DbManager();

    private final ObservableList<Question> questions = FXCollections.observableArrayList();

    public static int questionId;

    @FXML
    private ListView<Question> questionsListView;
    @FXML
    private Button openQuestionBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        questionsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        questionsListView.setItems(questions);

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
            openQuestionBtn.setDisable(false);
        }
    }

    @FXML
    private void openQuestion(ActionEvent event) {
        final Question question = questionsListView.getSelectionModel().getSelectedItem();
        if (question == null) {
            Main.callAlertWindow("Warning", "Quiz is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            questionId = question.getQuestionId();
            OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/quizmanagement/questionmanagement/OpenQuestionWindow.fxml", getClass(), false, "Open question window", new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

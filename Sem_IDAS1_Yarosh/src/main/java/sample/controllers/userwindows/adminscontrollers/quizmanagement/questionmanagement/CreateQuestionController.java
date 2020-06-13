package sample.controllers.userwindows.adminscontrollers.quizmanagement.questionmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.Cosmetic;
import sample.TextConstraint;
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.controllers.userwindows.adminscontrollers.quizmanagement.EditQuizsQuestionsController;
import sample.controllers.userwindows.adminscontrollers.quizmanagement.QuizManagementController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Category;
import sample.dbtableclasses.Question;
import sample.enums.QuestionCatColumns;
import sample.enums.QuestionColumns;
import sample.enums.QuizColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static sample.Cosmetic.changeTextFieldStyle;

public class CreateQuestionController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private ObservableList<Category> questionCategories = FXCollections.observableArrayList();

    @FXML
    private ComboBox<String> questionCatComboBox;
    @FXML
    private Spinner<Integer> pointsForAnswerSpinner;
    @FXML
    private TextArea questionTextTextArea;
    @FXML
    private Label messageLabel;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        pointsForAnswerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        pointsForAnswerSpinner.setStyle(StylesEnum.FONT_STYLE.getStyle());

        questionTextTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            changeTextFieldStyle(newValue, questionTextTextArea, StylesEnum.EMPTY_STRING.getStyle());
            Cosmetic.changeLabelAttributes(messageLabel, "", Color.TRANSPARENT);
        });
        questionCatComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());

        questionCategories = Category.getQuestionCatList();
        questionCategories.forEach(category -> questionCatComboBox.getItems().add(category.toComboBoxString()));
        if (questionCategories.size() >= 1) {
            questionCatComboBox.setValue(questionCategories.get(0).toComboBoxString());
        }

        questionTextTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(300)));
    }

    @FXML
    private void createQuestion(ActionEvent event) {
        try {
            final String selectCurNumOfQues = "SELECT COUNT(QUESTION_QUESTION_ID) AS CUR_NUM_OF_QUESTIONS FROM ST58310.QUIZZES_QUESTION WHERE QUIZ_QUIZ_ID = ?";
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectCurNumOfQues);
            preparedStatement.setInt(1, QuizManagementController.quizId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            int curNumOfQues = 0;
            if (resultSet.next()) {
                curNumOfQues = resultSet.getInt("CUR_NUM_OF_QUESTIONS");
            }
            if (EditQuizsQuestionsController.numberOfQuestions == curNumOfQues) {
                Main.callAlertWindow("Warning", "You can't add one more question! Max size is " + EditQuizsQuestionsController.numberOfQuestions, Alert.AlertType.WARNING, "/images/warning_icon.png");
                close(event);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        final String questionText = questionTextTextArea.getText().trim();
        if (questionText.isEmpty()) {
            Cosmetic.shake(questionTextTextArea);
            questionTextTextArea.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            Cosmetic.changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                final String insertQuery = "INSERT INTO ST58310.QUESTION (POINTS, QUESTION_TEXT, QSTN_CTGY_QUESTIONS_CAT_ID) VALUES (?,?,?)";
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
                preparedStatement.setInt(1, pointsForAnswerSpinner.getValue());
                preparedStatement.setString(2, questionText);
                preparedStatement.setInt(3, getQuestionCatId());
                preparedStatement.execute();

                final String selectQuery = "SELECT MAX(QUESTION_ID) AS QUESTION_ID FROM ST58310.QUESTION";
                final PreparedStatement selectStatement = dbManager.getConnection().prepareStatement(selectQuery);
                final ResultSet resultSet = selectStatement.executeQuery();
                int questionId = 0;
                if (resultSet.next()) {
                    questionId = resultSet.getInt(QuestionColumns.QUESTION_ID.toString());
                }
                final String insertQuery1 = "INSERT INTO ST58310.QUIZZES_QUESTION VALUES (?,?)";
                final PreparedStatement insertStatement = dbManager.getConnection().prepareStatement(insertQuery1);
                insertStatement.setInt(1, QuizManagementController.quizId);
                insertStatement.setInt(2, questionId);
                insertStatement.execute();

                clearFiedls();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkForMaxNumberOfQuestions(ActionEvent event){
        try {
            final String selectCurNumOfQues = "SELECT COUNT(QUESTION_QUESTION_ID) AS CUR_NUM_OF_QUESTIONS FROM ST58310.QUIZZES_QUESTION WHERE QUIZ_QUIZ_ID = ?";
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectCurNumOfQues);
            preparedStatement.setInt(1, QuizManagementController.quizId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            int curNumOfQues = 0;
            if (resultSet.next()) {
                curNumOfQues = resultSet.getInt("CUR_NUM_OF_QUESTIONS");
            }
            if (EditQuizsQuestionsController.numberOfQuestions == curNumOfQues) {
                Main.callAlertWindow("Warning", "You can't add one more question! Max size is " + EditQuizsQuestionsController.numberOfQuestions, Alert.AlertType.WARNING, "/images/warning_icon.png");
                close(event);
            } else {
                System.out.println("vlazit");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFiedls() {
        questionTextTextArea.clear();
        questionTextTextArea.setStyle(StylesEnum.EMPTY_STRING.getStyle());
        Cosmetic.changeLabelAttributes(messageLabel, "Question was created successfully", Color.BLACK);
    }

    private int getQuestionCatId() {
        final String questionCat = questionCatComboBox.getValue();
        for (Category questionCategory : questionCategories) {
            if (questionCategory.getCategoryName().equals(questionCat)) {
                return questionCategory.getCategoryId();
            }
        }
        return -1;
    }


    @FXML
    private void scroll(@NotNull ScrollEvent event) {
        ((Spinner) event.getSource()).increment((int) event.getDeltaY() / 10);
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

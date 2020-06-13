package sample.controllers.userwindows.adminscontrollers.quizmanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.Checker;
import sample.Cosmetic;
import sample.TextConstraint;
import sample.controllers.userwindows.adminscontrollers.categorymanagement.StudyMatCategoryController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Category;
import sample.dbtableclasses.Quiz;
import sample.enums.CategoryColumns;
import sample.enums.QuizColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static sample.Cosmetic.changeLabelAttributes;

public class ChangeQuizController implements Initializable {

    private final DbManager dbManager = new DbManager();

    @FXML
    private Spinner<Integer> numberOfQuestionsSpinner;
    @FXML
    private TextField quizNameTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button changeQuizBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberOfQuestionsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        numberOfQuestionsSpinner.setStyle(StylesEnum.FONT_STYLE.getStyle());

        final String selectQuery = "SELECT QUIZ_NAME, NUMBER_OF_QUESTIONS, DESCRIPTION FROM ST58310.QUIZ WHERE QUIZ_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, QuizManagementController.quizId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final String quizName = resultSet.getString(QuizColumns.QUIZ_NAME.toString());
                final int numberOfQuestions = resultSet.getInt(QuizColumns.NUMBER_OF_QUESTIONS.toString());
                final String description = resultSet.getString(QuizColumns.DESCRIPTION.toString());
                quizNameTextField.setText(quizName);
                descriptionTextArea.setText(description);
                numberOfQuestionsSpinner.getValueFactory().setValue(numberOfQuestions);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        quizNameTextField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        descriptionTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(500)));
    }

    @FXML
    private void changeQuiz(ActionEvent event) {
        final String quizName = quizNameTextField.getText();
        if (quizName == null || quizName.trim().isEmpty()) {
            Cosmetic.shake(quizNameTextField);
            quizNameTextField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                final String updateQuery = "UPDATE ST58310.QUIZ SET QUIZ_NAME = ?, NUMBER_OF_QUESTIONS = ?, DESCRIPTION = ? WHERE QUIZ_ID = ?";
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);
                preparedStatement.setString(1, quizName);
                preparedStatement.setInt(2, numberOfQuestionsSpinner.getValue());

                final String description = descriptionTextArea.getText();
                Checker.checkTextField(description == null || description.trim().isEmpty(), 3, description, preparedStatement);

                preparedStatement.setInt(4, QuizManagementController.quizId);
                preparedStatement.execute();
                ((Stage) changeQuizBtn.getScene().getWindow()).close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void scroll(@NotNull ScrollEvent event) {
        ((Spinner) event.getSource()).increment((int) event.getDeltaY() / 8);
    }

    @FXML
    private void cancel(ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}

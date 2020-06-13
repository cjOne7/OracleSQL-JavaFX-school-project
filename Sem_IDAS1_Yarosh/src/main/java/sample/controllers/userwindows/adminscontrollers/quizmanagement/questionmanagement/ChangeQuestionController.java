package sample.controllers.userwindows.adminscontrollers.quizmanagement.questionmanagement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sample.Checker;
import sample.Cosmetic;
import sample.TextConstraint;
import sample.controllers.userwindows.adminscontrollers.quizmanagement.EditQuizsQuestionsController;
import sample.controllers.userwindows.adminscontrollers.quizmanagement.QuizManagementController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Category;
import sample.enums.QuestionCatColumns;
import sample.enums.QuestionColumns;
import sample.enums.QuizColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static sample.Cosmetic.changeLabelAttributes;
import static sample.Cosmetic.changeTextFieldStyle;

public class ChangeQuestionController implements Initializable {

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
    private Button changeQuestionBtn;
    @FXML
    private Button cancelBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pointsForAnswerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        pointsForAnswerSpinner.setStyle(StylesEnum.FONT_STYLE.getStyle());
        questionTextTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            changeTextFieldStyle(newValue, questionTextTextArea, StylesEnum.EMPTY_STRING.getStyle());
        });
        questionCatComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());

        questionCategories = Category.getQuestionCatList();
        questionCategories.forEach(category -> questionCatComboBox.getItems().add(category.toComboBoxString()));
        if (questionCategories.size() >= 1) {
            questionCatComboBox.setValue(questionCategories.get(0).toComboBoxString());
        }

        final String selectQuery = "SELECT POINTS, QUESTION_TEXT, QSTN_CTGY_QUESTIONS_CAT_ID FROM ST58310.QUESTION WHERE QUESTION_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, EditQuizsQuestionsController.questionId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                pointsForAnswerSpinner.getValueFactory().setValue(resultSet.getInt(QuestionColumns.POINTS.toString()));
                questionCatComboBox.setValue(getQuestionCatName(resultSet.getInt(QuestionColumns.QSTN_CTGY_QUESTIONS_CAT_ID.toString())));
                questionTextTextArea.setText(resultSet.getString(QuestionColumns.QUESTION_TEXT.toString()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        questionTextTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(300)));
    }

    @Nullable
    private String getQuestionCatName(final int questionCatId) {
        final String selectQuery = "SELECT QUESTION_CAT_NAME FROM ST58310.QUESTIONS_CTGY WHERE QUESTIONS_CAT_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, questionCatId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            String questionCatName = "";
            if (resultSet.next()) {
                questionCatName = resultSet.getString(QuestionCatColumns.QUESTION_CAT_NAME.toString());
            }
            return questionCatName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void changeQuestion(ActionEvent event) {
        final String questionText = questionTextTextArea.getText().trim();
        if (questionText.isEmpty()) {
            Cosmetic.shake(questionTextTextArea);
            questionTextTextArea.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                final String updateQuery = "UPDATE ST58310.QUESTION SET POINTS = ?, QUESTION_TEXT = ?, QSTN_CTGY_QUESTIONS_CAT_ID = ? WHERE QUESTION_ID = ?";
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);
                preparedStatement.setInt(1, pointsForAnswerSpinner.getValue());
                preparedStatement.setString(2, questionText);

                preparedStatement.setInt(3, getQuestionCatId());

                preparedStatement.setInt(4, EditQuizsQuestionsController.questionId);
                preparedStatement.execute();
                ((Stage) changeQuestionBtn.getScene().getWindow()).close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
    private void cancel(ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}

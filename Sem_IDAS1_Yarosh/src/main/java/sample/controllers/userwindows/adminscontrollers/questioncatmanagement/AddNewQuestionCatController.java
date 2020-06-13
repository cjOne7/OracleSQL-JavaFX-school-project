package sample.controllers.userwindows.adminscontrollers.questioncatmanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sample.Checker;
import sample.Cosmetic;
import sample.TextConstraint;
import sample.databasemanager.DbManager;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ResourceBundle;

import static sample.Cosmetic.changeLabelAttributes;

public class AddNewQuestionCatController implements Initializable {

    private final DbManager dbManager = new DbManager();

    @FXML
    private TextField categoryNameTextField;
    @FXML
    private Button createCategoryBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createCategoryBtn.textProperty().addListener((observable, oldValue, newValue) -> {
            Cosmetic.changeTextFieldStyle(newValue, categoryNameTextField, StylesEnum.EMPTY_STRING.getStyle());
        });
        categoryNameTextField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        descriptionTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(500)));
    }

    @FXML
    private void createCategory(ActionEvent event) {
        final String categoryName = categoryNameTextField.getText();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            Cosmetic.shake(categoryNameTextField);
            categoryNameTextField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                if (Checker.checkForUnique(categoryNameTextField, "SELECT QUESTION_CAT_NAME FROM ST58310.QUESTIONS_CTGY WHERE UPPER(QUESTION_CAT_NAME) LIKE UPPER(?)",
                        messageLabel, categoryName, "Category name must be unique!")) {
                    final String insertQuery = "INSERT INTO ST58310.QUESTIONS_CTGY (QUESTION_CAT_NAME, DESCRIPTION) VALUES (?,?)";
                    final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
                    preparedStatement.setString(1, categoryName);
                    final String description = descriptionTextArea.getText();
                    if (description == null || description.trim().isEmpty()) {
                        preparedStatement.setNull(2, Types.NULL);
                    } else {
                        preparedStatement.setString(2, description.trim());
                    }
                    preparedStatement.execute();
                    clearText();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearText() {
        categoryNameTextField.clear();
        descriptionTextArea.clear();
        categoryNameTextField.setStyle(StylesEnum.EMPTY_STRING.getStyle());
        changeLabelAttributes(messageLabel, "New category has been added successfully!", Color.BLACK);
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

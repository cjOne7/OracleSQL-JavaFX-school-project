package sample.controllers.userwindows.adminscontrollers.categorymanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.Shake;
import sample.databasemanager.DbManager;
import sample.enums.CategoryColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ResourceBundle;

public class AddNewCategoryController implements Initializable {

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
    public void initialize(final URL location, final ResourceBundle resources) {
        createCategoryBtn.textProperty().addListener((observable, oldValue, newValue) -> {
            changeTextFieldStyle(newValue, categoryNameTextField, StylesEnum.EMPTY_STRING.getStyle());
        });
    }

    private void changeTextFieldStyle(@NotNull final String newValue, final TextField textField, final String style) {
        if (!newValue.isEmpty()) {
            textField.setStyle(style);
        }
    }

    private void changeLabelAttributes(@NotNull final Label label, final String text, final Color textColor) {
        label.setText(text);
        label.setTextFill(textColor);
    }

    @FXML
    private void createCategory(ActionEvent event) {
        final String categoryName = categoryNameTextField.getText();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            Shake.shake(categoryNameTextField);
            categoryNameTextField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                if (checkNameForUnique()) {
                    final String insertQuery = "INSERT INTO ST58310.CATEGORY (CATEGORY_NAME, DESCRIPTION) VALUES (?,?)";
                    final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
                    preparedStatement.setString(CategoryColumns.CATEGORY_NAME.getColumnIndex(), categoryName);
                    final String description = descriptionTextArea.getText();
                    if (description == null || description.trim().isEmpty()) {
                        preparedStatement.setNull(CategoryColumns.DESCRIPTION.getColumnIndex(), Types.NULL);
                    } else {
                        preparedStatement.setString(CategoryColumns.DESCRIPTION.getColumnIndex(), description.trim());
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
        messageLabel.setText("New category has been added successfully!");
        categoryNameTextField.clear();
        descriptionTextArea.clear();
    }

    private boolean checkNameForUnique() throws SQLException {
        final String categoryName = categoryNameTextField.getText();
        final String selectQuery = "SELECT CATEGORY.CATEGORY_NAME FROM ST58310.CATEGORY WHERE UPPER(CATEGORY_NAME) LIKE UPPER(?)";
        final PreparedStatement checkSelection = dbManager.getConnection().prepareStatement(selectQuery);
        checkSelection.setString(1, categoryName);
        final ResultSet loginFields = checkSelection.executeQuery();
        if (loginFields.next()) {
            changeLabelAttributes(messageLabel, "Category name must be unique!", Color.RED);
            Shake.shake(categoryNameTextField);
            categoryNameTextField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            return false;
        } else {
            return true;
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

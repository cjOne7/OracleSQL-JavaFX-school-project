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
import sample.dbtableclasses.Category;
import sample.enums.CategoryColumns;
import sample.enums.QuestionCatColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ResourceBundle;

import static sample.Cosmetic.changeLabelAttributes;

public class ChangeQuestionCatController implements Initializable {
    private final DbManager dbManager = new DbManager();

    private Category category;

    @FXML
    private TextField categoryNameTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Label messageLabel;
    @FXML
    private Button createCategoryBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final String selectQuery = "SELECT * FROM ST58310.QUESTIONS_CTGY WHERE QUESTIONS_CAT_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, QuestionCatManagerController.categoryId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final int categoryId = resultSet.getInt(QuestionCatColumns.QUESTIONS_CAT_ID.toString());
                final String categoryName = resultSet.getString(QuestionCatColumns.QUESTION_CAT_NAME.toString());
                final String description = resultSet.getString(QuestionCatColumns.DESCRIPTION.toString());
                category = new Category(categoryId, categoryName, description);
                categoryNameTextField.setText(categoryName);
                descriptionTextArea.setText(description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        categoryNameTextField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        descriptionTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(500)));
    }

    @FXML
    private void changeCategory(ActionEvent event) {
        final String categoryName = categoryNameTextField.getText();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            Cosmetic.shake(categoryNameTextField);
            categoryNameTextField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                if (checkNameForUnique()) {
                    final String updateQuery = "UPDATE ST58310.QUESTIONS_CTGY SET QUESTION_CAT_NAME = ?, DESCRIPTION = ? WHERE QUESTIONS_CAT_ID = ?";
                    final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);
                    preparedStatement.setString(CategoryColumns.CATEGORY_NAME.getColumnIndex(), categoryName.trim());
                    final String description = descriptionTextArea.getText();
                    if (description == null || description.trim().isEmpty()) {
                        preparedStatement.setNull(2, Types.NULL);
                    } else {
                        preparedStatement.setString(2, description.trim());
                    }
                    preparedStatement.setInt(3, QuestionCatManagerController.categoryId);
                    preparedStatement.execute();
                    ((Stage) createCategoryBtn.getScene().getWindow()).close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkNameForUnique() throws SQLException {
        final String categoryName = categoryNameTextField.getText();
        if (categoryName != null && !categoryName.trim().equalsIgnoreCase(category.getCategoryName())) {
            return Checker.checkForUnique(categoryNameTextField, "SELECT QUESTION_CAT_NAME FROM ST58310.QUESTIONS_CTGY WHERE UPPER(QUESTION_CAT_NAME) LIKE UPPER(?)",
                    messageLabel, categoryName, "Category name must be unique!");
        } else {
            return true;
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

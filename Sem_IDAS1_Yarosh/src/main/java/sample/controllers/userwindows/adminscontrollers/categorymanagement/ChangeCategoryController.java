package sample.controllers.userwindows.adminscontrollers.categorymanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.Checker;
import sample.Cosmetic;
import sample.TextConstraint;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Category;
import sample.enums.CategoryColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ResourceBundle;

import static sample.Cosmetic.changeLabelAttributes;

public class ChangeCategoryController implements Initializable {

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
        final String selectQuery = "SELECT * from ST58310.CATEGORY where CATEGORY_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, StudyMatCategoryController.categoryId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final int categoryId = resultSet.getInt(CategoryColumns.CATEGORY_ID.toString());
                final String categoryName = resultSet.getString(CategoryColumns.CATEGORY_NAME.toString());
                final String description = resultSet.getString(CategoryColumns.DESCRIPTION.toString());
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
                    final String updateQuery = "UPDATE ST58310.CATEGORY SET CATEGORY_NAME = ?, DESCRIPTION = ? WHERE CATEGORY_ID = ?";
                    final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);
                    preparedStatement.setString(1, categoryName.trim());
                    final String description = descriptionTextArea.getText();
                    Checker.checkTextField(description == null || description.trim().isEmpty(), 2, description, preparedStatement);
                    preparedStatement.setInt(3, StudyMatCategoryController.categoryId);
                    preparedStatement.execute();
                    ((Stage) createCategoryBtn.getScene().getWindow()).close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkNameForUnique() throws SQLException {
        final String categoryName = categoryNameTextField.getText().trim();
        if (categoryName.equalsIgnoreCase(category.getCategoryName())) {
            return true;
        } else {
            return Checker.checkForUnique(categoryNameTextField,
                    "SELECT CATEGORY_NAME FROM ST58310.CATEGORY WHERE UPPER(CATEGORY_NAME) LIKE UPPER(?)",
                    messageLabel, categoryName, "Category name must be unique!");
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

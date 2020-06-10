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
import sample.dbtableclasses.Category;
import sample.enums.CategoryColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ResourceBundle;

public class ChangeCategoryController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;
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
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
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
    }

    private void changeLabelAttributes(@NotNull final Label label, final String text, final Color textColor) {
        label.setText(text);
        label.setTextFill(textColor);
    }

    @FXML
    private void changeCategory(ActionEvent event) {
        final String categoryName = categoryNameTextField.getText();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            Shake.shake(categoryNameTextField);
            categoryNameTextField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                if (checkNameForUnique()) {
                    final String updateQuery = "UPDATE ST58310.CATEGORY SET CATEGORY_NAME = ?, DESCRIPTION = ? WHERE CATEGORY_ID = ?";
                    final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);
                    preparedStatement.setString(CategoryColumns.CATEGORY_NAME.getColumnIndex(), categoryName.trim());
                    final String description = descriptionTextArea.getText();
                    if (description == null || description.trim().isEmpty()) {
                        preparedStatement.setNull(CategoryColumns.DESCRIPTION.getColumnIndex(), Types.NULL);
                    } else {
                        preparedStatement.setString(CategoryColumns.DESCRIPTION.getColumnIndex(), description.trim());
                    }
                    preparedStatement.setInt(CategoryColumns.CATEGORY_ID.getColumnIndex(), StudyMatCategoryController.categoryId);
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
            final String selectQuery = "SELECT CATEGORY_NAME FROM ST58310.CATEGORY WHERE UPPER(CATEGORY_NAME) LIKE UPPER(?)";
            final PreparedStatement checkSelection = dbManager.getConnection().prepareStatement(selectQuery);
            checkSelection.setString(1, categoryName);
            final ResultSet loginFields = checkSelection.executeQuery();
            if (loginFields.next()) {
                changeLabelAttributes(messageLabel, "Category name must be unique!", Color.RED);
                categoryNameTextField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
                Shake.shake(categoryNameTextField);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @FXML
    private void close(ActionEvent event) { ((Stage) closeBtn.getScene().getWindow()).close(); }
}

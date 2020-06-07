package sample.controllers.adminscontrollers.categorymanagement;

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
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Category;
import sample.dbtableclasses.Subject;
import sample.enums.CategoryColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class StudyMatCategoryController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;

    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    public static int categoryId;

    @FXML
    private Button closeBtn;
    @FXML
    private ListView<Category> categoryListView;
    @FXML
    private Button changeCategoryBtn;
    @FXML
    private Button deleteCategoryBtn;


    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        categoryListView.setItems(categories);
        categoryListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        fillListView();
        checkDisable();
    }

    private void fillListView() {
        final String selectQuery = "SELECT * FROM ST58310.CATEGORY ORDER BY CATEGORY_ID";
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int categoryId = resultSet.getInt(CategoryColumns.CATEGORY_ID.toString());
                final String categoryName = resultSet.getString(CategoryColumns.CATEGORY_NAME.toString());
                final String description = resultSet.getString(CategoryColumns.DESCRIPTION.toString());
                final Category category = new Category(categoryId, categoryName, description);
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addCategory(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/categorymanagement/AddNewCategoryWindow.fxml", getClass(), false, "Add new category window", new Image("/images/admin_icon.png"));
        checkDisable();
    }

    @FXML
    private void changeCategory(ActionEvent event) {
        final Category category = categoryListView.getSelectionModel().getSelectedItem();
        if (category == null) {
            Main.callAlertWindow("Warning", "Subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            categoryId = category.getCategoryId();
            OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/categorymanagement/ChangeCategoryWindow.fxml", getClass(), false, "Change category window", new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void deleteCategory(ActionEvent event) {
        final Category category = categoryListView.getSelectionModel().getSelectedItem();
        if (category == null) {
            Main.callAlertWindow("Warning", "Subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String deleteQuery = "DELETE FROM ST58310.CATEGORY WHERE CATEGORY_ID = ?";
            try {
                preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, category.getCategoryId());
                preparedStatement.execute();
                categories.remove(category);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (categories.isEmpty()) {
            changeDisable(true);
        }
    }

    private void changeDisable(final boolean state) {
        changeCategoryBtn.setDisable(state);
        deleteCategoryBtn.setDisable(state);
    }

    private void checkDisable(){
        if (categories.size() >= 1) {
            changeDisable(false);
        }
    }

    @FXML
    private void refreshList(ActionEvent event) {
        categories.clear();
        fillListView();
        checkDisable();
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

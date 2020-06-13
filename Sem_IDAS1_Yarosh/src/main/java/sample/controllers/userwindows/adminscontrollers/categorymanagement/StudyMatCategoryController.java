package sample.controllers.userwindows.adminscontrollers.categorymanagement;

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
import sample.dbtableclasses.Category;
import sample.enums.CategoryColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.util.ResourceBundle;

public class StudyMatCategoryController implements Initializable {
    private ObservableList<Category> categories = FXCollections.observableArrayList();
    public static int categoryId;

    @FXML
    private ListView<Category> categoryListView;
    @FXML
    private Button changeCategoryBtn;
    @FXML
    private Button deleteCategoryBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        categoryListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        categories = Category.fillListView("SELECT * FROM ST58310.CATEGORY ORDER BY CATEGORY_ID",
                CategoryColumns.CATEGORY_ID.toString(),
                CategoryColumns.CATEGORY_NAME.toString(),
                CategoryColumns.DESCRIPTION.toString());
        categoryListView.setItems(categories);
        checkDisable();
    }

    @FXML
    private void addCategory(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/categorymanagement/AddNewCategoryWindow.fxml", getClass(), false, "Add new category window", new Image("/images/admin_icon.png"));
        checkDisable();
    }

    @FXML
    private void changeCategory(ActionEvent event) {
        final Category category = categoryListView.getSelectionModel().getSelectedItem();
        if (category == null) {
            Main.callAlertWindow("Warning", "Category is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            categoryId = category.getCategoryId();
            OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/categorymanagement/ChangeCategoryWindow.fxml", getClass(), false, "Change category window", new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void deleteCategory(ActionEvent event) {
        final Category category = categoryListView.getSelectionModel().getSelectedItem();
        Category.deleteCategory(category, "DELETE FROM ST58310.CATEGORY WHERE CATEGORY_ID = ?");
        categories.remove(category);
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
        categories = Category.fillListView("SELECT * FROM ST58310.CATEGORY ORDER BY CATEGORY_ID",
                CategoryColumns.CATEGORY_ID.toString(),
                CategoryColumns.CATEGORY_NAME.toString(),
                CategoryColumns.DESCRIPTION.toString());
        categoryListView.setItems(categories);
        checkDisable();
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

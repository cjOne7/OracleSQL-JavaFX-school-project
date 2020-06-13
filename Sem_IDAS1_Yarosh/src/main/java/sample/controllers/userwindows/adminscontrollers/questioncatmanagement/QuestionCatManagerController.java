package sample.controllers.userwindows.adminscontrollers.questioncatmanagement;

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
import sample.enums.CategoryColumns;
import sample.enums.QuestionCatColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class QuestionCatManagerController implements Initializable {
    private ObservableList<Category> categories = FXCollections.observableArrayList();
    public static int categoryId;

    @FXML
    private Button closeBtn;
    @FXML
    private ListView<Category> questionCatListView;
    @FXML
    private Button changeCategoryBtn;
    @FXML
    private Button deleteCategoryBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        questionCatListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        categories = Category.fillListView("SELECT * FROM ST58310.QUESTIONS_CTGY ORDER BY QUESTIONS_CAT_ID",
                QuestionCatColumns.QUESTIONS_CAT_ID.toString(),
                QuestionCatColumns.QUESTION_CAT_NAME.toString(),
                QuestionCatColumns.DESCRIPTION.toString());
        questionCatListView.setItems(categories);
        checkDisable();
    }

    private void checkDisable() {
        if (categories.size() >= 1) {
            changeDisable(false);
        }
    }

    private void changeDisable(final boolean state) {
        changeCategoryBtn.setDisable(state);
        deleteCategoryBtn.setDisable(state);
    }

    @FXML
    private void addCategory(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/questioncatmanagement/AddNewQuestionCatWindow.fxml", getClass(), false, "Add new category window", new Image("/images/admin_icon.png"));
        checkDisable();
    }

    @FXML
    private void changeCategory(ActionEvent event) {
        final Category category = questionCatListView.getSelectionModel().getSelectedItem();
        if (category == null) {
            Main.callAlertWindow("Warning", "Category is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            categoryId = category.getCategoryId();
            OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/questioncatmanagement/ChangeQuestionCatWindow.fxml", getClass(), false, "Change category window", new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void deleteCategory(ActionEvent event) {
        final Category category = questionCatListView.getSelectionModel().getSelectedItem();
        Category.deleteCategory(category, "DELETE FROM ST58310.QUESTIONS_CTGY WHERE QUESTIONS_CAT_ID = ?");
        categories.remove(category);
        if (categories.isEmpty()) {
            changeDisable(true);
        }
    }

    @FXML
    private void refreshList(ActionEvent event) {
        categories.clear();
        categories = Category.fillListView("SELECT * FROM ST58310.QUESTIONS_CTGY ORDER BY QUESTIONS_CAT_ID",
                QuestionCatColumns.QUESTIONS_CAT_ID.toString(),
                QuestionCatColumns.QUESTION_CAT_NAME.toString(),
                QuestionCatColumns.DESCRIPTION.toString());
        questionCatListView.setItems(categories);
        checkDisable();
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

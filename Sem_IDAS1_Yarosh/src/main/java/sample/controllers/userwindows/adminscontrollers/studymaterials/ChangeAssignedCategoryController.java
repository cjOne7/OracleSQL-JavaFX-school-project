package sample.controllers.userwindows.adminscontrollers.studymaterials;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Category;
import sample.enums.CategoryColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static sample.controllers.userwindows.adminscontrollers.studymaterials.AssignCategoryController.categoryId;
import static sample.controllers.userwindows.adminscontrollers.studymaterials.AssignCategoryController.studyMatId;

public class ChangeAssignedCategoryController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;

    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    @FXML
    private ListView<Category> categoriesListView;
    @FXML
    private Button applyChangesBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoriesListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        categoriesListView.setItems(categories);

        fillUnwrittenSubjectsList();
        if (categories.size() >= 1) {
            applyChangesBtn.setDisable(false);
        }
    }

    private void fillUnwrittenSubjectsList(){
        final String selectQuery = "SELECT CATEGORY_ID, CATEGORY_NAME FROM ST58310.CATEGORY WHERE CATEGORY_ID NOT IN (SELECT CATEGORY_CATEGORY_ID FROM ST58310.STUDY_MAT_CTGY WHERE STY_MTRL_STY_MTRL_ID = ?)";
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, studyMatId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                final int categoryId = resultSet.getInt(CategoryColumns.CATEGORY_ID.toString());
                final String categoryName = resultSet.getString(CategoryColumns.CATEGORY_NAME.toString());
                final Category category = new Category(categoryId, categoryName);
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void applyChanges(ActionEvent event) {
        final Category category = categoriesListView.getSelectionModel().getSelectedItem();
        if (category == null) {
            Main.callAlertWindow("Warning", "Category is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                executeQuery("DELETE FROM ST58310.STUDY_MAT_CTGY WHERE CATEGORY_CATEGORY_ID = ? AND STY_MTRL_STY_MTRL_ID = ?", categoryId, studyMatId);
                //эта строчка нужна, чтобы при удалении и последующем добавлении предметы перезаписывались, а не начали после первого же удаления просто записываться
                categoryId = category.getCategoryId();
                executeQuery("INSERT INTO ST58310.STUDY_MAT_CTGY values(?,?)", category.getCategoryId(), studyMatId);

                refreshList();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void executeQuery(final String query, @NotNull final int... values) throws SQLException {
        preparedStatement = dbManager.getConnection().prepareStatement(query);
        preparedStatement.setInt(1, values[0]);
        preparedStatement.setInt(2, values[1]);
        preparedStatement.execute();
    }

    private void refreshList(){
        categories.clear();
        fillUnwrittenSubjectsList();
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

package sample.controllers.adminscontrollers.studymaterials;

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
import oracle.ucp.proxy.annotation.Pre;
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Category;
import sample.dbtableclasses.StudyMatCtgy;
import sample.dbtableclasses.StudyMaterial;
import sample.dbtableclasses.UserSubject;
import sample.enums.CategoryColumns;
import sample.enums.StudyMatColumns;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AssignCategoryController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private final ObservableList<StudyMatCtgy> studyMatCtgies = FXCollections.observableArrayList();

    public static int categoryId;
    public static int studyMatId;

    @FXML
    private Button deleteRowBtn;
    @FXML
    private ListView<StudyMatCtgy> usersSubjectsListView;
    @FXML
    private Button changeCategoryBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usersSubjectsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        usersSubjectsListView.setItems(studyMatCtgies);

        fillStudyMatCategoryList();

        if (studyMatCtgies.size() >= 1) {
            changeDisable(false);
        }
    }

    private void fillStudyMatCategoryList() {
        final String selectQuery = "SELECT CATEGORY.CATEGORY_ID, CATEGORY.CATEGORY_NAME, STY_MTRL.STUDY_MATERIAL_ID, STY_MTRL.FILE_NAME, STY_MTRL.FILE_TYPE, STY_MTRL.CREATER, STY_MTRL.SUBJECT_SUBJECT_ID\n" +
                "FROM CATEGORY INNER JOIN Study_Mat_ctgy ON CATEGORY.CATEGORY_ID = Study_Mat_ctgy.CATEGORY_CATEGORY_ID\n" +
                "INNER JOIN STY_MTRL ON Study_Mat_ctgy.STY_MTRL_STY_MTRL_ID = STY_MTRL.STUDY_MATERIAL_ID";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int categoryId = resultSet.getInt(CategoryColumns.CATEGORY_ID.toString());
                final String categoryName = resultSet.getString(CategoryColumns.CATEGORY_NAME.toString());
                final Category category = new Category(categoryId, categoryName);

                final int studyMatId = resultSet.getInt(StudyMatColumns.STUDY_MATERIAL_ID.toString());
                final String fileName = resultSet.getString(StudyMatColumns.FILE_NAME.toString());
                final String fileType = resultSet.getString(StudyMatColumns.FILE_TYPE.toString());
                final String creator = resultSet.getString(StudyMatColumns.CREATER.toString());
                final int subjectId = resultSet.getInt(StudyMatColumns.SUBJECT_SUBJECT_ID.toString());
                final StudyMaterial studyMaterial = new StudyMaterial(studyMatId, fileName, fileType, creator, subjectId);

                final StudyMatCtgy studyMatCtgy = new StudyMatCtgy(category, studyMaterial);
                studyMatCtgies.add(studyMatCtgy);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addCategory(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/studymaterials/AddCategoryToMaterialsWindow.fxml", getClass(), false, "Add categories to study materials window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void changeCategory(ActionEvent event) {
        final StudyMatCtgy studyMatCtgy = usersSubjectsListView.getSelectionModel().getSelectedItem();
        if (studyMatCtgy == null) {
            Main.callAlertWindow("Warning", "Row is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            categoryId = studyMatCtgy.getCategory().getCategoryId();
            studyMatId = studyMatCtgy.getStudyMaterial().getStudyMatId();
            OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/studymaterials/ChangeAssignedCategoryWindow.fxml", getClass(), false, "Change assigned category(s) to study materials window", new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void deleteRow(ActionEvent event) {
        final StudyMatCtgy studyMatCtgy = usersSubjectsListView.getSelectionModel().getSelectedItem();
        if (studyMatCtgy == null) {
            Main.callAlertWindow("Warning", "Row is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String deleteQuery = "DELETE FROM ST58310.STUDY_MAT_CTGY WHERE CATEGORY_CATEGORY_ID = ? AND STY_MTRL_STY_MTRL_ID = ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, studyMatCtgy.getCategory().getCategoryId());
                preparedStatement.setInt(2, studyMatCtgy.getStudyMaterial().getStudyMatId());
                preparedStatement.execute();
                studyMatCtgies.remove(studyMatCtgy);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (studyMatCtgies.isEmpty()) {
            changeDisable(true);
        }
    }

    @FXML
    private void refreshList(ActionEvent event) {
        studyMatCtgies.clear();
        fillStudyMatCategoryList();
    }

    private void changeDisable(final boolean state) {
        changeCategoryBtn.setDisable(state);
        deleteRowBtn.setDisable(state);
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

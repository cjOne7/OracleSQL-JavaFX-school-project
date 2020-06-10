package sample.controllers.userwindows.adminscontrollers.studymaterials;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Category;
import sample.dbtableclasses.StudyMaterial;
import sample.enums.*;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddCategoryToMaterialsController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<StudyMaterial> studyMaterials = FXCollections.observableArrayList();

    @FXML
    private ListView<Category> categoriesListView;
    @FXML
    private ListView<StudyMaterial> studyMatListView;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListView(categoriesListView, categories);
        initializeListView(studyMatListView, studyMaterials);
        categoriesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        String selectQuery = "SELECT STUDY_MATERIAL_ID, FILE_NAME, FILE_TYPE, CREATER, SUBJECT_SUBJECT_ID FROM ST58310.STY_MTRL";
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int studyMatId = resultSet.getInt(StudyMatColumns.STUDY_MATERIAL_ID.toString());
                final String fileName = resultSet.getString(StudyMatColumns.FILE_NAME.toString());
                final String fileType = resultSet.getString(StudyMatColumns.FILE_TYPE.toString());
                final String creator = resultSet.getString(StudyMatColumns.CREATER.toString());
                final int subjectId = resultSet.getInt(StudyMatColumns.SUBJECT_SUBJECT_ID.toString());
                final StudyMaterial studyMaterial = new StudyMaterial(studyMatId, fileName, fileType, creator, subjectId);
                studyMaterials.add(studyMaterial);
            }

            selectQuery = "SELECT CATEGORY_ID, CATEGORY_NAME FROM ST58310.CATEGORY";
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int categoryId = resultSet.getInt(CategoryColumns.CATEGORY_ID.toString());
                final String categoryName = resultSet.getString(CategoryColumns.CATEGORY_NAME.toString());
                final Category category = new Category(categoryId, categoryName);
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private <T> void initializeListView(@NotNull final ListView<T> listView, final ObservableList<T> observableList) {
        listView.setItems(observableList);
        listView.setStyle(StylesEnum.FONT_STYLE.getStyle());
    }

    private void colorizeSubjectsListView() {
        final StudyMaterial studyMaterial = studyMatListView.getSelectionModel().getSelectedItem();
        final ObservableList<Category> categories = FXCollections.observableArrayList();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Student is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String selectQuery = "SELECT CATEGORY_ID, CATEGORY_NAME FROM ST58310.CATEGORY WHERE CATEGORY_ID IN (SELECT CATEGORY_CATEGORY_ID from ST58310.STUDY_MAT_CTGY where STY_MTRL_STY_MTRL_ID = ?)";
            try {
                preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
                preparedStatement.setInt(1, studyMaterial.getStudyMatId());
                final ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    final int categoryId = resultSet.getInt(CategoryColumns.CATEGORY_ID.toString());
                    final String categoryName = resultSet.getString(CategoryColumns.CATEGORY_NAME.toString());
                    final Category category = new Category(categoryId, categoryName);
                    categories.add(category);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            final List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++) {
                for (int j = 0; j < this.categories.size(); j++) {
                    if (this.categories.get(j).getCategoryId() == categories.get(i).getCategoryId()) {
                        indexes.add(j);
                    }
                }
            }
            categoriesListView.setCellFactory(param -> new ListCell<Category>() {
                        @Override
                        protected void updateItem(final Category item, final boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle(null);
                            } else {
                                setText(item.toString());
                                setStyle(indexes.contains(getIndex()) ? "-fx-background-color: cornsilk;" : "-fx-background-color: #00e6e6;");
                            }
                        }
                    }
            );
        }
    }

    @FXML
    private void addNewCategories(ActionEvent event) {
        int addedIndex = 0;
        final ObservableList<Category> notAddedCategories = FXCollections.observableArrayList();

        final StudyMaterial studyMaterial = studyMatListView.getSelectionModel().getSelectedItem();
        final ObservableList<Category> categories = categoriesListView.getSelectionModel().getSelectedItems();

        if (studyMaterial == null || categories.isEmpty()) {
            Main.callAlertWindow("Warning", "Study material or category is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                for (final Category value : categories) {
                    if (checkForExistingRecord(studyMaterial.getStudyMatId(), value.getCategoryId())) {
                        final String insertQuery = "INSERT INTO ST58310.STUDY_MAT_CTGY values(?,?)";
                        preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
                        preparedStatement.setInt(1, value.getCategoryId());
                        preparedStatement.setInt(2, studyMaterial.getStudyMatId());
                        preparedStatement.execute();
                        addedIndex++;
                    } else {
                        notAddedCategories.add(value);
                    }
                }
                if (addedIndex == categories.size()) {
                    Main.callAlertWindow("Successful adding", "The new record has been added successfully!", Alert.AlertType.INFORMATION, "/images/information_icon.png");
                } else {
                    final StringBuilder stringBuilder = new StringBuilder();
                    notAddedCategories.forEach(category -> stringBuilder.append(category).append("\n"));
                    Main.callAlertWindow("Something went wrong",
                            "The one or more records have NOT been added successfully because of study materials had written this category(s) earlier!\nNot added items:\n" + stringBuilder.toString(),
                            Alert.AlertType.WARNING, "/images/warning_icon.png");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        colorizeSubjectsListView();
    }

    private boolean checkForExistingRecord(final int userId, final int subjectId) throws SQLException {
        final String selectQuery = "SELECT * FROM ST58310.STUDY_MAT_CTGY WHERE STY_MTRL_STY_MTRL_ID = ? AND CATEGORY_CATEGORY_ID = ?";
        preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, subjectId);
        resultSet = preparedStatement.executeQuery();
        return !resultSet.next();
    }

    @FXML
    private void displayWrittenSubjects(MouseEvent event) {
        colorizeSubjectsListView();
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        colorizeSubjectsListView();
    }

    @FXML
    private void keyReleased(KeyEvent event) {
        colorizeSubjectsListView();
    }

    @FXML
    private void close(ActionEvent event) { ((Stage) closeBtn.getScene().getWindow()).close(); }
}

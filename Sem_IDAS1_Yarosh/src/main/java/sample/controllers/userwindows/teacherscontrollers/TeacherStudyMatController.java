package sample.controllers.userwindows.teacherscontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.Checker;
import sample.SelectStudyMaterials;
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.controllers.userwindows.adminscontrollers.studymaterials.CreateStudyMaterialsController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.StudyMaterial;
import sample.dbtableclasses.Subject;
import sample.enums.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TeacherStudyMatController implements Initializable {

    private final DbManager dbManager = new DbManager();

    private ObservableList<StudyMaterial> sortedList = FXCollections.observableArrayList();
    private ObservableList<StudyMaterial> studyMaterials = FXCollections.observableArrayList();
    private ObservableList<Integer> teacherSubjectIdList = FXCollections.observableArrayList();
    private List<Subject> subjectList = new ArrayList<>();
    private final SelectStudyMaterials selectStudyMaterials = new SelectStudyMaterials();

    private File file;

    @FXML
    private ListView<StudyMaterial> materialsListView;
    @FXML
    private Button loadMaterialBtn;
    @FXML
    private Button updateMaterialBtn;
    @FXML
    private Button deleteMaterialBtn;
    @FXML
    private Button downloadFileBtn;
    @FXML
    private Button openMaterialBtn;
    @FXML
    private Button openDiscussionBtn;
    @FXML
    private Button openQuizBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private ComboBox<String> subjectComboBox;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField numberOfPagesTextField;
    @FXML
    private CheckBox updateAdditionalInfoCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        materialsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        subjectComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());

        //set listener to sort study materials
        subjectComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            sortedList = studyMaterials.stream()
                    .filter(studyMaterial -> getSubjectId() == studyMaterial.getSubjectId())
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            materialsListView.setItems(sortedList);

            if (teacherSubjectIdList.contains(getSubjectId())) {
                changeDisableAllBtn(false);
            } else {
                changeDisableAllBtn(true);//disable all buttons
                changeDisable(false);//to enable buttons just for downloading and viewing
            }
        });

        final String selectQuery = "SELECT SUBJECT_ID FROM ST58310.SUBJECT WHERE SUBJECT_ID " +
                "IN (SELECT SUBJECT_SUBJECT_ID FROM ST58310.USER_SUBJECT WHERE USER_USER_ID = ?)";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, TeacherWindowController.userId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
                teacherSubjectIdList.add(subjectId);
            }

            subjectList = Subject.getAllSubjectList();//get all subjects list from DB
            studyMaterials = selectStudyMaterials.getStudyMaterials();//get all study materials from DB
            materialsListView.setItems(studyMaterials);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //fill combobox using special toString() method
        subjectList.forEach(subject -> subjectComboBox.getItems().add(subject.toComboBoxString()));
        subjectComboBox.setValue(subjectList.get(0).toComboBoxString());
    }

    private int getSubjectId() {
        final String subjectNameAbbreviation = subjectComboBox.getValue();
        final int index = subjectNameAbbreviation.indexOf('/');
        final String name = subjectNameAbbreviation.substring(0, index);
        final String abbreviation = subjectNameAbbreviation.substring(index + 1);
        for (final Subject subject : subjectList) {
            if (subject.getName().equals(name) && subject.getAbbreviation().equals(abbreviation)) {
                return subject.getSubjectId();
            }
        }
        return -1;
    }

    private void changeDisableAllBtn(final boolean state) {
        loadMaterialBtn.setDisable(state);
        updateMaterialBtn.setDisable(state);
        deleteMaterialBtn.setDisable(state);
        updateAdditionalInfoCheckBox.setDisable(state);
        changeDisable(state);
    }

    private void changeDisable(final boolean state) {
        downloadFileBtn.setDisable(state);
        openMaterialBtn.setDisable(state);
        openDiscussionBtn.setDisable(state);
        openQuizBtn.setDisable(state);
    }

    @FXML
    private void deleteMaterial(ActionEvent event) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        selectStudyMaterials.deleteMaterial(studyMaterial);
        studyMaterials.removeAll(studyMaterials.stream().filter(studyMat -> studyMat.getStudyMatId() == studyMaterial.getStudyMatId()).collect(Collectors.toList()));
        try {
            DbManager.removeRemainingDataFromDB(
                    "SELECT COMMENT_ID FROM ST58310.COMMENT_DISCUSSION " +
                            "RIGHT JOIN THE_COMMENT ON COMMENT_DISCUSSION.THE_COMMENT_COMMENT_ID = THE_COMMENT.COMMENT_ID " +
                            "WHERE COMMENT_ID IS NOT NULL AND THE_COMMENT_COMMENT_ID IS NULL",
                    "DELETE FROM ST58310.THE_COMMENT WHERE COMMENT_ID = ?",
                    CommentColumns.COMMENT_ID.toString());
            DbManager.removeRemainingDataFromDB(
                    "SELECT QUESTION_QUESTION_ID, QUESTION_ID FROM ST58310.QUIZZES_QUESTION " +
                            "RIGHT JOIN QUESTION ON QUIZZES_QUESTION.QUESTION_QUESTION_ID = QUESTION.QUESTION_ID " +
                            "WHERE QUESTION_ID IS NOT NULL AND QUESTION_QUESTION_ID IS NULL",
                    "DELETE FROM ST58310.QUESTION WHERE QUESTION_ID = ?",
                    QuestionColumns.QUESTION_ID.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCurrentDisplayedValues();
        if (studyMaterials.isEmpty()) {
            changeDisableAllBtn(true);
        }
    }

    @FXML
    private void downloadFile(ActionEvent event) {
        selectStudyMaterials.downloadFile(materialsListView.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void getInformationFromList(MouseEvent event) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final int numberOfPages = studyMaterial.getNumberOfPages();
            numberOfPagesTextField.setText(numberOfPages == 0 ? "" : numberOfPages + "");
            final String description = studyMaterial.getDescription();
            descriptionTextArea.setText(description == null ? "" : description);
            subjectComboBox.setValue(studyMaterial.getSubjectName());
        }
    }

    @FXML
    private void loadMaterial(ActionEvent event) {
        file = chooseFile();
        if (file != null) {
            try {
                insertInStyMtrl();
                descriptionTextArea.clear();
                numberOfPagesTextField.clear();
            } catch (SQLException | FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Main.callAlertWindow("Information", "File is not chosen!", Alert.AlertType.INFORMATION, "/images/information_icon.png");
        }
        if (studyMaterials.size() >= 1) {
            changeDisableAllBtn(false);
        }
    }

    private File chooseFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("DOC/DOCX", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("TXT", "*.txt"),
                new FileChooser.ExtensionFilter("TXT, DOC/DOCX, PDF", "*.txt", "*.doc", "*.docx", "*.pdf"));
        return fileChooser.showOpenDialog(null);
    }

    private void insertInStyMtrl() throws SQLException, FileNotFoundException {
        final String fileName = getFileName(file.getName());
        final String fileType = getFileType(file.getName());

        final String insertQuery = "INSERT INTO ST58310.STY_MTRL (FILE_NAME, THE_FILE, FILE_TYPE, CREATER, NUMBER_OF_PAGES, DESCRIPTION, SUBJECT_SUBJECT_ID) VALUES (?,?,?,?,?,?,?)";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
        preparedStatement.setString(1, fileName);

        final InputStream fileInputStream = new FileInputStream(file);
        preparedStatement.setBlob(2, fileInputStream, file.length());

        preparedStatement.setString(3, fileType);

        final String creator = getNameAndSurname();
        preparedStatement.setString(4, creator);

        final String numberOfPages = numberOfPagesTextField.getText().trim();
        prepareIntField(preparedStatement, numberOfPages, 5);

        final String description = descriptionTextArea.getText().trim();
        Checker.checkTextField(description.isEmpty(), 6, description, preparedStatement);

        preparedStatement.setInt(7, getSubjectId());
        preparedStatement.execute();

        final String selectQuery = "SELECT MAX(STUDY_MATERIAL_ID) AS STUDY_MATERIAL_ID FROM ST58310.STY_MTRL";
        final PreparedStatement preparedStatement1 = dbManager.getConnection().prepareStatement(selectQuery);
        final ResultSet resultSet = preparedStatement1.executeQuery();
        int subjectId = 0;
        if (resultSet.next()) {
            subjectId = resultSet.getInt(StudyMatColumns.STUDY_MATERIAL_ID.toString());
        }
        final StudyMaterial studyMaterial = new StudyMaterial(subjectId, fileName, fileType, creator,
                numberOfPages.isEmpty() ? 0 : Integer.parseInt(numberOfPages), description, getSubjectId());
        studyMaterials.add(studyMaterial);

        updateCurrentDisplayedValues();
    }

    private void updateCurrentDisplayedValues() {
        final String value = subjectComboBox.getValue();
        subjectComboBox.setValue(subjectList.get(0).toComboBoxString());
        subjectComboBox.setValue(value);
    }

    @NotNull
    private String getNameAndSurname() throws SQLException {
        final String selectQuery = "SELECT NAME, SURNAME FROM ST58310.ELSA_USER WHERE USER_ID = ?";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        preparedStatement.setInt(1, TeacherWindowController.userId);
        final ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
            final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
            return name + " " + surname;
        } else {
            return "";
        }
    }

    @NotNull
    private String getFileName(@NotNull final String fullFileName) {
        final int index = fullFileName.indexOf('.');
        return fullFileName.substring(0, index);
    }

    @NotNull
    private String getFileType(@NotNull final String fullFileName) {
        final int index = fullFileName.indexOf('.');
        return fullFileName.substring(index + 1);
    }

    private void prepareIntField(final PreparedStatement preparedStatement, @NotNull final String number, final int index) throws SQLException {
        if (number.isEmpty()) {
            preparedStatement.setNull(index, Types.NULL);
        } else {
            preparedStatement.setInt(index, Integer.parseInt(number));
        }
    }

    @FXML
    private void updateMaterial(ActionEvent event) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                if (updateAdditionalInfoCheckBox.isSelected()) {
                    String updateQuery = "UPDATE ST58310.STY_MTRL SET NUMBER_OF_PAGES = ?, CHANGER = ?, DESCRIPTION = ?, SUBJECT_SUBJECT_ID = ?, DATE_OF_CHANGES = SYSDATE WHERE STUDY_MATERIAL_ID = ?";
                    PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);

                    prepareAdditionalParameters(preparedStatement, studyMaterial, 1, 2, 3, 4, 5);
                    preparedStatement.execute();
                    updateList(studyMaterial);
                } else {
                    file = chooseFile();
                    if (file == null) {
                        Main.callAlertWindow("Information", "File for updating is not chosen!", Alert.AlertType.INFORMATION, "/images/information_icon.png");
                    } else {
                        final String updateQuery = "UPDATE ST58310.STY_MTRL SET FILE_NAME = ?, THE_FILE = ?, FILE_TYPE = ?, NUMBER_OF_PAGES = ?, CHANGER = ?, DESCRIPTION = ?, SUBJECT_SUBJECT_ID = ?, DATE_OF_CHANGES = SYSDATE WHERE STUDY_MATERIAL_ID = ?";
                        final String fileName = getFileName(file.getName());
                        final String fileType = getFileType(file.getName());
                        PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);

                        preparedStatement.setString(1, fileName);

                        final InputStream fileInputStream = new FileInputStream(file);
                        preparedStatement.setBlob(2, fileInputStream, file.length());
                        preparedStatement.setString(3, fileType);

                        prepareAdditionalParameters(preparedStatement, studyMaterial, 4, 5, 6, 7, 8);

                        preparedStatement.execute();
                        updateList(studyMaterial);
                    }
                }
            } catch (SQLException | FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareAdditionalParameters(final PreparedStatement preparedStatement, @NotNull final StudyMaterial studyMaterial, @NotNull final int... indexes) throws SQLException {
        final String numberOfPages = numberOfPagesTextField.getText().trim();
        prepareIntField(preparedStatement, numberOfPages, indexes[0]);

        preparedStatement.setString(indexes[1], getNameAndSurname());

        final String description = descriptionTextArea.getText().trim();
        Checker.checkTextField(description.isEmpty(),indexes[2], description, preparedStatement);

        preparedStatement.setInt(indexes[3], getSubjectId());
        preparedStatement.setInt(indexes[4], studyMaterial.getStudyMatId());
    }

    private void updateList(@NotNull final StudyMaterial studyMaterial) throws SQLException {
        final String selectQuery = "SELECT * FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";//update list view
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        preparedStatement.setInt(1, studyMaterial.getStudyMatId());
        final ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            studyMaterials.removeAll(studyMaterials.stream().filter(studyMat -> studyMat.getStudyMatId() == studyMaterial.getStudyMatId()).collect(Collectors.toList()));
            studyMaterials.add(createInstanceStyMtrl(resultSet));
        }
        updateCurrentDisplayedValues();
    }

    @NotNull
    private StudyMaterial createInstanceStyMtrl(@NotNull final ResultSet resultSet) throws SQLException {
        final int studyMatId = resultSet.getInt(StudyMatColumns.STUDY_MATERIAL_ID.getColumnName());
        final String fileName = resultSet.getString(StudyMatColumns.FILE_NAME.getColumnName());
        final String fileType = resultSet.getString(StudyMatColumns.FILE_TYPE.getColumnName());
        final Date dateOfCreation = resultSet.getDate(StudyMatColumns.DATE_OF_CREATION.getColumnName());
        final String creater = resultSet.getString(StudyMatColumns.CREATER.getColumnName());
        final int numberOfPages = resultSet.getInt(StudyMatColumns.NUMBER_OF_PAGES.getColumnName());
        final Date dateOfChanges = resultSet.getDate(StudyMatColumns.DATE_OF_CHANGES.getColumnName());
        final String changer = resultSet.getString(StudyMatColumns.CHANGER.getColumnName());
        final String description = resultSet.getString(StudyMatColumns.DESCRIPTION.getColumnName());
        final int subjectId = resultSet.getInt(StudyMatColumns.SUBJECT_SUBJECT_ID.getColumnName());
        return new StudyMaterial(studyMatId, fileName, fileType, dateOfCreation, creater, numberOfPages, dateOfChanges, changer, description, subjectId);
    }

    @FXML
    private void openMaterial(ActionEvent event) {
        openWindow("/fxmlfiles/userwindows/adminsfxmls/studymaterials/StudyMaterialWindow.fxml", "Study materials window");
    }

    @FXML
    private void openDiscussion(ActionEvent event) {
        openWindow("/fxmlfiles/userwindows/adminsfxmls/DiscussionWindow.fxml", "Discussion window");
    }

    private void openWindow(final String fxmlFilePath, final String title) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            CreateStudyMaterialsController.studyMatId = studyMaterial.getStudyMatId();
            OpenNewWindow.openNewWindow(fxmlFilePath, getClass(), false, title, new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void openQuiz(ActionEvent event) {
        openWindow("/fxmlfiles/userwindows/adminsfxmls/quizmanagement/QuizManagementWindow.fxml", "Quiz management window");
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

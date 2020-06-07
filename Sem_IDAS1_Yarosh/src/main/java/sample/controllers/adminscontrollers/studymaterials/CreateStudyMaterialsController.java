package sample.controllers.adminscontrollers.studymaterials;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.controllers.adminscontrollers.AdminController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.StudyMaterial;
import sample.dbtableclasses.Subject;
import sample.enums.*;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CreateStudyMaterialsController implements Initializable {

    private final DbManager dbManager = new DbManager();

    private final ObservableList<StudyMaterial> studyMaterials = FXCollections.observableArrayList();
    private final List<Subject> subjectList = new ArrayList<>();

    private File file;
    public static int studyMatId;

    @FXML
    private ListView<StudyMaterial> materialsListView;
    @FXML
    private ComboBox<String> subjectComboBox;
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
    private Button assignCategoryBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField numberOfPagesTextField;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        materialsListView.setItems(studyMaterials);
        materialsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());

        subjectComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());

        numberOfPagesTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    Integer.parseInt(newValue);
                }
            } catch (NumberFormatException e) {
                numberOfPagesTextField.setText(oldValue);
            }
        });

        try {
            final String selectQuery = "SELECT SUBJECT_ID, NAME, ABBREVIATION FROM ST58310.SUBJECT ORDER BY YEAR, SEMESTER";
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
                final String subjectName = resultSet.getString(SubjectColumns.NAME.toString());
                final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
                final Subject subject = new Subject(subjectId, subjectName, abbreviation);
                subjectList.add(subject);
            }
            fillListView();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        subjectList.forEach(subject -> subjectComboBox.getItems().add(subject.toComboBoxString()));
        subjectComboBox.setValue(subjectList.get(0).toComboBoxString());

        if (studyMaterials.size() >= 1) {
            changeDisable(false);
        }
    }

    private void changeDisable(final boolean state) {
        updateMaterialBtn.setDisable(state);
        deleteMaterialBtn.setDisable(state);
        downloadFileBtn.setDisable(state);
        openMaterialBtn.setDisable(state);
        assignCategoryBtn.setDisable(state);
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
            changeDisable(false);
        }
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
        prepareNumberOfPages(preparedStatement, numberOfPages, 5);

        final String description = descriptionTextArea.getText().trim();
        prepareDescriptionField(preparedStatement, description, 6);

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

    @NotNull
    private String getNameAndSurname() throws SQLException {
        final String selectQuery = "SELECT NAME, SURNAME FROM ST58310.ELSA_USER WHERE USER_ID = ?";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        preparedStatement.setInt(1, AdminController.curUserId);
        final ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
            final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
            return name + " " + surname;
        } else {
            return "";
        }
    }

    @FXML
    private void downloadFile(ActionEvent event) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String selectQuery = "SELECT THE_FILE FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
                preparedStatement.setInt(1, studyMaterial.getStudyMatId());
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    final Blob blob = resultSet.getBlob(StudyMatColumns.THE_FILE.getColumnName());
                    final File file = getFile(blob, studyMaterial.getFileName(), studyMaterial.getFileType());
                    if (file != null) {
                        Main.callAlertWindow("Information", "Chosen file has been downloaded! Its filepath: " + file.getPath(), Alert.AlertType.INFORMATION, "/images/information_icon.png");
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getFile(final Blob blob, final String fileName, final String fileType) throws SQLException, IOException {
        if (blob != null) {
            final InputStream input = blob.getBinaryStream();
            final String filePath = System.getProperty("user.home") + "/Downloads/" + fileName + '.' + fileType;
            final File file = new File(filePath);
            final OutputStream fos = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            while (input.read(buffer) > 0) {
                fos.write(buffer);
            }
            return file;
        } else {
            return null;
        }
    }

    private File chooseFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("TXT", "*.txt"),
                new ExtensionFilter("DOC/DOCX", "*.doc", "*.docx"),
                new ExtensionFilter("PDF", "*.pdf"),
                new ExtensionFilter("TXT, DOC/DOCX, PDF", "*.txt", "*.doc", "*.docx", "*.pdf"));
        return fileChooser.showOpenDialog(null);
    }

    @FXML
    private void deleteMaterial(ActionEvent event) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String deleteQuery = "DELETE FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, studyMaterial.getStudyMatId());
                preparedStatement.execute();

                studyMaterials.removeAll(studyMaterials.stream().filter(studyMat -> studyMat.getStudyMatId() == studyMaterial.getStudyMatId()).collect(Collectors.toList()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (studyMaterials.isEmpty()) {
            changeDisable(true);
        }
    }

    @FXML
    private void openMaterial(ActionEvent event) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            studyMatId = studyMaterial.getStudyMatId();
            OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/studymaterials/StudyMaterialWindow.fxml", getClass(), false, "Study materials window", new Image("/images/admin_icon.png"));
        }
    }

    private void fillListView() throws SQLException {
        final String selectQuery = "SELECT * FROM ST58310.STY_MTRL";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        final ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            studyMaterials.add(createInstanceStyMtrl(resultSet));
        }
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

    private void refreshList() {
        studyMaterials.clear();
        try {
            fillListView();
        } catch (SQLException e) {
            e.printStackTrace();
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

    @FXML
    private void updateMaterial(ActionEvent event) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            file = chooseFile();
            if (file == null) {
                Main.callAlertWindow("Information", "File for updating is not chosen!", Alert.AlertType.INFORMATION, "/images/information_icon.png");
            } else {
                final String updateQuery = "UPDATE ST58310.STY_MTRL SET FILE_NAME = ?, THE_FILE = ?, FILE_TYPE = ?, NUMBER_OF_PAGES = ?, CHANGER = ?, DESCRIPTION = ?, SUBJECT_SUBJECT_ID = ?, DATE_OF_CHANGES = SYSDATE WHERE STUDY_MATERIAL_ID = ?";
                try {
                    final String fileName = getFileName(file.getName());
                    final String fileType = getFileType(file.getName());
                    final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);

                    preparedStatement.setString(1, fileName);

                    final InputStream fileInputStream = new FileInputStream(file);
                    preparedStatement.setBlob(2, fileInputStream, file.length());

                    preparedStatement.setString(3, fileType);

                    final String numberOfPages = numberOfPagesTextField.getText().trim();
                    prepareNumberOfPages(preparedStatement, numberOfPages, 4);

                    preparedStatement.setString(5, getNameAndSurname());

                    final String description = descriptionTextArea.getText().trim();
                    prepareDescriptionField(preparedStatement, description, 6);

                    preparedStatement.setInt(7, getSubjectId());

                    preparedStatement.setInt(8, studyMaterial.getStudyMatId());

                    preparedStatement.execute();
                    refreshList();
                } catch (SQLException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void prepareNumberOfPages(final PreparedStatement preparedStatement, @NotNull final String number, final int index) throws SQLException {
        if (number.isEmpty()) {
            preparedStatement.setNull(index, Types.NULL);
        } else {
            preparedStatement.setInt(index, Integer.parseInt(number));
        }
    }

    private void prepareDescriptionField(final PreparedStatement preparedStatement, @NotNull final String description, final int index) throws SQLException {
        if (description.trim().isEmpty()) {
            preparedStatement.setNull(index, Types.NULL);
        } else {
            preparedStatement.setString(index, description);
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

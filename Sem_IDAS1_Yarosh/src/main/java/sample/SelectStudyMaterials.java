package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.StudyMaterial;
import sample.dbtableclasses.Subject;
import sample.enums.StudyMatColumns;
import sample.enums.SubjectColumns;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SelectStudyMaterials {

    private final DbManager dbManager = new DbManager();

    @NotNull //return all study materials
    public ObservableList<StudyMaterial> getStudyMaterials() throws SQLException {
        final ObservableList<StudyMaterial> studyMaterials = FXCollections.observableArrayList();
        final String selectQuery = "SELECT * FROM ST58310.STY_MTRL ORDER BY FILE_NAME";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        final ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            studyMaterials.add(createInstanceStyMtrl(resultSet));
        }
        return studyMaterials;
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

    //download file to folder 'Downloads'
    public void downloadFile(final StudyMaterial studyMaterial) {
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String selectQuery = "SELECT THE_FILE FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
                preparedStatement.setInt(1, studyMaterial.getStudyMatId());
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    final Blob blob = resultSet.getBlob(StudyMatColumns.THE_FILE.getColumnName());//get Blob from db
                    final File file = getFile(blob, studyMaterial.getFileName(), studyMaterial.getFileType());//convert Blob to file
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

    public void deleteMaterial(final StudyMaterial studyMaterial) {
        if (studyMaterial == null) {//if material is not choosen
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String deleteQuery = "DELETE FROM ST58310.STY_MTRL WHERE STUDY_MATERIAL_ID = ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, studyMaterial.getStudyMatId());
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

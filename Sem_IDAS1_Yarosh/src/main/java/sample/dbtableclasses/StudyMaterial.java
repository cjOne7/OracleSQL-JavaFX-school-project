package sample.dbtableclasses;

import org.jetbrains.annotations.NotNull;
import sample.databasemanager.DbManager;
import sample.enums.SubjectColumns;

import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class StudyMaterial {
    private int studyMatId;
    private String fileName;
    private File theFile;
    private String fileType;
    private Date dateOfCreation;
    private String creater;
    private int numberOfPages;
    private Date dateOfChange;
    private String changer;
    private String description;
    private int subjectId;

    public StudyMaterial(final int studyMatId,
                         final String fileName,
                         final String fileType,
                         final Date dateOfCreation,
                         final String creater,
                         final int numberOfPages,
                         final Date dateOfChange,
                         final String changer,
                         final String description,
                         final int subjectId) {
        this.studyMatId = studyMatId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.dateOfCreation = dateOfCreation;
        this.creater = creater;
        this.numberOfPages = numberOfPages;
        this.dateOfChange = dateOfChange;
        this.changer = changer;
        this.description = description;
        this.subjectId = subjectId;
    }

    public StudyMaterial(final int studyMatId,
                         final String fileName,
                         final String fileType,
                         final String creater,
                         final int numberOfPages,
                         final String description,
                         final int subjectId) {
        this.studyMatId = studyMatId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.creater = creater;
        this.numberOfPages = numberOfPages;
        this.description = description;
        this.subjectId = subjectId;
    }

    public StudyMaterial(final int studyMatId,
                         final String fileName,
                         final String fileType,
                         final String creater,
                         final int subjectId) {
        this.studyMatId = studyMatId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.creater = creater;
        this.subjectId = subjectId;
    }

    public int getStudyMatId() {
        return studyMatId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getCreater() {
        return creater;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public String getDescription() {
        return description;
    }

    @NotNull
    public String getSubjectName() {
        final DbManager dbManager = new DbManager();
        final String selectQuery = "SELECT NAME, ABBREVIATION FROM ST58310.SUBJECT WHERE SUBJECT_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, subjectId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final String name = resultSet.getString(SubjectColumns.NAME.toString());
                final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
                return name + '/' + abbreviation;
            } else {
                return "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String toString() {
        return "File name: " + fileName +
                (fileType == null ? "" : "." + fileType) +
                (dateOfCreation == null ? "" : ", date of creation: " + dateOfCreation.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))) +
                (creater == null ? "" : ", creater: " + creater) +
                (numberOfPages == 0 ? "" : ", number of pages: " + numberOfPages) +
                (subjectId == 0 ? "" : ", subject: " + getSubjectName()) +
                (dateOfChange == null ? "" : ", date of change: " + dateOfChange.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))) +
                (changer == null ? "" : ", changer: " + changer) +
                (description == null || description.isEmpty() ? "" : ", description: " + description);
    }
}

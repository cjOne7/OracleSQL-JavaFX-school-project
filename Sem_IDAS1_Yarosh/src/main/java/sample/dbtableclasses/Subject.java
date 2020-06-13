package sample.dbtableclasses;

import org.jetbrains.annotations.NotNull;
import sample.databasemanager.DbManager;
import sample.enums.SubjectColumns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Subject {
    private int subjectId;
    private String name;
    private String abbreviation;
    private int credits;
    private int semester;
    private int year;
    private String description;

    public Subject(final int subjectId,
                   final String name,
                   final String abbreviation,
                   final int credits,
                   final int semester,
                   final int year,
                   final String description) {
        this.subjectId = subjectId;
        this.name = name;
        this.abbreviation = abbreviation;
        this.credits = credits;
        this.semester = semester;
        this.year = year;
        this.description = description;
    }

    public Subject(final int subjectId, final String name, final String abbreviation) {
        this.subjectId = subjectId;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public Subject(final int subjectId) {
        this.subjectId = subjectId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public int getSemester() {
        return semester;
    }

    public int getYear() {
        return year;
    }

    @NotNull
    public static List<Subject> getAllSubjectList() throws SQLException {
        final DbManager dbManager = new DbManager();
        final String selectQuery = "SELECT SUBJECT_ID, SUBJECT_NAME, ABBREVIATION FROM ST58310.SUBJECT ORDER BY YEAR, SEMESTER";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        final ResultSet resultSet = preparedStatement.executeQuery();
        final List<Subject> subjectList = new ArrayList<>();
        while (resultSet.next()) {
            final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
            final String subjectName = resultSet.getString(SubjectColumns.SUBJECT_NAME.toString());
            final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
            final Subject subject = new Subject(subjectId, subjectName, abbreviation);
            subjectList.add(subject);
        }
        return subjectList;
    }

    @Override
    public String toString() {
        return "Subject's name: " + name +
                (abbreviation == null || abbreviation.isEmpty() ? "" : "/" + abbreviation) +
                (credits == 0 ? "" : ", credits: " + credits) +
                (year == 0 ? "" : ", year: " + year) +
                (semester == 0 ? "" : ", semester: " + semester) +
                (description == null || description.isEmpty() ? "" : ". Description: " + description);
    }

    public String toComboBoxString() {
        return name + "/" + abbreviation;
    }
}

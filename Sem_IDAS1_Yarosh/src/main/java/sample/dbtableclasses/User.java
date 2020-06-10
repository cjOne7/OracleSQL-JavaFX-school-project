package sample.dbtableclasses;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import sample.controllers.userwindows.teacherscontrollers.TeacherWindowController;
import sample.databasemanager.DbManager;
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.enums.SubjectColumns;
import sample.enums.UserSubjectColumns;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String surname;
    private String login;
    private String password;
    private String email;
    private String telephone;
    private String about;

    private Blob image;

    private int roleId;
    private int userId;

    public User(final String name,
                final String surname,
                final String login,
                final String password,
                final String email,
                final String telephone,
                final String about,
                final Blob image,
                final int roleId,
                final int userId) {
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
        this.about = about;
        this.image = image;
        this.roleId = roleId;
        this.userId = userId;
    }

    public User(final String name,
                final String surname,
                final String email,
                final String telephone,
                final String login,
                final int roleId,
                final int userId) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.telephone = telephone;
        this.login = login;
        this.roleId = roleId;
        this.userId = userId;
    }

    public User(final String surname, final String login, final int userId, final int roleId) {
        this.surname = surname;
        this.login = login;
        this.userId = userId;
        this.roleId = roleId;
    }

    public User(final String name,
                final String surname,
                final String login,
                final int userId,
                final int roleId) {
        this(surname, login, userId, roleId);
        this.name = name;
    }

    public User(final String name,
                final String surname,
                final String email,
                final int userId) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.userId = userId;
    }

    public User(final String name,
                final String surname,
                final String email,
                final int userId,
                final String login) {
        this(name, surname, email, userId);
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public String getLogin() {
        return login;
    }

    public int getRoleId() {
        return roleId;
    }

    public String getPassword() {
        return password;
    }

    public int getUserId() {
        return userId;
    }

    public String getSurname() {
        return surname;
    }

    public void setRoleId(final int roleId) {
        this.roleId = roleId;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @NotNull
    public static ObservableList<User> fillStudentsList() throws SQLException {
        final DbManager dbManager = new DbManager();
        final ObservableList<User> students = FXCollections.observableArrayList();

        String selectQuery = "SELECT ELSA_USER.NAME, ELSA_USER.SURNAME, ELSA_USER.EMAIL, ELSA_USER.USER_ID, SUBJECT.SUBJECT_ID\n" +
                "FROM ELSA_USER\n" +
                "INNER JOIN USER_SUBJECT ON (ELSA_USER.USER_ID = USER_SUBJECT.USER_USER_ID AND ROLE_ID = 2)\n" +
                "INNER JOIN SUBJECT ON (SUBJECT.SUBJECT_ID = USER_SUBJECT.SUBJECT_SUBJECT_ID)";
        PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        ResultSet resultSet = preparedStatement.executeQuery();
        final List<UserSubject> userSubjects = new ArrayList<>();
        while (resultSet.next()) {
            final int userId = resultSet.getInt(ElsaUserColumns.USER_ID.toString());
            final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
            final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
            final String email = resultSet.getString(ElsaUserColumns.EMAIL.toString());

            final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
            final UserSubject userSubject = new UserSubject(new User(name, surname, email, userId), new Subject(subjectId));
            userSubjects.add(userSubject);
        }

        String selectQuery1 = "SELECT SUBJECT_SUBJECT_ID FROM ST58310.USER_SUBJECT WHERE USER_USER_ID = ?";
        PreparedStatement preparedStatement1 = dbManager.getConnection().prepareStatement(selectQuery1);
        preparedStatement1.setInt(1, TeacherWindowController.userId);
        resultSet = preparedStatement1.executeQuery();
        final List<Integer> teacherSubjectsId = new ArrayList<>();
        while (resultSet.next()) {
            final int subjectId = resultSet.getInt(UserSubjectColumns.SUBJECT_SUBJECT_ID.toString());
            teacherSubjectsId.add(subjectId);
        }

        for (int i = 0; i < teacherSubjectsId.size(); i++) {
            for (int j = 0; j < userSubjects.size(); j++) {
                if (teacherSubjectsId.get(i) == userSubjects.get(j).getSubject().getSubjectId()) {
                    students.add(userSubjects.get(j).getUser());
                }
            }
        }
        return students;
    }

    @Override
    public String toString() {
        return "User's name: " + name +
                ", surname: " + surname +
                (telephone == null ? "" : ", telephone: " + telephone) +
                (email == null ? "" : ", email: " + email) +
                (login == null ? "" : ", login: " + login) +
                (roleId == 0 ? "" : ", role: " + Role.getRole(roleId));
    }
}

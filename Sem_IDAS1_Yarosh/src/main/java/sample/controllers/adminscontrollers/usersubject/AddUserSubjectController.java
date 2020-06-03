package sample.controllers.adminscontrollers.usersubject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Subject;
import sample.dbtableclasses.User;
import sample.enums.ElsaUserColumns;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddUserSubjectController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private final ObservableList<User> students = FXCollections.observableArrayList();
    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();

    @FXML
    private ListView<User> studentsListView;
    @FXML
    private ListView<Subject> subjectsListView;
    @FXML
    private Button cancelBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListView(studentsListView, students);
        initializeListView(subjectsListView, subjects);
        subjectsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        String selectQuery = "SELECT USER_ID, NAME, SURNAME, LOGIN FROM ST58310.ELSA_USER WHERE ROLE_ID = 2";
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int userId = resultSet.getInt(ElsaUserColumns.USER_ID.toString());
                final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
                final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
                final String login = resultSet.getString(ElsaUserColumns.LOGIN.toString());
                final User student = new User(name, surname, login, userId);
                students.add(student);
            }

            selectQuery = "SELECT SUBJECT_ID, NAME, ABBREVIATION FROM ST58310.SUBJECT order by YEAR, SEMESTER";
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
                final String name = resultSet.getString(SubjectColumns.NAME.toString());
                final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
                final Subject subject = new Subject(subjectId, name, abbreviation);
                subjects.add(subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private <T> void initializeListView(@NotNull final ListView<T> listView, final ObservableList<T> observableList) {
        listView.setItems(observableList);
        listView.setStyle(StylesEnum.FONT_STYLE.getStyle());
    }

    @FXML
    private void addNewUserSubject(ActionEvent event) {
        int addedIndex = 0;
        final ObservableList<Subject> notAddedSubjects = FXCollections.observableArrayList();

        final User student = studentsListView.getSelectionModel().getSelectedItem();
        final ObservableList<Subject> subjects = subjectsListView.getSelectionModel().getSelectedItems();

        if (student == null || subjects.isEmpty()) {
            Main.callAlertWindow("Warning", "Student or subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                for (final Subject value : subjects) {
                    if (checkForExistingRecord(student.getUserId(), value.getSubjectId())) {
                        final String insertQuery = "INSERT INTO ST58310.USER_SUBJECT values(?,?)";
                        preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
                        preparedStatement.setInt(1, value.getSubjectId());
                        preparedStatement.setInt(2, student.getUserId());
                        preparedStatement.execute();
                        addedIndex++;
                    } else {
                        notAddedSubjects.add(value);
                    }
                }
                if (addedIndex == subjects.size()) {
                    Main.callAlertWindow("Successful adding", "The new record has been added successfully!", Alert.AlertType.INFORMATION, "/images/information_icon.png");
                } else {
                    final StringBuilder stringBuilder = new StringBuilder();
                    notAddedSubjects.forEach(subject -> stringBuilder.append(subject).append("\n"));
                    Main.callAlertWindow("Something went wrong",
                            "The one or more records have NOT been added successfully because of student or administrator had written this subject(s) earlier!\nNot added items:\n" + stringBuilder.toString(),
                            Alert.AlertType.WARNING, "/images/warning_icon.png");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkForExistingRecord(final int userId, final int subjectId) throws SQLException {
        final String selectQuery = "SELECT * FROM ST58310.USER_SUBJECT WHERE USER_USER_ID = ? AND SUBJECT_SUBJECT_ID = ?";
        preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, subjectId);
        resultSet = preparedStatement.executeQuery();
        return !resultSet.next();
    }

    @FXML
    private void cancel(ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}

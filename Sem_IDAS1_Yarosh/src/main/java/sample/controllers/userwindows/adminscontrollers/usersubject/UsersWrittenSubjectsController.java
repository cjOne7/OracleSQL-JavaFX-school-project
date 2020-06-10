package sample.controllers.userwindows.adminscontrollers.usersubject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Subject;
import sample.dbtableclasses.User;
import sample.dbtableclasses.UserSubject;
import sample.enums.*;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UsersWrittenSubjectsController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;

    private ObservableList<UserSubject> userSubjects = FXCollections.observableArrayList();
    private ObservableList<UserSubject> sortedList = FXCollections.observableArrayList();
    public static int studentId;
    public static int subjectId;

    @FXML
    private ListView<UserSubject> usersSubjectsListView;
    @FXML
    private Button cancelBtn;
    @FXML
    private ComboBox<Role> studentTeacherCombobox;
    @FXML
    private Button changeUserSubjectBtn;
    @FXML
    private Button deleteRowBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentTeacherCombobox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());
        final Role[] roles = {Role.STUDENT, Role.TEACHER};
        studentTeacherCombobox.getItems().addAll(roles);
        studentTeacherCombobox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterValues(userSubject -> newValue.getIndex() == userSubject.getUser().getRoleId());
        });

        usersSubjectsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        usersSubjectsListView.setItems(userSubjects);
        fillUserSubjectListView();

        studentTeacherCombobox.setValue(roles[0]);

        if (userSubjects.size() >= 1) {
            changeDisable(false);
        }
    }

    private void changeDisable(final boolean state) {
        studentTeacherCombobox.setDisable(state);
        changeUserSubjectBtn.setDisable(state);
        deleteRowBtn.setDisable(state);
    }

    private void fillUserSubjectListView() {
        try {
            final String selectQuery = "SELECT ELSA_USER.LOGIN, ELSA_USER.SURNAME, ELSA_USER.USER_ID, ELSA_USER.ROLE_ID, SUBJECT.NAME, SUBJECT.ABBREVIATION, SUBJECT.SUBJECT_ID " +
                    "FROM ELSA_USER " +
                    "INNER JOIN USER_SUBJECT ON ELSA_USER.USER_ID = USER_SUBJECT.USER_USER_ID " +
                    "INNER JOIN SUBJECT ON USER_SUBJECT.SUBJECT_SUBJECT_ID = SUBJECT.SUBJECT_ID " +
                    "ORDER BY USER_USER_ID, SUBJECT_SUBJECT_ID";
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int userId = resultSet.getInt(ElsaUserColumns.USER_ID.toString());
                final String userLogin = resultSet.getString(ElsaUserColumns.LOGIN.toString());
                final String userSurname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
                final int roleId = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
                final User user = new User(userSurname, userLogin, userId, roleId);

                final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
                final String subjectName = resultSet.getString(SubjectColumns.NAME.toString());
                final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
                final Subject subject = new Subject(subjectId, subjectName, abbreviation);

                final UserSubject userSubject = new UserSubject(user, subject);
                userSubjects.add(userSubject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteRow(ActionEvent event) {
        final UserSubject userSubject = usersSubjectsListView.getSelectionModel().getSelectedItem();
        if (userSubject == null) {
            Main.callAlertWindow("Warning", "Row is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String deleteQuery = "DELETE FROM ST58310.USER_SUBJECT WHERE USER_USER_ID = ? AND SUBJECT_SUBJECT_ID = ?";
            try {
                preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(UserSubjectColumns.USER_USER_ID.getColumnIndex(), userSubject.getUser().getUserId());
                preparedStatement.setInt(UserSubjectColumns.SUBJECT_SUBJECT_ID.getColumnIndex(), userSubject.getSubject().getSubjectId());
                preparedStatement.execute();
                userSubjects.remove(userSubject);
                sortedList.remove(userSubject);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (userSubjects.isEmpty()) {
            changeDisable(true);
        }
    }

    @FXML
    private void addUserSubject(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/usersubject/AddUserSubjectWindow.fxml", getClass(), false, "Add subjects to users window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void changeUserSubject(ActionEvent event) {
        final UserSubject userSubject = usersSubjectsListView.getSelectionModel().getSelectedItem();
        if (userSubject == null) {
            Main.callAlertWindow("Warning", "Row is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            studentId = userSubject.getUser().getUserId();
            subjectId = userSubject.getSubject().getSubjectId();
            OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/usersubject/ChangeUserSubjectWindow.fxml", getClass(), false, "Change written subjects to users window", new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void refreshList(ActionEvent event) {
        userSubjects.clear();
        fillUserSubjectListView();
        sortedList.clear();
        filterValues(userSubject -> studentTeacherCombobox.getValue().getIndex() == userSubject.getUser().getRoleId());
        if (userSubjects.size() >= 1) {
            changeDisable(false);
        }
    }

    private void filterValues(final Predicate<UserSubject> predicate) {
        sortedList = userSubjects.stream()
                .filter(predicate)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        usersSubjectsListView.setItems(sortedList);
    }

    @FXML
    private void cancel(ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}

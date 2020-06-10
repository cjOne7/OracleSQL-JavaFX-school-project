package sample.controllers.userwindows.adminscontrollers.usersubject;

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
import sample.dbtableclasses.Subject;
import sample.dbtableclasses.User;
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddUserSubjectController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();
    private ObservableList<User> sortedList = FXCollections.observableArrayList();

    @FXML
    private ListView<User> usersListView;
    @FXML
    private ListView<Subject> subjectsListView;
    @FXML
    private Button cancelBtn;
    @FXML
    private ComboBox<Role> studentTeacherCombobox;
    @FXML
    private Label titleLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListView(usersListView, users);
        initializeListView(subjectsListView, subjects);
        subjectsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        studentTeacherCombobox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());
        final Role[] roles = {Role.STUDENT, Role.TEACHER};
        studentTeacherCombobox.getItems().addAll(roles);

        studentTeacherCombobox.valueProperty().addListener((observable, oldValue, newValue) -> {
            sortedList = users.stream()
                    .filter(user -> newValue.getIndex() == user.getRoleId())
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            usersListView.setItems(sortedList);
            if (Role.STUDENT == newValue) {
                titleLabel.setText("Student list");
            } else {
                titleLabel.setText("Teacher list");
            }
        });

        String selectQuery = "SELECT USER_ID, NAME, SURNAME, LOGIN, ROLE_ID FROM ST58310.ELSA_USER WHERE ROLE_ID IN (?,?) ORDER BY ROLE_ID";
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, Role.STUDENT.getIndex());
            preparedStatement.setInt(2, Role.TEACHER.getIndex());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int userId = resultSet.getInt(ElsaUserColumns.USER_ID.toString());
                final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
                final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
                final String login = resultSet.getString(ElsaUserColumns.LOGIN.toString());
                final int roleId = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
                final User student = new User(name, surname, login, userId, roleId);
                users.add(student);
            }

            selectQuery = "SELECT SUBJECT_ID, NAME, ABBREVIATION FROM ST58310.SUBJECT ORDER BY YEAR, SEMESTER";
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
        studentTeacherCombobox.setValue(roles[0]);
    }

    private <T> void initializeListView(@NotNull final ListView<T> listView, final ObservableList<T> observableList) {
        listView.setItems(observableList);
        listView.setStyle(StylesEnum.FONT_STYLE.getStyle());
    }

    private void colorizeSubjectsListView() {
        final User user = usersListView.getSelectionModel().getSelectedItem();
        final ObservableList<Subject> subjects = FXCollections.observableArrayList();
        if (user == null) {
            Main.callAlertWindow("Warning", "Student is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String selectQuery = "select NAME, ABBREVIATION, SUBJECT_ID from ST58310.SUBJECT where SUBJECT_ID in (select SUBJECT_SUBJECT_ID from ST58310.USER_SUBJECT where USER_USER_ID = ?) ORDER BY YEAR, SEMESTER";
            try {
                preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
                preparedStatement.setInt(1, user.getUserId());
                final ResultSet resultSet = preparedStatement.executeQuery();
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
            final List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < subjects.size(); i++) {
                for (int j = 0; j < this.subjects.size(); j++) {
                    if (this.subjects.get(j).getSubjectId() == subjects.get(i).getSubjectId()) {
                        indexes.add(j);
                    }
                }
            }
            subjectsListView.setCellFactory(param -> new ListCell<Subject>() {
                        @Override
                        protected void updateItem(final Subject item, final boolean empty) {
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
    private void addNewUserSubject(ActionEvent event) {
        int addedIndex = 0;
        final ObservableList<Subject> notAddedSubjects = FXCollections.observableArrayList();

        final User student = usersListView.getSelectionModel().getSelectedItem();
        final ObservableList<Subject> subjects = subjectsListView.getSelectionModel().getSelectedItems();

        if (student == null || subjects.isEmpty()) {
            Main.callAlertWindow("Warning", "Student or subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                for (final Subject value : subjects) {
                    if (checkForExistingRecord(student.getUserId(), value.getSubjectId())) {
                        final String insertQuery = "INSERT INTO ST58310.USER_SUBJECT VALUES (?,?)";
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
                            "The one or more records have NOT been added successfully because of student or teacher had written this subject(s) earlier!\nNot added items:\n" + stringBuilder.toString(),
                            Alert.AlertType.WARNING, "/images/warning_icon.png");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
    private void displayWrittenSubjects(MouseEvent event) { colorizeSubjectsListView(); }

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

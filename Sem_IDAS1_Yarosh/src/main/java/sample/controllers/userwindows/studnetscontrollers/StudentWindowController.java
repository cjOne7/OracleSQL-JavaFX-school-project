package sample.controllers.userwindows.studnetscontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.controllers.MainWindowController;
import sample.controllers.OpenNewWindow;
import sample.dbtableclasses.Subject;
import sample.databasemanager.DbManager;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class StudentWindowController implements Initializable {

    private final DbManager dbManager = new DbManager();

    private final ObservableList<Subject> allSubjects = FXCollections.observableArrayList();
    private final ObservableList<Subject> writtenSubjects = FXCollections.observableArrayList();

    @FXML
    private ListView<Subject> writtenSubjectsListView;
    @FXML
    private Button logOutBtn;
    @FXML
    private ListView<Subject> allSubjectsListView;
    @FXML
    private Button writeSubjectBtn;
    @FXML
    private Button unsubscribeBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        changeStyleAndSetItemsToListView(writtenSubjectsListView, writtenSubjects);
        changeStyleAndSetItemsToListView(allSubjectsListView, allSubjects);

        try {
            //get all subjects
            final String selectAllAvailableSubjects = "SELECT * FROM ST58310.SUBJECT ORDER BY YEAR, SEMESTER";
            final PreparedStatement selectAllSubjects = dbManager.getConnection().prepareStatement(selectAllAvailableSubjects);
            final ResultSet allSubjects = selectAllSubjects.executeQuery();
            fillObservableList(allSubjects, this.allSubjects);

            final String selectWrittenSubjects = "SELECT * FROM ST58310.SUBJECT WHERE SUBJECT_ID IN (SELECT SUBJECT_SUBJECT_ID FROM ST58310.USER_SUBJECT WHERE USER_USER_ID = ?)";
            final PreparedStatement selectWrittenSubjectsStatement = dbManager.getConnection().prepareStatement(selectWrittenSubjects);
            selectWrittenSubjectsStatement.setInt(1, MainWindowController.curUserId);
            final ResultSet allWrittenSubjects = selectWrittenSubjectsStatement.executeQuery();
            fillObservableList(allWrittenSubjects, writtenSubjects);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        changeDisable(allSubjects, writeSubjectBtn);
        changeDisable(writtenSubjects, unsubscribeBtn);
    }

    private void changeDisable(@NotNull final ObservableList<Subject> observableList, final Button button) {
        if (observableList.isEmpty()) {
            button.setDisable(true);
        } else {
            button.setDisable(false);
        }
    }

    private void fillObservableList(@NotNull final ResultSet resultSet, final ObservableList<Subject> observableList) throws SQLException {
        while (resultSet.next()) {
            final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
            final String name = resultSet.getString(SubjectColumns.SUBJECT_NAME.toString());
            final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
            final int credits = resultSet.getInt(SubjectColumns.CREDITS.toString());
            final int semester = resultSet.getInt(SubjectColumns.SEMESTER.toString());
            final int year = resultSet.getInt(SubjectColumns.YEAR.toString());
            final String description = resultSet.getString(SubjectColumns.DESCRIPTION.toString());
            final Subject subject = new Subject(subjectId, name, abbreviation, credits, semester, year, description);
            observableList.add(subject);
        }
        resultSet.close();
    }

    private void changeStyleAndSetItemsToListView(@NotNull final ListView<Subject> listView, final ObservableList<Subject> subjects) {
        listView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        listView.setItems(subjects);
    }

    @FXML
    private void deleteSubject(ActionEvent event) {
        final Subject subject = writtenSubjectsListView.getSelectionModel().getSelectedItem();
        if (subject == null) {
            Main.callAlertWindow("Warning", "Subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            writtenSubjects.remove(subject);
            execQueryForUserSubject("DELETE FROM ST58310.USER_SUBJECT WHERE SUBJECT_SUBJECT_ID = ? AND USER_USER_ID = ?", subject);
        }
        if (writtenSubjects.isEmpty()) {
            unsubscribeBtn.setDisable(true);
        }
    }

    @FXML
    private void writeSubject(ActionEvent event) {
        final Subject subject = allSubjectsListView.getSelectionModel().getSelectedItem();
        if (subject == null) {//if subject is not choosen
            Main.callAlertWindow("Warning", "Subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final long count = writtenSubjects.stream().filter(sub -> sub.getSubjectId() == subject.getSubjectId()).count();
            if (count == 0) {
                writtenSubjects.add(subject);
                execQueryForUserSubject("INSERT INTO ST58310.USER_SUBJECT VALUES (?,?)", subject);
            } else {
                //if student have already written this subject
                Main.callAlertWindow("Warning", "Subject have been already written!", Alert.AlertType.WARNING, "/images/warning_icon.png");
            }
        }
        if (writtenSubjects.size() >= 1) {
            unsubscribeBtn.setDisable(false);
        }
    }

    private void execQueryForUserSubject(final String query, @NotNull final Subject subject) {
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, subject.getSubjectId());
            preparedStatement.setInt(2, MainWindowController.curUserId);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void watchMaterials(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/studentsfxmls/StudentStudyMatWindow.fxml", getClass(), false, "Study materials window", new Image("/images/student_icon.png"));
    }

    @FXML
    private void editProfile(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/UserProfileWindow.fxml", getClass(), false, "Profile window", new Image("/images/profile_icon.png"));
    }

    @FXML
    private void logOut(ActionEvent event) {
        MainWindowController.openMainStage(logOutBtn);
    }
}

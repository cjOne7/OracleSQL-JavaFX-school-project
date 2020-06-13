package sample.controllers.userwindows.teacherscontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import sample.controllers.MainWindowController;
import sample.controllers.OpenNewWindow;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Subject;
import sample.dbtableclasses.User;
import sample.dbtableclasses.UserSubject;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class TeacherWindowController implements Initializable {

    private final DbManager dbManager = new DbManager();

    private ObservableList<UserSubject> students = FXCollections.observableArrayList();
    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();

    public static int userId;

    @FXML
    private ListView<UserSubject> studentsList;
    @FXML
    private ListView<Subject> subjectListView;
    @FXML
    private Button logOutBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userId = MainWindowController.curUserId;

        try {
            //get all current teacher's subjects
            final String selectQuery = "SELECT * FROM ST58310.SUBJECT WHERE SUBJECT_ID " +
                    "IN (SELECT SUBJECT_SUBJECT_ID FROM ST58310.USER_SUBJECT WHERE USER_USER_ID = ?)";
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, userId);
            fillObservableList(preparedStatement.executeQuery(), subjects);

            students = User.fillStudentsList();//get students list who have the subjects in current teacher
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initializeListView(studentsList, students);
        initializeListView(subjectListView, subjects);
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

    private <T> void initializeListView(@NotNull final ListView<T> listView, final ObservableList<T> observableList) {
        listView.setItems(observableList);
        listView.setStyle(StylesEnum.FONT_STYLE.getStyle());
    }

    @FXML
    private void watchMaterials(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/teachersfxmls/TeacherStudyMatWindow.fxml", getClass(), false, "Study material window", new Image("/images/teacher_icon.png"));
    }

    @FXML
    private void openProfile(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/UserProfileWindow.fxml", getClass(), false, "Profile window", new Image("/images/profile_icon.png"));
    }

    @FXML
    private void logOut(ActionEvent event) {
        MainWindowController.openMainStage(logOutBtn);
    }
}

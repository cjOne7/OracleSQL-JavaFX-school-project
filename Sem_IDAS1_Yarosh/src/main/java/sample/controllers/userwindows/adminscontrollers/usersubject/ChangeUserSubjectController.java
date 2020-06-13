package sample.controllers.userwindows.adminscontrollers.usersubject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Subject;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static sample.controllers.userwindows.adminscontrollers.usersubject.UsersWrittenSubjectsController.*;

public class ChangeUserSubjectController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;

    private final ObservableList<Subject> subjects = FXCollections.observableArrayList();

    @FXML
    private ListView<Subject> subjectsListView;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button applyChangesBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        subjectsListView.setItems(subjects);
        subjectsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());

        fillUnwrittenSubjectsList();
        if (subjects.isEmpty()) {
            applyChangesBtn.setDisable(true);
        }
    }
    private void fillUnwrittenSubjectsList() {
        final String selectQuery = "SELECT SUBJECT_NAME, ABBREVIATION, SUBJECT_ID FROM ST58310.SUBJECT WHERE SUBJECT_ID NOT IN (SELECT SUBJECT_SUBJECT_ID FROM ST58310.USER_SUBJECT WHERE USER_USER_ID = ?)";
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, studentId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
                final String name = resultSet.getString(SubjectColumns.SUBJECT_NAME.toString());
                final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
                final Subject subject = new Subject(subjectId, name, abbreviation);
                subjects.add(subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void applyChanges(ActionEvent event) {
        final Subject subject = subjectsListView.getSelectionModel().getSelectedItem();
        if (subject == null) {
            Main.callAlertWindow("Warning", "Subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                executeQuery("DELETE FROM ST58310.USER_SUBJECT WHERE SUBJECT_SUBJECT_ID = ? AND USER_USER_ID = ?", subjectId, studentId);
                //this line is necessary so that when you delete and then add items, they are overwritten, and not just written after the first deletion
                subjectId = subject.getSubjectId();
                executeQuery("INSERT INTO ST58310.USER_SUBJECT values(?,?)", subject.getSubjectId(), studentId);

                refreshList();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void executeQuery(final String query, @NotNull final int... values) throws SQLException {
        preparedStatement = dbManager.getConnection().prepareStatement(query);
        preparedStatement.setInt(1, values[0]);
        preparedStatement.setInt(2, values[1]);
        preparedStatement.execute();
    }

    private void refreshList() {
        subjects.clear();
        fillUnwrittenSubjectsList();
    }

    @FXML
    private void cancel(ActionEvent event) { ((Stage) cancelBtn.getScene().getWindow()).close(); }
}

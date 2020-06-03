package sample.controllers.adminscontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import sample.controllers.MainWindowController;
import sample.controllers.OpenNewWindow;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML
    private Button exitBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void openAssignPrivileges(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/AssignPrivilegesWindow.fxml", getClass(), false, "Assign privileges window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void openSubjectManagement(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/subjectmanagement/SubjectManagementWindow.fxml", getClass(), false, "Subject management window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void openUsersWrittenSubjects(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/usersubject/UsersWrittenSubjectsWindow.fxml", getClass(), false, "Users written subjects window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void openProfile(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/UserProfileWindow.fxml", getClass(), false, "Profile window", new Image("/images/profile_icon.png"));
    }

    @FXML
    private void exitToMainScreen(ActionEvent event) {
        MainWindowController.openMainStage(exitBtn);
    }
}

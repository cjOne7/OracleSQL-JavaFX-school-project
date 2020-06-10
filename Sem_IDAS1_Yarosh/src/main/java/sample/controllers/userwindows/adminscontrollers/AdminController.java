package sample.controllers.userwindows.adminscontrollers;

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

    public static int curUserId;

    @FXML
    private Button exitBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        curUserId = MainWindowController.curUserId; //запомнить айди текущего авторизированного пользователя
    }

    @FXML
    private void openAssignPrivileges(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/AssignPrivilegesWindow.fxml", getClass(), false, "Assign privileges window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void openSubjectManagement(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/subjectmanagement/SubjectManagementWindow.fxml", getClass(), false, "Subject management window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void openUsersWrittenSubjects(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/usersubject/UsersWrittenSubjectsWindow.fxml", getClass(), false, "Users written subjects window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void categoryManagement(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/categorymanagement/StudyMatCategoryWindow.fxml", getClass(), false, "Study material category window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void createStudyMaterial(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/studymaterials/CreateStudyMaterialsWindow.fxml", getClass(), false, "Create study materials window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void assignCategory(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/userwindows/adminsfxmls/studymaterials/AssignCategoryWindow.fxml", getClass(), false, "Assign category window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void openProfile(ActionEvent event) {
        MainWindowController.curUserId = curUserId; //вернуть прежний айди текущего авторизированного пользователя для просмотра его профиля
        OpenNewWindow.openNewWindow("/fxmlfiles/UserProfileWindow.fxml", getClass(), false, "Profile window", new Image("/images/profile_icon.png"));
    }

    @FXML
    private void exitToMainScreen(ActionEvent event) {
        MainWindowController.openMainStage(exitBtn);
    }
}

package sample.controllers.userwindows.teacherscontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import sample.controllers.MainWindowController;
import sample.controllers.OpenNewWindow;

import java.net.URL;
import java.util.ResourceBundle;

public class TeacherWindowController implements Initializable {


    @FXML
    private Button logOutBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void createQuiz(ActionEvent event) {

    }

    @FXML
    private void watchMaterials(ActionEvent event) {
//        OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/studymaterials/CreateStudyMaterialsWindow.fxml", getClass(), false, "Study materials window", new Image("/images/admin_icon.png"));
    }

    @FXML
    private void refreshList(ActionEvent event) {

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

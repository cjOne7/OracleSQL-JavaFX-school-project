package sample.controllers.userwindows.adminscontrollers.quizmanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.Checker;
import sample.Cosmetic;
import sample.TextConstraint;
import sample.controllers.Main;
import sample.controllers.MainWindowController;
import sample.controllers.userwindows.adminscontrollers.AdminController;
import sample.controllers.userwindows.adminscontrollers.studymaterials.CreateStudyMaterialsController;
import sample.databasemanager.DbManager;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import static sample.Cosmetic.changeTextFieldStyle;

public class CreateQuizController implements Initializable {

    private final DbManager dbManager = new DbManager();

    @FXML
    private Spinner<Integer> numberOfQuestionsSpinner;
    @FXML
    private TextField quizNameTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button createQuizBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberOfQuestionsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        numberOfQuestionsSpinner.setStyle(StylesEnum.FONT_STYLE.getStyle());
        quizNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            changeTextFieldStyle(newValue, quizNameTextField, StylesEnum.EMPTY_STRING.getStyle());
        });

        quizNameTextField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        descriptionTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(500)));
    }

    @FXML
    private void createQuiz(ActionEvent event) {
        final String quizName = quizNameTextField.getText().trim();
        if (quizName.isEmpty()){
            Cosmetic.shake(quizNameTextField);
            quizNameTextField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            Cosmetic.changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            final String insertQuery = "INSERT INTO ST58310.QUIZ (QUIZ_NAME, NUMBER_OF_QUESTIONS, DESCRIPTION, STY_MTRL_STY_MTRL_ID, USER_CREATER_ID) VALUES (?,?,?,?,?)";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
                preparedStatement.setString(1, quizName);
                preparedStatement.setInt(2, numberOfQuestionsSpinner.getValue());

                final String description = descriptionTextArea.getText();
                Checker.checkTextField(description == null || description.trim().isEmpty(), 3, description, preparedStatement);

                preparedStatement.setInt(4, CreateStudyMaterialsController.studyMatId);

                int userId = AdminController.curUserId;
                if (userId == 0) {
                    userId = MainWindowController.curUserId;
                }
                preparedStatement.setInt(5, userId);
                preparedStatement.execute();
                final Optional<ButtonType> op = Main.callAlertWindow("Inform window", "You have been created quiz successfully.", Alert.AlertType.INFORMATION, "/images/information_icon.png");
                if (op.get().equals(ButtonType.OK)) {
                    ((Stage) createQuizBtn.getScene().getWindow()).close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void scroll(@NotNull ScrollEvent event) {
        ((Spinner) event.getSource()).increment((int) event.getDeltaY() / 8);
    }

    @FXML
    private void cancel(ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}

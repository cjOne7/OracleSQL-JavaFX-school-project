package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.databasemanager.DbManager;
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.Shake;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    private final DbManager dbManager = new DbManager();

    public static int curUserId;

    @FXML
    private Button signInBtn;
    @FXML
    private TextField loginTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label loginErrorMessage;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        loginTextField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(loginTextField));
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(passwordField));

//        loginTextField.setText("cj_one1");//new user
//        passwordField.setText("123");
//        loginTextField.setText("cj_one2");//student
//        passwordField.setText("123");
        loginTextField.setText("cj_one4");//teacher
        passwordField.setText("123");
//        loginTextField.setText("nikyarosh07@gmail.com");//admin
//        passwordField.setText("Rfhnjirf258");
    }

    @FXML
    private void logIn(ActionEvent event) {
        if (checkLoginFieldForEmpty() || checkPasswordFieldForEmpty()) {
            checkIncomingField(checkLoginFieldForEmpty(), loginTextField);
            checkIncomingField(checkPasswordFieldForEmpty(), passwordField);
        } else {
            final String login = loginTextField.getText().trim();
            final String password = passwordField.getText();
            final String query = "select ST58310.ELSA_USER.LOGIN, ST58310.ELSA_USER.PASSWORD, ST58310.ELSA_USER.ROLE_ID, ST58310.ELSA_USER.USER_ID from st58310.ELSA_USER where LOGIN like ? and PASSWORD like ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(query);
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, password);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    curUserId = resultSet.getInt(ElsaUserColumns.USER_ID.toString());
                    final int roleId = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
                    switch (Objects.requireNonNull(Role.getRole(roleId))) {
                        case NEW:
                            closeCurAndOpenNewStage(signInBtn, "/fxmlfiles/userwindows/NewUserWindow.fxml", "New user window", "/images/new_user_icon.png");
                            break;
                        case STUDENT:
                            closeCurAndOpenNewStage(signInBtn, "/fxmlfiles/userwindows/studentsfxmls/StudentWindow.fxml", "Student window", "/images/student_icon.png");
                            break;
                        case TEACHER:
                            closeCurAndOpenNewStage(signInBtn, "/fxmlfiles/userwindows/teachersfxmls/TeacherWindow.fxml", "Teacher window", "/images/teacher_icon.png");
                            break;
                        case ADMINISTRATOR:
                        case MAIN_ADMIN:
                            closeCurAndOpenNewStage(signInBtn, "/fxmlfiles/userwindows/adminsfxmls/AdminWindow.fxml", "Admin window", "/images/admin_icon.png");
                            break;
                    }
                } else {
                    checkIncomingField(StylesEnum.ERROR_STYLE.getStyle(), loginTextField);
                    checkIncomingField(StylesEnum.ERROR_STYLE.getStyle(), passwordField);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeCurAndOpenNewStage(final Button button, final String filePath, final String title, final String imagePath){
        closeStage(button);
        OpenNewWindow.openNewWindow(filePath, getClass(), false, title, new Image(imagePath));
    }

    @FXML
    private void signUp(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/RegisterWindow.fxml", getClass(), false, "Registration window", new Image("/images/new_user.png"));
    }

    private void checkIncomingField(
            final boolean checkResult,
            final TextField textField) {
        if (checkResult) {
            textField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            Shake.shake(textField);
            decorateErrorLabel("Empty fields");
        }
    }

    private void checkIncomingField(final String style, @NotNull final TextField textField) {
        textField.setStyle(style);
        Shake.shake(textField);
        decorateErrorLabel("Login or password are wrong!");
    }

    private void decorateErrorLabel(final String labelText) {
        loginErrorMessage.setText(labelText);
        loginErrorMessage.setTextFill(Color.RED);
    }

    private boolean checkLoginFieldForEmpty() {
        return loginTextField.getText().trim().isEmpty();
    }

    private boolean checkPasswordFieldForEmpty() {
        return passwordField.getText().trim().isEmpty();
    }

    private void changeTextFieldStyle(@NotNull final TextField textField) {
        textField.setStyle(StylesEnum.EMPTY_STRING.getStyle());
        decorateErrorLabel(StylesEnum.EMPTY_STRING.getStyle());
    }

    private static void closeStage(@NotNull final Button button) {
        final Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

    public static void openMainStage(@NotNull final Button button) {
        closeStage(button);
        OpenNewWindow.openNewWindow("/fxmlfiles/MainWindow.fxml", MainWindowController.class, false, "Exiting window", new Image("/images/list.png"));
    }
}

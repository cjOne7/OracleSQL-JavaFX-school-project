package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.TextConstraint;
import sample.databasemanager.DbManager;
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.Cosmetic;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static sample.Checker.checkTextField;

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

        loginTextField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(64)));
        passwordField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));

//        loginTextField.setText("cj_one1");//new user
//        passwordField.setText("123");
//        loginTextField.setText("cj_one2");//student
//        passwordField.setText("123");
//        loginTextField.setText("cj_one4");//teacher
//        passwordField.setText("123");
        loginTextField.setText("nikyarosh07@gmail.com");//admin
        passwordField.setText("123");
    }

    @FXML
    private void logIn(ActionEvent event) {
        //check login and password fields, they must be not empty
        if (checkTextField(loginTextField) || checkTextField(passwordField)) {
            checkIncomingField(checkTextField(loginTextField), loginTextField);
            checkIncomingField(checkTextField(passwordField), passwordField);
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
                    checkIncomingField(StylesEnum.ERROR_STYLE.getStyle(), loginTextField, "Login or password are wrong!");
                    checkIncomingField(StylesEnum.ERROR_STYLE.getStyle(), passwordField, "Login or password are wrong!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeCurAndOpenNewStage(@NotNull final Button button, final String filePath, final String title, final String imagePath) {
        ((Stage) button.getScene().getWindow()).close();
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
            checkIncomingField(StylesEnum.ERROR_STYLE.getStyle(), textField, "Empty fields");
        }
    }

    private void checkIncomingField(final String style, @NotNull final TextField textField, final String text) {
        textField.setStyle(style);
        Cosmetic.shake(textField);
        Cosmetic.changeLabelAttributes(loginErrorMessage, text, Color.RED);
    }

    private void changeTextFieldStyle(@NotNull final TextField textField) {
        textField.setStyle(StylesEnum.EMPTY_STRING.getStyle());
        Cosmetic.changeLabelAttributes(loginErrorMessage, StylesEnum.EMPTY_STRING.getStyle(), Color.BLACK);
    }

    public static void openMainStage(@NotNull final Button button) {
        ((Stage) button.getScene().getWindow()).close();
        OpenNewWindow.openNewWindow("/fxmlfiles/MainWindow.fxml", MainWindowController.class, false, "Exiting window", new Image("/images/list.png"));
    }
}

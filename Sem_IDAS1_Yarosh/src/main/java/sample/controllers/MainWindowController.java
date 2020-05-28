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
import sample.DatabaseManagement.DbManager;
import sample.Role;
import sample.Shake;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    public static int role_id;

    @FXML
    private Button singUpBtn;
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
        loginTextField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, loginTextField, ""));
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, passwordField, ""));

        loginTextField.setText("nikyarosh07@gmail.com");
        passwordField.setText("Rfhnjirf258");
    }

    @FXML
    private void logIn(ActionEvent event) {
        if (checkLoginFieldForEmpty() || checkPasswordFieldForEmpty()) {
            checkIncomingField(checkLoginFieldForEmpty(), "-fx-border-color: red; -fx-border-radius: 5;", loginTextField);
            checkIncomingField(checkPasswordFieldForEmpty(), "-fx-border-color: red; -fx-border-radius: 5;", passwordField);
        } else {
            final String login = loginTextField.getText().trim();
            final String password = passwordField.getText();
            final String query = "select ST58310.ELSA_USER.LOGIN, ST58310.ELSA_USER.PASSWORD, ST58310.ELSA_USER.ROLE_ID from st58310.ELSA_USER where LOGIN like ? and PASSWORD like ?";
            try {
                final DbManager dbManager = new DbManager();
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(query);
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, password);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    role_id = resultSet.getInt("ROLE_ID");
                    switch (Role.getRole(role_id)) {
                        case NEW:
                            break;
                        case STUDENT:
                            break;
                        case TEACHER:
                            break;
                        case ADMINISTRATOR:
                        case MAIN_ADMIN:
                            final Stage stage = (Stage) signInBtn.getScene().getWindow();
                            stage.close();
                            OpenNewWindow.openNewWindow("/AdministratorWindow.fxml", getClass(), true, "Admin window", new Image("/images/admin_icon.png"));
                            break;
                    }
                } else {
                    checkIncomingField("-fx-border-color: red; -fx-border-radius: 5;", loginTextField);
                    checkIncomingField("-fx-border-color: red; -fx-border-radius: 5;", passwordField);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void signUp(ActionEvent event) {
        OpenNewWindow.openNewWindow("/RegisterWindow.fxml", getClass(), false, "Registration window", new Image("/images/new_user.png"));
    }

    private void checkIncomingField(
            final boolean checkResult,
            final String style,
            final TextField textField) {
        if (checkResult) {
            textField.setStyle(style);
            shake(textField);
            decorateErrorLabel("Empty fields");
        }
    }

    private void checkIncomingField(final String style, @NotNull final TextField textField) {
        textField.setStyle(style);
        shake(textField);
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

    private void changeTextFieldStyle(@NotNull final String newValue, final TextField textField, final String style) {
        if (!newValue.isEmpty()) {
            textField.setStyle(style);
            decorateErrorLabel("");
        }
    }

    private void shake(final TextField textField) {
        final Shake shake = new Shake(textField);
        shake.getTranslateTransition().playFromStart();
    }
}

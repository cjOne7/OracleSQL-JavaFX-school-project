package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.*;
import sample.databasemanager.DbManager;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import sample.enums.ElsaUserColumns;
import sample.dbtableclasses.User;
import sample.enums.StylesEnum;

import static sample.Cosmetic.*;

public class UserProfileWindowController implements Initializable {

    private static final String UPDATE_QUERY =
            "UPDATE ST58310.ELSA_USER " +
            "SET NAME = ?, SURNAME = ?, LOGIN = ?, PASSWORD = ?, EMAIL = ?, TELEPHONE = ?, ABOUT = ?, IMAGE = ? " +
            "WHERE USER_ID = ?";

    private final DbManager dbManager = new DbManager();
    private PreparedStatement updateStatement;

    private File file;
    private User user;

    private List<TextField> textFieldList;

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> countryCodePicker;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextArea aboutYourselfTextArea;
    @FXML
    private TextField loginField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField emailField;
    @FXML
    private ImageView imageView;
    @FXML
    private Button finishEditingBtn;
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField reenteredPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Button cancelBtn;
    @FXML
    private Label messageLabel;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        textFieldList = fillListByTextFields();
        oldPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            Cosmetic.cutSpace(oldPasswordField, newValue, Color.RED, messageLabel);
            oldPasswordField.setStyle(StylesEnum.EMPTY_STRING.getStyle());
        });
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            Cosmetic.changePasswordsLabel(reenteredPasswordField.getText(), newValue, Color.RED, messageLabel, newPasswordField, reenteredPasswordField);
            Cosmetic.cutSpace(newPasswordField, newValue, Color.RED, messageLabel);
        });
        reenteredPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            Cosmetic.changePasswordsLabel(newPasswordField.getText(), newValue, Color.RED, messageLabel, newPasswordField, reenteredPasswordField);
            Cosmetic.cutSpace(reenteredPasswordField, newValue, Color.RED, messageLabel);
        });
        //clear error style after typing/deleting a character
        loginField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, loginField, StylesEnum.EMPTY_STRING.getStyle()));
        nameField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, nameField, StylesEnum.EMPTY_STRING.getStyle()));
        surnameField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, surnameField, StylesEnum.EMPTY_STRING.getStyle()));
        emailField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, emailField, StylesEnum.EMPTY_STRING.getStyle()));
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            //forbid input any symbols except digits
            try {
                if (!newValue.isEmpty()) {
                    Integer.parseInt(newValue);
                }
            } catch (NumberFormatException e) {
                telephoneField.setText(oldValue);
            }
        });

        loginField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(64)));
        emailField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(64)));
        oldPasswordField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        reenteredPasswordField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        newPasswordField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        nameField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        surnameField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        telephoneField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(20)));
        aboutYourselfTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(1000)));

        final String selectQuery = "SELECT * from ST58310.ELSA_USER where USER_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, MainWindowController.curUserId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //fill text fields by suitable values
                final String name = fillTextField(nameField, ElsaUserColumns.NAME.toString(), resultSet);
                final String surname = fillTextField(surnameField, ElsaUserColumns.SURNAME.toString(), resultSet);
                final String email = fillTextField(emailField, ElsaUserColumns.EMAIL.toString(), resultSet);
                final String login = fillTextField(loginField, ElsaUserColumns.LOGIN.toString(), resultSet);
                final String password = resultSet.getString(ElsaUserColumns.PASSWORD.toString());
                final String telephone = resultSet.getString(ElsaUserColumns.TELEPHONE.toString());
                final String about = resultSet.getString(ElsaUserColumns.ABOUT.toString());
                aboutYourselfTextArea.setText(about);

                final Blob blob = resultSet.getBlob(ElsaUserColumns.IMAGE.toString());
                try {
                    file = ImageManager.loadImage(blob, imageView);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final int role = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
                final int userId = resultSet.getInt(ElsaUserColumns.USER_ID.toString());
                user = new User(name, surname, login, password, email, telephone, about, blob, role, userId);

                //transform combobox to country code picker
                countryCodePicker.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());
                countryCodePicker.setItems(CountryCodePicker.getCountryCodes());
                countryCodePicker.setValue(CountryCodePicker.getISOCountry(telephone));
                telephoneField.setText(CountryCodePicker.getPhoneNumber(telephone));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String fillTextField(@NotNull final TextField textField, final String columnName, @NotNull final ResultSet resultSet) throws SQLException {
        final String string = resultSet.getString(columnName);
        textField.setText(string);
        return string;
    }

    private boolean changePasswordIsSuccessful() {
        //if one of the password field is empty
        if (oldPasswordField.getText().isEmpty()
                && newPasswordField.getText().isEmpty()
                && reenteredPasswordField.getText().isEmpty()) {
            return true;
        } else {
            final String oldPassword = user.getPassword();
            final String oldUserEnteredPassword = oldPasswordField.getText();
            final String newPassword = newPasswordField.getText();
            final String newReenteredPassword = reenteredPasswordField.getText();
            if (oldPassword.equals(oldUserEnteredPassword)) {
                if (newPassword.equals(newReenteredPassword) && !newPassword.isEmpty()) {
                    user.setPassword(newPassword);
                    return true;
                } else {
                    changeLabelAttributes(messageLabel, "Fields are empty or don't match", Color.RED);
                    changeFieldStyle(newPasswordField, StylesEnum.ERROR_STYLE.getStyle());
                    changeFieldStyle(reenteredPasswordField, StylesEnum.ERROR_STYLE.getStyle());
                    return false;
                }
            } else {
                changeLabelAttributes(messageLabel, "Passwords don't match!", Color.RED);
                changeFieldStyle(oldPasswordField, StylesEnum.ERROR_STYLE.getStyle());
                return false;
            }
        }
    }

    private void changeFieldStyle(final TextField textField, final String style) {
        Cosmetic.shake(textField);
        textField.setStyle(style);
    }

    @FXML
    private void loadImage(ActionEvent event) {
        file = ImageManager.loadImage(imageView);
    }

    @FXML
    private void updateUsersData(ActionEvent event) {
        final List<Boolean> resultList = writeNotNullFieldsInList();

        if (resultList.contains(true)) {//если хоть одно обязательное поле пустое
            for (int i = 0; i < textFieldList.size(); i++) {
                if (resultList.get(i)) {
                    Cosmetic.shake(textFieldList.get(i));
                    textFieldList.get(i).setStyle(StylesEnum.ERROR_STYLE.getStyle());
                }
            }
            changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                //check if login or password were changed
                if (checkLoginForUnique() && changePasswordIsSuccessful()) {
                    updateStatement = dbManager.getConnection().prepareStatement(UPDATE_QUERY);
                    updateStatement.setString(ElsaUserColumns.NAME.getColumnIndex(), nameField.getText().trim());
                    updateStatement.setString(ElsaUserColumns.SURNAME.getColumnIndex(), surnameField.getText().trim());
                    updateStatement.setString(ElsaUserColumns.LOGIN.getColumnIndex(), loginField.getText().trim());
                    updateStatement.setString(ElsaUserColumns.PASSWORD.getColumnIndex(), user.getPassword());
                    updateStatement.setString(ElsaUserColumns.EMAIL.getColumnIndex(), emailField.getText().trim());
                    checkNullPossibleFields();
                    updateStatement.setInt(ElsaUserColumns.USER_ID.getColumnIndex() - 1, MainWindowController.curUserId);
                    updateStatement.executeUpdate();
                    //open information window
                    final Optional<ButtonType> op = Main.callAlertWindow("Inform window", "Your changes have been processed successfully.", Alert.AlertType.INFORMATION, "/images/information_icon.png");
                    if (op.get().equals(ButtonType.OK)) {
                        file.deleteOnExit();
                        ((Stage) finishEditingBtn.getScene().getWindow()).close();
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void cancelEditing(ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
        file.deleteOnExit();
    }

    private boolean checkLoginForUnique() throws SQLException {
        final String login = loginField.getText().trim();
        if (login.equals(user.getLogin())) {
            return true;
        } else {
            return Checker.checkForUnique(loginField, "select st58310.elsa_user.login from st58310.elsa_user where login like ?",
                    messageLabel, login, "Login must be unique!");
        }
    }
    //check nullable fields
    private void checkNullPossibleFields() throws SQLException, FileNotFoundException {
        //check telephone field
        Checker.checkTextField(Checker.checkTextField(telephoneField), ElsaUserColumns.TELEPHONE.getColumnIndex(),
                countryCodePicker.getValue().concat(telephoneField.getText().trim()), updateStatement);
        //check about yourself field
        Checker.checkTextField(aboutYourselfTextArea.getText().trim().isEmpty(), ElsaUserColumns.ABOUT.getColumnIndex(),
                aboutYourselfTextArea.getText().trim(), updateStatement);
        Checker.checkImage(file == null, updateStatement, file);
    }

    @NotNull
    private List<Boolean> writeNotNullFieldsInList() {
        final List<Boolean> resultList = new ArrayList<>();// create and fill in a sheet with the results of checking values that must be
        resultList.add(Checker.checkTextField(nameField));//0
        resultList.add(Checker.checkTextField(surnameField));//1
        resultList.add(Checker.checkTextField(loginField));//2
        resultList.add(Checker.checkTextField(emailField));//3
        return resultList;
    }

    @NotNull
    private List<TextField> fillListByTextFields() {
        final List<TextField> textFields = new ArrayList<>();
        textFields.add(nameField);//0
        textFields.add(surnameField);//1
        textFields.add(loginField);//2
        textFields.add(emailField);//3
        return textFields;
    }
}

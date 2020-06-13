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
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.enums.StylesEnum;

import java.io.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static sample.Cosmetic.*;

public class RegisterWindowController implements Initializable {

    private static final String EXECUTE_QUERY = "INSERT into ST58310.ELSA_USER " +
            "(name, surname, login, password, email, telephone, about, image, role_id) " +
            "values (?,?,?,?,?,?,?,?,?)";

    private File file;

    private List<TextField> textFieldList;
    private DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;

    private String prevLogin;

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> countryCodePicker;
    @FXML
    private TextArea aboutYourselfTextArea;
    @FXML
    private TextField loginField;
    @FXML
    private TextField firstPasswordField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField secondPasswordField;
    @FXML
    private TextField emailField;
    @FXML
    private ImageView imageView;
    @FXML
    private TextField telephoneField;
    @FXML
    private CheckBox checkMailBox;
    @FXML
    private Button registerBtn;
    @FXML
    private Label messageLabel;
    @FXML
    private Label comparePasswordsLabel;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        textFieldList = fillListByNotNullTextFields();
        firstPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            Cosmetic.changePasswordsLabel(secondPasswordField.getText(), newValue, Color.RED, comparePasswordsLabel, firstPasswordField, secondPasswordField);
            Cosmetic.cutSpace(firstPasswordField, newValue, Color.RED, comparePasswordsLabel);
        });
        secondPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            Cosmetic.changePasswordsLabel(firstPasswordField.getText(), newValue, Color.RED, comparePasswordsLabel, firstPasswordField, secondPasswordField);
            Cosmetic.cutSpace(secondPasswordField, newValue, Color.RED, comparePasswordsLabel);
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
        firstPasswordField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        secondPasswordField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        nameField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        surnameField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        telephoneField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(20)));
        aboutYourselfTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(1000)));

        //transform combobox to country code picker
        countryCodePicker.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());
        countryCodePicker.setItems(CountryCodePicker.getCountryCodes());
        countryCodePicker.setValue(CountryCodePicker.getCountryCodes().get(0));

        example();
    }

    private void example() {
        nameField.setText("Nikita");
        surnameField.setText("Yarosh");
        loginField.setText("cj_one");
        firstPasswordField.setText("123");
        secondPasswordField.setText("123");
        emailField.setText("nik_yar@gmail.com");
        aboutYourselfTextArea.setText("Hi! My name is Nikita.");
        telephoneField.setText("773089030");
    }

    @FXML
    private void cancelRegistration(ActionEvent event) {
        ((Stage) registerBtn.getScene().getWindow()).close();
    }

    @FXML
    private void registerNewUser(ActionEvent event) {
        try {
            //fill result list to check for empty
            final List<Boolean> resultList = writeNotNullFieldsInList();

            if (resultList.contains(true)) {//если хоть одно обязательное поле пустое
                for (int i = 0; i < textFieldList.size(); i++) {
                    if (resultList.get(i)) {//if resultList has value "true", start animation and change style on text field
                        Cosmetic.shake(textFieldList.get(i));
                        textFieldList.get(i).setStyle(StylesEnum.ERROR_STYLE.getStyle());
                    }
                }
                //add message to label
                changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
            } else {
                //if no true values, check for unique
                if (Checker.checkForUnique(loginField, "select st58310.elsa_user.login from st58310.elsa_user where login like ?",
                        messageLabel, loginField.getText().trim(), "Login must be unique!")) {
                    preparedStatement = dbManager.getConnection().prepareStatement(EXECUTE_QUERY);
                    preparedStatement.setString(ElsaUserColumns.NAME.getColumnIndex(), nameField.getText().trim());
                    preparedStatement.setString(ElsaUserColumns.SURNAME.getColumnIndex(), surnameField.getText().trim());
                    preparedStatement.setString(ElsaUserColumns.LOGIN.getColumnIndex(), loginField.getText().trim());
                    preparedStatement.setString(ElsaUserColumns.PASSWORD.getColumnIndex(), firstPasswordField.getText().trim());
                    preparedStatement.setString(ElsaUserColumns.EMAIL.getColumnIndex(), emailField.getText().trim());
                    preparedStatement.setInt(ElsaUserColumns.ROLE_ID.getColumnIndex(), Role.NEW.getIndex());
                    checkNullPossibleFields();
                    preparedStatement.execute();
                    ((Stage) registerBtn.getScene().getWindow()).close();
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void emailAsLogin(ActionEvent actionEvent) {
        final String prevLogin = loginField.getText();
        if (checkMailBox.isSelected()) {
            setTextInLoginField(loginField, emailField.getText(), true);
        } else {
            setTextInLoginField(loginField, this.prevLogin, false);
        }
        this.prevLogin = prevLogin;
    }

    private void setTextInLoginField(@NotNull final TextField textField, final String text, final boolean state) {
        textField.setText(text);
        textField.setDisable(state);
    }

    @FXML
    private void loadImage(ActionEvent event) {
        file = ImageManager.loadImage(imageView);
    }

    //check nullable fields
    private void checkNullPossibleFields() throws SQLException, FileNotFoundException {
        //check telephone field
        Checker.checkTextField(Checker.checkTextField(telephoneField), ElsaUserColumns.TELEPHONE.getColumnIndex(),
                countryCodePicker.getValue().concat(telephoneField.getText().trim()), preparedStatement);
        //check about yourself field
        Checker.checkTextField(aboutYourselfTextArea.getText().trim().isEmpty(), ElsaUserColumns.ABOUT.getColumnIndex(),
                aboutYourselfTextArea.getText().trim(), preparedStatement);
        Checker.checkImage(file == null, preparedStatement, file);
    }

    @NotNull
    private List<Boolean> writeNotNullFieldsInList() {
        final List<Boolean> resultList = new ArrayList<>();// создать и заполнить лист результатами проверок значений что обязательно должны быть
        resultList.add(Checker.checkTextField(nameField));//0
        resultList.add(Checker.checkTextField(surnameField));//1
        resultList.add(Checker.checkTextField(loginField));//2
        resultList.add(checkPasswordField());//3
        resultList.add(checkPasswordField());//4
        resultList.add(Checker.checkTextField(emailField));//5
        return resultList;
    }

    @NotNull
    private List<TextField> fillListByNotNullTextFields() {
        final List<TextField> textFields = new ArrayList<>();
        textFields.add(nameField);//0
        textFields.add(surnameField);//1
        textFields.add(loginField);//2
        textFields.add(firstPasswordField);//3
        textFields.add(secondPasswordField);//4
        textFields.add(emailField);//5
        return textFields;
    }

    //check password fields, they must match or not to be an empty
    private boolean checkPasswordField() {
        final String password1 = firstPasswordField.getText().trim();
        final String password2 = secondPasswordField.getText().trim();
        if (password1.isEmpty() || password2.isEmpty()) {
            return true;
        }
        return !password1.equals(password2);
    }
}

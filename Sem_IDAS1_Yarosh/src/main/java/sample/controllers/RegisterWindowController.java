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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
            changePasswordsLabel(secondPasswordField.getText(), newValue, Color.RED);
            cutSpace(firstPasswordField, newValue, Color.RED);
        });
        secondPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            changePasswordsLabel(firstPasswordField.getText(), newValue, Color.RED);
            cutSpace(secondPasswordField, newValue, Color.RED);
        });
        loginField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, loginField, StylesEnum.EMPTY_STRING.getStyle()));
        nameField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, nameField, StylesEnum.EMPTY_STRING.getStyle()));
        surnameField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, surnameField, StylesEnum.EMPTY_STRING.getStyle()));
        emailField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, emailField, StylesEnum.EMPTY_STRING.getStyle()));
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    Integer.parseInt(newValue);
                }
            } catch (NumberFormatException e) {
                telephoneField.setText(oldValue);
            }
        });

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

    private void cutSpace(final TextField textField, @NotNull final String text, final Color textColor) {
        if (text.contains(" ")) {
            final String result = text.replace(" ", StylesEnum.EMPTY_STRING.getStyle());
            textField.setText(result);
            changeLabelAttributes(comparePasswordsLabel, "Spaces in password are forbidden.", textColor);
        }
    }

    private void changePasswordsLabel(final String password, @NotNull final String newValue, final Color textColor) {
        if (!newValue.equals(password)) {
            changeLabelAttributes(comparePasswordsLabel, "Passwords have to match!", textColor);
        } else {
            changeLabelAttributes(comparePasswordsLabel, StylesEnum.EMPTY_STRING.getStyle(), Color.TRANSPARENT);
            changePasswordFieldsStyle(StylesEnum.EMPTY_STRING.getStyle());
        }
    }

    private void changeLabelAttributes(@NotNull final Label label, final String text, final Color textColor) {
        label.setText(text);
        label.setTextFill(textColor);
    }

    private void changePasswordFieldsStyle(final String style) {
        firstPasswordField.setStyle(style);
        secondPasswordField.setStyle(style);
    }

    private void changeTextFieldStyle(@NotNull final String newValue, final TextField textField, final String style) {
        if (!newValue.isEmpty()) {
            textField.setStyle(style);
        }
    }

    @FXML
    void cancelRegistration(ActionEvent event) {
        closeWindow();
    }

    @FXML
    private void registerNewUser(ActionEvent event) {
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(EXECUTE_QUERY);

            final List<Boolean> resultList = writeNotNullFieldsInList();

            if (resultList.contains(true)) {//если хоть одно обязательное поле пустое
                for (int i = 0; i < textFieldList.size(); i++) {
                    if (resultList.get(i)) {
                        Shake.shake(textFieldList.get(i));
                        textFieldList.get(i).setStyle(StylesEnum.ERROR_STYLE.getStyle());
                    }
                }
                changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
            } else {
                if (checkLoginForUnique()) {
                    preparedStatement.setString(ElsaUserColumns.NAME.getColumnIndex(), nameField.getText().trim());
                    preparedStatement.setString(ElsaUserColumns.SURNAME.getColumnIndex(), surnameField.getText().trim());
                    preparedStatement.setString(ElsaUserColumns.LOGIN.getColumnIndex(), loginField.getText().trim());
                    preparedStatement.setString(ElsaUserColumns.PASSWORD.getColumnIndex(), firstPasswordField.getText().trim());
                    preparedStatement.setString(ElsaUserColumns.EMAIL.getColumnIndex(), emailField.getText().trim());
                    preparedStatement.setInt(ElsaUserColumns.ROLE_ID.getColumnIndex(), Role.NEW.getIndex());
                    checkNullPossibleFields();
                    preparedStatement.execute();
                    closeWindow();
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkLoginForUnique() throws SQLException {
        final String login = loginField.getText().trim();
        final String selectQuery = "select st58310.elsa_user.login from st58310.elsa_user where login like ?";
        final PreparedStatement checkSelection = dbManager.getConnection().prepareStatement(selectQuery);
        checkSelection.setString(1, login);
        final ResultSet loginFields = checkSelection.executeQuery();
        if (loginFields.next()) {
            changeLabelAttributes(messageLabel, "Login must be unique!", Color.RED);
            textFieldList.get(ElsaUserColumns.LOGIN.getColumnIndex() - 1).setStyle(StylesEnum.ERROR_STYLE.getStyle());
            Shake.shake(textFieldList.get(ElsaUserColumns.LOGIN.getColumnIndex() - 1));
            return false;
        } else {
            return true;
        }
    }

    private void closeWindow() {
        final Stage currentStage = (Stage) registerBtn.getScene().getWindow();
        currentStage.close();
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

    private void checkNullPossibleFields() throws SQLException, FileNotFoundException {
        checkTextField(checkTextField(telephoneField),
                ElsaUserColumns.TELEPHONE.getColumnIndex(),
                countryCodePicker.getValue().concat(telephoneField.getText().trim()));
        checkTextField(aboutYourselfTextArea.getText().trim().isEmpty(),
                ElsaUserColumns.ABOUT.getColumnIndex(),
                aboutYourselfTextArea.getText().trim());
        checkImage(file == null);
    }

    private void checkImage(final boolean state) throws SQLException, FileNotFoundException {
        if (state) {// check image field
            preparedStatement.setNull(ElsaUserColumns.IMAGE.getColumnIndex(), Types.NULL);
        } else {
            final InputStream fileInputStream = new FileInputStream(file);
            preparedStatement.setBlob(ElsaUserColumns.IMAGE.getColumnIndex(), fileInputStream, file.length());
        }
    }

    @NotNull
    private List<Boolean> writeNotNullFieldsInList() {
        final List<Boolean> resultList = new ArrayList<>();// создать и заполнить лист результатами проверок значений что обязательно должны быть
        resultList.add(checkTextField(nameField));//0
        resultList.add(checkTextField(surnameField));//1
        resultList.add(checkTextField(loginField));//2
        resultList.add(checkPasswordField());//3
        resultList.add(checkPasswordField());//4
        resultList.add(checkTextField(emailField));//5
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

    private boolean checkPasswordField() {
        final String password1 = firstPasswordField.getText().trim();
        final String password2 = secondPasswordField.getText().trim();
        if (password1.isEmpty() || password2.isEmpty()) {
            return true;
        }
        return !password1.equals(password2);
    }

    private boolean checkTextField(@NotNull final TextField textField) { return textField.getText().trim().isEmpty(); }

    private void checkTextField(final boolean state, final int columnIndex, final String text) throws SQLException {
        if (state) {
            preparedStatement.setNull(columnIndex, Types.NULL);
        } else {
            preparedStatement.setString(columnIndex, text);
        }
    }
}

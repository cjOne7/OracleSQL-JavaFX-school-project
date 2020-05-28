package sample.controllers;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.DatabaseManagement.DbManager;
import sample.Role;
import sample.Shake;

import java.io.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegisterWindowController implements Initializable {

    private static final int NAME = 1;
    private static final int SURNAME = 2;
    private static final int LOGIN = 3;
    private static final int PASSWORD = 4;
    private static final int E_MAIL = 5;
    private static final int TELEPHONE = 6;
    private static final int ABOUT = 7;
    private static final int IMAGE = 8;
    private static final int USER_STATUS = 9;

    private static final String EXECUTE_QUERY = "INSERT into ST58310.ELSA_USER " +
            "(name, surname, login, password, email, telephone, about, image, role_id) " +
            "values (?,?,?,?,?,?,?,?,?)";

    private Image image;
    private File file;

    private List<TextField> textFieldList;
    private DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;

    private String prevLogin;

    private ObservableList<String> countryCodes;

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
        textFieldList = fillList();
        firstPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            changePasswordsLabel(secondPasswordField.getText(), newValue, Color.RED);
            cutSpace(firstPasswordField, newValue, Color.RED);
        });
        secondPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            changePasswordsLabel(firstPasswordField.getText(), newValue, Color.RED);
            cutSpace(secondPasswordField, newValue, Color.RED);
        });
        loginField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, loginField, ""));
        nameField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, nameField, ""));
        surnameField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, surnameField, ""));
        emailField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, emailField, ""));

        countryCodes = Stream.of(Locale.getISOCountries())
                .map(locales -> new Locale("", locales).getCountry())
                .filter(countryCode -> getPhoneCountyCode(countryCode) != 0)
                .map(countryCode -> String.format("%s +%d", countryCode, getPhoneCountyCode(countryCode)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        //Запускаем стрим на все доступные страны
        //возвращаем код страны
        //нужна проверка, потому что если нет в базе присоеденённой библиотеки кода страны, то вернёт 0
        //собираем строку вида АБ +123
        //возвращаем нужный объект

        countryCodePicker.setItems(countryCodes);
        countryCodePicker.setValue(countryCodes.get(0));

        nameField.setText("Nikita");
        surnameField.setText("Yarosh");
        loginField.setText("cj_one");
        firstPasswordField.setText("123");
        secondPasswordField.setText("123");
        emailField.setText("nik_yar@gmail.com");
        aboutYourselfTextArea.setText("Hi! My name is Nikita.");
        telephoneField.setText("773089030");
    }

    private int getPhoneCountyCode(final String countryCode) {
        return PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryCode);
    }

    private void cutSpace(final TextField textField, @NotNull final String text, final Color textColor) {
        if (text.contains(" ")) {
            final String result = text.replace(" ", "");
            textField.setText(result);
            changeLabelAttributes(comparePasswordsLabel, "Spaces in password are forbidden.", textColor);
        }
    }

    private void changePasswordsLabel(final String password, @NotNull final String newValue, final Color textColor) {
        if (!newValue.equals(password)) {
            changeLabelAttributes(comparePasswordsLabel, "Passwords have to match!", textColor);
        } else {
            changeLabelAttributes(comparePasswordsLabel, "", Color.TRANSPARENT);
            changePasswordFieldsStyle("");
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
    private void loadImage(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG, JPEG, PNG", "*.jpg", "*.jpeg", "*.png"));
        file = fileChooser.showOpenDialog(null);
        if (file != null) {
            image = new Image(file.toURI().toString());
            imageView.setImage(image);
            imageView.setPreserveRatio(false);
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
                        shake(i);
                        textFieldList.get(i).setStyle("-fx-border-color: red; -fx-border-radius: 5;");
                    }
                }
                changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
//----альтернативный варант проверки и тряски незаполненных полей при условии, что в resultList нет проверки на второе парольное поле----
//                for (int i = 0; i < resultList.size(); i++) {
//                    if (resultList.get(i)) {
//                        if (i == 3) {
//                            shakeTextField(i);
//                            shakeTextField(i + 1);
//                        } else if (i > 3) {
//                            shakeTextField(i + 1);
//                        } else {
//                            shakeTextField(i);
//                        }
//                    }
//                }
//                changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED)
//----------------------------------------------------------------------------------------------------------------------
            } else {
                if (checkLoginForUnique()) {
                    preparedStatement.setString(NAME, nameField.getText().trim());
                    preparedStatement.setString(SURNAME, surnameField.getText().trim());
                    preparedStatement.setString(LOGIN, loginField.getText().trim());
                    preparedStatement.setString(PASSWORD, firstPasswordField.getText().trim());
                    preparedStatement.setString(E_MAIL, emailField.getText().trim());
                    preparedStatement.setInt(USER_STATUS, Role.NEW.getIndex());
                    checkNullPossibleFields(preparedStatement);
                    preparedStatement.execute();
                    closeWindow();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
            textFieldList.get(LOGIN - 1).setStyle("-fx-border-color: red; -fx-border-radius: 5;");
            shake(LOGIN - 1);
            return false;
        } else {
            return true;
        }
    }

    private void shake(final int index) {
        final Shake shake = new Shake(textFieldList.get(index));
        shake.getTranslateTransition().playFromStart();
    }

    private void closeWindow() {
        final Stage currentStage = (Stage) registerBtn.getScene().getWindow();
        currentStage.close();
    }
//--------------метод для использования в методе registerNewUser при использовании альтернативной проверки--------------
//    private void shakeTextField(final int index) {
//        if (index >= 0) {
//            final Shake shake = new Shake(textFieldList.get(index));
//            shake.getTranslateTransition().playFromStart();
//            textFieldList.get(index).setStyle("-fx-border-color: red; -fx-border-radius: 5;");
//        }
//    }

    @FXML
    private void emailAsLogin(ActionEvent actionEvent) {
        final String prevLogin = loginField.getText();
        if (checkMailBox.isSelected()) {
            loginField.setText(emailField.getText());
            loginField.setDisable(true);
        } else {
            loginField.setText(this.prevLogin);
            loginField.setDisable(false);
        }
        this.prevLogin = prevLogin;
    }

    private void checkNullPossibleFields(final PreparedStatement preparedStatement) throws SQLException, FileNotFoundException {
        if (checkTelephoneField()) {// check phone field
            preparedStatement.setNull(TELEPHONE, Types.NULL);
        } else {
            String countryCode = countryCodePicker.getValue();
            final int index = countryCode.indexOf("+");
            countryCode = countryCode.substring(index);
            preparedStatement.setString(TELEPHONE, countryCode.concat(telephoneField.getText().trim()));
        }
        if (checkAboutField()) {//check about field
            preparedStatement.setNull(ABOUT, Types.NULL);
        } else {
            preparedStatement.setString(ABOUT, aboutYourselfTextArea.getText().trim());
        }
        if (checkImage()) {// check image field
            preparedStatement.setNull(IMAGE, Types.NULL);
        } else {
            final InputStream fileInputStream = new FileInputStream(file);
            preparedStatement.setBlob(IMAGE, fileInputStream, file.length());
        }
    }

    @NotNull
    private List<Boolean> writeNotNullFieldsInList() {
        final List<Boolean> resultList = new ArrayList<>();// создать и заполнить лист результатами проверок значений что обязательно должны быть
        resultList.add(checkNameField());//0
        resultList.add(checkSurnameField());//1
        resultList.add(checkLoginField());//2
        resultList.add(checkPasswordField());//3
        resultList.add(checkPasswordField());//4
        resultList.add(checkEmailField());//5
        return resultList;
    }

    @NotNull
    private List<TextField> fillList() {
        final List<TextField> textFields = new ArrayList<>();
        textFields.add(nameField);//0
        textFields.add(surnameField);//1
        textFields.add(loginField);//2
        textFields.add(firstPasswordField);//3
        textFields.add(secondPasswordField);//4
        textFields.add(emailField);//5
        return textFields;
    }

    private boolean checkNameField() {
        return nameField.getText().trim().isEmpty();
    }

    private boolean checkSurnameField() {
        return surnameField.getText().trim().isEmpty();
    }

    private boolean checkEmailField() {
        return emailField.getText().trim().isEmpty();
    }

    private boolean checkLoginField() {
        return checkMailBox.isSelected() ? checkEmailField() : loginField.getText().trim().isEmpty();
    }

    private boolean checkPasswordField() {
        final String password1 = firstPasswordField.getText().trim();
        final String password2 = secondPasswordField.getText().trim();
        if (password1.isEmpty() || password2.isEmpty()) {
            return true;
        }
        return !password1.equals(password2);
    }

    private boolean checkTelephoneField() {
        return telephoneField.getText().trim().isEmpty();
    }

    private boolean checkAboutField() {
        return aboutYourselfTextArea.getText().trim().isEmpty();
    }

    private boolean checkImage() {
        return image == null;
    }
}
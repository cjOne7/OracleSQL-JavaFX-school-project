package sample.controllers;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.CountryCodePicker;
import sample.DatabaseManagement.DbManager;

import java.io.*;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sample.Enums.ElsaUserFields;
import sample.User;

public class UserProfileWindowController implements Initializable {

    private static final int LAYOUT_X_OF_LOAD_BTN = 735;
    private static final int LAYOUT_Y_OF_LOAD_BTN = 161;
    private static final int WIDTH_OF_LOAD_BTN = 142;

    private static final int NAME = 1;
    private static final int SURNAME = 2;
    private static final int LOGIN = 3;
    private static final int PASSWORD = 4;
    private static final int E_MAIL = 5;
    private static final int TELEPHONE = 6;
    private static final int ABOUT = 7;
    private static final int IMAGE = 8;


    private final DbManager dbManager = new DbManager();
    private ObservableList<String> countryCodes;
    private Image image;
    private File file;
    private User user;

    public static int userID;

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
    private Button changePasswordBtn;
    @FXML
    private Button loadImageBtn;
    @FXML
    private Button cancelBtn;

    public UserProfileWindowController() {
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        final String selectQuery = "SELECT * from ST58310.ELSA_USER where USER_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, MainWindowController.userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                final String name = fillTextField(nameField, ElsaUserFields.NAME.toString(), resultSet);
                final String surname = fillTextField(surnameField, ElsaUserFields.SURNAME.toString(), resultSet);
                final String email = fillTextField(emailField, ElsaUserFields.EMAIL.toString(), resultSet);
                final String login = fillTextField(loginField, ElsaUserFields.LOGIN.toString(), resultSet);
                final String password = resultSet.getString(ElsaUserFields.PASSWORD.toString());
                final String telephone = resultSet.getString(ElsaUserFields.TELEPHONE.toString());

                final String about = resultSet.getString(ElsaUserFields.ABOUT.toString());
                aboutYourselfTextArea.setText(about);
                final Blob image = resultSet.getBlob(ElsaUserFields.IMAGE.toString());
                if (image != null) {
                    try {
                        final InputStream input = image.getBinaryStream();
                        file = new File("image");
                        final FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        while (input.read(buffer) > 0) {
                            fos.write(buffer);
                        }
                        this.image = new Image(file.toURI().toString());
                        imageView.setImage(new Image(file.toURI().toString()));

                        double rootWidth = imageView.getFitWidth();
                        double rootHeight = imageView.getFitHeight();
                        double imageWidth = this.image.getWidth();
                        double imageHeight = this.image.getHeight();
                        double ratioX = rootWidth / imageWidth;
                        double ratioY = rootHeight / imageHeight;
                        double ratio = Math.min(ratioX, ratioY);
                        imageView.setPreserveRatio(false);
                        double width = ratio * imageWidth;
                        double height = ratio * imageHeight;
                        imageView.setFitWidth(width);
                        imageView.setFitHeight(height);

                        imageView.setLayoutX(LAYOUT_X_OF_LOAD_BTN + (WIDTH_OF_LOAD_BTN - imageView.getFitWidth()) / 2);
                        imageView.setLayoutY(LAYOUT_Y_OF_LOAD_BTN - imageView.getFitHeight() - 10);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                final int role = resultSet.getInt("ROLE_ID");
                user = new User(name, surname, login, password, email, telephone, about, image, role);

                countryCodes = CountryCodePicker.getCountryCodes();
                countryCodePicker.setItems(countryCodes);
                countryCodePicker.setValue(getISOCountry(telephone));
                telephoneField.setText(getPhoneNumber(telephone));
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

    private String getISOCountry(final String telephone) {
        final List<String> list = countryCodes.stream().filter(str -> str.substring(0, 2).equals(telephone.substring(0, 2))).collect(Collectors.toList());
        return list.isEmpty() ? countryCodes.get(0) : list.get(0);
    }

    @NotNull
    private String getPhoneNumber(@NotNull final String telephone) {
        return telephone.substring(getISOCountry(telephone).length());
    }

    @FXML
    private void changePassword(ActionEvent event) {

    }

    @FXML
    private void loadImage(ActionEvent event) {
//      file =  ImageManager.loadImage(imageView, loadImageBtn);
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG, JPEG, PNG", "*.jpg", "*.jpeg", "*.png"));
        file = fileChooser.showOpenDialog(null);
        if (file != null) {
            image = new Image(file.toURI().toString());
            imageView.setImage(image);
            double rootWidth = imageView.getFitWidth();
            double rootHeight = imageView.getFitHeight();
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double ratioX = rootWidth / imageWidth;
            double ratioY = rootHeight / imageHeight;
            double ratio = Math.min(ratioX, ratioY);
            imageView.setPreserveRatio(false);
            double width = ratio * imageWidth;
            double height = ratio * imageHeight;
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);

            imageView.setLayoutX(loadImageBtn.getLayoutX() + (loadImageBtn.getWidth() - imageView.getFitWidth()) / 2);
            imageView.setLayoutY(loadImageBtn.getLayoutY() - imageView.getFitHeight() - 10);
        }
    }

    @FXML
    private void updateUsersData(ActionEvent event) {
        final String updateQuery = "UPDATE ST58310.ELSA_USER set NAME = ?, SURNAME = ?, LOGIN = ?, PASSWORD = ?, EMAIL = ?, TELEPHONE = ?, ABOUT = ?, IMAGE = ? where USER_ID = ?";
        try {
            final PreparedStatement updateStatement = dbManager.getConnection().prepareStatement(updateQuery);
            updateStatement.setString(NAME, nameField.getText().trim());
            updateStatement.setString(SURNAME, surnameField.getText().trim());
            updateStatement.setString(LOGIN, loginField.getText().trim());
            updateStatement.setString(PASSWORD, user.getPassword());
            updateStatement.setString(E_MAIL, emailField.getText().trim());
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void cancelEditing(ActionEvent event) {
        final Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
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
        return loginField.getText().trim().isEmpty();
    }

    private boolean checkPasswordField() {
        return false;
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

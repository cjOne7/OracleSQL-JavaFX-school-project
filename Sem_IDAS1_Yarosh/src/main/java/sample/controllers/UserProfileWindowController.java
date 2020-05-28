package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class UserProfileWindowController implements Initializable {

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
    private Button registerBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void cancelRegistration(ActionEvent event) {

    }

    @FXML
    void loadImage(ActionEvent event) {

    }

    @FXML
    void registerNewUser(ActionEvent event) {

    }
}

package sample.controllers;

import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.DatabaseManagement.DbManager;
import sample.Role;
import sample.User;

public class AdministratorWindowController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<User> sortedList = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Role> roleFilterComboBox;
    @FXML
    private ComboBox<Role> roleChangerComboBox;
    @FXML
    private ListView<User> listViewWithNewUsers;
    @FXML
    private Button exitBtn;
    @FXML
    private Button changeRoleBtn;
    @FXML
    private Button deleteUserBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        listViewWithNewUsers.setItems(users);

        String selectQuery = "";

        if (MainWindowController.role_id == Role.ADMINISTRATOR.getIndex()) {
            fillComboBoxes(Role.ADMINISTRATOR, roleFilterComboBox, roleChangerComboBox);
            selectQuery = String.format("select NAME, SURNAME, EMAIL, TELEPHONE, LOGIN, ROLE_ID from ST58310.ELSA_USER where ROLE_ID < %d", Role.MAIN_ADMIN.getIndex());

        } else if (MainWindowController.role_id == Role.MAIN_ADMIN.getIndex()) {
            fillComboBoxes(Role.MAIN_ADMIN, roleFilterComboBox, roleChangerComboBox);
            selectQuery = "select NAME, SURNAME, EMAIL, TELEPHONE, LOGIN, ROLE_ID from ST58310.ELSA_USER";
        }

        roleFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            sortedList = users.stream()
                    .filter(user -> newValue.getIndex() == user.getRoleId())
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            listViewWithNewUsers.setItems(sortedList);
            if (MainWindowController.role_id == roleFilterComboBox.getValue().getIndex()) {
                changeDisable(true);
            } else {
                changeDisable(false);
            }
        });

        fillListView(selectQuery);
        roleFilterComboBox.setValue(Role.NEW);
        roleChangerComboBox.setValue(Role.NEW);
    }

    private void fillComboBoxes(@NotNull final Role topBorder, final ComboBox<Role>... comboBox) {
        Arrays.stream(Role.values()).limit(topBorder.getIndex()).forEach(role -> comboBox[0].getItems().add(role));//fill combobox for filter watching
        Arrays.stream(Role.values()).limit(topBorder.getIndex() - 1).forEach(role -> comboBox[1].getItems().add(role));//fill combobox for choosing an user who will be deleted/updated
    }

    private void fillListView(final String selectQuery) {
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final String name = resultSet.getString("NAME");
                final String surname = resultSet.getString("SURNAME");
                final String email = resultSet.getString("EMAIL");
                final String telephone = resultSet.getString("TELEPHONE");
                final String login = resultSet.getString("LOGIN");
                final int role = resultSet.getInt("ROLE_ID");
                final User newUser = new User(name, surname, email, telephone, login, role);
                users.add(newUser);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void changeRole(ActionEvent event) {
        final User user = listViewWithNewUsers.getSelectionModel().getSelectedItem();
        if (user != null) {
            final int role = roleChangerComboBox.getValue().getIndex();
            final String updateQuery = "UPDATE ST58310.ELSA_USER set ROLE_ID = ? where LOGIN like ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(updateQuery);
                preparedStatement.setInt(1, role);
                preparedStatement.setString(2, user.getLogin());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            listViewWithNewUsers.getItems().remove(user);
            user.setRoleId(role);
        } else {
            Main.callAlertWindow("Warning", "User is not selected!", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void deleteUserFromTable(ActionEvent event) {
        final User user = listViewWithNewUsers.getSelectionModel().getSelectedItem();
        if (user != null) {
            final String deleteQuery = "DELETE from ST58310.ELSA_USER where LOGIN like ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setString(1, user.getLogin());
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            listViewWithNewUsers.getItems().remove(user);
        } else {
            Main.callAlertWindow("Warning", "User is not selected!", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void openProfile(ActionEvent event) {
        OpenNewWindow.openNewWindow("/UserProfileWindow.fxml", getClass(), false, "Profile window", new Image("/images/profile_icon.png"));

    }

    @FXML
    private void exitToMainScreen(ActionEvent event) {
        final Stage stage = (Stage) exitBtn.getScene().getWindow();
        stage.close();
        OpenNewWindow.openNewWindow("/MainWindow.fxml", getClass(), false, "Exiting window", new Image("/images/list.png"));
    }

    private void changeDisable(final boolean state) {
        deleteUserBtn.setDisable(state);
        changeRoleBtn.setDisable(state);
    }
}

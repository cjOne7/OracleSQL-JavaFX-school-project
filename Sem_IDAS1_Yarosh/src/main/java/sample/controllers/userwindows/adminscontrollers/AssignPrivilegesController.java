package sample.controllers.userwindows.adminscontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.controllers.MainWindowController;
import sample.controllers.OpenNewWindow;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.User;
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.enums.StylesEnum;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AssignPrivilegesController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<User> sortedList = FXCollections.observableArrayList();

    private int roleID;
    private String selectQuery;

    @FXML
    private ComboBox<Role> roleFilterComboBox;
    @FXML
    private ComboBox<Role> roleChangerComboBox;
    @FXML
    private ListView<User> listViewWithNewUsers;
    @FXML
    private Button changeRoleBtn;
    @FXML
    private Button deleteUserBtn;
    @FXML
    private Button watchProfileBtn;
    @FXML
    private Button closeBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listViewWithNewUsers.setItems(users);

        listViewWithNewUsers.setStyle(StylesEnum.FONT_STYLE.getStyle());

        roleFilterComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());
        roleChangerComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());

        selectQuery = "SELECT ROLE_ID from ST58310.ELSA_USER where USER_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, MainWindowController.curUserId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                roleID = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //if user logged as ADMINISTRATOR
        if (roleID == Role.ADMINISTRATOR.getIndex()) {
            fillRoleComboBoxes(Role.ADMINISTRATOR, roleFilterComboBox, roleChangerComboBox);
            selectQuery = String.format("SELECT USER_ID, NAME, SURNAME, EMAIL, TELEPHONE, LOGIN, ROLE_ID from ST58310.ELSA_USER where ROLE_ID < %d", Role.MAIN_ADMIN.getIndex());
            //if user logged as MAIN_ADMIN
        } else if (roleID == Role.MAIN_ADMIN.getIndex()) {
            fillRoleComboBoxes(Role.MAIN_ADMIN, roleFilterComboBox, roleChangerComboBox);
            selectQuery = "SELECT USER_ID, NAME, SURNAME, EMAIL, TELEPHONE, LOGIN, ROLE_ID from ST58310.ELSA_USER";
        }

        roleFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            sortedList = users.stream()
                    .filter(user -> newValue.getIndex() == user.getRoleId())
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            listViewWithNewUsers.setItems(sortedList);

            if (roleID == roleFilterComboBox.getValue().getIndex()) {
                changeDisable(true);
            } else {
                changeDisable(false);
            }
        });

        fillUsersListView(selectQuery);
        roleFilterComboBox.setValue(Role.NEW);
        roleChangerComboBox.setValue(Role.STUDENT);

        if (users.size() >= 1) {
            changeDisable(false);
        }
    }

    @SafeVarargs
    private final void fillRoleComboBoxes(@NotNull final Role topBorder, final ComboBox<Role>... comboBox) {
        Arrays.stream(Role.values()).limit(topBorder.getIndex()).forEach(role -> comboBox[0].getItems().add(role));//fill combobox for filter watching
        Arrays.stream(Role.values()).limit(topBorder.getIndex() - 1).forEach(role -> comboBox[1].getItems().add(role));//fill combobox for choosing an user who will be deleted/updated
    }

    private void fillUsersListView(final String selectQuery) {
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int userId = resultSet.getInt(ElsaUserColumns.USER_ID.toString());
                final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
                final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
                final String email = resultSet.getString(ElsaUserColumns.EMAIL.toString());
                final String telephone = resultSet.getString(ElsaUserColumns.TELEPHONE.toString());
                final String login = resultSet.getString(ElsaUserColumns.LOGIN.toString());
                final int role = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
                final User newUser = new User(name, surname, email, telephone, login, role, userId);
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
            Main.callAlertWindow("Warning", "User is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        }
    }

    @FXML
    private void deleteUserFromTable(ActionEvent event) {
        final User user = listViewWithNewUsers.getSelectionModel().getSelectedItem();
        if (user == null) {
            Main.callAlertWindow("Warning", "User is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String deleteQuery = "DELETE from ST58310.ELSA_USER where LOGIN like ?";
            try {
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setString(1, user.getLogin());
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            listViewWithNewUsers.getItems().remove(user);
        }
        if (users.isEmpty()) {
            changeDisable(true);
        }
    }

    @FXML
    private void addNewUser(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/RegisterWindow.fxml", getClass(), false, "Register window", new Image("/images/new_user.png"));
        refreshList(event);
        if (users.size() >= 1 && roleFilterComboBox.getValue() != Role.MAIN_ADMIN) {
            changeDisable(false);
        }
    }

    private void changeDisable(final boolean state) {
        deleteUserBtn.setDisable(state);
        changeRoleBtn.setDisable(state);
        watchProfileBtn.setDisable(state);
    }

    @FXML
    private void refreshList(ActionEvent event) {
        users.clear();
        fillUsersListView(selectQuery);
        //filter to match with combobox value
        final ObservableList<User> observableList = users.stream().filter(user -> user.getRoleId() == roleFilterComboBox.getValue().getIndex()).collect(Collectors.toCollection(FXCollections::observableArrayList));
        listViewWithNewUsers.setItems(observableList);
        if (users.size() >= 1) {
            changeDisable(false);
        }
    }

    @FXML
    private void watchUserProfile(ActionEvent event) {
        final User user = listViewWithNewUsers.getSelectionModel().getSelectedItem();
        if (user == null) {
            Main.callAlertWindow("Warning", "User is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            MainWindowController.curUserId = user.getUserId();
            OpenNewWindow.openNewWindow("/fxmlfiles/UserProfileWindow.fxml", getClass(), false, "Profile window", new Image("/images/profile_icon.png"));
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

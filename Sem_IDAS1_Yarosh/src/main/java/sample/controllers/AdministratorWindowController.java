package sample.controllers;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import sample.Shake;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Subject;
import sample.enums.ElsaUserColumns;
import sample.enums.Role;
import sample.dbtableclasses.User;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

public class AdministratorWindowController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<User> sortedList = FXCollections.observableArrayList();

    //---------------------elements for the first tab on the admin window (assign privileges)---------------------------
    //---------------------elements for the first tab on the admin window (assign privileges)---------------------------
    //---------------------elements for the first tab on the admin window (assign privileges)---------------------------
    private int roleID;
    private String selectQuery;
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
    @FXML
    private Tab assignPrivilegeTab;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        //------------------------------------------code for the first tab----------------------------------------------
        //------------------------------------------code for the first tab----------------------------------------------
        //------------------------------------------code for the first tab----------------------------------------------
        listViewWithNewUsers.setItems(users);

        listViewWithNewUsers.setStyle(StylesEnum.FONT_STYLE.getStyle());

        assignPrivilegeTab.setStyle(StylesEnum.TAB_STYLE.getStyle());
        subjectManagementTab.setStyle(StylesEnum.TAB_STYLE.getStyle());

        roleFilterComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());
        roleChangerComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());

        selectQuery = "SELECT ROLE_ID from ST58310.ELSA_USER where USER_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, MainWindowController.userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                roleID = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (roleID == Role.ADMINISTRATOR.getIndex()) {
            fillRoleComboBoxes(Role.ADMINISTRATOR, roleFilterComboBox, roleChangerComboBox);
            selectQuery = String.format("SELECT NAME, SURNAME, EMAIL, TELEPHONE, LOGIN, ROLE_ID from ST58310.ELSA_USER where ROLE_ID < %d", Role.MAIN_ADMIN.getIndex());
        } else if (roleID == Role.MAIN_ADMIN.getIndex()) {
            fillRoleComboBoxes(Role.MAIN_ADMIN, roleFilterComboBox, roleChangerComboBox);
            selectQuery = "SELECT NAME, SURNAME, EMAIL, TELEPHONE, LOGIN, ROLE_ID from ST58310.ELSA_USER";
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
        roleChangerComboBox.setValue(Role.NEW);

        //-------------------------------------------code for the second tab--------------------------------------------
        //-------------------------------------------code for the second tab--------------------------------------------
        //-------------------------------------------code for the second tab--------------------------------------------
        allSubjectsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        allSubjectsListView.setItems(allSubjects);
        fillSubjectsListView(allSubjects);

        textFieldList = fillListByNotNullTextFields();

        initializeSpinner(yearSpinner, 1, 3);
        initializeSpinner(semesterSpinner, 1, 2);

        creditsField.onMouseClickedProperty().addListener((observable, oldValue, newValue) -> creditsField.selectAll());
        creditsField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    Integer.parseInt(newValue);
                }
            } catch (NumberFormatException e) {
                creditsField.setText(oldValue);
            }
            changeTextFieldStyle(newValue, creditsField, StylesEnum.EMPTY_STRING.getStyle());
        });
        subjectNameField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, subjectNameField, StylesEnum.EMPTY_STRING.getStyle()));
        abbreviationField.textProperty().addListener((observable, oldValue, newValue) -> changeTextFieldStyle(newValue, abbreviationField, StylesEnum.EMPTY_STRING.getStyle()));
        example();
    }

    private void example() {
        subjectNameField.setText("Matan");
        abbreviationField.setText("Mat");
        creditsField.setText("6");
    }

    private void fillRoleComboBoxes(@NotNull final Role topBorder, final ComboBox<Role>... comboBox) {
        Arrays.stream(Role.values()).limit(topBorder.getIndex()).forEach(role -> comboBox[0].getItems().add(role));//fill combobox for filter watching
        Arrays.stream(Role.values()).limit(topBorder.getIndex() - 1).forEach(role -> comboBox[1].getItems().add(role));//fill combobox for choosing an user who will be deleted/updated
    }

    private void fillUsersListView(final String selectQuery) {
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final String name = resultSet.getString(ElsaUserColumns.NAME.toString());
                final String surname = resultSet.getString(ElsaUserColumns.SURNAME.toString());
                final String email = resultSet.getString(ElsaUserColumns.EMAIL.toString());
                final String telephone = resultSet.getString(ElsaUserColumns.TELEPHONE.toString());
                final String login = resultSet.getString(ElsaUserColumns.LOGIN.toString());
                final int role = resultSet.getInt(ElsaUserColumns.ROLE_ID.toString());
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
            Main.callAlertWindow("Warning", "User is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
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
            Main.callAlertWindow("Warning", "User is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        }
    }

    @FXML
    private void openProfile(ActionEvent event) {
        OpenNewWindow.openNewWindow("/fxmlfiles/UserProfileWindow.fxml", getClass(), false, "Profile window", new Image("/images/profile_icon.png"));
    }

    @FXML
    private void exitToMainScreen(ActionEvent event) {
        MainWindowController.openMainStage(exitBtn);
    }

    @FXML
    private void refreshList(ActionEvent event) {
        users.removeAll(users);
        fillUsersListView(selectQuery);
        final ObservableList<User> observableList = users.stream().filter(user -> user.getRoleId() == roleFilterComboBox.getValue().getIndex()).collect(Collectors.toCollection(FXCollections::observableArrayList));
        listViewWithNewUsers.setItems(observableList);
    }

    private void changeDisable(final boolean state) {
        deleteUserBtn.setDisable(state);
        changeRoleBtn.setDisable(state);
    }

    //-----------------------elements for the second tab on the admin window (subjects management)----------------------
    //-----------------------elements for the second tab on the admin window (subjects management)----------------------
    //-----------------------elements for the second tab on the admin window (subjects management)----------------------

    private final ObservableList<Subject> allSubjects = FXCollections.observableArrayList();
    private List<TextField> textFieldList;
    private PreparedStatement preparedStatement;
    private Subject subject;
    private int subjectId;

    @FXML
    private Tab subjectManagementTab;
    @FXML
    private TextField abbreviationField;
    @FXML
    private TextField creditsField;
    @FXML
    private TextField subjectNameField;
    @FXML
    private Spinner<Integer> yearSpinner;
    @FXML
    private Spinner<Integer> semesterSpinner;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private ListView<Subject> allSubjectsListView;
    @FXML
    private Label createSubjectMesLabel;
    @FXML
    private Label changeSubjectMessageLabel;

    //------------------------------------------methods for the second tab----------------------------------------------
    //------------------------------------------methods for the second tab----------------------------------------------
    //------------------------------------------methods for the second tab----------------------------------------------

    @FXML
    private void createSubject(ActionEvent event) {
        final String insertQuery = "INSERT INTO ST58310.SUBJECT (NAME, ABBREVIATION, CREDITS, SEMESTER, DESCRIPTION, YEAR) VALUES (?,?,?,?,?,?)";
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(insertQuery);
            final List<Boolean> resultList = writeNotNullFieldsInList();
            if (resultList.contains(true)) {//если хоть одно обязательное поле пустое
                for (int i = 0; i < textFieldList.size(); i++) {
                    if (resultList.get(i)) {
                        Shake.shake(textFieldList.get(i));
                        textFieldList.get(i).setStyle(StylesEnum.ERROR_STYLE.getStyle());
                    }
                }
                changeLabelAttributes(createSubjectMesLabel, "Fields with * have to be filled!", Color.RED);
            } else {
                if (checkForUnique()){
                    subject = new Subject(
                            subjectId,
                            subjectNameField.getText().trim(),
                            abbreviationField.getText().trim(),
                            Integer.parseInt(creditsField.getText().trim()),
                            semesterSpinner.getValue(),
                            yearSpinner.getValue(),
                            descriptionTextArea.getText().trim());
                    preparedStatement.setString(SubjectColumns.NAME.getColumnIndex(), subjectNameField.getText().trim());
                    preparedStatement.setString(SubjectColumns.ABBREVIATION.getColumnIndex(), abbreviationField.getText().trim());
                    preparedStatement.setInt(SubjectColumns.CREDITS.getColumnIndex(), Integer.parseInt(creditsField.getText().trim()));
                    preparedStatement.setInt(SubjectColumns.SEMESTER.getColumnIndex(), semesterSpinner.getValue());
                    preparedStatement.setInt(SubjectColumns.YEAR.getColumnIndex(), yearSpinner.getValue());
                    checkNullPossibleFields();
                    preparedStatement.execute();
                    allSubjects.add(subject);
                clearTextFields();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkForUnique() throws SQLException {
        final String name = subjectNameField.getText().trim();
        final String abbreviation = abbreviationField.getText().trim();
        final String selectQuery = "SELECT NAME, ABBREVIATION FROM ST58310.SUBJECT WHERE NAME like ? AND ABBREVIATION like ?";
        final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, abbreviation);
        final ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            changeLabelAttributes(createSubjectMesLabel, "Subject's name or abbreviation must be unique!", Color.RED);

            doAnimationOnTextField(textFieldList.get(SubjectColumns.ABBREVIATION.getColumnIndex() - 1));
            doAnimationOnTextField(textFieldList.get(SubjectColumns.NAME.getColumnIndex() - 1));
            return false;
        } else {
            return true;
        }
    }

    private void doAnimationOnTextField(@NotNull final TextField textField){
        textField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
        Shake.shake(textField);
    }

    private void changeTextFieldStyle(@NotNull final String newValue, final TextField textField, final String style) {
        if (!newValue.isEmpty()) {
            textField.setStyle(style);
        }
    }

    private void fillSubjectsListView(final ObservableList<Subject> observableList) {
        final String selectQuery = "SELECT * FROM ST58310.SUBJECT";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
                final String name = resultSet.getString(SubjectColumns.NAME.toString());
                final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
                final int credits = resultSet.getInt(SubjectColumns.CREDITS.toString());
                final int semester = resultSet.getInt(SubjectColumns.SEMESTER.toString());
                final int year = resultSet.getInt(SubjectColumns.YEAR.toString());
                final String description = resultSet.getString(SubjectColumns.DESCRIPTION.toString());
                final Subject subject = new Subject(subjectId, name, abbreviation, credits, semester, year, description);
                observableList.add(subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTextField(@NotNull final TextField textField) {
        return textField.getText().trim().isEmpty();
    }

    @NotNull
    private List<Boolean> writeNotNullFieldsInList() {
        final List<Boolean> resultList = new ArrayList<>();// создать и заполнить лист результатами проверок значений что обязательно должны быть
        resultList.add(checkTextField(subjectNameField));//0
        resultList.add(checkTextField(abbreviationField));//1
        resultList.add(checkTextField(creditsField));//2
        return resultList;
    }

    @NotNull
    private List<TextField> fillListByNotNullTextFields() {
        final List<TextField> textFields = new ArrayList<>();
        textFields.add(subjectNameField);//0
        textFields.add(abbreviationField);//1
        textFields.add(creditsField);//2
        return textFields;
    }

    private void changeLabelAttributes(@NotNull final Label label, final String text, final Color textColor) {
        label.setText(text);
        label.setTextFill(textColor);
    }

    private void checkNullPossibleFields() throws SQLException {
        checkTextField(descriptionTextArea.getText().trim().isEmpty(), SubjectColumns.DESCRIPTION.getColumnIndex(), descriptionTextArea.getText().trim());
    }

    private void checkTextField(final boolean state, final int columnIndex, final String text) throws SQLException {
        if (state) {
            preparedStatement.setNull(columnIndex, Types.NULL);
        } else {
            preparedStatement.setString(columnIndex, text);
        }
    }

    private void initializeSpinner(@NotNull final Spinner<Integer> integerSpinner, final int startValue, final int endValue) {
        integerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(startValue, endValue));
        integerSpinner.setStyle(StylesEnum.FONT_STYLE.getStyle());
    }

    private void clearTextFields() {
        textFieldList.forEach(textField -> textField.setText(""));
        descriptionTextArea.setText("");
    }

    @FXML
    private void changeSubject(ActionEvent event) {

    }

    @FXML
    private void deleteSubject(ActionEvent event) {

    }
}

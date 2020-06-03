package sample.controllers.adminscontrollers.subjectmanagement;

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
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Subject;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SubjectManagementController implements Initializable {

    private final DbManager dbManager = new DbManager();
    private PreparedStatement preparedStatement;

    private final ObservableList<Subject> allSubjects = FXCollections.observableArrayList();
    private List<TextField> textFieldList;
    private Subject subject;
    public static int subjectId;

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
    private Spinner<Integer> yearFilterSpinner;
    @FXML
    private Spinner<Integer> semesterFilterSpinner;
    @FXML
    private Button changeSubjectBtn;
    @FXML
    private Button deleteSubjectBtn;
    @FXML
    private Button refreshListBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allSubjectsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        fillSubjectsListView(allSubjects);

        textFieldList = fillListByNotNullTextFields();

        initializeSpinner(yearSpinner, 1, 3);
        initializeSpinner(semesterSpinner, 1, 2);
        initializeSpinner(yearFilterSpinner, 0, 3);
        initializeSpinner(semesterFilterSpinner, 0, 2);

        setFilterListenerOnSpinner(yearFilterSpinner);
        setFilterListenerOnSpinner(semesterFilterSpinner);

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
        yearSpinner.getValueFactory().setValue(3);
    }

    private void setFilterListenerOnSpinner(@NotNull final Spinner<Integer> spinner) {
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (yearFilterSpinner.getValue() == 0 && semesterFilterSpinner.getValue() == 0) {
                allSubjectsListView.setItems(allSubjects);
            } else if (yearFilterSpinner.getValue() == 0) {
                allSubjectsListView.setItems(getSubObservableList(subject1 -> subject1.getSemester() == semesterFilterSpinner.getValue()));
            } else if (semesterFilterSpinner.getValue() == 0) {
                allSubjectsListView.setItems(getSubObservableList(subject1 -> subject1.getYear() == yearFilterSpinner.getValue()));
            } else {
                allSubjectsListView.setItems(getSubObservableList(subject1 -> subject1.getYear() == yearFilterSpinner.getValue() && subject1.getSemester() == semesterFilterSpinner.getValue()));
            }
        });
    }

    private ObservableList<Subject> getSubObservableList(final Predicate<Subject> predicate) {
        return allSubjects.stream().filter(predicate)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

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
                if (checkForUnique()) {

                    preparedStatement.setString(SubjectColumns.NAME.getColumnIndex(), subjectNameField.getText().trim());
                    preparedStatement.setString(SubjectColumns.ABBREVIATION.getColumnIndex(), abbreviationField.getText().trim());
                    preparedStatement.setInt(SubjectColumns.CREDITS.getColumnIndex(), Integer.parseInt(creditsField.getText().trim()));
                    preparedStatement.setInt(SubjectColumns.SEMESTER.getColumnIndex(), semesterSpinner.getValue());
                    preparedStatement.setInt(SubjectColumns.YEAR.getColumnIndex(), yearSpinner.getValue());
                    checkNullPossibleFields();
                    preparedStatement.execute();

                    final String selectQuery = "SELECT MAX(SUBJECT_ID) AS SUBJECT_ID FROM ST58310.SUBJECT";
                    preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
                    final ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
                    }
                    subject = new Subject(
                            subjectId,
                            subjectNameField.getText().trim(),
                            abbreviationField.getText().trim(),
                            Integer.parseInt(creditsField.getText().trim()),
                            semesterSpinner.getValue(),
                            yearSpinner.getValue(),
                            descriptionTextArea.getText().trim());
                    allSubjects.add(subject);
//                    clearTextFields();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (allSubjects.size() >= 1){
            changeDisable(false);
        }
    }

    private void changeDisable(final boolean state){
        deleteSubjectBtn.setDisable(state);
        changeSubjectBtn.setDisable(state);
        refreshListBtn.setDisable(state);
        yearFilterSpinner.setDisable(state);
        semesterFilterSpinner.setDisable(state);
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

    private void doAnimationOnTextField(@NotNull final TextField textField) {
        textField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
        Shake.shake(textField);
    }

    private void changeTextFieldStyle(@NotNull final String newValue, final TextField textField, final String style) {
        if (!newValue.isEmpty()) {
            textField.setStyle(style);
        }
    }

    private void fillSubjectsListView(final ObservableList<Subject> observableList) {
        allSubjectsListView.setItems(observableList);
        final String selectQuery = "SELECT * FROM ST58310.SUBJECT ORDER BY YEAR, SEMESTER";
        try {
            preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                observableList.add(createSubject(resultSet));
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
    private void deleteSubject(ActionEvent event) {
        subject = allSubjectsListView.getSelectionModel().getSelectedItem();
        if (subject == null) {
            Main.callAlertWindow("Warning", "Subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            final String deleteQuery = "DELETE FROM ST58310.SUBJECT WHERE SUBJECT_ID = ?";
            try {
                preparedStatement = dbManager.getConnection().prepareStatement(deleteQuery);
                preparedStatement.setInt(1, subject.getSubjectId());
                preparedStatement.execute();
                allSubjects.remove(subject);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (allSubjects.isEmpty()){
            changeDisable(true);
        }
    }

    @FXML
    private void changeSubject(ActionEvent event) {
        subject = allSubjectsListView.getSelectionModel().getSelectedItem();
        if (subject == null) {
            Main.callAlertWindow("Warning", "Subject is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            subjectId = subject.getSubjectId();
            OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/subjectmanagement/SubChangerWindow.fxml", getClass(), false, "Subject changer window", new Image("/images/admin_icon.png"));
        }
    }

    @NotNull
    private Subject createSubject(@NotNull final ResultSet resultSet) throws SQLException {
        final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
        final String name = resultSet.getString(SubjectColumns.NAME.toString());
        final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
        final int credits = resultSet.getInt(SubjectColumns.CREDITS.toString());
        final int semester = resultSet.getInt(SubjectColumns.SEMESTER.toString());
        final int year = resultSet.getInt(SubjectColumns.YEAR.toString());
        final String description = resultSet.getString(SubjectColumns.DESCRIPTION.toString());
        return new Subject(subjectId, name, abbreviation, credits, semester, year, description);
    }

    @FXML
    private void refreshList() {
        allSubjects.removeAll(allSubjects);
        fillSubjectsListView(allSubjects);
    }
}

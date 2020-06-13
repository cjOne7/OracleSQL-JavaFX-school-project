package sample.controllers.userwindows.adminscontrollers.subjectmanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.Checker;
import sample.Cosmetic;
import sample.TextConstraint;
import sample.controllers.Main;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Subject;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static sample.Cosmetic.changeLabelAttributes;
import static sample.Cosmetic.changeTextFieldStyle;

public class SubjectChangerController implements Initializable {

    private static final String UPDATE_QUERY =
            "UPDATE ST58310.SUBJECT " +
                    "SET SUBJECT_NAME = ?, ABBREVIATION = ?, CREDITS = ?, SEMESTER = ?, DESCRIPTION = ?, YEAR = ? " +
                    "WHERE SUBJECT_ID = ?";

    private final DbManager dbManager = new DbManager();
    private PreparedStatement updateStatement;

    private List<TextField> textFieldList;
    private Subject subject;

    @FXML
    private TextField subjectNameField;
    @FXML
    private TextField abbreviationField;
    @FXML
    private TextField creditsField;
    @FXML
    private Spinner<Integer> yearSpinner;
    @FXML
    private Spinner<Integer> semesterSpinner;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button finishBtn;
    @FXML
    private Label messageLabel;
    @FXML
    private Button cancelEditingBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSpinner(yearSpinner, 1, 3);
        initializeSpinner(semesterSpinner, 1, 2);

        textFieldList = fillListByNotNullTextFields();

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

        subjectNameField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(50)));
        abbreviationField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(10)));
        creditsField.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(38)));
        descriptionTextArea.setTextFormatter(new TextFormatter<String>(TextConstraint.getDenyChange(500)));

        fillTextFields();
    }

    private void fillTextFields() {
        final String selectQuery = "SELECT * FROM ST58310.SUBJECT WHERE SUBJECT_ID = ?";
        try {
            updateStatement = dbManager.getConnection().prepareStatement(selectQuery);
            updateStatement.setInt(1, SubjectManagementController.subjectId);
            final ResultSet resultSet = updateStatement.executeQuery();
            if (resultSet.next()) {
                final int subjectId = resultSet.getInt(SubjectColumns.SUBJECT_ID.toString());
                final String subjectName = resultSet.getString(SubjectColumns.SUBJECT_NAME.toString());
                final String abbreviation = resultSet.getString(SubjectColumns.ABBREVIATION.toString());
                final int credits = resultSet.getInt(SubjectColumns.CREDITS.toString());
                final String description = resultSet.getString(SubjectColumns.DESCRIPTION.toString());
                final int semester = resultSet.getInt(SubjectColumns.SEMESTER.toString());
                final int year = resultSet.getInt(SubjectColumns.YEAR.toString());
                subject = new Subject(subjectId, subjectName, abbreviation, credits, semester, year, description);

                subjectNameField.setText(subjectName);
                abbreviationField.setText(abbreviation);
                creditsField.setText(credits + "");
                descriptionTextArea.setText(description);
                semesterSpinner.getValueFactory().setValue(semester);
                yearSpinner.getValueFactory().setValue(year);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkForUnique() throws SQLException {
        final String name = subjectNameField.getText().trim();
        final String abbreviation = abbreviationField.getText().trim();
        if (name.equals(subject.getName()) && abbreviation.equals(subject.getAbbreviation())) {
            return true;
        } else {
            final String selectQuery = "SELECT SUBJECT_NAME, ABBREVIATION FROM ST58310.SUBJECT WHERE SUBJECT_NAME LIKE ? AND ABBREVIATION LIKE ?";
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, abbreviation);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                changeLabelAttributes(messageLabel, "Subject's name or abbreviation must be unique!", Color.RED);
                doAnimationOnTextField(textFieldList.get(SubjectColumns.ABBREVIATION.getColumnIndex() - 1));
                doAnimationOnTextField(textFieldList.get(SubjectColumns.SUBJECT_NAME.getColumnIndex() - 1));
                return false;
            } else {
                return true;
            }
        }
    }

    @NotNull
    private List<Boolean> writeNotNullFieldsInList() {
        final List<Boolean> resultList = new ArrayList<>();// create and fill in a sheet with the results of checking values that must be
        resultList.add(Checker.checkTextField(subjectNameField));//0
        resultList.add(Checker.checkTextField(abbreviationField));//1
        resultList.add(Checker.checkTextField(creditsField));//2
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

    private void checkNullPossibleFields() throws SQLException {
        //I added a null check only because here, this is where the TextArea returns null when calling the getText() method, not ""
        final String text = descriptionTextArea.getText();
        Checker.checkTextField(text != null && text.trim().isEmpty(), 5, text == null ? "" : text.trim(), updateStatement);
    }

    private void doAnimationOnTextField(@NotNull final TextField textField) {
        textField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
        Cosmetic.shake(textField);
    }

    private void initializeSpinner(@NotNull final Spinner<Integer> integerSpinner, final int startValue, final int endValue) {
        integerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(startValue, endValue));
        integerSpinner.setStyle(StylesEnum.FONT_STYLE.getStyle());
    }

    @FXML
    private void saveChanges(ActionEvent event) {
        final List<Boolean> resultList = writeNotNullFieldsInList();

        if (resultList.contains(true)) {//if at least one mandatory field is empty
            for (int i = 0; i < textFieldList.size(); i++) {
                if (resultList.get(i)) {
                    Cosmetic.shake(textFieldList.get(i));
                    textFieldList.get(i).setStyle(StylesEnum.ERROR_STYLE.getStyle());
                }
            }
            changeLabelAttributes(messageLabel, "Fields with * have to be filled!", Color.RED);
        } else {
            try {
                if (checkForUnique()) {
                    updateStatement = dbManager.getConnection().prepareStatement(UPDATE_QUERY);
                    updateStatement.setString(SubjectColumns.SUBJECT_NAME.getColumnIndex(), subjectNameField.getText().trim());
                    updateStatement.setString(SubjectColumns.ABBREVIATION.getColumnIndex(), abbreviationField.getText().trim());
                    updateStatement.setString(SubjectColumns.CREDITS.getColumnIndex(), creditsField.getText().trim());
                    updateStatement.setInt(SubjectColumns.SEMESTER.getColumnIndex(), semesterSpinner.getValue());
                    checkNullPossibleFields();
                    updateStatement.setInt(SubjectColumns.YEAR.getColumnIndex(), yearSpinner.getValue());
                    updateStatement.setInt(SubjectColumns.SUBJECT_ID.getColumnIndex(), SubjectManagementController.subjectId);
                    updateStatement.executeUpdate();
                    final Optional<ButtonType> op = Main.callAlertWindow("Inform window", "Your changes have been processed successfully.", Alert.AlertType.INFORMATION, "/images/information_icon.png");
                    if (op.get().equals(ButtonType.OK)) {
                        ((Stage) finishBtn.getScene().getWindow()).close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void scroll(@NotNull ScrollEvent event) {
        ((Spinner) event.getSource()).increment((int) event.getDeltaY() / 40);
    }

    @FXML
    private void cancelEditing(ActionEvent event) {
        ((Stage) cancelEditingBtn.getScene().getWindow()).close();
    }
}

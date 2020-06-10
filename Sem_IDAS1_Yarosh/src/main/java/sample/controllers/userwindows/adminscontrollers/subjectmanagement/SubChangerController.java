package sample.controllers.userwindows.adminscontrollers.subjectmanagement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.Shake;
import sample.controllers.Main;
import sample.databasemanager.DbManager;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class SubChangerController implements Initializable {

    private static final String UPDATE_QUERY = "UPDATE ST58310.SUBJECT set NAME = ?, ABBREVIATION = ?, CREDITS = ?, SEMESTER = ?, DESCRIPTION = ?, YEAR = ? WHERE SUBJECT_ID = ?";

    private final DbManager dbManager = new DbManager();
    private PreparedStatement updateStatement;

    private List<TextField> textFieldList;

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
        fillTextFields();
    }

    private void fillTextFields() {
        final String selectQuery = "SELECT * FROM ST58310.SUBJECT WHERE SUBJECT_ID = ?";
        try {
            updateStatement = dbManager.getConnection().prepareStatement(selectQuery);
            updateStatement.setInt(1, SubjectManagementController.subjectId);
            final ResultSet resultSet = updateStatement.executeQuery();
            if (resultSet.next()) {
                subjectNameField.setText(resultSet.getString(SubjectColumns.NAME.toString()));
                abbreviationField.setText(resultSet.getString(SubjectColumns.ABBREVIATION.toString()));
                creditsField.setText(resultSet.getString(SubjectColumns.CREDITS.toString()));
                descriptionTextArea.setText(resultSet.getString(SubjectColumns.DESCRIPTION.toString()));
                semesterSpinner.getValueFactory().setValue(resultSet.getInt(SubjectColumns.SEMESTER.toString()));
                yearSpinner.getValueFactory().setValue(resultSet.getInt(SubjectColumns.YEAR.toString()));
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
            changeLabelAttributes(messageLabel, "Subject's name or abbreviation must be unique!", Color.RED);

            doAnimationOnTextField(textFieldList.get(SubjectColumns.ABBREVIATION.getColumnIndex() - 1));
            doAnimationOnTextField(textFieldList.get(SubjectColumns.NAME.getColumnIndex() - 1));
            return false;
        } else {
            return true;
        }
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
        //я добавил проверку на null только потому что здесь, именно здесь ебучий TextArea при вызове метода getText() возвразает null, а не ""
        final String text = descriptionTextArea.getText();
        checkTextField(text != null && text.trim().isEmpty(), SubjectColumns.DESCRIPTION.getColumnIndex(), text == null ? "" : text.trim());
    }

    private boolean checkTextField(@NotNull final TextField textField) {
        return textField.getText().trim().isEmpty();
    }

    private void checkTextField(final boolean state, final int columnIndex, final String text) throws SQLException {
        if (state) {
            updateStatement.setNull(columnIndex, Types.NULL);
        } else {
            updateStatement.setString(columnIndex, text);
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

    private void initializeSpinner(@NotNull final Spinner<Integer> integerSpinner, final int startValue, final int endValue) {
        integerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(startValue, endValue));
        integerSpinner.setStyle(StylesEnum.FONT_STYLE.getStyle());
    }

    @FXML
    private void saveChanges(ActionEvent event) {
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
            try {
                if (checkForUnique()) {
                    System.out.println(SubjectColumns.NAME.getColumnIndex());
                    updateStatement = dbManager.getConnection().prepareStatement(UPDATE_QUERY);
                    updateStatement.setString(SubjectColumns.NAME.getColumnIndex(), subjectNameField.getText().trim());
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
    private void cancelEditing(ActionEvent event) { ((Stage) cancelEditingBtn.getScene().getWindow()).close(); }
}

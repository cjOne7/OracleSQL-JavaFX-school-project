package sample.controllers.userwindows.studnetscontrollers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import sample.SelectStudyMaterials;
import sample.controllers.Main;
import sample.controllers.OpenNewWindow;
import sample.controllers.adminscontrollers.studymaterials.CreateStudyMaterialsController;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.StudyMaterial;
import sample.dbtableclasses.Subject;
import sample.dbtableclasses.UserSubject;
import sample.enums.StudyMatColumns;
import sample.enums.StylesEnum;
import sample.enums.SubjectColumns;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentStudyMatController implements Initializable {

    private ObservableList<StudyMaterial> sortedList = FXCollections.observableArrayList();
    private ObservableList<StudyMaterial> studyMaterials = FXCollections.observableArrayList();
    private List<Subject> subjectList = new ArrayList<>();
    private final SelectStudyMaterials selectStudyMaterials = new SelectStudyMaterials();

    @FXML
    private ListView<StudyMaterial> materialsListView;
    @FXML
    private Button downloadFileBtn;
    @FXML
    private Button openMaterialBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private ComboBox<String> subjectComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        materialsListView.setStyle(StylesEnum.FONT_STYLE.getStyle());
        subjectComboBox.setStyle(StylesEnum.COMBO_BOX_STYLE.getStyle());

        subjectComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            sortedList = studyMaterials.stream()
                    .filter(studyMaterial -> getSubjectId() == studyMaterial.getSubjectId())
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            materialsListView.setItems(sortedList);
        });
        try {
            subjectList = Subject.getSubjectList();
            studyMaterials = selectStudyMaterials.getStudyMaterials();
            materialsListView.setItems(studyMaterials);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        subjectList.forEach(subject -> subjectComboBox.getItems().add(subject.toComboBoxString()));
        subjectComboBox.setValue(subjectList.get(0).toComboBoxString());
    }

    private int getSubjectId() {
        final String subjectNameAbbreviation = subjectComboBox.getValue();
        final int index = subjectNameAbbreviation.indexOf('/');
        final String name = subjectNameAbbreviation.substring(0, index);
        final String abbreviation = subjectNameAbbreviation.substring(index + 1);
        for (final Subject subject : subjectList) {
            if (subject.getName().equals(name) && subject.getAbbreviation().equals(abbreviation)) {
                return subject.getSubjectId();
            }
        }
        return -1;
    }

    @FXML
    private void downloadFile(ActionEvent event) {
        selectStudyMaterials.downloadFile(materialsListView.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void openMaterial(ActionEvent event) {
        final StudyMaterial studyMaterial = materialsListView.getSelectionModel().getSelectedItem();
        if (studyMaterial == null) {
            Main.callAlertWindow("Warning", "Study material is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            CreateStudyMaterialsController.studyMatId = studyMaterial.getStudyMatId();
            OpenNewWindow.openNewWindow("/fxmlfiles/adminsfxmls/studymaterials/StudyMaterialWindow.fxml", getClass(), false, "Study materials window", new Image("/images/admin_icon.png"));
        }
    }

    @FXML
    private void close(ActionEvent event) {
        ((Stage) closeBtn.getScene().getWindow()).close();
    }
}

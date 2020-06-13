package sample.controllers.userwindows.adminscontrollers.quizmanagement.questionmanagement;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import sample.databasemanager.DbManager;
import sample.dbtableclasses.Question;
import sample.enums.QuestionColumns;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class OpenQuestionController implements Initializable {

    @FXML
    private Text questionText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final DbManager dbManager = new DbManager();
        final String selectQuery = "SELECT QUESTION_TEXT FROM ST58310.QUESTION WHERE QUESTION_ID = ?";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            preparedStatement.setInt(1, ShowQuestionsController.questionId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final String questionText = resultSet.getString(QuestionColumns.QUESTION_TEXT.toString());
                this.questionText.setText(questionText);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

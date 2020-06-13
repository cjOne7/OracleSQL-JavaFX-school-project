package sample.controllers.userwindows;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import sample.controllers.MainWindowController;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class NewUserWindowController implements Initializable {

    @FXML
    private Label timeLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Button logOutBtn;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        //set start values: current date and time
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        timeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        //start infinite timeLine
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        //change state every second
        final KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            final LocalDateTime localDateTime = LocalDateTime.now();//get current time
            timeLabel.setText(String.format("%02d:%02d:%02d", localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond()));
            if (localDateTime.getHour() == 0 && localDateTime.getMinute() == 0 && localDateTime.getSecond() == 0) {
                dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.playFromStart();
    }

    @FXML
    private void logOut(ActionEvent event) {
        MainWindowController.openMainStage(logOutBtn);
    }
}

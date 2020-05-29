package sample.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(@NotNull final Stage primaryStage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("/MainWindow.fxml"));
        primaryStage.setTitle("ELSA");

        primaryStage.setOnCloseRequest((event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exiting window");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure that you want to close app?");
            Optional<ButtonType> op = alert.showAndWait();
            if (op.get().equals(ButtonType.OK)) {
                primaryStage.close();
            } else {
                event.consume();
            }
        });
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(new Image("/images/list.png"));
        primaryStage.show();
    }

    public static void main(String[] args) {launch(args);}

    @NotNull
    public static Alert callAlertWindow(
            final String titleText,
            final String contextText,
            final Alert.AlertType alertType) {
        final Alert alert = new Alert(alertType);
        alert.setTitle(titleText);
        alert.setHeaderText(null);
        alert.setContentText(contextText);
        alert.showAndWait();
        return alert;
    }
}

package sample.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OpenNewWindow {

    private OpenNewWindow() {
    }

    public static void openNewWindow(
            final String fxmlFile,
            @NotNull final Class cl,
            final boolean resizable,
            final String title,
            final Image iconImage) {
        try {
            final Stage stage = new Stage();
            final Parent root = FXMLLoader.load(cl.getResource(fxmlFile));
            configureStage(stage, root, title, resizable, iconImage);
        } catch (IOException ex) {
            Logger.getLogger(cl.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void configureStage(@NotNull final Stage stage, final Parent root, final String title, final boolean resizable, final Image iconImage){
        stage.setScene(new Scene(root));

        stage.setOnCloseRequest((event) -> {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exiting");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure that you want to close this window?");
            Optional<ButtonType> op = alert.showAndWait();
            if (op.get().equals(ButtonType.OK)) {
                stage.close();
            } else {
                event.consume();
            }
        });
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(resizable);
        stage.setTitle(title);
        stage.getIcons().add(iconImage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}

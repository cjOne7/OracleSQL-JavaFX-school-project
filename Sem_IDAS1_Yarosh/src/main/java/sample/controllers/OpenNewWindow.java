package sample.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
            final Parent root = FXMLLoader.load(cl.getResource(fxmlFile));//load fxml file
            configureStage(root, title, resizable, iconImage);
        } catch (IOException ex) {
            Logger.getLogger(cl.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void configureStage(final Parent root,
                                       final String title,
                                       final boolean resizable,
                                       final Image iconImage) {
        final Stage stage = new Stage();
        stage.setScene(new Scene(root));//set scene
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(resizable);
        stage.setTitle(title);
        stage.getIcons().add(iconImage);//add image to the corner
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}

package sample;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ImageManager {

    @Nullable
    public static File loadImage(final ImageView imageView, final Button loadImageBtn) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG, JPEG, PNG", "*.jpg", "*.jpeg", "*.png"));
        final File file = fileChooser.showOpenDialog(null);
        if (file != null) {
           final  Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            double rootWidth = imageView.getFitWidth();
            double rootHeight = imageView.getFitHeight();
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double ratioX = rootWidth / imageWidth;
            double ratioY = rootHeight / imageHeight;
            double ratio = Math.min(ratioX, ratioY);
            imageView.setPreserveRatio(false);
            double width = ratio * imageWidth;
            double height = ratio * imageHeight;
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);

            imageView.setLayoutX(loadImageBtn.getLayoutX() + (loadImageBtn.getWidth() - imageView.getFitWidth()) / 2);
            imageView.setLayoutY(loadImageBtn.getLayoutY() - imageView.getFitHeight() - 10);
            return file;
        } else {
            return null;
        }
    }
}

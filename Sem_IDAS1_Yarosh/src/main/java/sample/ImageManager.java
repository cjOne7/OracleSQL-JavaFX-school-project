package sample;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;

public final class ImageManager {

    private static final int IMAGE_VIEW_FIT_WIDTH = 150;
    private static final int IMAGE_VIEW_FIT_HEIGHT = 125;

    private static final int LAYOUT_X_OF_LOAD_BTN = 735;
    private static final int LAYOUT_Y_OF_LOAD_BTN = 160;
    private static final int WIDTH_OF_LOAD_BTN = 140;

    private ImageManager() {
    }

    @Nullable
    public static File loadImage(final ImageView imageView) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG, JPEG, PNG", "*.jpg", "*.jpeg", "*.png"));//adding filters to FileChooser
        final File file = fileChooser.showOpenDialog(null);//selecting the desired file
        if (file != null) {//if the file is selected
            //call the method for placing the image in the center of ImageView with saving the aspect ratio
            scaleImage(imageView, file);
            return file;
        } else {
            return null;
        }
    }

    public static File loadImage(final Blob blob, final ImageView imageView) throws SQLException, IOException {
        if (blob != null) {
            final InputStream input = blob.getBinaryStream();//converting Blob to InputStream
            final File file = new File(System.getProperty("user.home") + "/Downloads/profile_image.png");//creating a file where the image will be placed
            final FileOutputStream fos = new FileOutputStream(file);//writing to this file
            final byte[] buffer = new byte[1024];
            while (input.read(buffer) > 0) {
                fos.write(buffer);
            }
            //call the method for placing the image in the center of ImageView with saving the aspect ratio
            scaleImage(imageView, file);
            return file;
        } else {
            return null;
        }
    }

    private static void scaleImage(@NotNull final ImageView imageView, @NotNull final File file) {
        final Image image = new Image(file.toURI().toString());
        imageView.setImage(new Image(file.toURI().toString()));
        final double imageWidth = image.getWidth();
        final double imageHeight = image.getHeight();
        final double ratioX = (double) IMAGE_VIEW_FIT_WIDTH / imageWidth;
        final double ratioY = (double) IMAGE_VIEW_FIT_HEIGHT / imageHeight;
        final double ratio = Math.min(ratioX, ratioY);
        imageView.setPreserveRatio(false);
        final double width = ratio * imageWidth;
        final double height = ratio * imageHeight;
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        //placing the image above the upload button in the center
        imageView.setLayoutX(LAYOUT_X_OF_LOAD_BTN + (WIDTH_OF_LOAD_BTN - imageView.getFitWidth()) / 2);
        imageView.setLayoutY(LAYOUT_Y_OF_LOAD_BTN - imageView.getFitHeight() - 10);
    }
}

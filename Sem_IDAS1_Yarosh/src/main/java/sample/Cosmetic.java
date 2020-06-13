package sample;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import sample.enums.StylesEnum;

import java.util.Arrays;

public final class Cosmetic {

    private Cosmetic() {
    }

    //make an animation on Node
    public static void shake(final Node node) {
        if (node != null) {
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(50), node);//creating an object for playing animation and specifying the duration
            translateTransition.setFromX(0);
            translateTransition.setByX(10);
            translateTransition.setCycleCount(4);//number of repetitions
            translateTransition.setAutoReverse(true);
            translateTransition.playFromStart();
        }
    }

    //change label text and text color
    public static void changeLabelAttributes(@NotNull final Label label, final String text, final Color textColor) {
        label.setText(text);
        label.setTextFill(textColor);
    }

    //change text field style is text in text field is not empty
    public static void changeTextFieldStyle(@NotNull final String newValue, final TextInputControl textField, final String style) {
        if (!newValue.isEmpty()) {
            textField.setStyle(style);
        }
    }

    public static void changePasswordFieldsStyle(@NotNull final TextField... textFields) {
        Arrays.stream(textFields).forEach(textField -> textField.setStyle(StylesEnum.EMPTY_STRING.getStyle()));
    }

    public static void changePasswordsLabel(final String enteredPasswordByUser, @NotNull final String newValue, final Color textColor,
                                            final Label comparePasswordsLabel, final TextField... textFields) {
        if (newValue.equals(enteredPasswordByUser)) {
            changeLabelAttributes(comparePasswordsLabel, StylesEnum.EMPTY_STRING.getStyle(), Color.TRANSPARENT);
            changePasswordFieldsStyle(textFields);
        } else {
            changeLabelAttributes(comparePasswordsLabel, "Passwords have to match!", textColor);
        }
    }

    //cut spaces in any password field, because they are forbidden
    public static void cutSpace(final TextField textField, @NotNull final String text, final Color textColor, final Label messageLabel) {
        if (text.contains(" ")) {
            final String result = text.replace(" ", StylesEnum.EMPTY_STRING.getStyle());
            textField.setText(result);
            changeLabelAttributes(messageLabel, "Spaces in password are forbidden.", textColor);
        }
    }
}

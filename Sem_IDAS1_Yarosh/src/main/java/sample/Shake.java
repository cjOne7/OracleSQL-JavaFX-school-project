package sample;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class Shake {
    private TranslateTransition translateTransition;

    public Shake(final Node node) {
        if (node != null){
            translateTransition = new TranslateTransition(Duration.millis(50), node);
            translateTransition.setFromX(0);
            translateTransition.setByX(10);
            translateTransition.setCycleCount(4);
            translateTransition.setAutoReverse(true);
        }
    }

    public TranslateTransition getTranslateTransition() {
        return translateTransition;
    }
}

package sample;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class Shake {
    private static TranslateTransition translateTransition;

    private Shake() {
    }

    public static void shake(final Node node) {
        if (node != null) {
            translateTransition = new TranslateTransition(Duration.millis(50), node);//создание объекта для проигрования анимации и указание длительности
            translateTransition.setFromX(0);
            translateTransition.setByX(10);
            translateTransition.setCycleCount(4);//количество повторений
            translateTransition.setAutoReverse(true);
            translateTransition.playFromStart();
        }
    }
}

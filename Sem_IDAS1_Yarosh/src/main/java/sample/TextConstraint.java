package sample;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextFormatter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import sample.enums.StylesEnum;

import java.util.function.UnaryOperator;

public final class TextConstraint {

    @NotNull
    @Contract(pure = true)
    public static UnaryOperator<TextFormatter.Change> getDenyChange(final int length) {
        return change -> {
            if (change.isContentChange()) {
                if (change.getControlNewText().length() > length) {
                    final ContextMenu menu = new ContextMenu();
                    menu.setStyle(StylesEnum.FONT_STYLE.getStyle());
                    menu.getItems().add(new MenuItem("This field can contain " + length + " characters only."));
                    menu.show(change.getControl(), Side.BOTTOM, 0, 0);
                    return null;
                }
            }
            return change;
        };
    }
}

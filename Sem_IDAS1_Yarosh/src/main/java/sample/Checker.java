package sample;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import sample.databasemanager.DbManager;
import sample.enums.ElsaUserColumns;
import sample.enums.StylesEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public final class Checker {

    private Checker() {
    }

    //checked for unique value in db
    public static boolean checkForUnique(final TextField textField,
                                         final String selectQuery,
                                         final Label messageLabel,
                                         final String value,
                                         final String labelText) throws SQLException {
        final DbManager dbManager = new DbManager();
        final PreparedStatement checkSelection = dbManager.getConnection().prepareStatement(selectQuery);
        checkSelection.setString(1, value);
        final ResultSet loginFields = checkSelection.executeQuery();
        if (loginFields.next()) {//if select statement return something, so we can say that that the value is not unique
            Cosmetic.changeLabelAttributes(messageLabel, labelText, Color.RED);
            Cosmetic.shake(textField);
            textField.setStyle(StylesEnum.ERROR_STYLE.getStyle());
            return false;
        } else {
            return true;
        }
    }

    //check if textfield has any value
    public static boolean checkTextField(@NotNull final TextField textField) {
        return textField.getText().trim().isEmpty();
    }

    //prepare statement to have the string or null(if state is empty)
    public static void checkTextField(final boolean state, final int columnIndex, final String text, final PreparedStatement updateStatement) throws SQLException {
        if (state) {
            updateStatement.setNull(columnIndex, Types.NULL);
        } else {
            updateStatement.setString(columnIndex, text);
        }
    }

    //prepare statement to have the image or null if image has not been choosen
    public static void checkImage(final boolean state, final PreparedStatement preparedStatement, final File file) throws SQLException, FileNotFoundException {
        if (state) {// check image field
            preparedStatement.setNull(ElsaUserColumns.IMAGE.getColumnIndex(), Types.NULL);
        } else {
            final InputStream fileInputStream = new FileInputStream(file);
            preparedStatement.setBlob(ElsaUserColumns.IMAGE.getColumnIndex(), fileInputStream, file.length());
        }
    }
}

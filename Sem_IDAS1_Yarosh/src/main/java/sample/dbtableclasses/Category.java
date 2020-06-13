package sample.dbtableclasses;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import sample.controllers.Main;
import sample.databasemanager.DbManager;
import sample.enums.QuestionCatColumns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Category {
    private int categoryId;
    private String categoryName;
    private String description;

    public Category(final int categoryId, final String categoryName, final String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
    }

    public Category(final int categoryId, final String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public String toString() {
        return (categoryName == null || categoryName.isEmpty() ? "" : "Category's name: " + categoryName) +
                (description == null || description.isEmpty() ? "" : ". Description: " + description);
    }

    public String toComboBoxString() {
        return categoryName;
    }

    public static void deleteCategory(final Category category, final String query) {
        if (category == null) {
            Main.callAlertWindow("Warning", "Category is not selected!", Alert.AlertType.WARNING, "/images/warning_icon.png");
        } else {
            try {
                final DbManager dbManager = new DbManager();
                final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(query);
                preparedStatement.setInt(1, category.getCategoryId());
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @NotNull//fill list view with categories
    public static ObservableList<Category> fillListView(final String selectQuery, final String... columns) {
        final ObservableList<Category> categories = FXCollections.observableArrayList();
        try {
            final DbManager dbManager = new DbManager();
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int questionCatId = resultSet.getInt(columns[0]);
                final String questionCatName = resultSet.getString(columns[1]);
                final String description = resultSet.getString(columns[2]);
                final Category category = new Category(questionCatId, questionCatName, description);
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @NotNull//fill list view with question categories
    public static ObservableList<Category> getQuestionCatList() {
        final DbManager dbManager = new DbManager();
        final ObservableList<Category> questionCategories = FXCollections.observableArrayList();
        final String selectQuery = "SELECT QUESTION_CAT_NAME, QUESTIONS_CAT_ID FROM ST58310.QUESTIONS_CTGY";
        try {
            final PreparedStatement preparedStatement = dbManager.getConnection().prepareStatement(selectQuery);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final int questionCatId = resultSet.getInt(QuestionCatColumns.QUESTIONS_CAT_ID.toString());
                final String questionCatName = resultSet.getString(QuestionCatColumns.QUESTION_CAT_NAME.toString());
                final Category category = new Category(questionCatId, questionCatName);
                questionCategories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questionCategories;
    }
}

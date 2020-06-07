package sample.dbtableclasses;

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
}

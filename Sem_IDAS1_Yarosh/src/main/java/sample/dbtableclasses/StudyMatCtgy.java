package sample.dbtableclasses;

public class StudyMatCtgy {

    private Category category;
    private StudyMaterial studyMaterial;

    public StudyMatCtgy(final Category category, final StudyMaterial studyMaterial) {
        this.category = category;
        this.studyMaterial = studyMaterial;
    }

    public Category getCategory() {
        return category;
    }

    public StudyMaterial getStudyMaterial() {
        return studyMaterial;
    }

    @Override
    public String toString() {
        return "Category ID: " + category.getCategoryId()
                + " and its name: " + category.getCategoryName()
                + ". Study material ID: " + studyMaterial.getStudyMatId()
                + ", filename: " + studyMaterial.getFileName() + "." + studyMaterial.getFileType()
                + ", subject: " + studyMaterial.getSubjectName()
                + " and file creator: " + studyMaterial.getCreater();
    }
}

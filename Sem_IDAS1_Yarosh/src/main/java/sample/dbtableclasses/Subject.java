package sample.dbtableclasses;


public class Subject {
    private int subjectId;
    private String name;
    private String abbreviation = "";
    private int credits;
    private int semester;
    private int year;
    private String description = "";

    public Subject(final int subjectId,
                   final String name,
                   final String abbreviation,
                   final int credits,
                   final int semester,
                   final int year,
                   final String description) {
        this.subjectId = subjectId;
        this.name = name;
        this.abbreviation = abbreviation;
        this.credits = credits;
        this.semester = semester;
        this.year = year;
        this.description = description;
    }

    public int getSubjectId() {
        return subjectId;
    }

    @Override
    public String toString() {
        return "Subject's name: " + name +
                (abbreviation == null || abbreviation.isEmpty() ? "" : ", its abbreviation: '" + abbreviation + '\'') +
                ", credits: " + credits +
                (year == 0 ? "" : ", year: " + year) +
                (semester == 0 ? "" : ", semester: " + semester) +
                (description == null || description.isEmpty() ? "" : ". Description: " + description);
    }
}

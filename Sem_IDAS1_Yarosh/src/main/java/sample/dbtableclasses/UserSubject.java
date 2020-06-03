package sample.dbtableclasses;

public class UserSubject {

    private User user;
    private Subject subject;

    public UserSubject(final User user, final Subject subject) {
        this.user = user;
        this.subject = subject;
    }

    public User getUser() {
        return user;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "User's ID:" + user.getUserId()
                + ", surname: " + user.getSurname()
                + ", login: " + user.getLogin()
                + " has written the subject with ID: " + subject.getSubjectId()
                + ", name: " + subject.getName() + "/" + subject.getAbbreviation();
    }
}

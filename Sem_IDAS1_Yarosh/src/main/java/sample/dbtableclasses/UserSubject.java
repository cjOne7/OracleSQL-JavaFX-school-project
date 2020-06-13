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
        return (user.getSurname() == null ? "" : "Surname: " + user.getSurname()) +
                (user.getName() == null ? "" : ", name: " + user.getName()) +
                (user.getLogin() == null ? "" : ", login: " + user.getLogin()) +
                (subject.getName() == null ? "" : ", subject: " + subject.getName()) + "/" +
                (subject.getAbbreviation() == null ? "" : subject.getAbbreviation());
    }
}

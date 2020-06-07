package sample.dbtableclasses;

import sample.enums.Role;

import java.sql.Blob;

public class User {
    private String name;
    private String surname;
    private String login;
    private String password;
    private String email;
    private String telephone;
    private String about;

    private Blob image;

    private int roleId;
    private int userId;

    public User(final String name,
                final String surname,
                final String login,
                final String password,
                final String email,
                final String telephone,
                final String about,
                final Blob image,
                final int roleId,
                final int userId) {
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
        this.about = about;
        this.image = image;
        this.roleId = roleId;
        this.userId = userId;
    }

    public User(final String name,
                final String surname,
                final String email,
                final String telephone,
                final String login,
                final int roleId,
                final int userId) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.telephone = telephone;
        this.login = login;
        this.roleId = roleId;
        this.userId = userId;
    }

    public User(final String surname, final String login, final int userId, final int roleId) {
        this.surname = surname;
        this.login = login;
        this.userId = userId;
        this.roleId = roleId;
    }

    public User(final String name,
                final String surname,
                final String login,
                final int userId,
                final int roleId) {
        this(surname, login, userId, roleId);
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public int getRoleId() {
        return roleId;
    }

    public String getPassword() {
        return password;
    }

    public int getUserId() {
        return userId;
    }

    public String getSurname() {
        return surname;
    }

    public void setRoleId(final int roleId) {
        this.roleId = roleId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User: " +
                "name: " + name +
                ", surname: " + surname +
                (telephone == null ? "" : ", telephone: " + telephone) +
                ", login: " + login +
                (roleId == 0 ? "" : ", role: " + Role.getRole(roleId));
    }
}

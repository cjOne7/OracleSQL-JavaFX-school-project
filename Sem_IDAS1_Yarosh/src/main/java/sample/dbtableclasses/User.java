package sample.dbtableclasses;

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

    public User(final String name,
                final String surname,
                final String login,
                final String password,
                final String email,
                final String telephone,
                final String about,
                final Blob image,
                final int roleId) {
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.password = password;
        this.email = email;
        this.telephone = telephone;
        this.about = about;
        this.image = image;
        this.roleId = roleId;
    }

    public User(final String name,
                final String surname,
                final String email,
                final String telephone,
                final String login,
                final int roleId) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.telephone = telephone;
        this.login = login;
        this.roleId = roleId;
    }

    public String getLogin() {
        return login;
    }

    public int getRoleId() {
        return roleId;
    }

    public String getPassword() { return password; }

    public void setRoleId(final int roleId) {
        this.roleId = roleId;
    }

    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User: " +
                "name: " + name +
                ", surname: " + surname +
                ", telephone: " + telephone +
                ", login: " + login +
                ", role_id: " + roleId;
    }
}

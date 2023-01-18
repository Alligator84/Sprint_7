package courier;

import org.apache.commons.lang3.RandomStringUtils;

public class Courier {

    private int id;
    private String login;
    private String password;
    private String firstName;

    public Courier(String firstName, String password) {
        this.login = RandomStringUtils.randomAlphanumeric(15);
        this.password = password;
        this.firstName = firstName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}

package nasi_bergizi_pajak.model;

public class Akun {
    private int userId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean active;
    private String signupDatetime;
    private String profileImageName;
    private boolean admin;

    public Akun(String email, String password, String firstName, String lastName, boolean active, String profileImageName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
        this.profileImageName = profileImageName;
        this.admin = false;
    }

    public Akun(int userId, String email, String password, String firstName, String lastName,
                boolean active, String signupDatetime, String profileImageName, boolean admin) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
        this.signupDatetime = signupDatetime;
        this.profileImageName = profileImageName;
        this.admin = admin;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSignupDatetime() {
        return signupDatetime;
    }

    public void setSignupDatetime(String signupDatetime) {
        this.signupDatetime = signupDatetime;
    }

    public String getProfileImageName() {
        return profileImageName;
    }

    public void setProfileImageName(String profileImageName) {
        this.profileImageName = profileImageName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "Akun{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", active=" + active +
                ", admin=" + admin +
                '}';
    }
}

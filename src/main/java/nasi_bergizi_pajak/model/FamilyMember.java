package nasi_bergizi_pajak.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FamilyMember {
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("id", "ID"));

    private int memberId;
    private int userId;
    private String name;
    private String relationship;
    private LocalDate birthDate;
    private double height;
    private double weight;
    private String allergy;

    public FamilyMember() {
    }

    public FamilyMember(int memberId, int userId, String name, String relationship,
                        LocalDate birthDate, double height, double weight, String allergy) {
        this.memberId = memberId;
        this.userId = userId;
        this.name = name;
        this.relationship = relationship;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.allergy = allergy;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getAllergy() {
        return allergy;
    }

    public void setAllergy(String allergy) {
        this.allergy = allergy;
    }

    public String getDisplayName() {
        String role = normalize(relationship);
        if (role.isEmpty()) {
            return name;
        }
        return name + " (" + role + ")";
    }

    public String getBirthDateAgeText() {
        if (birthDate == null) {
            return "-";
        }

        int age = Math.max(0, Period.between(birthDate, LocalDate.now()).getYears());
        return birthDate.format(DISPLAY_DATE_FORMAT) + "\n" + age + " tahun";
    }

    public String getHeightText() {
        return formatNumber(height);
    }

    public String getWeightText() {
        return formatNumber(weight);
    }

    public String getAllergyDisplay() {
        String value = normalize(allergy);
        return value.isEmpty() ? "-" : value;
    }

    public boolean hasAllergy() {
        return !normalize(allergy).isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.US, "%.1f", value);
    }
}

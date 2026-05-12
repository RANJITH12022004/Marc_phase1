package com.marc.helmet.models;

public class UserProfile {

    private int id;
    private String name;
    private int age;
    private String bloodType;
    private String allergies;
    private String medicalConditions;
    private String medications;
    private String emergencyNotes;
    private String profilePhotoPath;
    private long updatedAt;

    public UserProfile() {
    }

    public UserProfile(
            int id,
            String name,
            int age,
            String bloodType,
            String allergies,
            String medicalConditions,
            String medications,
            String emergencyNotes,
            String profilePhotoPath,
            long updatedAt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.medicalConditions = medicalConditions;
        this.medications = medications;
        this.emergencyNotes = emergencyNotes;
        this.profilePhotoPath = profilePhotoPath;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getEmergencyNotes() {
        return emergencyNotes;
    }

    public void setEmergencyNotes(String emergencyNotes) {
        this.emergencyNotes = emergencyNotes;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserProfile{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", age=" + age
                + ", bloodType='" + bloodType + '\''
                + ", updatedAt=" + updatedAt
                + '}';
    }
}

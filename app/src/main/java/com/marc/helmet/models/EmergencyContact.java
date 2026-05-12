package com.marc.helmet.models;

public class EmergencyContact {

    private int id;
    private int priority;
    private String name;
    private String phone;
    private String relationship;
    private long createdAt;

    public EmergencyContact() {
    }

    public EmergencyContact(
            int id,
            int priority,
            String name,
            String phone,
            String relationship,
            long createdAt) {
        this.id = id;
        this.priority = priority;
        this.name = name;
        this.phone = phone;
        this.relationship = relationship;
        this.createdAt = createdAt;
    }

    public boolean isPrimary() {
        return priority == 1;
    }

    public String getPriorityLabel() {
        return priority == 1 ? "CALL + SMS" : "SMS ONLY";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "EmergencyContact{"
                + "id=" + id
                + ", priority=" + priority
                + ", name='" + name + '\''
                + ", phone='" + phone + '\''
                + ", relationship='" + relationship + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}

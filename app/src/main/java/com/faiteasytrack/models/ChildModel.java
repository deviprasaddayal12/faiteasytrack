package com.faiteasytrack.models;

public class ChildModel {

    private String name;
    private float age;
    private int institutionType;
    private String institutionName;
    private String institutionId;
    private String institutionAdminId;
    private String assignedRouteId;
    private String assignedVehicleId;

    public ChildModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAge() {
        return age;
    }

    public void setAge(float age) {
        this.age = age;
    }

    public int getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(int institutionType) {
        this.institutionType = institutionType;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionAdminId() {
        return institutionAdminId;
    }

    public void setInstitutionAdminId(String institutionAdminId) {
        this.institutionAdminId = institutionAdminId;
    }

    public String getAssignedRouteId() {
        return assignedRouteId;
    }

    public void setAssignedRouteId(String assignedRouteId) {
        this.assignedRouteId = assignedRouteId;
    }

    public String getAssignedVehicleId() {
        return assignedVehicleId;
    }

    public void setAssignedVehicleId(String assignedVehicleId) {
        this.assignedVehicleId = assignedVehicleId;
    }
}

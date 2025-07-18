package com.Megatram.Megatram.Dto;

public class PermissionRequestDTO {
    private String action;
    private Boolean autorise;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getAutorise() {
        return autorise;
    }

    public void setAutorise(Boolean autorise) {
        this.autorise = autorise;
    }
}
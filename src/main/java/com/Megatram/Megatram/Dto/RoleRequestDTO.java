package com.Megatram.Megatram.Dto;

import java.util.List;

public class RoleRequestDTO {
    private String nom;
    private List<PermissionRequestDTO> permissions;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<PermissionRequestDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionRequestDTO> permissions) {
        this.permissions = permissions;
    }
}
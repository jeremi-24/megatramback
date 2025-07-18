package com.Megatram.Megatram.Dto;

import java.util.List;

public class RoleDetailResponseDTO {

    private long id;
    private String nom;
    private List<PermissionResponseDTO> permissions;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<PermissionResponseDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionResponseDTO> permissions) {
        this.permissions = permissions;
    }
}

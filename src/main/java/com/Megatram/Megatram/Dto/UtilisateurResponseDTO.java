package com.Megatram.Megatram.Dto;

import java.util.List; // <-- IMPORT À AJOUTER

public class UtilisateurResponseDTO {
    private long id;
    private String email;
    private Long roleId;
    private String roleNom;
    private String lieuNom;
    private Long clientId;
    private String clientNom;
    private String clientTel;
    private List<PermissionResponseDTO> permissions; // <-- CHAMP À AJOUTER
    private Long lieuStockId;

    // Getters et Setters...



    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public String getClientTel() {
        return clientTel;
    }

    public void setClientTel(String clientTel) {
        this.clientTel = clientTel;
    }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleNom() {
        return roleNom;
    }

    public void setRoleNom(String roleNom) {
        this.roleNom = roleNom;
    }

    public String getLieuNom() {
        return lieuNom;
    }

    public void setLieuNom(String lieuNom) {
        this.lieuNom = lieuNom;
    }

    public List<PermissionResponseDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionResponseDTO> permissions) {
        this.permissions = permissions;
    }

    public Long getLieuStockId() {
        return lieuStockId;
    }

    public void setLieuStockId(Long lieuStockId) {
        this.lieuStockId = lieuStockId;
    }


}
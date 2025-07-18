package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Client;

public class ClientDto {
    private Long id;

    private String nom;

    private String tel;

    public ClientDto() {
    }
    public ClientDto(Client client) {
        this.id = client.getId();
        this.nom = client.getNom();
        this.tel = client.getTel();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
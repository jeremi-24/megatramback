package com.Megatram.Megatram.Entity;

import com.Megatram.Megatram.enums.TypeMouvement;
import jakarta.persistence.*;
import java.util.Date;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "LieuStock_id", nullable = false)
    private LieuStock lieuStock;

    private int quantte;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = new Date();
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public LieuStock getLieuStock() {
        return lieuStock;
    }

    public void setLieuStock(LieuStock lieuStock) {
        this.lieuStock = lieuStock;
    }

    public int getQuantte() {
        return quantte;
    }

    public void setQuantte(int quantte) {
        this.quantte = quantte;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TypeMouvement getType() {
        return type;
    }

    public void setType(TypeMouvement type) {
        this.type = type;
    }
}


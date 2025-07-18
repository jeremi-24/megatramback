package com.Megatram.Megatram.service;



import com.Megatram.Megatram.Entity.Organisation;
import com.Megatram.Megatram.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganisationService {

    @Autowired
    private OrganisationRepository organisationRepository;

    public Organisation createOrganisation(Organisation organisation) {
        return organisationRepository.save(organisation);
    }

    public List<Organisation> getAllOrganisations() {
        return organisationRepository.findAll();
    }

    public Optional<Organisation> getOrganisationById(Long id) {
        return organisationRepository.findById(id);
    }

    public Organisation updateOrganisation(Long id, Organisation updatedOrg) {
        return organisationRepository.findById(id).map(org -> {
            org.setNom(updatedOrg.getNom());
            org.setLogoUrl(updatedOrg.getLogoUrl());
            org.setAdresse(updatedOrg.getAdresse());
            org.setVille(updatedOrg.getVille());
            org.setNumero(updatedOrg.getNumero());
            org.setTelephone(updatedOrg.getTelephone());
            org.setEmail(updatedOrg.getEmail());
            return organisationRepository.save(org);
        }).orElseThrow(() -> new RuntimeException("Organisation introuvable : id=" + id));
    }

    public void deleteOrganisation(Long id) {
        organisationRepository.deleteById(id);
    }
}

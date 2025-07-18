package com.Megatram.Megatram.Controller;


import com.Megatram.Megatram.Entity.Organisation;
import com.Megatram.Megatram.service.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

    @RestController
    @RequestMapping("/api/organisations")
    @Tag(name = "Organisation", description = "CRUD des organisations")
    public class OrganisationApi {

        @Autowired
        private OrganisationService organisationService;

        @Operation(summary = "Créer une nouvelle organisation")
        @PostMapping
        public Organisation createOrganisation(@RequestBody Organisation organisation) {
            return organisationService.createOrganisation(organisation);
        }

        @Operation(summary = "Lister toutes les organisations")
        @GetMapping
        public List<Organisation> getAllOrganisations() {
            return organisationService.getAllOrganisations();
        }

        @Operation(summary = "Récupérer une organisation par ID")
        @GetMapping("/{id}")
        public Optional<Organisation> getOrganisationById(@PathVariable Long id) {
            return organisationService.getOrganisationById(id);
        }

        @Operation(summary = "Mettre à jour une organisation")
        @PutMapping("/{id}")
        public Organisation updateOrganisation(@PathVariable Long id, @RequestBody Organisation organisation) {
            return organisationService.updateOrganisation(id, organisation);
        }

        @Operation(summary = "Supprimer une organisation")
        @DeleteMapping("/{id}")
        public void deleteOrganisation(@PathVariable Long id) {
            organisationService.deleteOrganisation(id);
        }
    }

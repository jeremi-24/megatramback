package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.InventaireResponseDto;
import com.Megatram.Megatram.Dto.ReapprovisionnementResponseDto;
import com.Megatram.Megatram.Dto.ReapprovisionnementDetailsDto;
import com.Megatram.Megatram.Dto.ReapprovisionnementRequestDto;
import com.Megatram.Megatram.service.ReapprovisionnementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reappro")
public class ReapprovisionnementController {

    @Autowired
    private ReapprovisionnementService reapproService;

    @PostMapping
    public ResponseEntity<ReapprovisionnementResponseDto> enregistrer(@RequestBody ReapprovisionnementRequestDto request) {
        ReapprovisionnementResponseDto response = reapproService.enregistrerReapprovisionnement(request);
        return ResponseEntity.ok(response);
    }



    @GetMapping
    public List<ReapprovisionnementResponseDto> getAll() {
        return reapproService.getAllReapprovisionnements();
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReapprovisionnementDetailsDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reapproService.getDetails(id));
    }

}
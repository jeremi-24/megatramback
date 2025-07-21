//package com.Megatram.Megatram.Controller;
//
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@ControllerAdvice
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", HttpStatus.CONFLICT.value());
//        response.put("message", "Impossible de supprimer ce produit car il est utilisé dans une autre table.");
//        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
//    }
//}
//

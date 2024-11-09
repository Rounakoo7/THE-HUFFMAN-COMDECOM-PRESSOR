package com.rounak.backend.controller;

import com.rounak.backend.service.HuffmanCodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class HuffmanCodingController {

    @Autowired
    private HuffmanCodingService service;

    @PostMapping("/compress")
    public ResponseEntity<Resource> compress(@RequestBody MultipartFile file) throws Exception {
        return service.encodeFile(file);
    }

    @PostMapping("/decompress")
    public ResponseEntity<Resource> decompress(@RequestBody MultipartFile file) throws Exception {
        return service.decodeFile(file);
    }
}

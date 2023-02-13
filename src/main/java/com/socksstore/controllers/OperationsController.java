package com.socksstore.controllers;

import com.socksstore.services.fileservice.FileService;
import com.socksstore.services.operationservice.OperationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/operations")
public class OperationsController {

    private final OperationService operationsService;

    private final FileService operationFileService;

    public OperationsController(OperationService operationsService, @Qualifier("operationsFileServiceImpl") FileService operationFileService) {
        this.operationsService = operationsService;
        this.operationFileService = operationFileService;
    }

    @GetMapping("/createTextFile")
    public ResponseEntity<Object> createTempTextFileWithRecipes() {
        File file = operationsService.getTextFile();
        try {
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            if (Files.size(file.toPath()) == 0) {
                return ResponseEntity.noContent().build();
            }
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"operationsText.txt\"")
                    .contentLength(Files.size(file.toPath()))
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }
}

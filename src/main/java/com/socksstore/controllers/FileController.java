package com.socksstore.controllers;

import com.socksstore.services.databaseservice.DataBaseService;
import com.socksstore.services.fileservice.FileService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService operationsFileService;

    private final DataBaseService dataBaseService;

    public FileController(@Qualifier("operationsFileServiceImpl") FileService operationsFileService,
                          DataBaseService dataBaseService) {
        this.operationsFileService = operationsFileService;
        this.dataBaseService = dataBaseService;
    }

    private InputStreamResource getInputStreamFromFileOfService(File file) throws FileNotFoundException {
        if (file.exists()) {
            return new InputStreamResource(new FileInputStream(file));
        } else return null;
    }

    @GetMapping("/socksintxt")
    public ResponseEntity<Object> createTempTextFileWithSocks() {
        File file = dataBaseService.getTextFileWithSocks();
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
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"socks.txt\"")
                    .contentLength(Files.size(file.toPath()))
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    private boolean cleanDataFile(MultipartFile multipartFile, FileService fileService) {
        File dataFile = fileService.getDataFile();
        fileService.cleanDataFile();
        try {
            if (dataFile != null) {
                try (FileOutputStream fos = new FileOutputStream(dataFile)) {
                    IOUtils.copy(multipartFile.getInputStream(), fos);
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
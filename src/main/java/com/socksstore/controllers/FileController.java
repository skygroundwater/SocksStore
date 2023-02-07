package com.socksstore.controllers;

import com.socksstore.services.fileservice.FileService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService socksFileService;
    private final FileService operationsFileService;

    public FileController(@Qualifier("socksFileServiceImpl") FileService socksFileService,
                          @Qualifier("operationsFileServiceImpl") FileService operationsFileService) {
        this.socksFileService = socksFileService;
        this.operationsFileService = operationsFileService;
    }

    private InputStreamResource getInputStreamFromFileOfService(File file) throws FileNotFoundException {
        if (file.exists()) {
            return new InputStreamResource(new FileInputStream(file));
        } else return null;
    }

    @GetMapping("/exportsocks")
    public ResponseEntity<InputStreamResource> downloadSocksDataFile() throws FileNotFoundException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .contentLength(socksFileService.getDataFile().length()).header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"SocksLog.json\"").body(getInputStreamFromFileOfService(socksFileService.getDataFile()));
    }

    @GetMapping("/exportoperations")
    public  ResponseEntity<InputStreamResource> downLoadOperationsDataFIle() throws FileNotFoundException{
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).contentLength(operationsFileService.getDataFile().length()).header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"Operations.json\"").body(getInputStreamFromFileOfService(operationsFileService.getDataFile()));
    }

    private ResponseEntity<Void> uploadDataFile(String value, MultipartFile multipartFile) {
        File dataFile;
        if (value.equals("importsocks")) {
            if (cleanDataFile(multipartFile, socksFileService)) return ResponseEntity.ok().build();
        }
        if(value.equals("importoperations")){
            if (cleanDataFile(multipartFile, operationsFileService)) return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private boolean cleanDataFile(MultipartFile multipartFile, FileService fileService) {
        File dataFile;
        fileService.cleanDataFile();
        dataFile = fileService.getDataFile();
        try {
            assert dataFile != null;
            try (FileOutputStream fos = new FileOutputStream(dataFile)) {
                IOUtils.copy(multipartFile.getInputStream(), fos);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping(value = "/importsocks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadSocksDataFile(@RequestParam MultipartFile file) {
        return uploadDataFile("importsocks", file);
    }
    @PostMapping(value = "/importoperations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadOperationsDataFile(@RequestParam MultipartFile file) {
        return uploadDataFile("importoperations", file);
    }
}

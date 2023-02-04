package com.socksstore.controllers;


import com.socksstore.services.fileservice.FileService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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

    public final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    private InputStreamResource getInputStreamFromFileOfService(File file) throws FileNotFoundException {
        if(file.exists()){
            return new InputStreamResource(new FileInputStream(file));
        }
        else return null;
    }

    @GetMapping("/loadData")
    public ResponseEntity<Void> readInfoFromDataFile(){
        fileService.readFromFile();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exportsocks")
    public ResponseEntity<InputStreamResource> downloadRecipesDataFile() throws FileNotFoundException {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .contentLength(fileService.getDataFile().length()).header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"SocksLog.json\"").body(getInputStreamFromFileOfService(fileService.getDataFile()));
    }

    private ResponseEntity<Void> uploadDataFile(String value, MultipartFile multipartFile) {
        File dataFile;
        if (value.equals("importsocks")) {
            fileService.cleanDataFile();
            dataFile = fileService.getDataFile();
            try {
                assert dataFile != null;
                try (FileOutputStream fos = new FileOutputStream(dataFile)) {
                    IOUtils.copy(multipartFile.getInputStream(), fos);
                    return ResponseEntity.ok().build();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping(value="/importsocks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadRecipesDataFile(@RequestParam MultipartFile file){
        return uploadDataFile("importsocks", file);
    }
}

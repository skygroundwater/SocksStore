package com.socksstore.controllers;

import com.socksstore.services.fileservice.FileService;
import com.socksstore.services.operationservice.OperationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operations")
public class OperationsController {

    private OperationService operationsService;

    private final FileService operationFileService;

    public OperationsController(OperationService operationsService, @Qualifier("operationsFileServiceImpl") FileService operationFileService) {
        this.operationsService = operationsService;
        this.operationFileService = operationFileService;
    }
}

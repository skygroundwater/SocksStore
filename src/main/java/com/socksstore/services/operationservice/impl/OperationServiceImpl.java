package com.socksstore.services.operationservice.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socksstore.models.operations.Operation;
import com.socksstore.services.fileservice.FileService;
import com.socksstore.services.operationservice.OperationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
public class OperationServiceImpl implements OperationService {
    private ArrayList<Operation> operations;

    private final FileService operationFileService;

    public OperationServiceImpl(@Qualifier("operationsFileServiceImpl") FileService operationsFileService) {
        this.operationFileService = operationsFileService;
        this.operations = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        try {
            readFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerTheOperation(Operation operation) {
        operations.add(operation);
        saveToFile();
    }


    private void saveToFile() {
        try {
            String json = new ObjectMapper().writeValueAsString(operations);
            operationFileService.saveToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFromFile() {
        try {
            String json = operationFileService.readFromFile();
            operations = new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }
}

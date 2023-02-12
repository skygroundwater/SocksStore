package com.socksstore.services.operationservice.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socksstore.models.operations.Operation;
import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.SocksSize;
import com.socksstore.models.socks.prototype.SocksPrototype;
import com.socksstore.services.fileservice.FileService;
import com.socksstore.services.operationservice.OperationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
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
    private void registerTheOperation(Operation operation) {
        operations.add(operation);
        saveToFile();
    }
    @Override
    public void registerAcceptOperation(SocksEntity socks, SocksSize socksSize, Long quantity){
        registerTheOperation(
                new Operation(Operation.TypeOfOperations.ACCEPTANCE,
                        String.valueOf(LocalDateTime.now()),
                        new SocksPrototype(socks, socksSize, quantity),
                        "Acceptance of socks to the warehouse from the supplier"));
    }

    @Override
    public void registerReleasingOperation(SocksEntity socks, SocksSize socksSize, Long quantity){
        registerTheOperation(
                new Operation(Operation.TypeOfOperations.RELEASING,
                        String.valueOf(LocalDateTime.now()),
                        new SocksPrototype(socks, socksSize, quantity), "Releasing to user"));
    }
    @Override
    public void registerWritingOffOperation(SocksEntity socks, SocksSize socksSize, Long quantity, String cause){
        registerTheOperation(
                new Operation(Operation.TypeOfOperations.WRITING_OFF,
                        String.valueOf(LocalDateTime.now()),
                        new SocksPrototype(socks, socksSize, quantity),
                        "Writing of the socks from the warehouse. \n Cause: " + cause));
    }

    @Override
    public ArrayList<Operation> getArrayListWithOperations() {
        return operations;
    }

    @Override
    public File getTextFile() {
        File file = operationFileService.createTempFile("operations").toFile();
        Path path = file.toPath();
        try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            for (Operation operation : operations) {
                if (operation.getTypeOfOperations().equals(Operation.TypeOfOperations.ACCEPTANCE)) {
                    writer.append("Replenishment of socks in stock").append("\n");
                } else if (operation.getTypeOfOperations().equals(Operation.TypeOfOperations.WRITING_OFF)) {
                    writer.append("Write-off of socks from the warehouse").append("\n");
                } else if (operation.getTypeOfOperations().equals(Operation.TypeOfOperations.RELEASING)) {
                    writer.append("Realization of socks to the user").append("\n");
                }
                SocksEntity socks = operation.getSocks().getSocksEntity();
                writer.append(operation.getDateOfOperation()).append("\n").
                        append("Color: ").append(socks.getColor().getNameToString()).append("\n").
                        append("Size: ").append(String.valueOf(socks.getReallySize())).append("\n").
                        append("Percentage of cotton: ").append(String.valueOf(socks.getComposition())).append("% \n").
                        append(String.valueOf(operation.getSocks().getSocksSize())).append("\n").
                        append("Quantity: ").append(String.valueOf(operation.getSocks().getQuantity())).append("\n").
                        append(operation.getDescription()).append("\n").append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
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

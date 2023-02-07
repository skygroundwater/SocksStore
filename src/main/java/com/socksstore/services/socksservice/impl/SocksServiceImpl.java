package com.socksstore.services.socksservice.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socksstore.exceptions.InvalidValueException;
import com.socksstore.exceptions.NotEnoughSocksException;
import com.socksstore.models.operations.Operation;
import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.Color;
import com.socksstore.models.socks.prototype.SocksPrototype;
import com.socksstore.models.socks.enams.SocksSize;
import com.socksstore.services.fileservice.FileService;
import com.socksstore.services.operationservice.OperationService;
import com.socksstore.services.socksservice.SocksService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class SocksServiceImpl implements SocksService {
    private ArrayList<SocksPrototype> store;
    private final FileService socksFileService;

    private OperationService operationService;

    public SocksServiceImpl(@Qualifier("socksFileServiceImpl") FileService socksFileService, OperationService operationsService) {
        this.store = new ArrayList<>();
        this.socksFileService = socksFileService;
        this.operationService = operationsService;
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
    public void addSocksToStore(SocksEntity socks, Long quantity) {
        throwsInvalidValueException(socks,quantity);
        SocksSize socksSize = null;
        for (SocksSize socksSizes : SocksSize.values()) {
            for (Double size : socksSizes.getSize()) {
                if (socks.getReallySize() == size) {
                    socksSize = socksSizes;
                    break;
                }
            }
        }
        if (!store.isEmpty()) {
            for (SocksPrototype socksPrototype : store) {
                if (socks.getReallySize() == socksPrototype.getSocksEntity().getReallySize() &&
                        socks.getColor().equals(socksPrototype.getSocksEntity().getColor()) &&
                        socks.getComposition() == socksPrototype.getSocksEntity().getComposition()) {
                    socksPrototype.setQuantity(socksPrototype.getQuantity() + quantity);
                    saveToFile();
                    operationService.registerTheOperation(
                            new Operation(Operation.TypeOfOperations.ACCEPTANCE,
                                    String.valueOf(LocalDateTime.now()),
                                    new SocksPrototype(socks, socksPrototype.getSocksSize(), quantity),
                                    "Acceptance of socks to the warehouse from the supplier"));
                    return;
                }
            }
        }
        store.add(new SocksPrototype(socks, socksSize, quantity));
        operationService.registerTheOperation(
                new Operation(Operation.TypeOfOperations.ACCEPTANCE,
                        String.valueOf(LocalDateTime.now()),
                        new SocksPrototype(socks, socksSize, quantity), "Acceptance of socks to the warehouse from the supplier"));
        saveToFile();
    }

    @Override
    public Long giveSameSocks(String color, Double size, Integer composition) {
        if(color.isEmpty() || color.isBlank() || size < 36 || size > 47 | composition > 100 || composition < 0){
            throw new InvalidValueException();
        }
        for (SocksPrototype socksPrototype : store) {
            if (socksPrototype.getSocksEntity().getReallySize() == size &&
                    socksPrototype.getSocksEntity().getColor().getNameToString().equals(color.toUpperCase()) &&
                    socksPrototype.getSocksEntity().getComposition() == composition) {
                return socksPrototype.getQuantity();
            }
        }
        return 0L;
    }

    @Override
    public void releaseSocksFromStore(SocksEntity socks, Long quantity) {
        throwsInvalidValueException(socks,quantity);
        if (!store.isEmpty()) {
            for (SocksPrototype socksPrototype : store) {
                if (socks.getReallySize() == socksPrototype.getSocksEntity().getReallySize() &&
                        socks.getColor().equals(socksPrototype.getSocksEntity().getColor()) &&
                        socks.getComposition() == socksPrototype.getSocksEntity().getComposition()) {
                    if (socksPrototype.getQuantity() - quantity < 0) {
                        throw new NotEnoughSocksException();
                    } else {
                        socksPrototype.setQuantity(socksPrototype.getQuantity() - quantity);
                        saveToFile();
                        operationService.registerTheOperation(
                                new Operation(Operation.TypeOfOperations.RELEASING,
                                        String.valueOf(LocalDateTime.now()),
                                        new SocksPrototype(socks, socksPrototype.getSocksSize(), quantity), "Releasing to user"));
                        return;
                    }
                }
            }
        }
        saveToFile();
    }

    @Override
    public void writeOffSocksFromStore(SocksEntity socks, Long quantity, String cause) {
        throwsInvalidValueException(socks,quantity);
        if (!store.isEmpty()) {
            for (SocksPrototype socksPrototype : store) {
                if (socks.getReallySize() == socksPrototype.getSocksEntity().getReallySize() &&
                        socks.getColor().equals(socksPrototype.getSocksEntity().getColor()) &&
                        socks.getComposition() == socksPrototype.getSocksEntity().getComposition()) {
                    if (socksPrototype.getQuantity() - quantity <= 0) {
                        throw new NotEnoughSocksException();
                    } else {
                        socksPrototype.setQuantity(socksPrototype.getQuantity() - quantity);
                        saveToFile();
                        operationService.registerTheOperation(
                                new Operation(Operation.TypeOfOperations.WRITING_OFF,
                                        String.valueOf(LocalDateTime.now()),
                                        new SocksPrototype(socks, socksPrototype.getSocksSize(), quantity),
                                        "Writing of the socks from the warehouse. \n Cause: " + cause));
                        return;
                    }
                }
            }
        }
        saveToFile();
    }

    private void throwsInvalidValueException(SocksEntity socks, Long quantity){
        if(socks.getReallySize() < 36 || socks.getReallySize() > 47 ||
                socks.getComposition() < 0 || socks.getComposition() > 100 || quantity <= 0){
            throw new InvalidValueException();
        }
    }

    private void saveToFile() {
        try {
            String json = new ObjectMapper().writeValueAsString(store);
            socksFileService.saveToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFromFile() {
        try {
            String json = socksFileService.readFromFile();
            store = new ObjectMapper().readValue(json, new TypeReference<ArrayList<SocksPrototype>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }
}

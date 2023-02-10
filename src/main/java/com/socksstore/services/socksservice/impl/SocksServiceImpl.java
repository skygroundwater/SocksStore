package com.socksstore.services.socksservice.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socksstore.exceptions.InvalidValueException;
import com.socksstore.exceptions.NotEnoughSocksException;
import com.socksstore.models.operations.Operation;
import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.Color;
import com.socksstore.models.socks.enams.SocksSize;
import com.socksstore.models.socks.prototype.SocksPrototype;
import com.socksstore.services.fileservice.FileService;
import com.socksstore.services.operationservice.OperationService;
import com.socksstore.services.socksservice.SocksService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class SocksServiceImpl implements SocksService {
    private static final String URL = "jdbc:postgresql://localhost:5433/socks_store";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "Tilitilitatata12345";
    private static Connection connectionToDatabase;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connectionToDatabase = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<SocksPrototype> store;
    private final FileService socksFileService;
    private final OperationService operationService;

    public SocksServiceImpl(@Qualifier("socksFileServiceImpl") FileService socksFileService, OperationService operationsService) {
        this.store = new ArrayList<>();
        this.socksFileService = socksFileService;
        this.operationService = operationsService;

    }

    @PostConstruct
    public void initFromDataBase() {
        try {
            Statement statement = connectionToDatabase.createStatement();
            String SQL = "SELECT * FROM Socks";
            ResultSet resultSet = statement.executeQuery(SQL);
            while (resultSet.next()) {
                store.add(new SocksPrototype(new SocksEntity(Color.getColor(resultSet.getString("color")),
                        resultSet.getFloat("reallySize"), resultSet.getInt("composition")), SocksSize.checkFitToSize(resultSet.getFloat("reallySize")), resultSet.getLong("quantity")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void init() {
        try {
            readFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void addSocksToStore(SocksEntity socks, Long quantity) {
        Statement statement = connectionToDatabase.createStatement();
        throwsInvalidValueException(socks, quantity);
        SocksSize socksSize = SocksSize.checkFitToSize(socks.getReallySize());
        if (!store.isEmpty()) {
            for (SocksPrototype socksPrototype : store) {
                if (socks.getReallySize() == socksPrototype.getSocksEntity().getReallySize() &&
                        socks.getColor().equals(socksPrototype.getSocksEntity().getColor()) &&
                        socks.getComposition() == socksPrototype.getSocksEntity().getComposition()) {
                    deleteFromTheDataBase(socksPrototype);
                    socksPrototype.setQuantity(socksPrototype.getQuantity() + quantity);
                    insertToDatabase(socksPrototype);
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
        SocksPrototype socksPrototype = new SocksPrototype(socks, socksSize, quantity);
        store.add(socksPrototype);
        insertToDatabase(socksPrototype);
        operationService.registerTheOperation(
                new Operation(Operation.TypeOfOperations.ACCEPTANCE,
                        String.valueOf(LocalDateTime.now()),
                        new SocksPrototype(socks, socksSize, quantity),
                        "Acceptance of socks to the warehouse from the supplier"));
        saveToFile();
    }

    @Override
    public Long giveSameSocks(String color, Double size, Integer minComposition, Integer maxComposition) {
        if (color.isEmpty() || color.isBlank() || size < 36.0 || size > 47.0 || maxComposition > 100 ||
                minComposition < 0 || minComposition > 100 || maxComposition < 0 || maxComposition < minComposition) {
            throw new InvalidValueException();
        }
        long quantity = 0;
        for (SocksPrototype socksPrototype : store) {
            if (socksPrototype.getSocksEntity().getReallySize() == size &&
                    socksPrototype.getSocksEntity().getColor().getNameToString().equals(color.toUpperCase()) &&
                    socksPrototype.getSocksEntity().getComposition() >= minComposition && socksPrototype.getSocksEntity().getComposition() <= maxComposition) {
                quantity += socksPrototype.getQuantity();
            }
        }
        return quantity;
    }

    @SneakyThrows
    @Override
    public void releaseSocksFromStore(SocksEntity socks, Long quantity) {
        throwsInvalidValueException(socks, quantity);
        for (SocksPrototype socksPrototype : store) {
            if (socks.getReallySize() == socksPrototype.getSocksEntity().getReallySize() &&
                    socks.getColor().equals(socksPrototype.getSocksEntity().getColor()) &&
                    socks.getComposition() == socksPrototype.getSocksEntity().getComposition()) {
                if (socksPrototype.getQuantity() - quantity < 0) {
                    throw new NotEnoughSocksException();
                } else {
                    deleteFromTheDataBase(socksPrototype);
                    socksPrototype.setQuantity(socksPrototype.getQuantity() - quantity);
                    insertToDatabase(socksPrototype);
                    saveToFile();
                    operationService.registerTheOperation(
                            new Operation(Operation.TypeOfOperations.RELEASING,
                                    String.valueOf(LocalDateTime.now()),
                                    new SocksPrototype(socks, socksPrototype.getSocksSize(), quantity), "Releasing to user"));
                    return;
                }
            }
        }
        saveToFile();
    }

    @Override
    public void writeOffSocksFromStore(SocksEntity socks, Long quantity, String cause) {
        throwsInvalidValueException(socks, quantity);
        if (!store.isEmpty()) {
            for (SocksPrototype socksPrototype : store) {
                if (socks.getReallySize() == socksPrototype.getSocksEntity().getReallySize() &&
                        socks.getColor().equals(socksPrototype.getSocksEntity().getColor()) &&
                        socks.getComposition() == socksPrototype.getSocksEntity().getComposition()) {
                    if (socksPrototype.getQuantity() - quantity <= 0) {
                        throw new NotEnoughSocksException();
                    } else {
                        deleteFromTheDataBase(socksPrototype);
                        socksPrototype.setQuantity(socksPrototype.getQuantity() - quantity);
                        insertToDatabase(socksPrototype);
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
        throw new NotEnoughSocksException();
    }

    private void preparedStatement(SocksPrototype socksPrototype, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, socksPrototype.getSocksEntity().getColor().getNameToString());
        preparedStatement.setDouble(2, socksPrototype.getSocksEntity().getReallySize());
        preparedStatement.setInt(3, socksPrototype.getSocksEntity().getComposition());
        preparedStatement.setString(4, socksPrototype.getSocksSize().getNameOfSize());
        preparedStatement.setLong(5, socksPrototype.getQuantity());
        preparedStatement.executeUpdate();
    }
    private void deleteFromTheDataBase(SocksPrototype socksPrototype) {
        try {
            PreparedStatement deletePreparedStatement =
                    connectionToDatabase.prepareStatement("DELETE FROM Socks WHERE color = ? AND reallysize = ?  AND composition = ? AND sockssize = ? AND quantity = ?");
            preparedStatement(socksPrototype, deletePreparedStatement);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertToDatabase(SocksPrototype socksPrototype) {
        try {
            PreparedStatement insertPreparedStatement =
                    connectionToDatabase.prepareStatement("INSERT INTO Socks VALUES(?,?,?,?,?)");
            preparedStatement(socksPrototype, insertPreparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void throwsInvalidValueException(SocksEntity socks, Long quantity) {
        if (socks.getReallySize() < 36 || socks.getReallySize() > 47 ||
                socks.getComposition() < 0 || socks.getComposition() > 100 || quantity <= 0) {
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

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
    private final OperationService operationService;

    public SocksServiceImpl(OperationService operationsService) {
        this.operationService = operationsService;
    }

    @PostConstruct
    public void initFromDataBase() {
        try {
            Statement statement = connectionToDatabase.createStatement();
            String SQL = "SELECT * FROM Socks";
            ResultSet resultSet = statement.executeQuery(SQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void addSocksToStore(SocksEntity socks, Long quantity) {
        throwsInvalidValueException(socks, quantity);
        SocksSize socksSize = SocksSize.checkFitToSize(socks.getReallySize());
        ResultSet resultSet = selectFromDataBase(socks.getColor().getNameToString(),
                socks.getReallySize(), socks.getComposition(), socks.getComposition());
        while (resultSet.next()) {
            SocksPrototype socksPrototype = new SocksPrototype(
                    new SocksEntity(
                            Color.getColor(resultSet.getString("color")),
                            resultSet.getFloat("reallysize"),
                            resultSet.getInt("composition")),
                    SocksSize.checkFitToSize(resultSet.getDouble("reallysize")),
                    resultSet.getLong("quantity"));
            addQuantityForSocks(socksPrototype, quantity);
            operationService.registerTheOperation(
                    new Operation(Operation.TypeOfOperations.ACCEPTANCE,
                            String.valueOf(LocalDateTime.now()),
                            new SocksPrototype(socks, socksSize, quantity),
                            "Acceptance of socks to the warehouse from the supplier"));
            return;
        }
        insertToDatabase(new SocksPrototype(socks, socksSize, quantity));
        operationService.registerTheOperation(
                new Operation(Operation.TypeOfOperations.ACCEPTANCE,
                        String.valueOf(LocalDateTime.now()),
                        new SocksPrototype(socks, socksSize, quantity),
                        "Acceptance of socks to the warehouse from the supplier"));
    }
    @SneakyThrows
    @Override
    public Long giveSameSocks(String color, Double size, Integer minComposition, Integer maxComposition) {
        if (color.isEmpty() || color.isBlank() || size < 36.0 || size > 47.0 || maxComposition > 100 ||
                minComposition < 0 || minComposition > 100 || maxComposition < 0 || maxComposition < minComposition) {
            throw new InvalidValueException();
        }
        long quantity = 0L;
        ResultSet resultSet = selectFromDataBase(color, size, minComposition, maxComposition);
        while (resultSet.next()) {
            quantity = quantity + resultSet.getLong("quantity");
        }
        return quantity;
    }

    @SneakyThrows
    @Override
    public void releaseSocksFromStore(SocksEntity socks, Long quantity) {
        throwsInvalidValueException(socks, quantity);
        ResultSet resultSet = selectFromDataBase(socks.getColor().getNameToString(),
                socks.getReallySize(), socks.getComposition(), socks.getComposition());
        while (resultSet.next()) {
            SocksPrototype socksPrototype = new SocksPrototype(
                    new SocksEntity(
                            Color.getColor(resultSet.getString("color")),
                            resultSet.getFloat("reallysize"),
                            resultSet.getInt("composition")),
                    SocksSize.checkFitToSize(resultSet.getDouble("reallysize")),
                    resultSet.getLong("quantity"));
            if (socksPrototype.getQuantity() - quantity < 0) {
                throw new NotEnoughSocksException();
            } else {
                takeAwayQuantityForSocks(socksPrototype, quantity);
                operationService.registerTheOperation(
                        new Operation(Operation.TypeOfOperations.RELEASING,
                                String.valueOf(LocalDateTime.now()),
                                new SocksPrototype(socks, socksPrototype.getSocksSize(), quantity), "Releasing to user"));
                return;
            }
        }
    }
    @SneakyThrows
    @Override
    public void writeOffSocksFromStore(SocksEntity socks, Long quantity, String cause) {
        throwsInvalidValueException(socks, quantity);
        ResultSet resultSet = selectFromDataBase(socks.getColor().getNameToString(),
                socks.getReallySize(), socks.getComposition(), socks.getComposition());
        while (resultSet.next()) {
            SocksPrototype socksPrototype = new SocksPrototype(
                    new SocksEntity(
                            Color.getColor(resultSet.getString("color")),
                            resultSet.getFloat("reallysize"),
                            resultSet.getInt("composition")),
                    SocksSize.checkFitToSize(resultSet.getDouble("reallysize")),
                    resultSet.getLong("quantity"));
                    if (socksPrototype.getQuantity() - quantity < 0) {
                        throw new NotEnoughSocksException();
                    } else {
                        takeAwayQuantityForSocks(socksPrototype, quantity);
                        operationService.registerTheOperation(
                                new Operation(Operation.TypeOfOperations.WRITING_OFF,
                                        String.valueOf(LocalDateTime.now()),
                                        new SocksPrototype(socks, socksPrototype.getSocksSize(), quantity),
                                        "Writing of the socks from the warehouse. \n Cause: " + cause));
                        return;
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
                    connectionToDatabase.prepareStatement("DELETE FROM Socks WHERE color = ? " +
                            "AND reallysize = ?  AND composition = ? AND sockssize = ? AND quantity = ?");
            preparedStatement(socksPrototype, deletePreparedStatement);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addQuantityForSocks(SocksPrototype socksPrototype, long quantity){
        try {
            PreparedStatement addPreparedStatement =
                    connectionToDatabase.prepareStatement("UPDATE Socks SET quantity=? WHERE color=? AND  reallysize=? AND  composition =? AND sockssize=?");
            addPreparedStatement.setLong(1, socksPrototype.getQuantity() + quantity);
            addPreparedStatement.setString(2, socksPrototype.getSocksEntity().getColor().getNameToString());
            addPreparedStatement.setDouble(3, socksPrototype.getSocksEntity().getReallySize());
            addPreparedStatement.setInt(4, socksPrototype.getSocksEntity().getComposition());
            addPreparedStatement.setString(5, socksPrototype.getSocksSize().getNameOfSize());
            addPreparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void takeAwayQuantityForSocks(SocksPrototype socksPrototype, long quantity){
        try {
            PreparedStatement takeAwayPreparedStatement =
                    connectionToDatabase.prepareStatement("UPDATE Socks SET quantity=? WHERE color=? AND  reallysize=? AND  composition =? AND sockssize=?");
            takeAwayPreparedStatement.setLong(1, socksPrototype.getQuantity() - quantity);
            takeAwayPreparedStatement.setString(2, socksPrototype.getSocksEntity().getColor().getNameToString());
            takeAwayPreparedStatement.setDouble(3, socksPrototype.getSocksEntity().getReallySize());
            takeAwayPreparedStatement.setInt(4, socksPrototype.getSocksEntity().getComposition());
            takeAwayPreparedStatement.setString(5, socksPrototype.getSocksSize().getNameOfSize());
            takeAwayPreparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    private ResultSet selectFromDataBase(String color, Double size, Integer minComposition, Integer maxComposition) {
        try {
            PreparedStatement selectPreparedStatement =
                    connectionToDatabase.prepareStatement("SELECT * FROM Socks WHERE color = ? " +
                            "AND reallysize = ? AND composition BETWEEN ? AND ?");
            selectPreparedStatement.setString(1, color.toUpperCase());
            selectPreparedStatement.setDouble(2, size);
            selectPreparedStatement.setInt(3, minComposition);
            selectPreparedStatement.setInt(4, maxComposition);
            return selectPreparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new InvalidValueException();
    }

    private void throwsInvalidValueException(SocksEntity socks, Long quantity) {
        if (socks.getReallySize() < 36 || socks.getReallySize() > 47 ||
                socks.getComposition() < 0 || socks.getComposition() > 100 || quantity <= 0) {
            throw new InvalidValueException();
        }
    }
}

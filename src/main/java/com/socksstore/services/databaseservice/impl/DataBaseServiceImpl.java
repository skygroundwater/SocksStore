package com.socksstore.services.databaseservice.impl;

import com.socksstore.exceptions.InvalidValueException;
import com.socksstore.models.socks.enams.SocksSize;
import com.socksstore.models.socks.prototype.SocksPrototype;
import com.socksstore.models.socks.prototype.SocksPrototypeMapper;
import com.socksstore.services.databaseservice.DataBaseService;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.List;

@Service
public class DataBaseServiceImpl implements DataBaseService {

    private static final String URL = "jdbc:postgresql://localhost:5433/socks_store";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "Tilitilitatata12345";
    private static Connection connectionToDatabase;
    private final JdbcTemplate jdbcTemplate;

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

    public DataBaseServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @SneakyThrows
    @Override
    public File getTextFileWithSocks() {
        File dataFile = Files.createTempFile(Path.of("src/main/resources"), "temp", "socks").toFile();
        Path path = dataFile.toPath();
        List<SocksPrototype> socks = jdbcTemplate.query("SELECT * FROM Socks", new SocksPrototypeMapper());
        try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            writer.append("ALL TYPES OF SOCKS IN THE WAREHOUSE");
            for (SocksPrototype socksPrototype : socks) {
                writer.append("Color: ").append(socksPrototype.getSocksEntity().getColor().getNameToString())
                        .append("\n")
                        .append("Type of sizes: ").append(String.valueOf(socksPrototype.getSocksSize()))
                        .append("\n")
                        .append("Really size: ").append(String.valueOf(socksPrototype.getSocksEntity().getReallySize()))
                        .append("\n")
                        .append("Cotton part in composition: ").append(String.valueOf(socksPrototype.getSocksEntity().getComposition())).append("%")
                        .append("\n")
                        .append("Quantity of this type of socks left in stock: ").append(String.valueOf(socksPrototype.getQuantity()))
                        .append("\n").append("\n");
            }
        }
        return dataFile;
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

    @Override
    public void addQuantityForSocks(SocksPrototype socksPrototype, long quantity) {
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

    @Override
    public void takeAwayQuantityForSocks(SocksPrototype socksPrototype, long quantity) {
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

    @Override
    public void insertToDatabase(SocksPrototype socksPrototype) {
        try {
            PreparedStatement insertPreparedStatement =
                    connectionToDatabase.prepareStatement("INSERT INTO Socks VALUES(?,?,?,?,?)");
            preparedStatement(socksPrototype, insertPreparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResultSet selectFromDataBase(String color, Double size, Integer minComposition, Integer maxComposition) {
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
}
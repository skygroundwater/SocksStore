package com.socksstore.services.databaseservice.impl;

import com.socksstore.exceptions.InvalidValueException;
import com.socksstore.models.socks.prototype.SocksPrototype;
import com.socksstore.services.databaseservice.DataBaseService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.*;
@Service
public class DataBaseServiceImpl implements DataBaseService {

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

    private void preparedStatement(SocksPrototype socksPrototype, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, socksPrototype.getSocksEntity().getColor().getNameToString());
        preparedStatement.setDouble(2, socksPrototype.getSocksEntity().getReallySize());
        preparedStatement.setInt(3, socksPrototype.getSocksEntity().getComposition());
        preparedStatement.setString(4, socksPrototype.getSocksSize().getNameOfSize());
        preparedStatement.setLong(5, socksPrototype.getQuantity());
        preparedStatement.executeUpdate();
    }

    @Override
    public void deleteFromTheDataBase(SocksPrototype socksPrototype) {
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
    public void addQuantityForSocks(SocksPrototype socksPrototype, long quantity){
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
    public void takeAwayQuantityForSocks(SocksPrototype socksPrototype, long quantity){
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

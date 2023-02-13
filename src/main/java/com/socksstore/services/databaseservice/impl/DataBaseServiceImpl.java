package com.socksstore.services.databaseservice.impl;

import com.socksstore.exceptions.InvalidValueException;
import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.Color;
import com.socksstore.models.socks.enams.SocksSize;
import com.socksstore.models.socks.prototype.SocksPrototype;
import com.socksstore.services.databaseservice.DataBaseService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;

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

    public void getExcelFileWithSocks() {
        try {
            Statement statement = connectionToDatabase.createStatement();
            String SQL = "SELECT * FROM Socks";
            ResultSet resultSet = statement.executeQuery(SQL);

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Socks");

            writeHeaderLine(sheet);

            writeDataLines(resultSet, workbook, sheet);

            FileOutputStream outputStream = new FileOutputStream("socks.xlsx");
            workbook.write(outputStream);
            workbook.close();
            statement.close();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeHeaderLine(XSSFSheet sheet) {

        Row headerRow = sheet.createRow(0);

        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Color");

        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("ReallySize");

        headerCell = headerRow.createCell(2);
        headerCell.setCellValue("Composition");

        headerCell = headerRow.createCell(3);
        headerCell.setCellValue("Type of socks size");

        headerCell = headerRow.createCell(4);
        headerCell.setCellValue("Quantity");
    }

    private void writeDataLines(ResultSet result, XSSFWorkbook workbook,
                                XSSFSheet sheet) throws SQLException {
        int rowCount = 1;

        while (result.next()) {
            String color = result.getString("color");
            double reallySize = result.getDouble("reallysize");
            long composition = result.getLong("composition");
            String socksSize = String.valueOf(SocksSize.checkFitToSize(
                    result.getDouble("reallysize")));
            long quantity = result.getLong("quantity");

            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(color);

            cell = row.createCell(columnCount++);
            cell.setCellValue(reallySize);

            cell = row.createCell(columnCount++);

            CellStyle cellStyle = workbook.createCellStyle();
            CreationHelper creationHelper = workbook.getCreationHelper();
            cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            cell.setCellStyle(cellStyle);

            cell.setCellValue(composition);

            cell = row.createCell(columnCount++);
            cell.setCellValue(socksSize);

            cell = row.createCell(columnCount);
            cell.setCellValue(quantity);
        }
    }

    @Override
    public File getTextFileWithSocks() {
        try {
            File dataFile = Files.createTempFile(Path.of("src/main/resources"), "temp", "socks").toFile();
            Path path = dataFile.toPath();
            Statement statement = connectionToDatabase.createStatement();
            String SQL = "SELECT * FROM Socks";
            ResultSet resultSet = statement.executeQuery(SQL);
            try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                writer.append("ALL TYPES OF SOCKS IN THE WAREHOUSE");
                while (resultSet.next()) {
                    writer.append("Color: ").append(resultSet.getString("color"))
                            .append("\n")
                            .append("Type of sizes: ").append(String.valueOf(SocksSize.checkFitToSize(resultSet.getDouble("reallysize")))).append("%")
                            .append("\n")
                            .append("Really size: ").append(String.valueOf(resultSet.getDouble("reallysize")))
                            .append("\n")
                            .append("Cotton part in composition: ").append(String.valueOf(resultSet.getInt("composition")))
                            .append("\n")
                            .append("Quantity of this type of socks left in stock: ").append(String.valueOf(resultSet.getLong("quantity")))
                            .append("\n").append("\n");
                }
            }
            return dataFile;
        } catch (SQLException | IOException e) {
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
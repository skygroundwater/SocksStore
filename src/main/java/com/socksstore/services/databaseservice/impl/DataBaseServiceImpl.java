package com.socksstore.services.databaseservice.impl;

import com.socksstore.models.socks.prototype.SocksPrototype;
import com.socksstore.models.socks.prototype.SocksPrototypeMapper;
import com.socksstore.services.databaseservice.DataBaseService;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
public class DataBaseServiceImpl implements DataBaseService {

    private final JdbcTemplate jdbcTemplate;

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

    @Override
    public void deleteFromTheDataBase(SocksPrototype socksPrototype) {
        jdbcTemplate.update("DELETE FROM Socks WHERE color = ? " +
                        "AND reallysize = ?  AND composition = ? AND sockssize = ? AND quantity = ?",
                socksPrototype.getSocksEntity().getColor().getNameToString(),
                socksPrototype.getSocksEntity().getReallySize(),
                socksPrototype.getSocksEntity().getComposition(),
                socksPrototype.getSocksSize().getNameOfSize(),
                socksPrototype.getQuantity());
    }

    @Override
    public void addQuantityForSocks(SocksPrototype socksPrototype, long quantity) {
        jdbcTemplate.update("UPDATE Socks SET quantity=? " +
                        "WHERE color=? AND  reallysize=? AND  composition =? " +
                        "AND sockssize=?", socksPrototype.getQuantity() + quantity,
                socksPrototype.getSocksEntity().getColor().getNameToString(),
                socksPrototype.getSocksEntity().getReallySize(),
                socksPrototype.getSocksEntity().getComposition(),
                socksPrototype.getSocksSize().getNameOfSize());
    }

    @Override
    public void takeAwayQuantityForSocks(SocksPrototype socksPrototype, long quantity) {
        jdbcTemplate.update("UPDATE Socks SET quantity=? " +
                        "WHERE color=? AND  reallysize=? AND  composition =? " +
                        "AND sockssize=?", socksPrototype.getQuantity() - quantity,
                socksPrototype.getSocksEntity().getColor().getNameToString(),
                socksPrototype.getSocksEntity().getReallySize(),
                socksPrototype.getSocksEntity().getComposition(),
                socksPrototype.getSocksSize().getNameOfSize());
    }

    @Override
    public void insertToDatabase(SocksPrototype socksPrototype) {
        jdbcTemplate.update("INSERT INTO Socks VALUES(?,?,?,?,?)",
                socksPrototype.getSocksEntity().getColor().getNameToString(),
                socksPrototype.getSocksEntity().getReallySize(),
                socksPrototype.getSocksSize().getNameOfSize(),
                socksPrototype.getSocksEntity().getComposition(),
                socksPrototype.getQuantity());
    }

    @Override
    public List<SocksPrototype> selectFromDataBase(String color, Double size, Integer minComposition, Integer maxComposition) {
        return jdbcTemplate.query("SELECT * FROM Socks", new SocksPrototypeMapper()).stream().filter(
                        socksPrototype -> socksPrototype.getSocksEntity().getColor().getNameToString().equals(color.toUpperCase()))
                .filter(socksPrototype -> socksPrototype.getSocksEntity().getReallySize() == size)
                .filter(socksPrototype -> socksPrototype.getSocksEntity().getComposition() <= maxComposition)
                .filter(socksPrototype -> socksPrototype.getSocksEntity().getComposition() >= minComposition).toList();
    }
}
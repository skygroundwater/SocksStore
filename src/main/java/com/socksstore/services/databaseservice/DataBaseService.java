package com.socksstore.services.databaseservice;

import com.socksstore.models.socks.prototype.SocksPrototype;

import java.io.File;
import java.sql.ResultSet;

public interface DataBaseService {

    File getTextFileWithSocks();

    void addQuantityForSocks(SocksPrototype socksPrototype, long quantity);

    void takeAwayQuantityForSocks(SocksPrototype socksPrototype, long quantity);

    void insertToDatabase(SocksPrototype socksPrototype);

    ResultSet selectFromDataBase(String color, Double size, Integer minComposition, Integer maxComposition);
}

package com.socksstore.services.databaseservice;

import com.socksstore.models.socks.prototype.SocksPrototype;

import java.sql.ResultSet;

public interface DataBaseService {
    void deleteFromTheDataBase(SocksPrototype socksPrototype);

    void addQuantityForSocks(SocksPrototype socksPrototype, long quantity);

    void takeAwayQuantityForSocks(SocksPrototype socksPrototype, long quantity);

    void insertToDatabase(SocksPrototype socksPrototype);

    ResultSet selectFromDataBase(String color, Double size, Integer minComposition, Integer maxComposition);
}

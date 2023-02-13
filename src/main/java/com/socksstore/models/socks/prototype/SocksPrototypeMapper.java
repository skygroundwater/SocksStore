package com.socksstore.models.socks.prototype;

import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.Color;
import com.socksstore.models.socks.enams.SocksSize;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SocksPrototypeMapper implements RowMapper<SocksPrototype> {

    @Override
    public SocksPrototype mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        SocksPrototype socksPrototype = new SocksPrototype();
        socksPrototype.setSocksEntity(new SocksEntity(
                Color.getColor(resultSet.getString("color")),
                resultSet.getDouble("reallysize"),
                resultSet.getInt("composition")));
        socksPrototype.setSocksSize(SocksSize.checkFitToSize(resultSet.getDouble("reallysize")));
        socksPrototype.setQuantity(resultSet.getLong("quantity"));
        return socksPrototype;
    }
}
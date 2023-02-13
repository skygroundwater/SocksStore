package com.socksstore.models.socks.prototype;

import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.SocksSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocksPrototype {

    private SocksEntity socksEntity;

    private SocksSize socksSize;

    private long quantity;
}
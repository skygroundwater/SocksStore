package com.socksstore.models.socks;


import com.socksstore.models.socks.enams.Color;
import com.socksstore.models.socks.enams.SocksSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Socks {
    private Color color;
    private SocksSize socksSize;
    private double reallySize;
    private int composition;
}

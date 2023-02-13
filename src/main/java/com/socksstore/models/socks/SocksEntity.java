package com.socksstore.models.socks;

import com.socksstore.models.socks.enams.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocksEntity {

    private Color color;

    private double reallySize;

    private int composition;
}
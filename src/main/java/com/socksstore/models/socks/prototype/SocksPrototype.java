package com.socksstore.models.socks.prototype;

import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.SocksSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocksPrototype {

    private SocksEntity socksEntity;
    private SocksSize socksSize;
    @Positive
    @Max(value = 100)
    @Min(0)
    private long quantity;

}

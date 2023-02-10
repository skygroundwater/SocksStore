package com.socksstore.models.socks;


import com.socksstore.models.socks.enams.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocksEntity {
    @NotBlank
    @NotEmpty
    private Color color;
    private double reallySize;
    @Positive
    @Max(value = 100)
    @Min(0)
    private int composition;
}

package com.socksstore.models.operations;

import com.socksstore.models.socks.prototype.SocksPrototype;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation {

    public enum TypeOfOperations {
        ACCEPTANCE, WRITING_OFF, RELEASING
    }

    private TypeOfOperations typeOfOperations;

    private String dateOfOperation;

    private SocksPrototype socks;

    private String description;

}

package com.socksstore.services.socksservice.impl;

import com.socksstore.exceptions.InvalidValueException;
import com.socksstore.exceptions.NotEnoughSocksException;
import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.SocksSize;
import com.socksstore.models.socks.prototype.SocksPrototype;
import com.socksstore.services.databaseservice.DataBaseService;
import com.socksstore.services.operationservice.OperationService;
import com.socksstore.services.socksservice.SocksService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SocksServiceImpl implements SocksService {

    private final OperationService operationService;

    private final DataBaseService dataBaseService;

    public SocksServiceImpl(OperationService operationsService,
                            DataBaseService dataBaseService) {
        this.operationService = operationsService;
        this.dataBaseService = dataBaseService;
    }

    @SneakyThrows
    @Override
    public void addSocksToStore(SocksEntity socks, Long quantity) {
        throwsInvalidValueException(socks, quantity);
        SocksSize socksSize = SocksSize.checkFitToSize(socks.getReallySize());
        List<SocksPrototype> socksList = dataBaseService.selectFromDataBase(socks.getColor().getNameToString(),
                socks.getReallySize(), socks.getComposition(), socks.getComposition());
        for (SocksPrototype socksPrototype: socksList){
            dataBaseService.addQuantityForSocks(socksPrototype, quantity);
            operationService.registerAcceptOperation(socks, socksSize, quantity);
            return;
        }
        dataBaseService.insertToDatabase(new SocksPrototype(socks, socksSize, quantity));
        operationService.registerAcceptOperation(socks, socksSize, quantity);
    }

    @SneakyThrows
    @Override
    public Long giveSameSocks(String color, Double size, Integer minComposition, Integer maxComposition) {
        if (color.isEmpty() || color.isBlank() || size < 36.0 || size > 47.0 || maxComposition > 100 ||
                minComposition < 0 || minComposition > 100 || maxComposition < 0 || maxComposition < minComposition) {
            throw new InvalidValueException();
        }
        long quantity = 0L;
        List<SocksPrototype> socksList =
                dataBaseService.selectFromDataBase(color, size, minComposition, maxComposition);
        for(SocksPrototype socksPrototype: socksList){
            quantity = quantity + socksPrototype.getQuantity();
        }
        return quantity;
    }

    @SneakyThrows
    @Override
    public void releaseSocksFromStore(SocksEntity socks, Long quantity) {
        throwsInvalidValueException(socks, quantity);
        List<SocksPrototype> socksList = dataBaseService.selectFromDataBase(socks.getColor().getNameToString(),
                socks.getReallySize(), socks.getComposition(), socks.getComposition());
        for(SocksPrototype socksPrototype: socksList){
            if (socksPrototype.getQuantity() - quantity < 0) {
                throw new NotEnoughSocksException();
            } else {
                dataBaseService.takeAwayQuantityForSocks(socksPrototype, quantity);
                operationService.registerReleasingOperation(socks, SocksSize.checkFitToSize(socks.getReallySize()), quantity);
                return;
            }
        }
    }

    @SneakyThrows
    @Override
    public void writeOffSocksFromStore(SocksEntity socks, Long quantity, String cause) {
        throwsInvalidValueException(socks, quantity);
        List<SocksPrototype> socksList = dataBaseService.selectFromDataBase(socks.getColor().getNameToString(),
                socks.getReallySize(), socks.getComposition(), socks.getComposition());
        for(SocksPrototype socksPrototype: socksList){
            if (socksPrototype.getQuantity() - quantity < 0) {
                throw new NotEnoughSocksException();
            } else {
                dataBaseService.takeAwayQuantityForSocks(socksPrototype, quantity);
                operationService.registerWritingOffOperation(socks,
                        SocksSize.checkFitToSize(socks.getReallySize()), quantity, cause);
                return;
            }
        }
        throw new NotEnoughSocksException();
    }

    private void throwsInvalidValueException(SocksEntity socks, Long quantity) {
        if (socks.getReallySize() < 36 || socks.getReallySize() > 47 ||
                socks.getComposition() < 0 || socks.getComposition() > 100 || quantity <= 0) {
            throw new InvalidValueException();
        }
    }
}
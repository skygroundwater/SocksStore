package com.socksstore.services.operationservice;

import com.socksstore.models.socks.SocksEntity;
import com.socksstore.models.socks.enams.SocksSize;
import java.io.File;

public interface OperationService {

    void registerAcceptOperation(SocksEntity socks, SocksSize socksSize, Long quantity);

    void registerReleasingOperation(SocksEntity socks, SocksSize socksSize, Long quantity);

    void registerWritingOffOperation(SocksEntity socks, SocksSize socksSize, Long quantity, String cause);

    File getTextFile();
}
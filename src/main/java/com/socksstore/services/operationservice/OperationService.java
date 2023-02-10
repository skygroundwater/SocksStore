package com.socksstore.services.operationservice;

import com.socksstore.models.operations.Operation;

import java.io.File;

public interface OperationService {

    void registerTheOperation(Operation operation);

    File getTextFile();
}

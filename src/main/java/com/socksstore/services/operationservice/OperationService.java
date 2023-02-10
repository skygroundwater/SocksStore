package com.socksstore.services.operationservice;

import com.socksstore.models.operations.Operation;

import java.io.File;
import java.util.ArrayList;

public interface OperationService {

    void registerTheOperation(Operation operation);

    ArrayList<Operation> getArrayListWithOperations();

    File getTextFile();
}

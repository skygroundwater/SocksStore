package com.socksstore.services.fileservice;

import java.io.File;
import java.nio.file.Path;

public interface FileService {
    File getDataFile();

    void saveToFile(String json);

    String readFromFile();

    void cleanDataFile();

    Path createTempFile(String suffix);

}

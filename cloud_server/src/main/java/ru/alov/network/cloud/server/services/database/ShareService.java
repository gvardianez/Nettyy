package ru.alov.network.cloud.server.services.database;

import java.nio.file.Path;
import java.util.List;

public interface ShareService {

    List<String> loadShareFiles(String nickName);

    void shareFile(String nickName, List<String> fileNames, Path filePath);

    void deleteFile(String filePath);
}

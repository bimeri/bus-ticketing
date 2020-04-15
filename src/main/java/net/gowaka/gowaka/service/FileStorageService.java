package net.gowaka.gowaka.service;

/**
 * Author: Edward Tanko <br/>
 * Date: 4/2/20 4:22 PM <br/>
 */
public interface FileStorageService {

    void savePublicFile(String filename, byte[] fileByteArray, String storageFolder);

    String getPublicFilePath(String filename, String storageFolder);

    String getLogo();

}

package net.gowaka.gowaka.network.api.storage;

import net.gowaka.gowaka.network.api.notification.service.NotificationRestClient;
import net.gowaka.gowaka.network.api.storage.config.FileStorageProps;
import net.gowaka.gowaka.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Author: Edward Tanko <br/>
 * Date: 4/2/20 4:21 PM <br/>
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private RestTemplate restTemplate;
    private FileStorageProps fileStorageProps;

    @Autowired
    public FileStorageServiceImpl(@Qualifier("apiSecurityRestTemplate") RestTemplate restTemplate, FileStorageProps fileStorageProps) {
        this.restTemplate = restTemplate;
        this.fileStorageProps = fileStorageProps;
    }

    @Override
    public void savePublicFile(String filename, byte[] fileByteArray, String storageFolder) {

        logger.info("saving file: {}", storageFolder + "/" + filename);
        String saveUrl = fileStorageProps.getHostUrl() + fileStorageProps.getStorefile()
                + "?bucketDirectory=" + fileStorageProps.getBucket() + "/" + storageFolder + "&identifier=PUBLIC";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "ApiKey " + fileStorageProps.getApiKey());

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(filename)
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(fileByteArray, fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        restTemplate.exchange(saveUrl, HttpMethod.POST, requestEntity, String.class);
    }

    @Override
    public String getPublicFilePath(String filename, String storageFolder) {

        logger.info("getting file: {}", storageFolder + "/" + filename);
        return fileStorageProps.getHostUrl() + fileStorageProps.getGetPublicFilePath()
                + "?file=" + fileStorageProps.getBucket() + "/" + storageFolder + "/" + filename + "&identifier=PUBLIC";

    }

}

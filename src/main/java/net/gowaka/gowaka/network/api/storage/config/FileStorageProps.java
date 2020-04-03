package net.gowaka.gowaka.network.api.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Author: Edward Tanko <br/>
 * Date: 4/2/20 4:26 PM <br/>
 */
@ConfigurationProperties("fileservice")
@Configuration
@Data
public class FileStorageProps {

    private String apiKey;
    private String hostUrl;
    private String storefile;
    private String getPublicFilePath;
    private String bucket;
}

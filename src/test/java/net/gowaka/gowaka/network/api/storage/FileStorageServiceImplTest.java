package net.gowaka.gowaka.network.api.storage;

import net.gowaka.gowaka.network.api.storage.config.FileStorageProps;
import net.gowaka.gowaka.service.FileStorageService;
import org.assertj.core.api.Java6Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Author: Edward Tanko <br/>
 * Date: 4/2/20 6:22 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class FileStorageServiceImplTest {

    @Mock
    private RestTemplate mockRestTemplate;

    private FileStorageService fileStorageService;

    @Before
    public void setUp() {

        FileStorageProps fileStorageProps = new FileStorageProps();
        fileStorageProps.setApiKey("key");
        fileStorageProps.setBucket("bucket");
        fileStorageProps.setGetPublicFilePath("/get/file");
        fileStorageProps.setHostUrl("http://localhost");
        fileStorageProps.setStorefile("/store/file");
        fileStorageService = new FileStorageServiceImpl(mockRestTemplate, fileStorageProps);
    }

    @Test
    public void savePublicFile() {
        fileStorageService.savePublicFile("my-file.png", "data".getBytes(), "storage-folder");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);

        verify(mockRestTemplate).exchange(urlCaptor.capture(), methodCaptor.capture(),
                httpEntityCaptor.capture(), classCaptor.capture());

        Java6Assertions.assertThat(urlCaptor.getValue()).isEqualTo("http://localhost/store/file?bucketDirectory=bucket/storage-folder&identifier=PUBLIC");
        Java6Assertions.assertThat(methodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        Java6Assertions.assertThat(classCaptor.getValue()).isEqualTo(String.class);

        HttpEntity request = httpEntityCaptor.getValue();
        Java6Assertions.assertThat(request.getHeaders().toString()).isEqualTo("[Content-Type:\"multipart/form-data\", Authorization:\"ApiKey key\"]");
        Java6Assertions.assertThat(request.getBody().toString()).contains("[Content-Disposition:\"form-data; name=\"file\"; filename=\"my-file.png\"\"]>]");

    }

    @Test
    public void getPublicFilePath() {
        String filePath = fileStorageService.getPublicFilePath("my-file.png", "storage-folder");
        assertThat(filePath).isEqualTo("http://localhost/get/file?file=bucket/storage-folder/my-file.png&identifier=PUBLIC");
    }

    @Test
    public void getLogo() {
        String filePath = fileStorageService.getLogo();
        assertThat(filePath).isEqualTo("http://localhost/get/file?file=bucket/logo.png&identifier=PUBLIC");
    }

}

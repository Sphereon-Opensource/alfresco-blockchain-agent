package com.sphereon.alfresco.blockchain.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sphereon.alfresco.blockchain.agent.rest.model.VerifyNodesRequest;
import com.sphereon.alfresco.blockchain.agent.config.RestControllerConfigTemplate;
import com.sphereon.alfresco.blockchain.agent.config.TestConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
@ActiveProfiles("write-swagger")
public class WriteSwaggerJson {
    @Value("${local.server.port}")
    private String port;

    @Autowired
    ObjectMapper primaryObjectMapper;

    @Test
    public void createSwaggerJsons() throws Exception {
        VerifyNodesRequest request = primaryObjectMapper.readValue("{\"nodeIds\":[\"eb1befa9-05f1-4d1e-b311-6ff2d36658e4\"]}", VerifyNodesRequest.class);
        createSwaggerJson(RestControllerConfigTemplate.Mode.DEFAULT, "swagger.default.json");
    }

    private void createSwaggerJson(RestControllerConfigTemplate.Mode mode, String jsonFile) throws Exception {
        File swaggerDir = new File("src/main/resources/swagger");
        swaggerDir.mkdirs();

        File swaggerFile = new File(swaggerDir.getAbsolutePath() + File.separator + jsonFile);
        if (swaggerFile.exists()) {
            Assert.assertTrue(swaggerFile.delete());
        }

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(getServiceUrl(mode));
        CloseableHttpResponse response = client.execute(get);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        swaggerFile.setWritable(true);
        Files.write(swaggerFile.toPath(), new JSONObject(json).toString(2).getBytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        if (swaggerFile.length() <= 10) {
            Assert.fail("Swagger file too small");
        }
        response.close();
        client.close();
    }

    private String getServiceUrl(RestControllerConfigTemplate.Mode mode) {
        return String.format("http://localhost:%s/v2/api-docs?group=%s", port, mode.getGroupName());
    }
}


// TODO: Find out how to use crypto-keys-ms without Azure, we are on Amazon here...

package com.sphereon.alfresco.blockchain.agent.backend.tasks.ondemand;

import org.springframework.stereotype.Component;

@Component
public class CheckCertificateTask /*implements Task*/ {

    private static final String EXCEPTION_MESSAGE_LIST_CERT = "An error occurred whilst loading certificates: "; // TODO: move to constants static class
    private static final String EXCEPTION_MESSAGE_IMPORT_CERT = "An error occurred whilst importing certificate: ";
    private static final String ASSERT_MESSAGE_GET_CONFIG = "An error occurred whilst retrieving crypto configuration %s. The response was empty.";
    private static final String EXCEPTION_MESSAGE_GET_CONFIG = "An error occurred whilst retrieving crypto configuration %s: %s";
    private static final String EXCEPTION_MESSAGE_CREATE_CONFIG = "An error occurred whilst creating config %s: %s";
/*


    @Value("${BLOCKCHAIN_CONFIG_NAME:#{null}}")
    private String configName;

    @Value("${BLOCKCHAIN_CERT_NAME:#{null}}")
    private String certificateName;

    @Value("${BLOCKCHAIN_CERT_PASSWORD:#{null}}")
    private String certificatePassword;

    @Value("${BLOCKCHAIN_CERT_PATH:#{null}}")
    private String certificatePathString;

    private Path certificatePath;

    @Autowired
    private ConfigApi cryptoConfigApi;

    @Autowired
    private CertApi cryptoCertApi;

    private ModelConfiguration configuration;


    @PostConstruct
    public void init() {
        Assert.notNull(configName, "The environment variable BLOCKCHAIN_CONFIG_NAME is not set.");
        Assert.notNull(certificatePathString, "The environment variable BLOCKCHAIN_CERT_PATH is not set.");
        Assert.notNull(certificateName, "The environment variable BLOCKCHAIN_CERT_NAME is not set.");
        Assert.notNull(certificatePassword, "The environment variable BLOCKCHAIN_CERT_PASSWORD is not set.");
        final var certificatePath = Paths.get(certificatePathString);
        if (Files.exists(certificatePath)) {
            this.certificatePath = certificatePath;
        } else {
            final var certificatePathUrl = getClass().getResource(this.certificatePathString);
            if (certificatePathUrl != null) {
                this.certificatePath = Paths.get(certificatePathString);
            }
        }
        Assert.notNull(this.certificatePath, "Certificate " + certificatePathString + " could not be found.");
    }


    @Override
    public void execute() {
        getConfiguration();
        checkCertificate();
    }


    private void getConfiguration() {
        try {
            final var configResponse = cryptoConfigApi.getConfiguration(configName);
            Assert.notNull(configResponse, String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
            Assert.notNull(configResponse.getConfiguration(), String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
            this.configuration = configResponse.getConfiguration();
        } catch (ApiException e) {
            if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
                createConfiguration();
            } else {
                throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_CONFIG, configName, "" + e.getCode()), e);
            }

        } catch (Throwable throwable) {
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_GET_CONFIG, configName, throwable.getMessage()), throwable);
        }
    }


    private void createConfiguration() {
        final var createConfigRequest = new CreateConfigurationRequest();
        final var modelConfiguration = new ModelConfiguration();
        modelConfiguration.setName(configName);
        modelConfiguration.setImplementationType(ImplementationTypeEnum.KEYSTORE_FILE);
        modelConfiguration.setStorageTypeType(StorageTypeTypeEnum.AZURE_KEYVAULT);
        createConfigRequest.setConfiguration(modelConfiguration);
        try {
            final var configResponse = cryptoConfigApi.createConfiguration(createConfigRequest);
            Assert.notNull(configResponse, String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
            Assert.notNull(configResponse.getConfiguration(), String.format(ASSERT_MESSAGE_GET_CONFIG, configName));
            this.configuration = configResponse.getConfiguration();
        } catch (ApiException e) {
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_CREATE_CONFIG, configName, "" + e.getCode()), e);

        } catch (Throwable throwable) {
            throw new RuntimeException(String.format(EXCEPTION_MESSAGE_CREATE_CONFIG, configName, throwable.getMessage()), throwable);
        }
    }


    private void checkCertificate() {
        try {
            var listCertsResponse = cryptoCertApi.listCerts(configName);
            listCertsResponse.getCertificateMetadata().forEach((s, certificateMetadata) -> {
                System.out.println(s + ":" + certificateMetadata.toString());
            });

            uploadCertificate();
        } catch (ApiException e) {
            throw new RuntimeException(EXCEPTION_MESSAGE_LIST_CERT + e.getCode(), e);

        } catch (Throwable throwable) {
            throw new RuntimeException(EXCEPTION_MESSAGE_LIST_CERT + throwable.getMessage(), throwable);
        }
    }


    private void uploadCertificate() {
        try {
            final var importCertificateRequest = new ImportCertificateRequest();
            importCertificateRequest.setName(certificateName);
            byte[] content = Files.readAllBytes(certificatePath);
            importCertificateRequest.setName(certificateName);
            importCertificateRequest.setPassword(certificatePassword);
            importCertificateRequest.setCertificate(new String(content, "UTF-8"));
            cryptoCertApi.importCert(configName, importCertificateRequest);
        } catch (ApiException e) {
            throw new RuntimeException(EXCEPTION_MESSAGE_IMPORT_CERT + e.getCode(), e);
        } catch (Throwable throwable) {
            throw new RuntimeException(EXCEPTION_MESSAGE_IMPORT_CERT + throwable.getMessage(), throwable);
        }
    }
*/
}

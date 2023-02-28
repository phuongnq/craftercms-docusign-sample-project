package plugins.org.craftercms.plugin.docusign.utils

@Grapes([
    @Grab(group='io.swagger', module='swagger-annotations', version='1.5.18', initClass=false),
    @Grab(group='jakarta.ws.rs', module='jakarta.ws.rs-api', version='2.1.6', initClass=false),
    @Grab(group='org.glassfish.jersey.media', module='jersey-media-multipart', version='2.29.1', initClass=false),
    @Grab(group='org.glassfish.jersey.media', module='jersey-media-json-jackson', version='2.29.1', initClass=false),
    @Grab(group='org.glassfish.jersey.core', module='jersey-client', version='2.29.1', initClass=false),
    @Grab(group='org.glassfish.jersey.inject', module='jersey-hk2', version='2.26', initClass=false),
    @Grab(group='com.fasterxml.jackson.core', module='jackson-core', version='2.12.1', initClass=false),
    @Grab(group='com.fasterxml.jackson.core', module='jackson-databind', version='2.12.1', initClass=false),
    @Grab(group='com.fasterxml.jackson.dataformat', module='jackson-dataformat-csv', version='2.12.1', initClass=false),
    @Grab(group='com.fasterxml.jackson.datatype', module='jackson-datatype-jsr310', version='2.12.1', initClass=false),
    @Grab(group='org.apache.oltu.oauth2', module='org.apache.oltu.oauth2.client', version='1.0.2', initClass=false),
    @Grab(group='com.auth0', module='java-jwt', version='3.4.1', initClass=false),
    @Grab(group='org.bouncycastle', module='bcprov-jdk15on', version='1.69', initClass=false),
    @Grab(group='com.docusign', module='docusign-esign-java', version='3.23.0', initClass=false)
])

import org.springframework.http.HttpHeaders
import java.nio.charset.StandardCharsets

import com.docusign.esign.client.ApiClient
import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.auth.OAuth

public final class ApiClientHelpers {
    static final String BEARER_AUTHENTICATION = "Bearer "

    static def createApiClient(String basePath, String userAccessToken) {
        ApiClient apiClient = new ApiClient(basePath)
        apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, BEARER_AUTHENTICATION + userAccessToken)
        return apiClient
    }

    static def createEnvelopesApi(String basePath, String userAccessToken) {
        ApiClient apiClient = createApiClient(basePath, userAccessToken)
        return new EnvelopesApi(apiClient)
    }

    static def getOneTimeAccessToken(String basePath, String privateKey, String integrationKey, String userId, List<String> scopes) {
        ApiClient apiClient = new ApiClient(basePath)
        byte[] privateKeyBytes = privateKey.getBytes(StandardCharsets.UTF_8)
        OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken(
                        integrationKey,
                        userId,
                        scopes,
                        privateKeyBytes,
                        120)
        return oAuthToken.getAccessToken()
    }
}
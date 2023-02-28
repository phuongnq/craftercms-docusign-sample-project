package plugins.org.craftercms.plugin.docusign.form

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

import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.model.EnvelopeDefinition
import com.docusign.esign.model.EnvelopeSummary

import plugins.org.craftercms.plugin.docusign.utils.ApiClientHelpers
import plugins.org.craftercms.plugin.docusign.utils.HttpHelpers
import plugins.org.craftercms.plugin.docusign.utils.EnvelopeHelpers
import plugins.org.craftercms.plugin.docusign.commons.WorkArguments
import plugins.org.craftercms.plugin.docusign.services.SigningViaEmailService

import groovy.util.logging.Slf4j

@Slf4j
class DefaultFormHandler implements FormHandler {

    def handle(params, request, siteConfig, siteItemService) {
        String basePath = siteConfig.getString('docusign.basePath')
        String privateKey = siteConfig.getString('docusign.privateKey')
        String integrationKey = siteConfig.getString('docusign.integrationKey')
        String userId = siteConfig.getString('docusign.userId')
        List<String> scopes = siteConfig.getList(String.class, 'docusign.scopes')
        String accessToken = ApiClientHelpers.getOneTimeAccessToken(basePath, privateKey, integrationKey, userId, scopes)

        EnvelopesApi envelopesApi = ApiClientHelpers.createEnvelopesApi(basePath, accessToken)

        WorkArguments args = new WorkArguments()
        args.signerEmail = params.email
        args.signerName = params.name
        args.ccEmail = siteConfig.getString('docusign.ccEmail')
        args.ccName = siteConfig.getString('docusign.ccName')
        args.status = siteConfig.getString('docusign.status')

        EnvelopeDefinition envelope = SigningViaEmailService.makeEnvelope(
            siteItemService,    // CrafterCMS SiteItemService instance
            args.getSignerEmail(),
            args.getSignerName(),
            args.getCcEmail(),
            args.getCcName(),
            args.getStatus(),
            args
        )

        def accountId = siteConfig.getString('docusign.accountId')
        EnvelopeSummary envelopeSummary = SigningViaEmailService.signingViaEmail(
            envelopesApi,
            accountId,
            envelope
        )

        log.info("Sent email with response: '{}'", envelopeSummary.toString())

        return "/templates/plugins/org/craftercms/plugin/docusign/success.ftl"
    }
}
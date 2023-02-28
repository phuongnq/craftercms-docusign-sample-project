package plugins.org.craftercms.plugin.docusign.services

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
import com.docusign.esign.client.ApiException
import com.docusign.esign.model.*

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Arrays

import org.craftercms.engine.service.SiteItemService
import plugins.org.craftercms.plugin.docusign.commons.WorkArguments
import plugins.org.craftercms.plugin.docusign.commons.DocumentType
import plugins.org.craftercms.plugin.docusign.utils.EnvelopeHelpers

public final class SigningViaEmailService {

    private static final String HTML_DOCUMENT_FILE_NAME = "/templates/plugins/org/craftercms/plugin/docusign/candy-bonbon.ftl"
    private static final String HTML_DOCUMENT_NAME = "Order acknowledgement"
    private static final String PDF_DOCUMENT_FILE_NAME = "/static-assets/plugins/org/craftercms/plugin/docusign/document/world_wide_corp_lorem.pdf"
    private static final String PDF_DOCUMENT_NAME = "Lorem Ipsum"
    private static final String DOCX_DOCUMENT_FILE_NAME = "/static-assets/plugins/org/craftercms/plugin/docusign/document/world_wide_corp_battle_plan_trafalgar.docx"
    private static final String DOCX_DOCUMENT_NAME = "Battle Plan"
    private static final int ANCHOR_OFFSET_Y = 10
    private static final int ANCHOR_OFFSET_X = 20

    public static EnvelopeSummary signingViaEmail(
        EnvelopesApi envelopesApi,
        String accountId,
        EnvelopeDefinition envelope
    ) throws ApiException {
        return envelopesApi.createEnvelope(accountId, envelope)
    }

    // document 1 (html) has tag **signature_1**
    // document 2 (docx) has tag /sn1/
    // document 3 (pdf) has tag /sn1/
    //
    // The envelope has two recipients.
    // recipient 1 - signer
    // recipient 2 - cc
    // The envelope will be sent first to the signer. After it is signed,
    // a copy is sent to the cc person.
    public static EnvelopeDefinition makeEnvelope(
        SiteItemService siteItemService,
        String signerEmail,
        String signerName,
        String ccEmail,
        String ccName,
        String status,
        WorkArguments args
    ) throws IOException {
        // The DocuSign platform searches throughout your envelope's documents
        // for matching anchor strings. So the signHere2 tab will be used in
        // both document 2 and 3 since they use the same anchor string for
        // their "signer 1" tabs.
        Tabs signerTabs = EnvelopeHelpers.createSignerTabs(
                EnvelopeHelpers.createSignHere("**signature_1**", ANCHOR_OFFSET_Y, ANCHOR_OFFSET_X),
                EnvelopeHelpers.createSignHere("/sn1/", ANCHOR_OFFSET_Y, ANCHOR_OFFSET_X))

        // Create a signer recipient to sign the document, identified by name
        // and email. We're setting the parameters via the object creation.
        // RoutingOrder (lower means earlier) determines the order of deliveries
        // to the recipients. Parallel routing order is supported by using the
        // same integer as the order for two or more recipients.
        Signer signer = new Signer()
        signer.setEmail(signerEmail)
        signer.setName(signerName)
        signer.setRecipientId("1")
        signer.setRoutingOrder("1")
        signer.setTabs(signerTabs)

        // create a cc recipient to receive a copy of the documents, identified by name and email
        CarbonCopy cc = new CarbonCopy()
        cc.setEmail(ccEmail)
        cc.setName(ccName)
        cc.setRecipientId("2")
        cc.setRoutingOrder("2")

        // The order in the docs array determines the order in the envelope
        byte[] htmlDocument = EnvelopeHelpers.createHtmlFromTemplateAsset(siteItemService, HTML_DOCUMENT_FILE_NAME, "args", args)
                .getBytes(StandardCharsets.UTF_8)
        EnvelopeDefinition envelope = new EnvelopeDefinition()
        envelope.setEmailSubject("Please sign this document set")
        envelope.setDocuments(Arrays.asList(
                EnvelopeHelpers.createDocument(htmlDocument, HTML_DOCUMENT_NAME,
                        DocumentType.HTML.getDefaultFileExtention(), "1"),
                EnvelopeHelpers.createDocumentFromAsset(siteItemService, DOCX_DOCUMENT_FILE_NAME, DOCX_DOCUMENT_NAME, "2"),
                EnvelopeHelpers.createDocumentFromAsset(siteItemService, PDF_DOCUMENT_FILE_NAME, PDF_DOCUMENT_NAME, "3")))
        envelope.setRecipients(EnvelopeHelpers.createRecipients(signer, cc))
        // Request that the envelope be sent by setting |status| to "sent".
        // To request that the envelope be created as a draft, set to "created"
        envelope.setStatus(status)

        return envelope
    }
}

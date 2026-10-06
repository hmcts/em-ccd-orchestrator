package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;


public class BundleException extends RuntimeException {

    public BundleException(JsonNode list,
                           String documentSelectorProperty,
                           String documentNameValue,
                           String documentLinkValue,
                           String customDocumentLinkValue,
                           boolean customDocument,
                           Exception ex) {
        super(ex);

        final Logger logger = LoggerFactory.getLogger(BundleFactory.class);

        logger.error("addDocumentSet failed,"
                        + "list:{},"
                        + "documentSelector property:{},"
                        + "documentNameValue:{},"
                        + "documentLinkValue:{},"
                        + "customDocumentLinkValue:{},"
                        + "customDocument:{}",
                list,
                documentSelectorProperty,
                documentNameValue,
                documentLinkValue,
                customDocumentLinkValue,
                customDocument,
                ex
        );
    }
}

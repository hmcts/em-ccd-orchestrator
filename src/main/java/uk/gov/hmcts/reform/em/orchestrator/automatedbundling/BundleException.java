package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleException extends Exception{

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

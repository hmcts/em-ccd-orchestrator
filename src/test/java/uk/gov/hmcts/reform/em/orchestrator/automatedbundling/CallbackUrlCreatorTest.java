package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import org.junit.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CallbackUrlCreatorTest {

    CallbackUrlCreator callbackUrlCreator =
            new CallbackUrlCreator("hostx.com", "https", 1111);

    @Test
    public void createCallbackUrl() {

        assertEquals(
            "https://hostx.com:1111/api/stitching-complete-callback/12/34/ab8e5c4f-2309-4a47-9ce8-be7f5c114c39",
            callbackUrlCreator.createCallbackUrl(
                    "12",
                    "34",
                    UUID.fromString("ab8e5c4f-2309-4a47-9ce8-be7f5c114c39").toString()));

    }
}

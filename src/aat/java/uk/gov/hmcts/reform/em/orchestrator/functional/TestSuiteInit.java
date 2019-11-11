package uk.gov.hmcts.reform.em.orchestrator.functional;

import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

public class TestSuiteInit {

    public static final TestUtil testUtil = new TestUtil();

    static {
        try {
            testUtil.getCcdHelper().initBundleTesterUser();
            testUtil.getCcdHelper().importCcdDefinitionFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

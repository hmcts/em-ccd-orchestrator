package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.em.EmTestConfig;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;
import uk.gov.hmcts.reform.em.test.retry.RetryExtension;

@SpringBootTest(classes = {TestUtil.class, EmTestConfig.class, ExtendedCcdHelper.class, TestConfig.class})
@TestPropertySource(value = "classpath:application.yml")
@ExtendWith({SerenityJUnit5Extension.class, SpringExtension.class})
@WithTags({@WithTag("testType:Functional")})
public abstract class BaseTest {

    protected final TestUtil testUtil;

    protected final ExtendedCcdHelper extendedCcdHelper;

    @RegisterExtension
    RetryExtension retryExtension = new RetryExtension(3);

    ObjectMapper mapper = new ObjectMapper();

    @Value("${cdam.validation.enabled}")
    protected boolean enableCdamValidation;

    @Autowired
    protected BaseTest(TestUtil testUtil, ExtendedCcdHelper extendedCcdHelper) {
        this.testUtil = testUtil;
        this.extendedCcdHelper = extendedCcdHelper;
    }
}

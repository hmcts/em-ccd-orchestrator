package uk.gov.hmcts.reform.em.stitching.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.em.stitching.testutil.{Env, TestUtil}

trait HttpConfiguration {

    val testUtil = new TestUtil

    val headers = Map(
        """Content-Type""" -> """application/json""")

    var httpProtocol = http
    if (Env.getUseProxy.equalsIgnoreCase("true")) {
        httpProtocol = http
          .proxy(Proxy("proxyout.reform.hmcts.net", 8080))
          .headers(headers)
          .disableFollowRedirect
          .disableWarmUp
    } else {
        httpProtocol = http
          .headers(headers)
          .disableFollowRedirect
          .disableWarmUp
    }
}

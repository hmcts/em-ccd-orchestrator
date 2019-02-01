package uk.gov.hmcts.reform.em.stitching.performance

import io.gatling.http.Predef.{http, status}

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.json.JSONObject
import scala.concurrent.duration._
import uk.gov.hmcts.reform.em.stitching.testutil.Env

class NpaLoad extends Simulation with HttpConfiguration {
//
//  object CreateTask {
//
//    var jsonObject = new JSONObject().put("bundle", testUtil.getDocumentId).toString()
//    def run = exec(
//          http("create-npa-task-" + testConfig.pdfName)
//            .post(Env.getTestUrl + "/api/document-tasks")
//            .header("Authorization", testUtil.getIdamToken())
//            .header("ServiceAuthorization", testUtil.getS2sToken())
//            .body(StringBody(jsonObject))
//            .check(
//              status.find.in(201),
//              jsonPath("$.bundle").exists,
//              jsonPath("$.outputDocumentId").exists,
//              jsonPath("$.taskState").in("DONE"),
//              jsonPath("$").saveAs("RESPONSE_DATA")
//            )
//        ).exec(session => {
//          println("Response:")
//          println(session("RESPONSE_DATA").as[String])
//          session
//        })
//  }
//
//  object Setup {
//    def run = exec()
//      testUtil.uploadDocument(testConfig.pdfName)
//      testUtil.createAnnotationSetForDocumentId(testUtil.getDocumentId)
//
//      var page = 1
//      for (i <- 1 to testConfig.totalAnnotations) {
//        if (page >= testConfig.pages) {
//          page = 1
//        } else {
//          page = page + 1
//        }
//        testUtil.saveAnnotation(testUtil.getAnnotationSetId(), page)
//      }
//  }
//
//  object testConfigOnePages {
//    var pdfName = "one-page.pdf"
//    var pages = 1
//    var totalAnnotations = 10
//  }
//
//  object testConfigTenPages {
//    var pdfName = "ten-page.pdf"
//    var pages = 10
//    var totalAnnotations = 100
//  }
//
//  object testConfigFiveHundredPages {
//    var pdfName = "five-hundred-page.pdf"
//    var pages = 500
//    var totalAnnotations = 5000
//  }
//
//  object testConfigHundredPages {
//    var pdfName = "hundred-page.pdf"
//    var pages = 100
//    var totalAnnotations = 1000
//  }
//
//  var testConfig = testConfigOnePages
//  val setup = scenario("setup").exec(Setup.run)
//  val createTaskScenario = scenario("create-npa-task")
//      .exec(setup)
//      .pause(10)
//      .exec(
//        CreateTask.run
//      )
//
//  setUp(
//    createTaskScenario
//    .inject(atOnceUsers(1))
//    .protocols(httpProtocol))
//    .maxDuration(10 minutes)
//    .assertions(
//      forAll.failedRequests.count.lt(1)
//    )
}


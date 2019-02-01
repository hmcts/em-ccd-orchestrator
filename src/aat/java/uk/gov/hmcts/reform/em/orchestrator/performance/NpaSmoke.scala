package uk.gov.hmcts.reform.em.stitching.performance
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.em.stitching.testutil.Env

class NpaSmoke extends Simulation with HttpConfiguration {

    object Health {

      val run = exec(http("health")
        .get(Env.getTestUrl + "/health")
        .check(
          status.find.in(200)
        ))
        .exec( session => {
          println( "Response:" )
          println( session( "RESPONSE_DATA" ).as[String] )
          session
      })
    }

    val scn = scenario("NPA smoke test").exec(Health.run)

    setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))
}


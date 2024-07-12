package restfulapi;


import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;


public class RestfulApiTest extends Simulation {

    // Case 1 - Como puedo testearlo en diferentes ambientes
    String baseUrl = System.getProperty("baseUrl", "https://api.restful-api.dev");

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(baseUrl)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    FeederBuilder.FileBased<Object> feeder = jsonFile("data/object.json").circular();


    Body payload = StringBody("{\"name\":\"AppleMacBookPro16\",\"data\":{\"year\":2019,\"price\":1849.99,\"CPUmodel\":\"IntelCorei9\",\"Harddisksize\":\"1TB\"}}");
    ScenarioBuilder scn = scenario("Restful API Test")
            .feed(feeder)
            .exec(
                    http("POST Create Object")
                            .post("/objects")
                            .body(payload).asJson()
                            .check(status().is(200))
                            .check(jsonPath("$.name").is("AppleMacBookPro16"))
                            .check(jsonPath("$.id").saveAs("postId"))
            )
            .pause(1)
            .exec(
                    http("PUT Update Object")
                            .put("/objects/#{postId}")
                            .body(StringBody("{\"name\":\"AppleMacBookPro16\",\"data\":{\"year\":2019,\"price\":2049.99,\"CPUmodel\":\"IntelCorei9\",\"Harddisksize\":\"1TB\",\"color\":\"silver\"}}"))
                            .check(status().is(200))
            )
           .pause(1)

            .exec(http("GET List of all objects")
                    .get("/objects/#{idObject}")
                    .check(jmesPath("name").isEL("#{name}"))
                    .check(bodyString().saveAs("BODY"))
                    .check(status().is(200)))


                    .exec(
                    session -> {
                        System.out.println("Objeto: " + session.getString("BODY"));
                        return session;
                    }
            )
            ;


    // Set up the scenario
    {
        setUp(
                scn.injectOpen(
                        atOnceUsers(10)
                )
        ).protocols(httpProtocol);
    }

//    {
//        setUp(
//                scn.injectOpen(
//                        atOnceUsers(10),
//                        nothingFor(Duration.ofSeconds(5)),
//                        rampUsers(10).during(Duration.ofSeconds(10)),
//                        constantUsersPerSec(10).during(Duration.ofSeconds(10))
//                )
//        ).protocols(httpProtocol);
//    }

}

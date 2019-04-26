package org.submarine.builder;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class QuarkusKnativeBuilderTest {

    private String body = "{\n" +
            "\"repo\": \"https://github.com/123/submarine-examples\",\n" +
            "\"branch\": \"dmn-quarkus-example\",\n" +
            "\"workDir\": \"dmn-quarkus-example\"\n" +
            "}";

    @Test
    public void testHelloEndpoint() {
        given()
                .pathParam("service", "dmn-quarkus-example")
                .pathParam("tag", "v1.0")
                .contentType("application/json")
                .accept("application/json")
                .body(body)
          .when()
                .post("/deploy/{service}/{tag}")
          .then()
                .statusCode(200);
//                .body(is("hello"));
    }

}
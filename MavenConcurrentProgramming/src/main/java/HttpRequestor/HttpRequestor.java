package HttpRequestor;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class HttpRequestor {

    private String BASE_URI = "http://localhost:5000";

    //gets route data having only uri path
    public Response getRequest(String uriPath) {
        RequestSpecification specification = new RequestSpecBuilder().setBaseUri(BASE_URI + uriPath).build();
        return given().spec(specification).get();
    }
    //gets route data having access token and uri path
    public Response getRequest(String uriPath, String header) {
        RequestSpecification specification = new RequestSpecBuilder().setBaseUri(BASE_URI + uriPath).addHeader("X-Access-Token", header).build();
        return given().spec(specification).get();
    }
}

package client;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.request.PostUpdateEventRequest;
import models.request.SignUpLoginRequest;
import util.Configuration;

public class EVPrimeClient {

       public Response signUp(SignUpLoginRequest body){
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .when().log().all()
                .body(body)
                .post(Configuration.SIGNUP)
                .thenReturn();
    }
    public Response login(SignUpLoginRequest body){
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .when().log().all()
                .body(body)
                .post(Configuration.LOGIN)
                .thenReturn();
    }
    public Response getAllEvents(){
        return RestAssured
                .given()
                .when().log().all()
                .get(Configuration.EVENTS)
                .thenReturn();
    }
    public Response getEventById(String id){
        return RestAssured
                .given()
                .when().log().all()
                .get(Configuration.EVENTS + "/" + id)
                .thenReturn();
    }
    public Response postEvent(PostUpdateEventRequest body, String token){
        return RestAssured
                .given()
                .header("Authorization", "bearer " + token)
                .contentType(ContentType.JSON)
                .when().log().all()
                .body(body)
                .post(Configuration.EVENTS)
                .thenReturn();
    }
    public Response updateEvent(PostUpdateEventRequest body, String token, String id){
        return RestAssured
                .given()
                .header("Authorization", "bearer " + token)
                .contentType(ContentType.JSON)
                .when().log().all()
                .body(body)
                .put(Configuration.EVENTS + "/" + id)
                .thenReturn();
    }
    public Response deleteEvent(String token, String id){
        return RestAssured
                .given()
                .header("Authorization", "bearer " + token)
                .when().log().all()
                .delete(Configuration.EVENTS + "/" + id)
                .thenReturn();
    }
}

package com.example.demo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserResourceIT {

    @BeforeAll
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 9080;
        RestAssured.basePath = "/api";
    }

    @Test
    public void testHealthEndpoint() {
        given()
            .when()
            .get("/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("service", equalTo("OpenLiberty Demo API"));
    }

    @Test
    public void testGetAllUsers() {
        given()
            .when()
            .get("/users")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(3)));
    }

    @Test
    public void testCreateUser() {
        String userJson = "{\"username\":\"integration_test\",\"email\":\"integration@test.com\",\"fullName\":\"Integration Test User\"}";

        given()
            .contentType(ContentType.JSON)
            .body(userJson)
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .body("username", equalTo("integration_test"))
            .body("email", equalTo("integration@test.com"))
            .body("id", notNullValue());
    }

    @Test
    public void testGetUserById() {
        // First create a user
        String userJson = "{\"username\":\"get_by_id\",\"email\":\"getbyid@test.com\",\"fullName\":\"Get By ID\"}";

        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(userJson)
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then get it by ID
        given()
            .when()
            .get("/users/" + userId)
            .then()
            .statusCode(200)
            .body("id", equalTo(userId))
            .body("username", equalTo("get_by_id"));
    }

    @Test
    public void testDeleteUser() {
        // First create a user
        String userJson = "{\"username\":\"to_delete\",\"email\":\"delete@test.com\",\"fullName\":\"To Delete\"}";

        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(userJson)
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then delete it
        given()
            .when()
            .delete("/users/" + userId)
            .then()
            .statusCode(204);

        // Verify it's deleted
        given()
            .when()
            .get("/users/" + userId)
            .then()
            .statusCode(404);
    }

    @Test
    public void testSearchByUsername() {
        given()
            .queryParam("username", "john_doe")
            .when()
            .get("/users/search")
            .then()
            .statusCode(200)
            .body("username", equalTo("john_doe"));
    }

    @Test
    public void testSearchByUsernameNotFound() {
        given()
            .queryParam("username", "non_existent_user")
            .when()
            .get("/users/search")
            .then()
            .statusCode(404);
    }
}
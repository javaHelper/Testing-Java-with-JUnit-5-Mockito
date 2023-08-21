package com.appsdeveloperblog.tutorials.junit.ui.controllers;

import com.appsdeveloperblog.tutorials.junit.security.SecurityConstants;
import com.appsdeveloperblog.tutorials.junit.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = "/application-test.properties",
//        properties = "server.port=8081")
//@TestPropertySource(locations = "/application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersControllerIntegrationTest {

    @Value("${server.port}")
    private int serverPort;

    @LocalServerPort
    private int localServerPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String authorizationToken;

    @Test
    @DisplayName("User can be created")
    @Order(1)
    void testCreateUser_whenValidDetailsProvided_returnsUserDetails() throws JSONException {
        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "Sergey");
        userDetailsRequestJson.put("lastName", "Kargopolov");
        userDetailsRequestJson.put("email", "test@test.com");
        userDetailsRequestJson.put("password", "12345678");
        userDetailsRequestJson.put("repeatPassword", "12345678");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(userDetailsRequestJson.toString(), headers);

        ResponseEntity<UserRest> createdUserDetailsEntity = testRestTemplate.postForEntity("/users", request, UserRest.class);
        UserRest createdUserDetails = createdUserDetailsEntity.getBody();

        // Assert
        assertEquals(HttpStatus.OK, createdUserDetailsEntity.getStatusCode());
        assert createdUserDetails != null;
        assertEquals(userDetailsRequestJson.getString("firstName"), createdUserDetails.getFirstName(), "Returned user's first name seems to be incorrect");
        assertEquals(userDetailsRequestJson.getString("lastName"), createdUserDetails.getLastName(), "Returned user's last name seems to be incorrect");
        assertEquals(userDetailsRequestJson.getString("email"), createdUserDetails.getEmail(), "Returned user's email seems to be incorrect");
        assertFalse(createdUserDetails.getUserId().trim().isEmpty(), "User id should not be empty");

        assertThat(createdUserDetailsEntity)
                .isNotNull()
                .extracting(HttpEntity::getBody)
                .isNotNull()
                .extracting(UserRest::getFirstName, UserRest::getLastName, UserRest::getEmail)
                .containsExactlyInAnyOrder("Sergey", "Kargopolov", "test@test.com");
    }

    @Test
    @DisplayName("GET /users requires JWT")
    @Order(2)
    void testGetUsers_whenMissingJWT_returns403() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity requestEntity = new HttpEntity(null, headers);

        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "HTTP Status code 403 Forbidden should have been returned");

        assertThat(response)
                .isNotNull()
                .extracting(HttpEntity::getBody)
                .isNull();

        assertThat(response)
                .isNotNull()
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("/login works")
    @Order(3)
    void testUserLogin_whenValidCredentialsProvided_returnsJWTinAuthorizationHeader() throws JSONException {
        JSONObject loginCredentials = new JSONObject();
        loginCredentials.put("email", "test@test.com");
        loginCredentials.put("password", "12345678");

        HttpEntity<String> request = new HttpEntity<>(loginCredentials.toString());

        ResponseEntity response = testRestTemplate.postForEntity("/users/login", request, null);

        authorizationToken = response.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING).get(0);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status code should be 200");
        assertNotNull(authorizationToken, "Response should contain Authorization header with JWT");
        assertNotNull(response.getHeaders().getValuesAsList("UserID").get(0), "Response should contain UserID in a response header");

        assertThat(response)
                .isNotNull()
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(HttpStatus.OK);

        assertThat(response)
                .isNotNull()
                .extracting(e -> e.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING))
                .isNotNull()
                .extracting(e -> e.get(0))
                .isNotNull()
                .isEqualTo(authorizationToken);

    }

    @Test
    @Order(4)
    @DisplayName("GET /users works")
    void testGetUsers_whenValidJWTProvided_returnsUsers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(authorizationToken);

        HttpEntity requestEntity = new HttpEntity(headers);

        // Act
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "HTTP Status code should be 200");
        Assertions.assertTrue(response.getBody().size() == 1,
                "There should be exactly 1 user in the list");

        assertThat(response)
                .isNotNull()
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(HttpStatus.OK);

        assertThat(response)
                .isNotNull()
                .extracting(e -> Objects.requireNonNull(e.getBody()).get(0))
                .isNotNull()
                .extracting(UserRest::getFirstName, UserRest::getLastName, UserRest::getEmail)
                .containsExactlyInAnyOrder("Sergey", "Kargopolov", "test@test.com");
    }
}
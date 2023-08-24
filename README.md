# Testing-Java-with-JUnit-5-Mockito

Done and dusted.

```java
import com.appsdeveloperblog.io.UsersDatabase;
import com.appsdeveloperblog.io.UsersDatabaseMapImpl;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceImplTest {
    private UsersDatabase usersDatabase;
    private UserService userService;
    private String createdUserId = "";

    @BeforeAll
    void setup() {
        // Create & initialize database
        usersDatabase = new UsersDatabaseMapImpl();
        usersDatabase.init();
        userService = new UserServiceImpl(usersDatabase);
    }

    @AfterAll
    void cleanup() {
        // Close connection
        // Delete database
        usersDatabase.close();
    }

    @Test
    @Order(1)
    @DisplayName("Create User works")
    void testCreateUser_whenProvidedWithValidDetails_returnsUserId() {
        Map<String, String> user = new HashMap<>();
        user.put("firstName","John");
        user.put("lastName","Doe");

        createdUserId = userService.createUser(user);
        assertNotNull(createdUserId, "User id should not be null");
    }


    @Test
    @Order(2)
    @DisplayName("Update user works")
    void testUpdateUser_whenProvidedWithValidDetails_returnsUpdatedUserDetails() {
        Map<String, String> newUser = new HashMap<>();
        newUser.put("firstName","Mike");
        newUser.put("lastName","Doe");

        Map updateUser = userService.updateUser(createdUserId, newUser);

        assertEquals(newUser.get("firstName"), updateUser.get("firstName"),
                "Return value of user's firstName is incorrect");

        assertEquals(newUser.get("lastName"), updateUser.get("lastName"),
                "Return value of user's lastName is incorrect");
    }

    @Test
    @Order(3)
    @DisplayName("Find user works")
    void testGetUserDetails_whenProvidedWithValidUserId_returnsUserDetails() {
        Map userDetails = userService.getUserDetails(createdUserId);
        assertNotNull(userDetails);
        assertEquals(createdUserId, userDetails.get("userId"),
                "Returned User details contains incorrect user id ");
    }

    @Test
    @Order(4)
    @DisplayName("Delete user works")
    void testDeleteUser_whenProvidedWithValidUserId_returnsUserDetails() {
        userService.deleteUser(createdUserId);
        assertNull(userService.getUserDetails(createdUserId), "User not found");
    }

}
```

------

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserEntityIntegrationTest {
    @Autowired
    private TestEntityManager testEntityManager;

    UserEntity userEntity;

    @BeforeEach
    void setup() {
        userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setFirstName("Sergey");
        userEntity.setLastName("Kargopolov");
        userEntity.setEmail("test@test.com");
        userEntity.setEncryptedPassword("12345678");
    }

    @Test
    void testUserEntity_whenValidUserDetailsProvided_shouldReturnStoredUserDetails() {
        UserEntity storedUserEntity = testEntityManager.persistAndFlush(userEntity);

        // Assert
        assertTrue(storedUserEntity.getId() > 0);
        assertEquals(userEntity.getUserId(), storedUserEntity.getUserId());
        assertEquals(userEntity.getFirstName(), storedUserEntity.getFirstName());
        assertEquals(userEntity.getLastName(), storedUserEntity.getLastName());
        assertEquals(userEntity.getEmail(), storedUserEntity.getEmail());
        assertEquals(userEntity.getEncryptedPassword(), storedUserEntity.getEncryptedPassword());

        assertThat(storedUserEntity)
                .isNotNull()
                .extracting(UserEntity::getFirstName, UserEntity::getLastName, UserEntity::getEmail)
                .containsExactlyInAnyOrder("Sergey", "Kargopolov", "test@test.com");
    }

    @Test
    void testUserEntity_whenFirstNameIsTooLong_shouldThrowException() {
        // Arrange
        userEntity.setFirstName("123456789012345678901234567890123456789012345678901234567890");

        // Assert & Act
        assertThrows(PersistenceException.class, () -> {
            testEntityManager.persistAndFlush(userEntity);
        }, "Was expecting a PersistenceException to be thrown.");
    }

    @Test
    void testUserEntity_whenExistingUserIdProvided_shouldThrowException() {
        UserEntity newEntity = new UserEntity();
        newEntity.setUserId("1");
        newEntity.setEmail("test2@test.com");
        newEntity.setFirstName("test");
        newEntity.setLastName("test");
        newEntity.setEncryptedPassword("test");
        testEntityManager.persistAndFlush(newEntity);

        // Update existing user entity with the same user id
        userEntity.setUserId("1");

        // Act & Assert
        assertThrows(PersistenceException.class, () -> {
            testEntityManager.persistAndFlush(userEntity);
        }, "Expected PersistenceException to be thrown here");
    }
}
```
-----

```java
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

@DataJpaTest
public class UsersRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UsersRepository usersRepository;

    private final String userId1 = UUID.randomUUID().toString();
    private final String userId2 = UUID.randomUUID().toString();
    private final String email1 = "test@test.com";
    private final String email2 = "test2@test.com";

    @BeforeEach
    void setup() {
        // Creating first user
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId1);
        userEntity.setEmail(email1);
        userEntity.setEncryptedPassword("12345678");
        userEntity.setFirstName("Sergey");
        userEntity.setLastName("Kargopolov");
        testEntityManager.persistAndFlush(userEntity);

        // Creating second user
        UserEntity userEntity2 = new UserEntity();
        userEntity2.setUserId(userId2);
        userEntity2.setEmail(email2);
        userEntity2.setEncryptedPassword("abcdefg1");
        userEntity2.setFirstName("John");
        userEntity2.setLastName("Sears");
        testEntityManager.persistAndFlush(userEntity2);
    }

    @Test
    void testFindByEmail_whenGivenCorrectEmail_returnsUserEntity() {
        // Act
        UserEntity storedUser = usersRepository.findByEmail(email1);

        // Assert
        Assertions.assertEquals(email1, storedUser.getEmail(),
                "The returned email address does not match the expected value");
    }

    @Test
    void testFindByUserId_whenGivenCorrectUserId_returnsUserEntity() {
        // Act
        UserEntity storedUser = usersRepository.findByUserId(userId2);

        // Assert
        Assertions.assertNotNull(storedUser,
                "UserEntity object should not be null");
        Assertions.assertEquals(userId2, storedUser.getUserId(),
                "Returned userId does not much expected value");
    }

    @Test
    void testFindUsersWithEmailEndsWith_whenGiveEmailDomain_returnsUsersWithGivenDomain() {
        // Arrange
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setEmail("test@gmail.com");
        userEntity.setEncryptedPassword("123456789");
        userEntity.setFirstName("Sergey");
        userEntity.setLastName("Kargopolov");
        testEntityManager.persistAndFlush(userEntity);

        String emailDomainName = "@gmail.com";

        // Act
        List<UserEntity> users = usersRepository.findUsersWithEmailEndingWith(emailDomainName);

        // Assert
        Assertions.assertEquals(1, users.size(),
                "There should be one user in the list");
        Assertions.assertTrue(users.get(0).getEmail().endsWith(emailDomainName),
                "User's email does not end with target domain name");
    }
}
```
----

```java
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
```

```java
import com.appsdeveloperblog.tutorials.junit.service.UsersService;
import com.appsdeveloperblog.tutorials.junit.service.UsersServiceImpl;
import com.appsdeveloperblog.tutorials.junit.shared.UserDto;
import com.appsdeveloperblog.tutorials.junit.ui.controllers.UsersController;
import com.appsdeveloperblog.tutorials.junit.ui.request.UserDetailsRequestModel;
import com.appsdeveloperblog.tutorials.junit.ui.response.UserRest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UsersController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
//@AutoConfigureMockMvc(addFilters = false)
//@MockBean({UsersServiceImpl.class})
public class UsersControllerWebLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    private UserDetailsRequestModel userDetailsRequestModel;

    @BeforeEach
    void setup() {
        userDetailsRequestModel = new UserDetailsRequestModel();
        userDetailsRequestModel.setFirstName("Sergey");
        userDetailsRequestModel.setLastName("Kargopolov");
        userDetailsRequestModel.setEmail("test@test.com");
        userDetailsRequestModel.setPassword("12345678");
    }

    @Test
    @DisplayName("User can be created")
    void testCreateUser_whenValidUserDetailsProvided_returnsCreatedUserDetails() throws Exception {
        // Arrange
        UserDto userDto = new ModelMapper().map(userDetailsRequestModel, UserDto.class);
        userDto.setUserId(UUID.randomUUID().toString());
        when(usersService.createUser(any(UserDto.class))).thenReturn(userDto);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseBodyAsString = mvcResult.getResponse().getContentAsString();
        UserRest createdUser = new ObjectMapper()
                .readValue(responseBodyAsString, UserRest.class);

        // Assert
        Assertions.assertEquals(userDetailsRequestModel.getFirstName(),
                createdUser.getFirstName(), "The returned user first name is most likely incorrect");

        Assertions.assertEquals(userDetailsRequestModel.getLastName(),
                createdUser.getLastName(), "The returned user last name is incorrect");

        Assertions.assertEquals(userDetailsRequestModel.getEmail(),
                createdUser.getEmail(), "The returned user email is incorrect");

        Assertions.assertFalse(createdUser.getUserId().isEmpty(), "userId should not be empty");

    }

    @Test
    @DisplayName("First name is not empty")
    void testCreateUser_whenFirstNameIsNotProvided_returns400StatusCode() throws Exception {
        // Arrange
        userDetailsRequestModel.setFirstName("");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),
                mvcResult.getResponse().getStatus(),
                "Incorrect HTTP Status Code returned");


    }

    @Test
    @DisplayName("First name cannot be shorter than 2 characters")
    void testCreateUser_whenFirstNameIsOnlyOneCharacter_returns400StatusCode() throws Exception {
        // Arrange
        userDetailsRequestModel.setFirstName("a");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        // Act
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),
                result.getResponse().getStatus(), "HTTP Status code is not set to 400");
    }

}
```

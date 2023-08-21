package com.appsdeveloperblog.tutorials.junit.io;

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
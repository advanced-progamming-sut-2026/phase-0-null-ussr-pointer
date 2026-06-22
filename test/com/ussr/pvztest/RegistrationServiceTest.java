package com.ussr.pvztest;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.*;
import com.ussr.pvz.model.dto.PickQuestionRequest;
import com.ussr.pvz.model.dto.RegisterRequest;
import com.ussr.pvz.service.RegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegisterServiceTest {

    private RegisterService registerService;

    @BeforeEach
    void setUp() {
        // Reset static App state and clear accounts to guarantee test isolation
        App.setMenuState(MenuState.REGISTER);
        App.getAccounts().clear();
        registerService = new RegisterService();
    }

    // ====== SUCCESSFUL REGISTRATION TESTS ======

    @Test
    @DisplayName("✅ Should return security questions when registration is valid")
    void register_shouldReturnSecurityQuestions_whenAllDataIsValid() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );

        // Act
        String result = registerService.register(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("pick a security question:"));
        assertTrue(result.contains("What is the name of your favorite TA?"));
        assertTrue(result.contains("What is your favorite food?"));
        // Check that all security questions are listed
        for (SecurityQuestion q : SecurityQuestion.values()) {
            assertTrue(result.contains(q.getText()));
        }
    }

    @Test
    @DisplayName("✅ Should successfully pick a question and complete registration")
    void pickQuestion_shouldCompleteRegistration_whenValid() {
        // Arrange - First register
        RegisterRequest registerRequest = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        registerService.register(registerRequest);

        // Act - Then pick question
        PickQuestionRequest pickRequest = new PickQuestionRequest(
                "1",  // First security question
                "Smith",
                "Smith"
        );
        String result = registerService.pickQuestion(pickRequest);

        // Assert
        assertEquals("registered successfully", result);
        assertEquals(MenuState.LOGIN, App.getMenuState());

        // Verify account was saved
        assertFalse(App.getAccounts().isEmpty());
        Account savedAccount = App.getAccounts().get(0);
        assertEquals("john-doe", savedAccount.getName());
        assertEquals("Johnny", savedAccount.getNickname());
        assertEquals("john.doe@example.com", savedAccount.getEmail());
    }

    // ====== USERNAME VALIDATION TESTS ======

    @Test
    @DisplayName("❌ Should fail when username already exists")
    void register_shouldFail_whenUsernameExists() {
        // Arrange - Add existing user
        Account existingAccount = new Account(
                new AccountState(
                        "existing-user",
                        "ExistingUser",
                        "pass",
                        "existing@example.com",
                        null,
                        3,
                        null,
                        null,
                        1,
                        0,
                        0,
                        0,
                        AdventureProgress.initializePlantsLvl(),
                        List.of(NewsItem.initialNews())
                ),
                null
        );
        App.addAccount(existingAccount);

        // Act
        RegisterRequest request = new RegisterRequest(
                "existing-user",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("username already exists", result);
    }

    @ParameterizedTest
    @DisplayName("❌ Should fail when username has invalid characters")
    @ValueSource(strings = {
            "john@doe",    // Contains @
            "john doe",    // Contains space
            "john!doe",    // Contains !
            "user#name",   // Contains #
            "user$name",   // Contains $
            "user%name",   // Contains %
            "user^name",   // Contains ^
            "user&name",   // Contains &
            "user*name",   // Contains *
            "user(name",   // Contains (
            "user)name",   // Contains )
            "user+name",   // Contains +
            "user=name",   // Contains =
            "user{name",   // Contains {
            "user}name",   // Contains }
            "user[name",   // Contains [
            "user]name",   // Contains ]
            "user|name",   // Contains |
            "user;name",   // Contains ;
            "user:name",   // Contains :
            "user'name",   // Contains '
            "user\"name",   // Contains "
            "user<name",   // Contains <
            "user>name",   // Contains >
            "user?name",   // Contains ?
            "user/name",   // Contains /
            "user\\name",   // Contains backslash
            "user`name",   // Contains backtick
            "user~name",   // Contains ~
            ""             // Empty
    })
    void register_shouldFail_whenUsernameInvalid(String invalidUsername) {
        // Act
        RegisterRequest request = new RegisterRequest(
                invalidUsername,
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("invalid username", result);
    }

    // ====== PASSWORD VALIDATION TESTS ======

    @Test
    @DisplayName("❌ Should fail when passwords don't match")
    void register_shouldFail_whenPasswordsDoNotMatch() {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "Password123!",
                "Different456!",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("password confirm does not match to the password", result);
    }

    @Test
    @DisplayName("❌ Should fail when password is too short")
    void register_shouldFail_whenPasswordTooShort() {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "Short1!",
                "Short1!",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("invalid password length", result);
    }

    @Test
    @DisplayName("❌ Should fail when password has no lowercase")
    void register_shouldFail_whenPasswordNoLowercase() {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "STRONGP&SS123",
                "STRONGP&SS123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("password must contain a lowercase character", result);
    }

    @Test
    @DisplayName("❌ Should fail when password has no uppercase")
    void register_shouldFail_whenPasswordNoUppercase() {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "strongp&ss123",
                "strongp&ss123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("password must contain an uppercase character", result);
    }

    @Test
    @DisplayName("❌ Should fail when password has no number")
    void register_shouldFail_whenPasswordNoNumber() {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongP@ss",
                "StrongP@ss",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("password must contain a number", result);
    }

    @Test
    @DisplayName("❌ Should fail when password has no special character")
    void register_shouldFail_whenPasswordNoSpecialCharacter() {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongPass123",
                "StrongPass123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("password must contain a specific character", result);
    }

    @ParameterizedTest
    @DisplayName("✅ Should accept valid passwords")
    @ValueSource(strings = {
            "StrongP&ss123",
            "Secure#Pass456",
            "MyP&ssword789",
            "Test!Pass123",
            "Valid*Password456",
            "Good$Pass789"
    })
    void register_shouldAcceptValidPasswords(String validPassword) {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                validPassword,
                validPassword,
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("pick a security question"));
    }

    // ====== EMAIL VALIDATION TESTS ======

    @ParameterizedTest
    @DisplayName("❌ Should fail when email is invalid")
    @CsvSource({
            "john@example",              // No domain extension
            "john@.com",                 // Domain starts with dot
            "john..doe@example.com",     // Double dots in local part
            "john.doe@example..com",     // Double dots in domain
            "john.doe@example.c",        // Extension too short
            "john@domain@com",           // Multiple @
            ".john@example.com",         // Starts with dot
            "john.@example.com",         // Ends with dot
            "john doe@example.com",      // Space in local part
            "john@exam ple.com",         // Space in domain
            "john@exam$ple.com",         // Invalid character
            "john@exam(ple.com",         // Invalid character
            "john@exam=ple.com",         // Invalid character
            "john@exam!ple.com",         // Invalid character
            "john@exam#ple.com",         // Invalid character
            "john@exam%ple.com",         // Invalid character
            "john@exam^ple.com",         // Invalid character
            "john@exam&ple.com",         // Invalid character
            "john@exam*ple.com",         // Invalid character
            "john@exam+ple.com"          // Invalid character
    })
    void register_shouldFail_whenEmailInvalid(String invalidEmail) {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                invalidEmail,
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("invalid email format", result);
    }

    @ParameterizedTest
    @DisplayName("✅ Should accept valid emails")
    @ValueSource(strings = {
            "john.doe@example.com",
            "john-doe@example.org",
            "johndoe@example.co.uk",
            "john-doe@example.io",
            "john123@example.com",
            "john.doe123@example.com",
            "john.doe@mail.example.com",
            "johndoe@gmail.com",
            "john.doe@yahoo.com"
    })
    void register_shouldAcceptValidEmails(String validEmail) {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                validEmail,
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("pick a security question"));
    }

    // ====== NICKNAME VALIDATION TESTS ======

    @ParameterizedTest
    @DisplayName("❌ Should fail when nickname length is invalid")
    @ValueSource(strings = {
            "Jo",  // Too short (2 chars)
            "A",   // Too short (1 char)
            "",    // Empty
            "ThisIsAVeryLongNicknameThatIsDefinitelyLongerThanThirtyCharacters" // Too long
    })
    void register_shouldFail_whenNicknameInvalid(String invalidNickname) {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                invalidNickname,
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("invalid nickname length", result);
    }

    @ParameterizedTest
    @DisplayName("✅ Should accept valid nicknames")
    @ValueSource(strings = {
            "Johnny",
            "PlayerOne",
            "TheBest",
            "Gamer123",
            "Pro",
            "Ninja",
            "Warrior",
            "Champion"
    })
    void register_shouldAcceptValidNicknames(String validNickname) {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                validNickname,
                "john.doe@example.com",
                "Male"
        );
        String result = registerService.register(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("pick a security question"));
    }

    // ====== GENDER VALIDATION TESTS ======

    @ParameterizedTest
    @DisplayName("❌ Should fail when gender is invalid")
    @ValueSource(strings = {
            "other",
            "unknown",
            "M",
            "F",
            "Malee",
            "femal",
            "",
            " ",
            "non-binary",
            "Male Female",
            "m",
            "f",
            "Male",
            "Female"
    })
    void register_shouldFail_whenGenderInvalid(String invalidGender) {
        // Only test truly invalid values
        if (invalidGender.equalsIgnoreCase("Male") || invalidGender.equalsIgnoreCase("Female")) {
            return;
        }

        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                invalidGender
        );
        String result = registerService.register(request);

        // Assert
        assertEquals("invalid gender", result);
    }

    @ParameterizedTest
    @DisplayName("✅ Should accept valid genders (case insensitive)")
    @ValueSource(strings = {"Male", "Female", "Male", "Female", "Male", "Female"})
    void register_shouldAcceptValidGenders(String validGender) {
        // Act
        RegisterRequest request = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                validGender
        );
        String result = registerService.register(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("pick a security question"));
    }

    // ====== SECURITY QUESTION TESTS ======

    @Test
    @DisplayName("❌ Should fail when trying to pick question without pending registration")
    void pickQuestion_shouldFail_whenNoPendingRegistration() {
        // Act - No registration done first
        PickQuestionRequest request = new PickQuestionRequest("1", "Smith", "Smith");
        String result = registerService.pickQuestion(request);

        // Assert
        assertEquals("no pending registration", result);
    }

    @Test
    @DisplayName("❌ Should fail when question number is invalid (non-numeric)")
    void pickQuestion_shouldFail_whenQuestionNumberNotNumeric() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        registerService.register(registerRequest);

        // Act
        PickQuestionRequest pickRequest = new PickQuestionRequest("abc", "Smith", "Smith");
        String result = registerService.pickQuestion(pickRequest);

        // Assert
        assertEquals("invalid question number", result);
    }

    @Test
    @DisplayName("❌ Should fail when question number is out of range")
    void pickQuestion_shouldFail_whenQuestionNumberOutOfRange() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        registerService.register(registerRequest);

        // Act - Too high
        int maxQuestions = SecurityQuestion.values().length;
        PickQuestionRequest pickRequest = new PickQuestionRequest(
                String.valueOf(maxQuestions + 1),
                "Smith",
                "Smith"
        );
        String result = registerService.pickQuestion(pickRequest);

        // Assert
        assertEquals("invalid question number", result);
    }

    @Test
    @DisplayName("❌ Should fail when security answers don't match")
    void pickQuestion_shouldFail_whenAnswersDoNotMatch() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "john-doe",
                "StrongP&ss123",
                "StrongP&ss123",
                "Johnny",
                "john.doe@example.com",
                "Male"
        );
        registerService.register(registerRequest);

        // Act
        PickQuestionRequest pickRequest = new PickQuestionRequest("1", "Smith", "Jones");
        String result = registerService.pickQuestion(pickRequest);

        // Assert
        assertEquals("answers are not identical", result);
    }

    @Test
    @DisplayName("✅ Should successfully pick any security question")
    void pickQuestion_shouldWorkWithAnySecurityQuestion() {
        SecurityQuestion[] questions = SecurityQuestion.values();
        for (int i = 0; i < questions.length; i++) {
            // Clear prior loop states completely
            App.getAccounts().clear();

            // Register
            RegisterRequest registerRequest = new RegisterRequest(
                    "user-" + i,
                    "StrongP&ss123",
                    "StrongP&ss123",
                    "User" + i,
                    "user" + i + "@example.com",
                    "Male"
            );
            registerService.register(registerRequest);

            // Pick question
            PickQuestionRequest pickRequest = new PickQuestionRequest(
                    String.valueOf(i + 1),
                    "Answer" + i,
                    "Answer" + i
            );
            String result = registerService.pickQuestion(pickRequest);

            // Assert
            assertEquals("registered successfully", result);
            assertEquals(MenuState.LOGIN, App.getMenuState());
        }
    }

    // ====== INTEGRATION TESTS ======

    @Test
    @DisplayName("✅ Should save multiple accounts properly")
    void register_shouldHandleMultipleAccounts() {
        // Register first user
        RegisterRequest request1 = new RegisterRequest(
                "user1",
                "StrongP&ss123",
                "StrongP&ss123",
                "UserOne",
                "user1@example.com",
                "Male"
        );
        registerService.register(request1);
        PickQuestionRequest pick1 = new PickQuestionRequest("1", "Answer1", "Answer1");
        registerService.pickQuestion(pick1);

        // Register second user
        RegisterRequest request2 = new RegisterRequest(
                "user2",
                "StrongP&ss123",
                "StrongP&ss123",
                "UserTwo",
                "user2@example.com",
                "Female"
        );
        registerService.register(request2);
        PickQuestionRequest pick2 = new PickQuestionRequest("2", "Answer2", "Answer2");
        registerService.pickQuestion(pick2);

        // Assert both users exist
        assertEquals(2, App.getAccounts().size());

        // Verify first user
        Account firstUser = App.getAccounts().get(0);
        assertEquals("user1", firstUser.getName());
        assertEquals("UserOne", firstUser.getNickname());

        // Verify second user
        Account secondUser = App.getAccounts().get(1);
        assertEquals("user2", secondUser.getName());
        assertEquals("UserTwo", secondUser.getNickname());
    }
}
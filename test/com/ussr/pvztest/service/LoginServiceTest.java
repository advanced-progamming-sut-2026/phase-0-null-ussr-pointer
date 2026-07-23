package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.dto.AnswerRequest;
import com.ussr.pvz.model.dto.ForgetPasswordRequest;
import com.ussr.pvz.model.dto.LoginRequest;
import com.ussr.pvz.model.util.SecurityUtil;
import com.ussr.pvz.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {

    private LoginService loginService;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Reset static App state to guarantee test isolation
        App.setMenuState(MenuState.LOGIN);
        App.getAccounts().clear();
        App.login(null);
        loginService = new LoginService();

        // Seed a valid account directly into the App state
        AccountState state = new AccountState(
                "testuser",
                "TestUser",
                SecurityUtil.hashPassword("StrongP&ss123"),
                "test@example.com",
                null, // Gender
                3,    // Difficulty
                null, // SecurityQuestion (null for testing)
                "Pizza",
                1, 1, 0, 0, 0, 0, 0,
                new java.util.HashMap<>(),
                new java.util.ArrayList<>(),
                new java.util.ArrayList<>(),
                null, null, 0,
                new java.util.HashMap<>(),
                new java.util.ArrayList<>(),
                new java.util.HashMap<>(),
                System.currentTimeMillis(),System.currentTimeMillis(),new ArrayList<>()
        );
        testAccount = new Account(state, null);
        App.addAccount(testAccount);
    }

    // ====== STANDARD LOGIN TESTS ======

    @Test
    @DisplayName("✅ Should login successfully with valid credentials")
    void login_shouldSucceed_whenCredentialsValid() {
        // Act
        LoginRequest request = new LoginRequest("testuser", "StrongP&ss123", false);
        String result = loginService.login(request);

        // Assert
        assertEquals("logged in successfully", result);
        assertEquals(testAccount, App.getAccount());
    }

    @Test
    @DisplayName("❌ Should fail login when username does not exist")
    void login_shouldFail_whenUsernameNotFound() {
        // Act
        LoginRequest request = new LoginRequest("unknownuser", "StrongP&ss123", false);
        String result = loginService.login(request);

        // Assert
        assertEquals("username not found", result);
        assertNull(App.getAccount());
    }

    @Test
    @DisplayName("❌ Should fail login when password is incorrect")
    void login_shouldFail_whenPasswordIncorrect() {
        // Act
        LoginRequest request = new LoginRequest("testuser", "WrongPass123!", false);
        String result = loginService.login(request);

        // Assert
        assertEquals("invalid password", result);
        assertNull(App.getAccount());
    }

    // ====== FORGET PASSWORD INITIALIZATION TESTS ======

    @Test
    @DisplayName("✅ Should initiate forget password flow and return security question")
    void forgetPassword_shouldReturnSecurityQuestion_whenValid() {
        // Act
        ForgetPasswordRequest request = new ForgetPasswordRequest("testuser", "test@example.com");
        String result = loginService.forgetPassword(request);

        // Assert
        assertTrue(result.contains("security question:"));
    }

    @Test
    @DisplayName("❌ Should fail forget password when username not found")
    void forgetPassword_shouldFail_whenUsernameNotFound() {
        // Act
        ForgetPasswordRequest request = new ForgetPasswordRequest("unknownuser", "test@example.com");
        String result = loginService.forgetPassword(request);

        // Assert
        assertEquals("username not found", result);
    }

    @Test
    @DisplayName("❌ Should fail forget password when email does not match")
    void forgetPassword_shouldFail_whenEmailIncorrect() {
        // Act
        ForgetPasswordRequest request = new ForgetPasswordRequest("testuser", "wrong@example.com");
        String result = loginService.forgetPassword(request);

        // Assert
        assertEquals("invalid email", result);
    }

    // ====== SECURITY QUESTION ANSWER TESTS ======

    @Test
    @DisplayName("✅ Should accept correct security answer and prompt for new password")
    void answer_shouldPromptNewPassword_whenAnswerCorrect() {
        // Arrange
        loginService.forgetPassword(new ForgetPasswordRequest("testuser", "test@example.com"));

        // Act
        AnswerRequest request = new AnswerRequest("Pizza");
        String result = loginService.answer(request);

        // Assert
        assertEquals("Enter your new password:", result);
    }

    @Test
    @DisplayName("❌ Should fail answer when no active password reset session exists")
    void answer_shouldFail_whenNoActiveReset() {
        // Act - No forgetPassword triggered prior
        AnswerRequest request = new AnswerRequest("Pizza");
        String result = loginService.answer(request);

        // Assert
        assertEquals("no active password reset", result);
    }

    @Test
    @DisplayName("❌ Should fail answer when security answer is wrong")
    void answer_shouldFail_whenAnswerWrong() {
        // Arrange
        loginService.forgetPassword(new ForgetPasswordRequest("testuser", "test@example.com"));

        // Act
        AnswerRequest request = new AnswerRequest("Burger");
        String result = loginService.answer(request);

        // Assert
        assertEquals("wrong answer", result);
    }

    // ====== RESET PASSWORD EXECUTION TESTS ======

    @Test
    @DisplayName("✅ Should successfully reset and hash the new password")
    void resetPassword_shouldUpdateAndHashPassword_whenValid() {
        // Arrange
        loginService.forgetPassword(new ForgetPasswordRequest("testuser", "test@example.com"));
        loginService.answer(new AnswerRequest("Pizza"));

        // Act
        String result = loginService.resetPassword("NewStr0ngP&ss!");

        // Assert
        assertEquals("Your password updated successfully now you can login to the game with your fresh password!", result);

        // Verify the new password grants access (ensuring it was hashed properly)
        LoginRequest loginRequest = new LoginRequest("testuser", "NewStr0ngP&ss!", false);
        assertEquals("logged in successfully", loginService.login(loginRequest));
    }

    @Test
    @DisplayName("❌ Should fail reset password if not actively waiting for new password")
    void resetPassword_shouldFail_whenNotWaitingForPassword() {
        // Act
        String result = loginService.resetPassword("NewStr0ngP&ss!");

        // Assert
        assertEquals("Invalid Command", result);
    }

    @ParameterizedTest
    @DisplayName("❌ Should fail reset password with invalid password format restrictions")
    @ValueSource(strings = {
            "short!",         // Length < 8
            "NOLOWERCASE1!",  // No lowercase
            "nouppercase1!",  // No uppercase
            "NoNumberHere!",  // No number
            "NoSpecialChar1"  // No special character
    })
    void resetPassword_shouldFail_whenPasswordFormatInvalid(String invalidPassword) {
        // Arrange
        loginService.forgetPassword(new ForgetPasswordRequest("testuser", "test@example.com"));
        loginService.answer(new AnswerRequest("Pizza"));

        // Act
        String result = loginService.resetPassword(invalidPassword);

        // Assert
        assertNotEquals("Your password updated successfully now you can login to the game with your fresh password!", result);
    }
}
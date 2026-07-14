package com.ussr.pvztest.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.dto.ChangeEmailRequest;
import com.ussr.pvz.model.dto.ChangeNicknameRequest;
import com.ussr.pvz.model.dto.ChangePasswordRequest;
import com.ussr.pvz.model.dto.ChangeUsernameRequest;
import com.ussr.pvz.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileServiceTest {

    private ProfileService profileService;
    private Account activeAccount;

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        App.login(null);
        profileService = new ProfileService();

        AccountState state = new AccountState(
                "main-user", "MainNick", "ValidP&ss1", "user@example.com", Gender.MALE, 3,
                null, null, 1, 1, 0, 0, 0, 0, 0,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );
        activeAccount = new Account(state, null);
        App.addAccount(activeAccount);
        App.login(activeAccount);
    }

    // ====== USERNAME TESTS ======

    @Test
    @DisplayName("✅ Should successfully change username when valid and unique")
    void changeUsername_shouldSucceed_whenValidAndUnique() {
        ChangeUsernameRequest request = new ChangeUsernameRequest("new-username-123");
        String result = profileService.changeUsername(request);

        assertEquals("username changed successfully", result);
        assertEquals("new-username-123", activeAccount.getName());
    }

    @Test
    @DisplayName("❌ Should fail to change username when already exists")
    void changeUsername_shouldFail_whenAlreadyExists() {
        AccountState otherState = new AccountState(
                "existing-user", "OtherNick", "pass", "other@example.com", Gender.FEMALE, 3,
                null, null, 1, 1, 0, 0, 0, 0, 0,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );
        App.addAccount(new Account(otherState, null));

        ChangeUsernameRequest request = new ChangeUsernameRequest("existing-user");
        String result = profileService.changeUsername(request);

        assertEquals("username already exists", result);
        assertEquals("main-user", activeAccount.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@name", "user name", "user!"})
    @DisplayName("❌ Should fail to change username when regex invalid")
    void changeUsername_shouldFail_whenInvalidRegex(String invalidUsername) {
        ChangeUsernameRequest request = new ChangeUsernameRequest(invalidUsername);
        String result = profileService.changeUsername(request);

        assertEquals("invalid username", result);
        assertEquals("main-user", activeAccount.getName());
    }

    // ====== NICKNAME TESTS ======

    @Test
    @DisplayName("✅ Should successfully change nickname when valid")
    void changeNickname_shouldSucceed_whenValid() {
        ChangeNicknameRequest request = new ChangeNicknameRequest("NewNickName");
        String result = profileService.changeNickname(request);

        assertEquals("nickname changed successfully", result);
        assertEquals("NewNickName", activeAccount.getNickname());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ab", "ThisNicknameIsWayTooLongToBeValidCheck"})
    @DisplayName("❌ Should fail to change nickname when length invalid")
    void changeNickname_shouldFail_whenLengthInvalid(String invalidNickname) {
        ChangeNicknameRequest request = new ChangeNicknameRequest(invalidNickname);
        String result = profileService.changeNickname(request);

        assertEquals("invalid nickname length", result);
        assertEquals("MainNick", activeAccount.getNickname());
    }

    // ====== EMAIL TESTS ======

    @Test
    @DisplayName("✅ Should successfully change email when format valid")
    void changeEmail_shouldSucceed_whenValid() {
        ChangeEmailRequest request = new ChangeEmailRequest("new.email@example.co.uk");
        String result = profileService.changeEmail(request);

        assertEquals("email changed successfully", result);
        assertEquals("new.email@example.co.uk", activeAccount.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@.com", "test@domain"})
    @DisplayName("❌ Should fail to change email when format invalid")
    void changeEmail_shouldFail_whenFormatInvalid(String invalidEmail) {
        ChangeEmailRequest request = new ChangeEmailRequest(invalidEmail);
        String result = profileService.changeEmail(request);

        assertEquals("invalid email format", result);
        assertEquals("user@example.com", activeAccount.getEmail());
    }

    // ====== PASSWORD TESTS ======

    @Test
    @DisplayName("✅ Should successfully change password when requirements met")
    void changePassword_shouldSucceed_whenValid() {
        ChangePasswordRequest request = new ChangePasswordRequest("NewSt&ong123", "ValidP&ss1");
        String result = profileService.changePassword(request);

        assertEquals("password changed successfully", result);
        assertEquals("NewSt&ong123", activeAccount.getPassword());
    }

    @Test
    @DisplayName("❌ Should fail to change password when old password incorrect")
    void changePassword_shouldFail_whenOldPasswordIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest("NewSt&ong123", "WrongPass123!");
        String result = profileService.changePassword(request);

        assertEquals("old password is incorrect", result);
        assertEquals("ValidP&ss1", activeAccount.getPassword());
    }

    @Test
    @DisplayName("❌ Should fail to change password when length is too short")
    void changePassword_shouldFail_whenLengthTooShort() {
        ChangePasswordRequest request = new ChangePasswordRequest("S&1rt", "ValidP&ss1");
        String result = profileService.changePassword(request);

        assertEquals("invalid password length", result);
    }

    @Test
    @DisplayName("❌ Should fail to change password when missing lowercase")
    void changePassword_shouldFail_whenMissingLowercase() {
        ChangePasswordRequest request = new ChangePasswordRequest("NOLOWER&123", "ValidP&ss1");
        String result = profileService.changePassword(request);

        assertEquals("password must contain a lowercase character", result);
    }

    @Test
    @DisplayName("❌ Should fail to change password when missing uppercase")
    void changePassword_shouldFail_whenMissingUppercase() {
        ChangePasswordRequest request = new ChangePasswordRequest("noupper&123", "ValidP&ss1");
        String result = profileService.changePassword(request);

        assertEquals("password must contain an uppercase character", result);
    }

    @Test
    @DisplayName("❌ Should fail to change password when missing number")
    void changePassword_shouldFail_whenMissingNumber() {
        ChangePasswordRequest request = new ChangePasswordRequest("NoNumber&Here", "ValidP&ss1");
        String result = profileService.changePassword(request);

        assertEquals("password must contain a number", result);
    }

    @Test
    @DisplayName("❌ Should fail to change password when missing special char")
    void changePassword_shouldFail_whenMissingSpecialChar() {
        ChangePasswordRequest request = new ChangePasswordRequest("NoSpecial123", "ValidP&ss1");
        String result = profileService.changePassword(request);

        assertEquals("password must contain a specific character", result);
    }

    // ====== INFO TESTS ======

    @Test
    @DisplayName("✅ Should format info block correctly")
    void showInfo_shouldReturnFormattedString() {
        String result = profileService.showInfo();
        String expected = "username: main-user\nnickname: MainNick\nemail: user@example.com\ngender: male";

        assertEquals(expected, result);
    }
}
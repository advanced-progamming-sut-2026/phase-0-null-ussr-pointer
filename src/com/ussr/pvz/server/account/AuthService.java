package com.ussr.pvz.server.account;

public class AuthService {

    private final AccountRepository accountRepository;
    private final ServerSessionManager sessionManager;

    private final LoginService loginService;
    private final RegisterService registerService;
    private final ProfileService profileService;

    public AuthService(
            AccountRepository accountRepository,
            ServerSessionManager sessionManager
    ) {
        this.accountRepository = accountRepository;
        this.sessionManager = sessionManager;

        this.loginService = new LoginService(accountRepository, sessionManager);
        this.registerService = new RegisterService(accountRepository);
        this.profileService = new ProfileService(accountRepository, sessionManager);
    }

    public LoginService getLoginService() { return loginService; }
    public RegisterService getRegisterService() { return registerService; }
    public ProfileService getProfileService() { return profileService; }
    public AccountRepository getAccountRepository() { return accountRepository; }
    public ServerSessionManager getSessionManager() { return sessionManager; }
}
package response;

import models.AuthData;

public record LoginResponse(String username, String authToken) {
    public static LoginResponse fromAuth(AuthData authData) {
        return new LoginResponse(authData.username(), authData.authToken());
    }

    public AuthData getAuthData() {
        return new AuthData(authToken, username);
    }
}

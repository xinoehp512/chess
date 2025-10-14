package dataaccess;

import models.AuthData;
import models.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuthDAO {
    private final List<AuthData> table = new ArrayList<>();
    public AuthData getAuth(String authToken) {
        for (var data : table) {
            if (Objects.equals(authToken,data.authToken())) {
                return data;
            }
        }
        return null;
    }
    public void insertAuth(AuthData authData) {
        table.add(authData);
    }
}

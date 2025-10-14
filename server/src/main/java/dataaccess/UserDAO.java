package dataaccess;

import models.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDAO {
    private final List<UserData> table = new ArrayList<>();
    public UserData getUser(String username) {
        for (var data : table) {
            if (Objects.equals(username,data.username())) {
                return data;
            }
        }
        return null;
    }

    public void insertUser(UserData userData) throws DataAccessException {
        if (getUser(userData.username())!=null) {
            throw new DataAccessException("User "+userData.username()+" already exists!");
        }
        table.add(userData);
    }

    public void clear() {
        table.clear();
    }
}

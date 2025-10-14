package dataaccess;

import models.AuthData;
import models.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface AuthDAO {

    public AuthData getAuth(String authToken);

    public void insertAuth(AuthData authData);

    public void deleteAuth(String authToken) throws DataAccessException;

    public void clear();
}

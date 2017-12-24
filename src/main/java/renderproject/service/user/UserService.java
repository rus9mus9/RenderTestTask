package renderproject.service.user;

import renderproject.model.User;

public interface UserService
{
    User createNewUser(User user);
    User getClient(int userId);
    User getClientByEmailPassword(String email, String password);
    boolean isClientExist(String email);
}

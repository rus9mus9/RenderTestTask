package renderproject.repository.user;

import renderproject.model.User;

public interface UserRepo
{
    User createNewUser(User user);
    User getUser(int userId);
    User getClientByEmailPassword(String email, String password);
    boolean isClientExist(String email);
}

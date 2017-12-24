package renderproject.service.user;

import org.springframework.stereotype.Service;
import renderproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import renderproject.repository.user.UserRepo;

@Service
public class UserServiceImpl implements UserService
{
    private final UserRepo userRepo;

    @Autowired
    public UserServiceImpl(UserRepo userRepo)
    {
        this.userRepo = userRepo;
    }


    public User createNewUser(User user)
    {
        return userRepo.createNewUser(user);
    }

    public User getClient(int userId)
    {
        return userRepo.getUser(userId);
    }

    public User getClientByEmailPassword(String email, String password)
    {
        return userRepo.getClientByEmailPassword(email, password);
    }

    @Override
    public boolean isClientExist(String email)
    {
        return userRepo.isClientExist(email);
    }

}

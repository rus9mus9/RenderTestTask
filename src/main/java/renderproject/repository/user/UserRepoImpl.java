package renderproject.repository.user;

import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import renderproject.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;

@Repository
public class UserRepoImpl implements UserRepo
{
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public User createNewUser(User user)
    {
        em.persist(user);
        return user;
    }

    public User getUser(int userId)
    {
        return em.find(User.class, userId);
    }

    public User getClientByEmailPassword(String email, String password)
    {
        User user;
        try
        {
        user =  em.createNamedQuery(User.GET_BY_EMAIL_PASSWORD, User.class)
               .setParameter("userEmail", email)
               .setParameter("userPassword", password).getSingleResult();
        return user;
        }
        catch (NoResultException e)
        {

        }
        return null;
    }


    @Override
    public boolean isClientExist(String email)
    {
        try
        {
            em.createNamedQuery(User.DOES_EMAIL_EXIST, User.class)
                    .setParameter("userEmail", email).getSingleResult();
            return true;
        }
        catch (NoResultException e)
        {

        }
        return false;
    }
}

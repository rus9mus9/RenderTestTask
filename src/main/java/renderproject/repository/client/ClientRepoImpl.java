package renderproject.repository.client;

import org.springframework.transaction.annotation.Transactional;
import renderproject.model.Client;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class ClientRepoImpl implements ClientRepo
{
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Client createNewUser(Client client)
    {
        em.persist(client);
        return client;
    }

    public Client getUser(int userId)
    {
        return em.find(Client.class, userId);
    }

    public Client getClientByEmailPassword(String email, String password)
    {
        Client client;
        try
        {
        client =  em.createNamedQuery(Client.GET_BY_EMAIL_PASSWORD, Client.class)
               .setParameter("clientEmail", email)
               .setParameter("password", password).getSingleResult();
        return client;
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
            em.createNamedQuery(Client.DOES_EMAIL_EXIST, Client.class)
                    .setParameter("clientEmail", email).getSingleResult();
            return true;
        }
        catch (NoResultException e)
        {

        }
        return false;
    }
}

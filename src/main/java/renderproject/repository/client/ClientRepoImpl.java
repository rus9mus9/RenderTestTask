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
        return em.createNamedQuery(Client.GET_BY_EMAIL_PASSWORD, Client.class)
               .setParameter("clientEmail", email)
               .setParameter("password", password).getSingleResult();
    }
}

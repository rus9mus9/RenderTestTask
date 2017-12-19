package renderproject.repository.client;

import renderproject.model.Client;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class ClientRepoImpl implements ClientRepo
{
    @PersistenceContext
    private EntityManager em;

    public Client createNewUser(Client client)
    {
        em.persist(client);
        return client;
    }

    public Client getUser(int userId)
    {
        return em.find(Client.class, userId);
    }

    public Client getUserByEmailPassword(String email)
    {
        return em.createNamedQuery(Client.GET_BY_EMAIL, Client.class)
                .setParameter("clientEmail", email).getSingleResult();
    }
}

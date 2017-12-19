package renderproject.service.client;

import renderproject.AuthorizedClient;
import renderproject.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import renderproject.repository.client.ClientRepo;

@Service("clientService")
public class ClientServiceImpl implements ClientService, UserDetailsService
{
    private final ClientRepo clientRepo;

    @Autowired
    public ClientServiceImpl(ClientRepo clientRepo)
    {
        this.clientRepo = clientRepo;
    }


    public Client createNewUser(Client client)
    {
        return clientRepo.createNewUser(client);
    }

    public Client getUser(int userId)
    {
        return clientRepo.getUser(userId);
    }

    public Client getUserByEmail(String email)
    {
        return clientRepo.getUserByEmailPassword(email);
    }

    public AuthorizedClient loadUserByUsername(String email) throws UsernameNotFoundException
    {
       Client client = clientRepo.getUserByEmailPassword(email.toLowerCase());
       return client == null ? null : new AuthorizedClient(client);
    }
}

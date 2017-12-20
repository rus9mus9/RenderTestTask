package renderproject.service.client;

import org.springframework.stereotype.Service;
import renderproject.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import renderproject.repository.client.ClientRepo;

@Service
public class ClientServiceImpl implements ClientService
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

    public Client getClient(int userId)
    {
        return clientRepo.getUser(userId);
    }

    public Client getClientByEmailPassword(String email, String password)
    {
        return clientRepo.getClientByEmailPassword(email, password);
    }

}

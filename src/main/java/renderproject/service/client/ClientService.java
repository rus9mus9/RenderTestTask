package renderproject.service.client;

import renderproject.model.Client;
import renderproject.model.RenderingStatus;

public interface ClientService
{
    Client createNewUser(Client client);
    Client getClient(int userId);
    Client getClientByEmailPassword(String email, String password);
}

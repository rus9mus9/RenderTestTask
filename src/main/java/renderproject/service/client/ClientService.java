package renderproject.service.client;

import renderproject.model.Client;
import renderproject.model.RenderingStatus;

public interface ClientService
{
    Client createNewUser(Client client);
    Client getUser(int userId);
    Client getUserByEmail(String email);
}

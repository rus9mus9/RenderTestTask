package renderproject.repository.client;

import renderproject.model.Client;

public interface ClientRepo
{
    Client createNewUser(Client client);
    Client getUser(int userId);
    Client getUserByEmailPassword(String email);
}

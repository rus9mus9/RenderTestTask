package renderproject;

import renderproject.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class AuthorizedClient extends org.springframework.security.core.userdetails.User
{
    private static final long serialVersionUID = 1L;

    private Client client;

    public AuthorizedClient(Client client)
    {
        super(client.getEmail(), client.getPassword(), null);
        this.client = client;
    }

    public static AuthorizedClient safeGet() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return (principal instanceof AuthorizedClient) ? (AuthorizedClient) principal : null;
    }

    public static AuthorizedClient get() {
        AuthorizedClient client = safeGet();
        return client;
    }

    public int getId() {
        return client.getId();
    }

    public Client getClient()
    {
        return client;
    }
    public static int id() {
        return get().client.getId();
    }

}

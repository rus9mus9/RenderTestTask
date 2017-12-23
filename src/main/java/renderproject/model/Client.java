package renderproject.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
@NamedQueries({
@NamedQuery(name = Client.GET_BY_EMAIL_PASSWORD, query = "SELECT c FROM Client c WHERE c.email=:clientEmail AND c.password=:password"),
@NamedQuery(name = Client.DOES_EMAIL_EXIST, query = "SELECT c FROM Client c WHERE c.email=:clientEmail")
})
@Entity
@Table(name = "render_users", uniqueConstraints = {@UniqueConstraint(columnNames = "email", name = "users_unique_email_idx")})
public class Client
{
    public static final String GET_BY_EMAIL_PASSWORD = "Client.getClientByEmailPassword";
    public static final String DOES_EMAIL_EXIST = "Client.isClientExist";

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true)
    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @Column(name = "password", nullable = false)
    @NotBlank
    @Size(min = 5, max = 64)
    private String password;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client")//, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OrderBy("timeCreated DESC")
    private List<Task> tasks;

    public Client(String email, String password)
    {
    }

    public Client()
    {

    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public List<Task> getTasks()
    {
        return tasks;
    }

    public void setTasks(List<Task> tasks)
    {
        this.tasks = tasks;
    }
}

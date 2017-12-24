package renderproject.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.util.Date;

@NamedQueries({
@NamedQuery(name = Task.ALL_TASKS, query = "SELECT t FROM Task t WHERE t.user.id=:userId ORDER BY t.timeCreated DESC")
})
@Entity
@Table(name = "users_tasks")
public class Task
{
    public static final String ALL_TASKS = "Task.getAll";

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    private Integer id;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RenderingStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "time_created", nullable = false)
    @NotNull
    private Date timeCreated = new Date();


    public Date getTimeCreated()
    {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated)
    {
        this.timeCreated = timeCreated;
    }
    public User getUser()
    {
        return user;
    }

    @Override
    public String toString()
    {
        return "Task{" +
                "id=" + id +
                ", статус - " + status +
                ", время создания:" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(timeCreated) +
                '}';
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public RenderingStatus getStatus()
    {
        return status;
    }

    public void setStatus(RenderingStatus status)
    {
        this.status = status;
    }

    public int getTask_id()
    {
        return id;
    }

    public void setTask_id(int task_id)
    {
        this.id = task_id;
    }
}

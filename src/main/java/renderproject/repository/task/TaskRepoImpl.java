package renderproject.repository.task;

import renderproject.model.Client;
import renderproject.model.RenderingStatus;
import renderproject.model.Task;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class TaskRepoImpl implements TaskRepo
{
    @PersistenceContext
    private EntityManager em;

    public Task createTask(Task task, int userId)
    {
        task.setClient(em.getReference(Client.class, userId));
        em.persist(task);
        return task;
    }

    public List<Task> getTasksForUser(int userId)
    {
        return em.createNamedQuery(Task.ALL_TASKS, Task.class)
                .setParameter("userId", userId).getResultList();
    }

    public RenderingStatus getRenderingStatus(int taskId, int userId)
    {
        Task task = em.find(Task.class, taskId);
        return task.getClient().getId() == userId ? task.getStatus() : null;
    }
}

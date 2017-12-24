package renderproject.repository.task;

import org.springframework.transaction.annotation.Transactional;
import renderproject.model.RenderingStatus;
import renderproject.model.User;
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

    @Transactional
    public Task createTask(Task task, int userId)
    {
        task.setUser(em.getReference(User.class, userId));
        em.persist(task);
        return task;
    }

    @Override
    @Transactional
    public Task update(Task task, int userId)
    {
        if(getTaskById(task.getTask_id(), userId) == null)
        {
            return null;
        }
        task.setUser(em.getReference(User.class, userId));
        return em.merge(task);
    }

    public List<Task> getTasksForUser(int userId)
    {
        return em.createNamedQuery(Task.ALL_TASKS, Task.class)
                .setParameter("userId", userId).getResultList();
    }

    public Task getTaskById(int taskId, int userId)
    {
        try
        {
            Task task = em.find(Task.class, taskId);
            return task.getUser().getId() == userId ? task : null;
        }
        catch (NullPointerException e)
        {

        }
        return null;
    }

}

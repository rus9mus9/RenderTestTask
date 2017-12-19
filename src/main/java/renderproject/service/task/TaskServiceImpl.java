package renderproject.service.task;

import renderproject.model.Client;
import renderproject.model.RenderingStatus;
import renderproject.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import renderproject.repository.task.TaskRepo;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService
{
    private final TaskRepo taskRepo;

    @Autowired
    public TaskServiceImpl(TaskRepo taskRepo)
    {
        this.taskRepo = taskRepo;
    }

    public Task createTask(Task task, int userId)
    {
        return taskRepo.createTask(task, userId);
    }

    public List<Task> getTasksForUser(int userId)
    {
        return taskRepo.getTasksForUser(userId);
    }

    public RenderingStatus getRenderingStatus(int taskId, int userId)
    {
        return taskRepo.getRenderingStatus(taskId, userId);
    }
}

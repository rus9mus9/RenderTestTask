package renderproject.service.task;

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

    public Task getTaskById(int taskId, int userId)
    {
        return taskRepo.getTaskById(taskId, userId);
    }

    @Override
    public Task update(Task task, int userId)
    {
        return taskRepo.update(task, userId);
    }
}

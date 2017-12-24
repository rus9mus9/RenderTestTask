package renderproject.repository.task;

import renderproject.model.RenderingStatus;
import renderproject.model.Task;

import java.util.List;

public interface TaskRepo
{
    Task createTask(Task task, int userId);
    Task update(Task task, int userId);
    List<Task> getTasksForUser(int userId);
    Task getTaskById(int taskId, int userId);
}

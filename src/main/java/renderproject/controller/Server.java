package renderproject.controller;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;
import renderproject.model.User;
import renderproject.model.RenderingStatus;
import renderproject.model.Task;
import renderproject.service.user.UserService;
import renderproject.service.task.TaskService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

@Component
public class Server
{
    private static final Map<Integer, HashMap<Integer, String>> commandMap = new HashMap<>();
    private static final Map<Integer, String> initialCommands = new HashMap<>();
    private static final Map<Integer, String> userCommands = new HashMap<>();

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    static
    {
        initialCommands.put(1, "Вход");
        initialCommands.put(2, "Регистрация");
        initialCommands.put(3, "Выход из программы");

        userCommands.put(1, "Создать новую задачу");
        userCommands.put(2, "Получить список задач");
        userCommands.put(3, "Получить статус задачи");
        userCommands.put(4, "Выйти в главное меню");

        commandMap.put(1, (HashMap<Integer, String>) initialCommands);
        commandMap.put(2, (HashMap<Integer, String>) userCommands);
    }

    private static JSONObject initialCommandsJSON = generateJSONObjectInitialCommands();

    private static JSONObject loggedUserCommandsJSON = generateJSONObjectLoggedUserCommands();

    private static JSONObject greetingJSONObject = new JSONObject().put("greetingMessage", "Добро пожаловать, выберите дальнейшее действие: ");

    private static JSONObject authorizeActionJSONObject = new JSONObject().put("action", "login");

    private static JSONObject registerActionJSONObject = new JSONObject().put("action", "register");

    private static JSONObject goodByeJSONObject = new JSONObject().put("action", "exit");

    private static JSONObject badCredentialsJSONObject = new JSONObject().put("authorizeResult", "bad credentials");

    private static JSONObject successCredentialsJSONObject = new JSONObject().put("authorizeResult", "success");

    private static JSONObject userDoesntExitJSONObject = new JSONObject().put("authorizeResult", "user doesn't exit");

    private static JSONObject newTaskJSONObject = new JSONObject().put("taskRequest", "new task created");

    private static JSONObject getAllTasksJSONObject = new JSONObject().put("taskRequest", "get all tasks");

    private static JSONObject getStatusForOneTaskJSONObject = new JSONObject().put("taskRequest", "get status for one");

    private static JSONObject goToMainMenuJSONObject = new JSONObject().put("taskRequest", "mainMenu");

    private static JSONObject generateJSONObjectInitialCommands()
    {
        JSONObject initialCommandObject = new JSONObject();
        JSONArray initialCommands = new JSONArray();

        for(Map.Entry entry: commandMap.get(1).entrySet())
        {
            initialCommands.put(entry.getKey().toString() + " - " + entry.getValue().toString());
        }

        initialCommandObject.put("initialCommands", initialCommands);
        return initialCommandObject;
    }

    private static JSONObject generateJSONObjectLoggedUserCommands()
    {
        JSONObject loggedUserCommandsObject = new JSONObject();
        JSONArray loggedUserCommands = new JSONArray();

        for (Map.Entry entry : commandMap.get(2).entrySet())
        {
            loggedUserCommands.put(entry.getKey().toString() + " - " + entry.getValue().toString());
        }
        loggedUserCommandsObject.put("loggedUserCommands", loggedUserCommands);
        return loggedUserCommandsObject;
    }

    private void startHandler(Socket socket) throws Exception
    {

        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                    try
                    {
                        BufferedReader inputFromUser = new BufferedReader(new InputStreamReader(socket.getInputStream())); // <- To get something from user
                        OutputStreamWriter outputToUser = new OutputStreamWriter(socket.getOutputStream());  // <- To send something to the user

                        outputToUser.write(greetingJSONObject.toString() + "\n");
                        outputToUser.flush();

                        while(true)
                        {
                        outputToUser.write(initialCommandsJSON.toString() + "\n");
                        outputToUser.flush();

                        int codeFromUser = Integer.parseInt(inputFromUser.readLine());

                        if(codeFromUser == 1)
                        {
                            outputToUser.write(authorizeActionJSONObject.toString() + "\n");
                            outputToUser.flush();

                            JSONObject userCredentials = new JSONObject(inputFromUser.readLine());

                            String email = userCredentials.getJSONObject("userCredentials").get("email").toString();
                            String password = userCredentials.getJSONObject("userCredentials").get("password").toString();

                            if(!isEmailExist(email))
                            {
                                outputToUser.write(userDoesntExitJSONObject.toString() + "\n");
                                outputToUser.flush();
                            }
                            else
                            {
                                User authorizedUser = tryLogin(email, password);

                                if(authorizedUser != null)
                                {
                                    successCredentialsJSONObject.put("userEmail", authorizedUser.getEmail());
                                    outputToUser.write(successCredentialsJSONObject.toString() + "\n");
                                    outputToUser.flush();

                                    while(true)
                                    {
                                    outputToUser.write(loggedUserCommandsJSON.toString() + "\n");
                                    outputToUser.flush();

                                    codeFromUser = Integer.parseInt(inputFromUser.readLine());

                                    if(codeFromUser == 1)
                                    {
                                        Task task = new Task();
                                        task.setStatus(RenderingStatus.RENDERING);
                                        taskService.createTask(task, authorizedUser.getId());
                                        newTaskJSONObject.put("taskId", task.getTask_id());
                                        outputToUser.write(newTaskJSONObject.toString() + "\n");
                                        outputToUser.flush();
                                    }

                                    else if(codeFromUser == 2)
                                    {
                                        JSONObject allUsersTasksObject = new JSONObject();
                                        JSONArray allUsersTasksJSONArray = new JSONArray();

                                        for(Task task : taskService.getTasksForUser(authorizedUser.getId()))
                                        {
                                            if((new Date().getTime() / 1000) - (task.getTimeCreated().getTime() / 1000) > 180)
                                            {
                                                task.setStatus(RenderingStatus.COMPLETE);
                                                taskService.update(task, authorizedUser.getId());
                                            }
                                            allUsersTasksJSONArray.put(task);
                                        }
                                        allUsersTasksObject.put("tasks", allUsersTasksJSONArray);
                                        getAllTasksJSONObject.put("tasksForUser", allUsersTasksObject);
                                        outputToUser.write(getAllTasksJSONObject.toString() + "\n");
                                        outputToUser.flush();
                                    }
                                    else if(codeFromUser == 3)
                                    {

                                        outputToUser.write(getStatusForOneTaskJSONObject.toString() + "\n");
                                        outputToUser.flush();

                                        int taskId = Integer.parseInt(inputFromUser.readLine());
                                        Task task = taskService.getTaskById(taskId, authorizedUser.getId());

                                        JSONObject taskRenderingResult = new JSONObject();

                                        if(task != null)
                                        {
                                            if((new Date().getTime() / 1000) - (task.getTimeCreated().getTime() / 1000) > 180)
                                            {
                                                task.setStatus(RenderingStatus.COMPLETE);
                                                taskService.update(task, authorizedUser.getId());
                                            }
                                            taskRenderingResult.put("result", task.getStatus());
                                        }
                                        else
                                        {
                                            taskRenderingResult.put("result", "");
                                        }
                                        getStatusForOneTaskJSONObject.put("taskGettingResult", taskRenderingResult);
                                        outputToUser.write(getStatusForOneTaskJSONObject.toString() + "\n");
                                        outputToUser.flush();
                                    }

                                    else if(codeFromUser == 4)
                                    {
                                        outputToUser.write(goToMainMenuJSONObject.toString() + "\n");
                                        outputToUser.flush();
                                        break;
                                    }
                                    }
                                }
                                else
                                {
                                    outputToUser.write(badCredentialsJSONObject.toString() + "\n");
                                    outputToUser.flush();
                                }
                            }

                        }
                        else if(codeFromUser == 2)
                        {
                            outputToUser.write(registerActionJSONObject.toString() + "\n");
                            outputToUser.flush();
                            JSONObject userCredentials = new JSONObject(inputFromUser.readLine());


                            if(userCredentials.getJSONObject("verifyResult").get("result").equals("success"))
                            {
                                String email = userCredentials.getJSONObject("userCredentials").get("email").toString();
                                String password = userCredentials.getJSONObject("userCredentials").get("password").toString();

                                JSONObject regResult = new JSONObject();

                                if(!userService.isClientExist(email))
                                {
                                    userService.createNewUser(new User(email, password));
                                    regResult.put("regResult", "success");
                                }
                                else
                                {
                                    regResult.put("regResult", "userAlreadyExists");
                                }

                                outputToUser.write(regResult.toString() + "\n");
                                outputToUser.flush();
                            }
                            else if(userCredentials.getJSONObject("verifyResult").get("result").equals("failed"))
                            {
                                JSONObject failed = new JSONObject();
                                failed.put("regResult", "failed");
                                outputToUser.write(failed.toString() + "\n");
                                outputToUser.flush();
                            }
                        }
                        else
                        {
                            outputToUser.write(goodByeJSONObject.toString() + "\n");
                            outputToUser.flush();
                        }

                    }}
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        socket.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
        }
    };
    thread.start();
    }
    private void run() throws Exception
    {
        System.out.println("Server has started successfully");
        ServerSocket s1 = new ServerSocket(5555);
        try
        {
            while (true)
            {
                Socket ss = s1.accept();
                startHandler(ss);
            }
        } finally
        {
            s1.close();
        }

        }


    private User tryLogin(String email, String password) throws Exception
    {
        return userService.getClientByEmailPassword(email, password);
    }

    private boolean isEmailExist(String email)
    {
        return userService.isClientExist(email);
    }

    public static void main(String[] args) throws Exception
    {
        GenericXmlApplicationContext appCtx = new GenericXmlApplicationContext("/spring/spring-config.xml");
        Server server = appCtx.getBean(Server.class);
        server.run();
    }
}



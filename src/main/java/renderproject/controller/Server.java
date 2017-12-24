package renderproject.controller;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;
import renderproject.model.Client;
import renderproject.model.Task;
import renderproject.service.client.ClientService;
import renderproject.service.task.TaskService;

import javax.persistence.NoResultException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Server
{
    private static final Map<Integer, HashMap<Integer, String>> commandMap = new HashMap<Integer, HashMap<Integer, String>>();
    private static final Map<Integer, String> initialCommands = new HashMap<Integer, String>();
    private static final Map<Integer, String> userCommands = new HashMap<Integer, String>();

    @Autowired
    private ClientService clientService;

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

    private static final BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));

    private static JSONObject initialCommandsJSON = generateJSONObjectInitialCommands();

    private static JSONObject loggedUserCommandsJSON = generateJSONObjectLoggedUserCommands();

    private static JSONObject greetingJSONObject = new JSONObject().put("greetingMessage", "Добро пожаловать, выберите дальнейшее действие: ");

    private static JSONObject authorizeActionJSONObject = new JSONObject().put("action", "login");

    private static JSONObject registerActionJSONObject = new JSONObject().put("action", "register");

    private static JSONObject goodByeJSONObject = new JSONObject().put("action", "exit");

    private static JSONObject badCredentialsJSONObject = new JSONObject().put("authorizeResult", "bad credentials");

    private static JSONObject successCredentialsJSONObject = new JSONObject().put("authorizeResult", "success");

    private static JSONObject userDoesntExitJSONObject = new JSONObject().put("authorizeResult", "user doesn't exit");


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
                        //System.out.println(object.toString());


                        outputToUser.write(greetingJSONObject.toString() + "\n");
                        outputToUser.flush();

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
                                Client authorizedClient = tryLogin(email, password);

                                if(authorizedClient != null)
                                {
                                    JSONObject successCredentialsJSONObjectinner = new JSONObject();
                                    successCredentialsJSONObjectinner.put("authorizeResult", "success?email=" + authorizedClient.getEmail());

                                    //System.out.println(successCredentialsJSONObject.getJSONObject("authorizeResult").put("userEmail", authorizedClient.getEmail()));
                                    outputToUser.write(successCredentialsJSONObject.toString() + "\n");
                                    //outputToUser.write("Добро пожаловать " + authorizedClient.getEmail() + "\n");
                                    outputToUser.flush();

                                    outputToUser.write(loggedUserCommandsJSON.toString() + "\n");
                                    outputToUser.flush();

                                    codeFromUser = Integer.parseInt(inputFromUser.readLine());

                                    if(codeFromUser == 1)
                                    {
                                        Task task = new Task();
                                        taskService.createTask(task, authorizedClient.getId());
                                    }
                                }
                                else
                                {
                                    outputToUser.write(badCredentialsJSONObject.toString() + "\n");
                                    //outputToUser.write("Неверная пара логин/пароль. Повторить попытку?" + "\n");
                                    outputToUser.flush();
                                }
                            }



                            //Client authorizedClient = tryLogin(inputFromUser, outputToUser);
                        }
                        else if(codeFromUser == 2)
                        {
                            outputToUser.write(registerActionJSONObject.toString() + "\n");
                            outputToUser.flush();
                        }
                        else
                        {
                            outputToUser.write(goodByeJSONObject.toString() + "\n");
                            outputToUser.flush();
                        }

                    }
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

           /* while(true)
            {
            //generateJSONObjectInitialCommands();

            String userInput = inputStream.readLine();
            int initialCode = Integer.parseInt(userInput);

            if(initialCode == 1)
            {
                Client loggedClient = tryLogin();

                if(loggedClient != null)
                {
                    System.out.println("Привет, " + loggedClient.getEmail() + "! Выбери дальнейшее действие:");

                   // showLoggedUserCommands();
                }
                /*else
                    {
                        continue;
                    }*/
                /*userInput = inputStream.readLine();

                if(Integer.parseInt(userInput) == 1)
                {
                    taskService.createTask(new Task(), client.getId());
                    System.out.println("Задача успешно создана!");
                }

                else if(Integer.parseInt(userInput) == 2)
                {
                    taskService.getTasksForUser(client.getId());
                }*/

            /*else if(initialCode == 2)
            {
               System.out.println("Регистрация нового пользователя");
               List<String> newUserCredentials =  proposeUserInputCredentials();
               System.out.println("Подтвердите пароль");
               String verifyPassword = inputStream.readLine();
               if(!newUserCredentials.get(1).equals(verifyPassword))
               {
                   System.out.println("Введенные пароли не совпадают.");
               }
               else
                   {
                       clientService.createNewUser(new Client(newUserCredentials.get(0), newUserCredentials.get(1)));
                   }
            }
            else if(initialCode == 3)
            {
                System.out.println("Удачи! :)");
                Thread.sleep(3000);
                System.exit(0);
            }*/

        }


    private Client tryLogin(String email, String password) throws Exception
    {
        return clientService.getClientByEmailPassword(email, password);
    }

    public boolean isEmailExist(String email)
    {
        return clientService.isClientExist(email);
    }

    public static void main(String[] args) throws Exception
    {
        GenericXmlApplicationContext appCtx = new GenericXmlApplicationContext("/spring/spring-config.xml");
        Server server = appCtx.getBean(Server.class);
        server.run();
    }
}




/*
public class 123
{
    private static final Map<Integer, HashMap<Integer, String>> commandMap = new HashMap<Integer, HashMap<Integer, String>>();
    private static final Map<Integer, String> initialCommands = new HashMap<Integer, String>();
    private static final Map<Integer, String> userCommands = new HashMap<Integer, String>();

    @Autowired
    private ClientService clientService;

    @Autowired
    private TaskService taskService;

    private static final BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));

    private static final String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

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

    private void showInitialCommandsToUser() throws Exception
    {
        // String [] initialCommands = new String[5];
        JSONObject object = new JSONObject();
        //JSONObject object = new JSONObject();

        JSONArray initialCommands = new JSONArray();

        for(Map.Entry entry: commandMap.get(1).entrySet())
        {
           *//* outputStreamWriter.write(entry.getKey() + " - " + entry.getValue());
            object.put(entry.getKey().toString(), entry.getValue().toString());*//*
            initialCommands.put(entry.getValue().toString());
        }
        object.put("commands", initialCommands);
        System.out.println(object.toString());
        //outputStreamWriter.flush();
    }

    private void showLoggedUserCommands(OutputStreamWriter outputStreamWriter) throws Exception
    {
        for (Map.Entry entry : commandMap.get(2).entrySet())
        {
            outputStreamWriter.write(entry.getKey() + " - " + entry.getValue());
        }
        //outputStreamWriter.flush();
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
                    JSONObject object = new JSONObject();
                    object.put("greetingMessage", "Добро пожаловать, выбери дальнейшее действие: ");
                    //System.out.println(object.toString());
                    outputToUser.write(object.toString() + "\n");
                    outputToUser.flush();
                    //outputToUser.write("Добро пожаловать, выбери дальнейшее действие: \n");
                    //showInitialCommandsToUser(outputToUser);
                *//*String resultFromUser = inputFromUser.readLine();
                System.out.println(resultFromUser);*//*

                    //List<String> commands = new ArrayList<>();
                    //object.put("greetingUserAndInitialCommands", commands);
                *//*int initialCode = Integer.parseInt(inputFromUser.readLine());
                System.out.println(initialCode);
                if(initialCode == 3)
                {
                    outputToUser.write("Удачи!");

                }
*//*
                }
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

           *//* while(true)
            {
            //showInitialCommandsToUser();

            String userInput = inputStream.readLine();
            int initialCode = Integer.parseInt(userInput);

            if(initialCode == 1)
            {
                Client loggedClient = tryLogin();

                if(loggedClient != null)
                {
                    System.out.println("Привет, " + loggedClient.getEmail() + "! Выбери дальнейшее действие:");

                   // showLoggedUserCommands();
                }
                *//*else
                    {
                        continue;
                    }*//*
                *//*userInput = inputStream.readLine();

                if(Integer.parseInt(userInput) == 1)
                {
                    taskService.createTask(new Task(), client.getId());
                    System.out.println("Задача успешно создана!");
                }

                else if(Integer.parseInt(userInput) == 2)
                {
                    taskService.getTasksForUser(client.getId());
                }*//*

            *//*else if(initialCode == 2)
            {
               System.out.println("Регистрация нового пользователя");
               List<String> newUserCredentials =  proposeUserInputCredentials();
               System.out.println("Подтвердите пароль");
               String verifyPassword = inputStream.readLine();
               if(!newUserCredentials.get(1).equals(verifyPassword))
               {
                   System.out.println("Введенные пароли не совпадают.");
               }
               else
                   {
                       clientService.createNewUser(new Client(newUserCredentials.get(0), newUserCredentials.get(1)));
                   }
            }
            else if(initialCode == 3)
            {
                System.out.println("Удачи! :)");
                Thread.sleep(3000);
                System.exit(0);
            }*//*

    }

    private List<String> proposeUserInputCredentials() throws Exception
    {
        List<String> userCredentials = new ArrayList<>();
        System.out.println("Введите адрес электронной почты:");
        String userEmail = inputStream.readLine();
        while(!userEmail.matches(emailRegex))
        {
            System.out.println("Некорректный email. Попробуйте еще раз.");
            userEmail = inputStream.readLine();
        }
        System.out.println("Введите пароль");
        String userPassword = inputStream.readLine();
        userCredentials.add(userEmail);
        userCredentials.add(userPassword);
        return userCredentials;
    }

    private Client tryLogin() throws Exception
    {
        List<String> credentials = proposeUserInputCredentials();
        Client client = null;
        try
        {
            client = clientService.getClientByEmailPassword(credentials.get(0), credentials.get(1));
        } catch (NoResultException e)
        {
            System.out.println("Пользователь не найден и/или неверная пара email-пароль");
        }
        return client;
    }

    public static void main(String[] args) throws Exception
    {
        Server server = new Server();
        server.showInitialCommandsToUser();
        *//*GenericXmlApplicationContext appCtx = new GenericXmlApplicationContext("/spring/spring-config.xml");
        Server server = appCtx.getBean(Server.class);
        server.run();*//*
    }
}*/


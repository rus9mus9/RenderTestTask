package renderproject.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;
import renderproject.model.Client;
import renderproject.service.client.ClientService;
import renderproject.service.task.TaskService;

import javax.persistence.NoResultException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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

    private void showInitialCommands()
    {
        for(Map.Entry entry: commandMap.get(1).entrySet())
        {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

    private void showLoggedUserCommands()
    {
        for (Map.Entry entry : commandMap.get(2).entrySet())
        {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

    private void run()
    {

        try
        {
           // boolean isMainMenu = true;
            //BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));
            //Console console = System.console();
            System.out.println("Добро пожаловать, выбери дальнейшее действие:");

            ServerSocket s1 = new ServerSocket(5555);
            Socket ss = s1.accept();
            Scanner sc = new Scanner(ss.getInputStream());
            while(true)
            {
            showInitialCommands();

            String userInput = inputStream.readLine();
            int initialCode = Integer.parseInt(userInput);

            if(initialCode == 1)
            {
                Client loggedClient = tryLogin();

                if(loggedClient != null)
                {
                    System.out.println("Привет, " + loggedClient.getEmail() + "! Выбери дальнейшее действие:");

                    showLoggedUserCommands();
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

                }

            else if(initialCode == 2)
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
            }

        }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    public static void main(String[] args)
    {
        GenericXmlApplicationContext appCtx = new GenericXmlApplicationContext("/spring/spring-config.xml");
        Server server = appCtx.getBean(Server.class);
        server.run();
    }
}

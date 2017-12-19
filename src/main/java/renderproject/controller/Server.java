package renderproject.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import renderproject.AuthorizedClient;
import renderproject.model.Client;
import renderproject.service.client.ClientService;
import renderproject.service.task.TaskService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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

    private void run()
    {

        try
        {
            boolean isMainMenu = true;
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));
            //Console console = System.console();
            System.out.println("Привет :) Добро пожаловать, выбери дальнейшее действие:");
            for(Map.Entry entry: commandMap.get(1).entrySet())
            {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }

            String userInput = inputStream.readLine();
            int initialCode = Integer.parseInt(userInput);

            if(initialCode == 1)
            {
                System.out.println("Введите адрес электронной почты:");
                String userEmail = inputStream.readLine();
                System.out.println("Введите пароль");
                String userPassword = inputStream.readLine();
                /*@AuthenticationPrincipal*/ AuthorizedClient authorizedClient = new AuthorizedClient(new );
                // = SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userEmail, userPassword));
                authorizedClient.getClient();
                //AuthorizedClient client = clientService.getUserByEmailPassword(userEmail);
                //SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userEmail, userPassword));
            }
            else if(initialCode == 2)
            {

            }
            else if(initialCode == 3)
            {
                System.out.println("Удачи! :)");
                Thread.sleep(3000);
                System.exit(0);
            }

            while (Integer.parseInt(userInput)!= 3 && isMainMenu)
            {

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static void main(String[] args)
    {
        GenericXmlApplicationContext appCtx = new GenericXmlApplicationContext("/spring/spring-config.xml");
        Server server = appCtx.getBean(Server.class);
        server.run();
        /*ApplicationContext ctx =
                new AnnotationConfigApplicationContext("package"); // Use annotated beans from the specified package

        Main main = ctx.getBean(Main.class);
        main.sampleService.getHelloWorld();*/

    }
}

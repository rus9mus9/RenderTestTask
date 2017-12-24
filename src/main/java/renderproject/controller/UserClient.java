package renderproject.controller;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class UserClient
{

    private static final Scanner userInput = new Scanner(System.in);

    private static final String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    public static void main(String[] args) throws Exception
    {
        //String resultFromServer;
        Socket socket = new Socket("127.0.0.1", 5555);
        BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); // <- To get something from the server
        //System.out.println("test");
       // BufferedWriter outputToServer = new BufferedWriter(new socket.getOutputStream()); // <- To send something to the server
        OutputStreamWriter outputToServer = new OutputStreamWriter(socket.getOutputStream());

        JSONObject greetingObject = new JSONObject(inputFromServer.readLine());
        System.out.println(greetingObject.get("greetingMessage"));

        JSONObject arrayOfInitialCommands = new JSONObject(inputFromServer.readLine());
        printFromJSONArray(arrayOfInitialCommands.getJSONArray("initialCommands"));

        proposeUserInputCode(1, 3, outputToServer);

        JSONObject initialCodeResult  = new JSONObject(inputFromServer.readLine());

        if(initialCodeResult.get("action").equals("login"))
        {
            List<String> credentials = proposeUserInputCredentials();
            JSONObject credentialsJSON = new JSONObject();
            Map<String, String> credentialMap = new HashMap<>();
            credentialMap.put("email", credentials.get(0));
            credentialMap.put("password", credentials.get(1));
            credentialsJSON.put("userCredentials", credentialMap);

            outputToServer.write(credentialsJSON.toString() + "\n");
            outputToServer.flush();

            JSONObject authResultObject = new JSONObject(inputFromServer.readLine());


            if(authResultObject.get("authorizeResult").equals("user doesn't exit"))
            {
                System.out.println("Пользователь с таким email не найден. Желаете зарегистрироваться?");
            }
            else if(authResultObject.get("authorizeResult").equals("bad credentials"))
            {
                System.out.println("Неверная пара логин/пароль. Повторить попытку?");
            }
            else if(authResultObject.get("authorizeResult").equals("success"))
            {
                System.out.println("Добро пожаловать " + authResultObject.get("userEmail") + "!");
                JSONObject arrayOfLoggedUser = new JSONObject(inputFromServer.readLine());
                printFromJSONArray(arrayOfLoggedUser.getJSONArray("loggedUserCommands"));
                proposeUserInputCode(1, 4, outputToServer);

                JSONObject taskRequestObject = new JSONObject(inputFromServer.readLine());

                if(taskRequestObject.get("taskRequest").equals("new task created"))
                {
                    System.out.println("Задача с id=" + taskRequestObject.get("taskId") + " создана!");
                }

                else if(taskRequestObject.get("taskRequest").equals("get all tasks"))
                {
                    printFromJSONArray(taskRequestObject.getJSONObject("tasksForUser").getJSONArray("tasks"));
                }
            }
        }

        else if(initialCodeResult.get("action").equals("register"))
        {
            System.out.println("Регистрация нового пользователя");
            List<String> newUserCredentials =  proposeUserInputCredentials();
            System.out.println("Подтвердите пароль");
            String verifyPassword = userInput.nextLine();
            if(!newUserCredentials.get(1).equals(verifyPassword))
            {
                System.out.println("Введенные пароли не совпадают. Повторить попытку?");
            }
        }
        else if(initialCodeResult.get("action").equals("exit"))
        {
            System.out.println("Удачи! :)");
            Thread.sleep(3000);
            System.exit(0);
        }
    }

    private static List<String> proposeUserInputCredentials() throws Exception
    {
        List<String> userCredentials = new ArrayList<>();
        System.out.println("Введите адрес электронной почты:");
        String userEmail = userInput.nextLine();
        while(!userEmail.matches(emailRegex))
        {
            System.out.println("Некорректный email. Попробуйте еще раз.");
            userEmail = userInput.nextLine();
        }
        System.out.println("Введите пароль");
        String userPassword = userInput.nextLine();
        userCredentials.add(userEmail);
        userCredentials.add(userPassword);
        return userCredentials;
    }

    private static void proposeUserInputCode(int minCode, int maxCode, OutputStreamWriter outputStreamToServer)
    {
        while(true)
        {
            try
            {
                int initialCode = Integer.parseInt(userInput.nextLine());
                if(initialCode >= minCode && initialCode <= maxCode)
                {
                    try
                    {
                        outputStreamToServer.write(initialCode + "\n");
                        outputStreamToServer.flush();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
                else
                {
                    System.out.printf("Введите код от %d до %d \n", minCode, maxCode);
                }
            }

            catch (NumberFormatException e)
            {
                System.out.printf("Пожалуйста, введите число от %d до %d \n",minCode, maxCode);
            }
        }
    }


    private static void printFromJSONArray(JSONArray jsonArray)
    {
        for(Object object : jsonArray)
        {
            System.out.println(object.toString());
        }
    }
}

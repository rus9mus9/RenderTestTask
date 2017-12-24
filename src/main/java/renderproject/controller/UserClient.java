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
        Socket socket = new Socket("127.0.0.1", 5555);
        BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); // <- To get something from the server
        OutputStreamWriter outputToServer = new OutputStreamWriter(socket.getOutputStream()); // <- To send something to the server

        JSONObject greetingObject = new JSONObject(inputFromServer.readLine());
        System.out.println(greetingObject.get("greetingMessage"));

        while(true)
        {
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
                System.out.println("Пользователь с таким email не найден.");
            }
            else if(authResultObject.get("authorizeResult").equals("bad credentials"))
            {
                System.out.println("Неверная пара логин/пароль.");
            }
            else if(authResultObject.get("authorizeResult").equals("success"))
            {
                System.out.println("Добро пожаловать " + authResultObject.get("userEmail") + "!");
                while(true)
                {
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

                else if(taskRequestObject.get("taskRequest").equals("get status for one"))
                {
                    System.out.println("Введите ID задачи");
                    proposeUserInputTaskId(outputToServer);

                    JSONObject result = new JSONObject(inputFromServer.readLine());

                    if(!result.getJSONObject("taskGettingResult").get("result").equals(""))
                    {
                        System.out.println("Статус задачи - " + result.getJSONObject("taskGettingResult").get("result"));
                    }
                    else
                        {
                            System.out.println("Задача с таким ID не найдена");
                        }
                }

                else if(taskRequestObject.get("taskRequest").equals("mainMenu"))
                {
                    break;
                }
                }
            }
        }

        else if(initialCodeResult.get("action").equals("register"))
        {
            JSONObject verifyResult = new JSONObject();
            JSONObject credentialsJSON = new JSONObject();
            System.out.println("Регистрация нового пользователя.");
            List<String> newUserCredentials =  proposeUserInputCredentials();
            System.out.println("Подтвердите пароль:");
            String verifyPassword = userInput.nextLine();
            if(!newUserCredentials.get(1).equals(verifyPassword))
            {
                JSONObject failed = new JSONObject();
                failed.put("result", "failed");
                credentialsJSON.put("verifyResult", failed);
            }
            else
            {
                verifyResult.put("result", "success");
                Map<String, String> credentialMap = new HashMap<>();
                credentialMap.put("email", newUserCredentials.get(0));
                credentialMap.put("password", newUserCredentials.get(1));
                credentialsJSON.put("userCredentials", credentialMap);
                credentialsJSON.put("verifyResult", verifyResult);
            }
            outputToServer.write(credentialsJSON.toString() + "\n");
            outputToServer.flush();

            JSONObject newUserResponseFromServer = new JSONObject(inputFromServer.readLine());

            if(newUserResponseFromServer.get("regResult").equals("success"))
            {
                System.out.println("Вы успешно зарегистрированы.");
            }
            else if(newUserResponseFromServer.get("regResult").equals("userAlreadyExists"))
            {
                System.out.println("Пользователь с таким email уже существует.");
            }
            else if(newUserResponseFromServer.get("regResult").equals("failed"))
            {
                System.out.println("Введенные пароли не совпадают.");
            }
        }

        else if(initialCodeResult.get("action").equals("exit"))
        {
            System.out.println("Удачи! :)");
            Thread.sleep(3000);
            System.exit(0);
        }
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
        System.out.println("Введите пароль:");
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
    private static void proposeUserInputTaskId(OutputStreamWriter outputStreamToServer)
    {
        while(true)
        {
            try
            {
                int initialCode = Integer.parseInt(userInput.nextLine());
                if(initialCode >= 1)
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
                        System.out.println("ID задачи не может быть отрицательным");
                    }
            }

            catch (NumberFormatException e)
            {
                System.out.println("Пожалуйста, введите корректный ID задачи");
            }
        }
    }



    private static void printFromJSONArray(JSONArray jsonArray)
    {
        if(jsonArray.length() > 0)
        {
        for(Object object : jsonArray)
        {
            System.out.println(object.toString());
        }
        } else System.out.println("У вас нет текущих задач");
    }
}



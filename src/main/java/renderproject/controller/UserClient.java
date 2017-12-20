package renderproject.controller;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class UserClient
{

    private static final Scanner userInput = new Scanner(System.in);

    public static void main(String[] args) throws Exception
    {
        String resultFromServer;
        Socket socket = new Socket("127.0.0.1", 5555);
        Scanner stream = new Scanner(socket.getInputStream());
        //System.out.println("test");
        PrintStream p = new PrintStream(socket.getOutputStream());
        p.println(userInput.nextLine());
        resultFromServer = stream.nextLine();
        System.out.println(resultFromServer);

    }
}

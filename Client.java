
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {


        try (Socket socket = new Socket("localhost", 5001);
             PrintWriter outputToServer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {
            boolean acceptedUserName = false;
            boolean acceptedPassword = false;
            boolean connected = true;
            boolean fileFound;
            String currentUser = null;
            System.out.println("+Hello from VSFTP Service!");
            String[] input = scanner.nextLine().trim().split("\\s+");
            String[] command = checkInput(input);
            int numberOfFiles;
            do {
                switch (command[0]) {
                    case "USER":
                        outputToServer.println(command[0]);
                        acceptedUserName = validateUsername(inputFromServer, outputToServer, command[1]);
                        if (acceptedUserName && !acceptedPassword) {
                            currentUser = command[1];
                            System.out.println("+User-id valid, send password ");
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                        }
                        else if(acceptedUserName){
                            System.out.println("-Already logged-in ");
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                        }
                        else {
                            System.out.println("-Invalid user-id ");
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                        }
                        break;

                    case "PASS":
                        if (acceptedUserName) {
                            outputToServer.println(command[0]);
                            acceptedPassword = validatePassword(inputFromServer, outputToServer, currentUser, command[1]);
                            if(acceptedPassword){
                                System.out.println("! Logged in");
                                System.out.println("+Password is ok and you can begin file transfers");
                                input = scanner.nextLine().trim().split("\\s+");
                                command = checkInput(input);
                            }
                            else{
                                System.out.println("-Wrong password ");
                                input = scanner.nextLine().trim().split("\\s+");
                                command = checkInput(input);
                            }
                        }
                        else {
                            System.out.println("-Must enter a valid username before password verification ");
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                        }
                        break;

                    case "LIST":
                        if (acceptedUserName && acceptedPassword) {
                            outputToServer.println(command[0]);
                            //print directory name
                            System.out.println(inputFromServer.readLine());
                            //get number of files in directory
                            numberOfFiles = Integer.parseInt(inputFromServer.readLine());
                            //retrieve file names from server and print
                            for(int i = 0; i < numberOfFiles; i++){
                                System.out.println(inputFromServer.readLine());
                            }
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                        }
                        else {
                            System.out.println("-Valid credentials needed to use this command ");
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                        }
                        break;

                    case "KILL":
                        if (acceptedUserName && acceptedPassword) {
                            outputToServer.println(command[0]);
                            outputToServer.println(command[1]);
                            System.out.println(inputFromServer.readLine());
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                        } else {
                            System.out.println("-Valid credentials needed to use this command ");
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                        }
                        break;

                    case "DONE":
                        outputToServer.println(command[0]);
                        connected = false;
                        break;

                    case "RETR":
                        if (acceptedUserName && acceptedPassword) {
                            outputToServer.println(command[0]);
                            outputToServer.println(command[1]);
                            fileFound = Boolean.parseBoolean(inputFromServer.readLine());
                            if(fileFound){
                                System.out.println(inputFromServer.readLine());
                                input = scanner.nextLine().trim().split("\\s+");
                                command = checkInputForRetrieveFunction(input);
                                outputToServer.println(command[0]);
                                System.out.println(inputFromServer.readLine());
                                input = scanner.nextLine().trim().split("\\s+");
                                command = checkInput(input);
                            }
                            else{
                                System.out.println("-File not found ");
                                input = scanner.nextLine().trim().split("\\s+");
                                command = checkInput(input);
                            }
                        } else {
                            System.out.println("-Valid credentials needed to use this command ");
                            input = scanner.nextLine().trim().split("\\s+");
                            command = checkInput(input);
                            }
                        break;

                    default:
                        System.out.println("-Command not found ");
                        input = scanner.nextLine().trim().split("\\s+");
                        command = checkInput(input);
                        break;
                }
            } while (connected);
        } catch (IOException e) {
            System.out.println("-Connection Error: VSFTP Server is out for lunch");
        }

    }//end main
    //////////////////////////////////////////////////////////////////////////////////////////

    private static boolean validateUsername(BufferedReader inputFromServer, PrintWriter outputToServer, String username) throws IOException {
        outputToServer.println(username);
        return Boolean.parseBoolean(inputFromServer.readLine());
    }//end validateUsername
    //////////////////////////////////////////////////////////////////////////////////////////

    private static boolean validatePassword(BufferedReader inputFromServer, PrintWriter outputToServer, String username, String password) throws IOException {
        outputToServer.println(username);
        outputToServer.println(password);
        return Boolean.parseBoolean(inputFromServer.readLine());
    }//end validatePassword
    //////////////////////////////////////////////////////////////////////////////////////////

    private static String[] checkInput(String[] command){

        Scanner scanner = new Scanner(System.in);

        do{
            switch (command[0]) {
                case "USER":
                case "PASS":
                case "KILL":
                case "RETR":
                    if (command.length < 2) {
                        System.out.println("-This command requires additional input.");
                        command = scanner.nextLine().trim().split("\\s+");
                    } else if (command.length > 2) {
                        System.out.println("-Too much input.");
                        command = scanner.nextLine().trim().split("\\s+");
                    } else {
                        return command;
                    }
                    break;
                case "LIST":
                case "DONE":
                    if (command.length > 1) {
                        System.out.println("-This command does not accept any additional input.");
                        command = scanner.nextLine().trim().split("\\s+");
                    } else {
                        return command;
                    }
                    break;
                default:
                    return command;
            }
        }while(true);

    }//end checkInput
    /////////////////////////////////////////////////////////////////////////////////////
    private static String[] checkInputForRetrieveFunction(String[] command) {
        Scanner scanner = new Scanner(System.in);
        do{
            if(command[0].equals("SEND") || command[0].equals("STOP")){
                if(command.length > 1){
                    System.out.println("-This command does not accept any additional input.");
                    command = scanner.nextLine().trim().split("\\s+");
                }
                else
                    return command;
            }
            else{
                System.out.println("-Only SEND and STOP commands available within RETR function ");
                command = scanner.nextLine().trim().split("\\s+");
            }


        }while(true);
    }//end checkInputForRetrieveFunction
    //////////////////////////////////////////////////////////////////////////////////////////

}//end Client

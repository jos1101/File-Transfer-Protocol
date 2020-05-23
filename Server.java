import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;


public class Server {

    public static void main(String[] args) throws IOException {

        ArrayList<User> usernameAndPasswordList = null;

        try {
            //load the user database
            File inputFile = new File("users.txt");
            //store it in an array list of users
            usernameAndPasswordList = uploadUsernameAndPasswords(inputFile);
        }
        //if no file is input, send an error message
        catch (FileNotFoundException e) {
            System.out.println("ERROR:File not found. Please ensure the user.txt and root folder are located with the Server executable before running this program.");
            System.exit(1);
        }
        //load the root folder
        File rootFolder = new File("root");
        //store its files
        File[] rootFiles = rootFolder.listFiles();


        //server declarations
        BufferedReader inputFromClient = null;
        PrintWriter outputToClient = null;
        Socket socket = null;

        //start server
        try (ServerSocket serverSocket = new ServerSocket(5001))
        {
            socket = serverSocket.accept();
            inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputToClient = new PrintWriter(socket.getOutputStream(), true);

            String fileName;

            System.out.println("Client Connected");

            boolean connected = true;
                do {
                    String command = inputFromClient.readLine();
                    switch (command) {
                        case "USER":
                            validateUsername(inputFromClient, outputToClient, usernameAndPasswordList);
                            break;
                        case "PASS":
                            validatePassword(inputFromClient, outputToClient, usernameAndPasswordList);
                            break;
                        case "LIST":
                            sendFileList(outputToClient, rootFolder);
                            break;
                        case "KILL":
                            fileName = inputFromClient.readLine();
                            rootFiles = deleteFile(outputToClient, rootFolder, rootFiles, fileName);
                            break;
                        case "DONE":
                            connected = false;
                            System.out.println("Client Disconnected");
                            break;
                        case "RETR":
                            fileName = inputFromClient.readLine();
                            retrieveFile(inputFromClient, outputToClient, rootFiles, fileName);
                            break;
                        default:

                            break;
                    }
                } while (connected);
            }
          catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (inputFromClient != null) {
                inputFromClient.close();
            }
            if (outputToClient != null) {
                outputToClient.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
    }//end main
    //////////////////////////////////////////////////////////////////////////////////////////////

    private static void retrieveFile(BufferedReader inputFromClient, PrintWriter outputToClient, File[] rootFiles, String fileToRetrieve) throws IOException {

        String input;

        boolean found = false;

        for (File x : rootFiles) {
            //convert File into String
            String fileName = x.getName();
            //if found, retrieve it
            if (fileToRetrieve.equals(fileName)) {
                found = true;
                outputToClient.println(true);
                outputToClient.println("#" + x.length());
                input = inputFromClient.readLine();
                if (input.equals("SEND")) {
                    outputToClient.println("+File sent");
                    break;
                } else if (input.equals("STOP")) {
                    outputToClient.println("+ok, RETR aborted");
                    break;
                }
            }
        }

        if(!found){
            outputToClient.println(false);
        }


    }//end retrieveFile
    /////////////////////////////////////////////////////////////////////////////////////////////

    private static File[] deleteFile(PrintWriter outputToClient, File rootFolder, File[] rootFiles, String fileToDelete) {

        boolean deleted = false;
        //search for the file
        for(File x: rootFiles){
            //convert File into String
            String fileName = x.getName();
            //if found, delete it
            if(fileToDelete.equals(fileName)){
                deleted = x.delete();
                outputToClient.println("+" + fileToDelete + " deleted");
                break;
            }
        }
        //if deleted == false, file wasn't found
        if(!deleted){
            outputToClient.println("-" + fileToDelete + " does not exist");
        }

        return rootFolder.listFiles();

    }//end deleteFile
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static void sendFileList(PrintWriter outputToClient, File folder) {
        String[] fileList = folder.list();
        outputToClient.println("+root");
        //send the number of file names to the client
        outputToClient.println(fileList.length);
        //for each file in the root directory, send to client
        for(String x: fileList){
            outputToClient.println(x);
        }
    }//end sendFileList
    ///////////////////////////////////////////////////////////////////////////////////////////



    private static ArrayList<User> uploadUsernameAndPasswords(File file) throws IOException {
        String inputString;
        BufferedReader br = null;
        ArrayList<User> userArr = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while ((inputString = br.readLine()) != null) {
            //This splits the line into username and password
            //trimmedString[0] is the username, trimmedString[1] is the password.
            String[] trimmedString = inputString.trim().split("\\s+");
            userArr.add(new User(trimmedString[0], trimmedString[1]));
            }
        return userArr;
    }//end uploadUserNameAndPasswords
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static void validateUsername(BufferedReader inputFromServer, PrintWriter outputToServer, ArrayList<User> usernameAndPasswordList) throws IOException {
        //read the username from the server
        String username = inputFromServer.readLine();
        //for each user
        for (User list : usernameAndPasswordList) {
            boolean check = list.usernameAuthentication(username);
            //if the username was found, send true.
            if (check) {
                outputToServer.println(true);
                return;
            }
        }
        //if username not found, send false
        outputToServer.println(false);
    }

    private static void validatePassword(BufferedReader inputFromServer, PrintWriter outputToServer, ArrayList<User> usernameAndPasswordList) throws IOException {
        //read the username and password from the server
        String username = inputFromServer.readLine();
        String password = inputFromServer.readLine();
        //for each user
        for (User list : usernameAndPasswordList) {
            boolean findUser = list.usernameAuthentication(username);
            //find the username, check password
            if (findUser) {
                boolean checkPassword = list.passwordAuthentication(password);
                //if password matches, send true
                if(checkPassword){
                    outputToServer.println(true);
                    return;
                }
            }
        }
        //if password doesn't match, send false
        outputToServer.println(false);
    }//end validatePassword
    //////////////////////////////////////////////////////////////////////////////////////////

}//end Server
/////////////////////////////////////////////////////////////////////////////////////////////

//a User class to hold the username and passwords.
class User {

    //fields
    private String username;
    private String password;

    //constructor
    User(String x, String y){
        this.username = x;
        this.password = y;
    }

    //only use boolean values to match usernames and passwords
    public boolean usernameAuthentication(String x) {
        return x.equals(this.username);
    }

    public boolean passwordAuthentication(String y) {
        return y.equals(this.password);
    }

}//end User
//////////////////////////////////////////////////////////////////////////

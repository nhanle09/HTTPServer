import java.io.*;
import java.net.*;
import java.util.*;

public class HTTPServer implements Runnable
{
    /*  Primary Socket  variable to accept connection   */
    static Socket data_socket;

    /*  Common String Variables to avoid retyping   */
    static final String NOT_FOUND = "404.html";
    static final String LOCAL_DIR = ".";
    static final String MOVED = "301.html";
    static final String MOVED_FILE = "moved_file.html";

    /*  Main method of HTTPServer   */
    public static void main(String[] args) throws Exception
    {
        /*  ServerSocket avariable to accept incoming 8181 port */
        ServerSocket handshake = new ServerSocket(8181);
        /*  Displaying what port is being listen on console */
        System.out.println("Listening for HTTP traffic on port 8181");

        /*  Server running indefinitely */
        while (true)
        {
            /*  Declare HTTPServer object for each request  */
            HTTPServer http_server = new HTTPServer();
            /*  New HTTPServer thread & start thread to receive incoming connection*/
            Thread server_thread = new Thread(http_server);
            server_thread.start();
            /*  Setup initial handshake & accept connection */
            data_socket = handshake.accept();
        }
    }

    /*  Overriding run() to implement custom run() method   */
    @Override
    public void run()
    {
        try
        {
            /*  Input stream attached to socket to get incoming texts   */
            BufferedReader in_client = new BufferedReader(new InputStreamReader(data_socket.getInputStream()));
            /*  Output stream attached to socket    */
            DataOutputStream out_data = new DataOutputStream(data_socket.getOutputStream());
            /*  Reading the client's request into a variable to parse and print out the received text   */
            String client_request = in_client.readLine();
            System.out.println(client_request);
            /*  Tokenize each line of the input into String array for easy access each index    */
            String[] token_str = client_request.split(" ");
            /*  Handling incoming GET request  */
            if (token_str[0].equals("GET"))
            {
                /*  Remove '/' from requested file name */
                token_str[1] = token_str[1].substring(1);
                /*  New file pointed to requested file based on file name   */
                File req_file = new File(new File(LOCAL_DIR), token_str[1]);
                /*  HTTP Response variables */
                String content_type, status_code, http_response = "";
                /*  Handle valid request and set variables for response code 200 OK  */
                if (req_file.exists() && !req_file.isDirectory())
                {
                    content_type = getType(token_str[1]);
                    status_code = "200 OK\n";
                }
                /*  Handle valid request and set variables for response code 301 Moved Permanently  */
                else if (token_str[1].equals(MOVED_FILE))
                {
                    content_type = getType(MOVED);
                    status_code = "301 Moved Permanently";
                    req_file = new File(new File(LOCAL_DIR), MOVED);
                }
                /*  Handle valid request and set variables for response code 404 Not Found  */
                else
                {
                    content_type = getType(NOT_FOUND);
                    status_code = "404 Not Found\n";
                    req_file = new File(new File(LOCAL_DIR), NOT_FOUND);
                }

                /*  Composing HTTP response into http_response variable */
                byte[] file_byte = file_to_bytes(req_file);
                http_response += "HTTP/1.1 " + status_code;
                http_response += "Date: " + new Date() + "\n";
                http_response += "Server: Generic Web Server\n";
                http_response += "Content-type: " + content_type + "\n";
                http_response += "Content-Length: " + (int) req_file.length() + "\n\n";

                /*  Send out HTTP Response Header   */
                out_data.writeBytes(http_response);
                /*  Send out HTTP Response data */
                out_data.write(file_byte, 0, (int) req_file.length());
            }

            /*  Closing all in and out streams  */
            in_client.close();
            out_data.close();
        }
        /*  Catching any exception for not able to receive and parse data   */
        catch (Exception e)
        {
            System.out.println("Unable to receive HTTP Request");
        }
        /*  Closing socket after each handling each TCP Connection  */
        finally
        {
            try
            {
                data_socket.close();
            }
            /*  Catch exception for not able to close socket    */
            catch (Exception ex)
            {
                System.out.println("Unable to close socket");
            }
        }
    }

    /*  Return Content-Type based on extension  */
    public static String getType (String requested_file)
    {
        /*  Handle PDF  */
        if (requested_file.endsWith(".pdf"))
        {
            return "application/pdf";
        }
        /*  Handle image types  */
        else if (requested_file.endsWith(".png"))
        {
            return "image/png";
        }
        else if (requested_file.endsWith(".jpeg"))
        {
            return "image/jpeg";
        }
        else if (requested_file.endsWith(".gif"))
        {
            return "image/gif";
        }
        /*  Handle text types   */
        if (requested_file.endsWith(".html"))
        {
            return "text/html";
        }
        else
        {
            return "text/plain";
        }       
    }

    /*  Convert file to byte array  */
    public static byte[] file_to_bytes(File req_file)
    {
        byte[] byte_file = new byte[(int) req_file.length()];
        try (FileInputStream stream_in = new FileInputStream(req_file))
        {
            stream_in.read(byte_file);
        }
        catch (Exception e)
        {
            System.out.println("Unable to convert files into bytes array");
        }
        return byte_file;
    }
}

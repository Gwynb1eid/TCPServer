/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


//////////////////////
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/////////////////////////

/**
 *
 * @author Gwynb1eid
 */
public class TCPServer implements Runnable {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        conn = DriverManager.getConnection("jdbc:derby:db;create=true");
        Statement st = conn.createStatement();
        try {
            st.execute(
                "CREATE TABLE users (" +
                "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "login VARCHAR(24) NOT NULL,"+
                "pswd VARCHAR(24) )"
            );
        } catch(SQLException ex) {}
        try {
            st.execute(
                "CREATE TABLE messages (" +
                "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "stamp TIMESTAMP," +
                "ids INTEGER," +
                "idr INTEGER," +
                "content VARCHAR(1024))"
            );
        } catch(SQLException ex) {}
////////////////////////////////////////////////////////////////////////
        try{
            st.execute(
                    "CREATE TABLE groups (" +
                            "gid INTEGER," +
                            "login VARCHAR(24) NOT NULL," +
                            "idr VARCHAR(24) NOT NULL," +
                            "groupName VARCHAR(24) NOT NULL )"
            );
            
        }catch (SQLException ex) {}//{ex.printStackTrace();}
        
        try{
            st.execute(
                    "CREATE TABLE gcount (" +
                            "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                            "count INTEGER )" 
                      
                     );
            
        }catch (SQLException ex) {}//{ex.printStackTrace();}
////////////////////////////////////////////////////////////////////////
        ServerSocket ssock = new ServerSocket(8888);
        for(;;) {
            Socket sock = ssock.accept();
            TCPServer client = new TCPServer(sock);
            clients.add(client);
            new Thread(client).start();
        }
    }
    private static Connection conn = null;
    private static HashSet<TCPServer> clients = new HashSet<>();
    private Socket sock;
    private String login = null;
    private String sendTo = null;
    private PrintWriter out = null;
//////////////////
    //private ArrayList<String> groupList = new ArrayList<String>();
    private ArrayList<String> groupList = new ArrayList<String>();
    private byte kot=0;
    private String pswd = null;
    private String groupMSG;
    private String fPath;
    
    

    
/////////////////
    
    
    private TCPServer(Socket sock) throws IOException {
        this.sock = sock;
        out = new PrintWriter(sock.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
	
	BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        for(;;) {
            String s = null;
            try {
                s = in.readLine();
            } catch(SocketException e) {
                break;
            }
            if(s == null) break;
            /*
                interpretation of a command/data sent from clients
            */
///////////////////////////////////
            if(s.charAt(0) == '@') 
            {
                
                if(login != null) 
                {
                    StringTokenizer st = new StringTokenizer(s);
                    String groupName = st.nextToken();
                    groupName = groupName.substring(1);
                    int msgCount = groupName.length()+1;
                
                    groupMSG = s.substring(msgCount);
                
                    out.println(groupName + " group chat motherfucker: " + groupMSG);
                
                    uList (login, groupName, groupMSG);
                    
                    
                } else 
                {
                    out.println("You have to log in first");
                }
                break;

            }
            
            
            if(s.charAt(0) == '/') {
                // out.println("You entered a command " + s);
                StringTokenizer st = new StringTokenizer(s);
                String cmd = st.nextToken();
                switch(cmd) {
////////////////////////////////////////                    
                    case "/send":
                        if(login != null) 
                        {
                            sendTo = st.nextToken();
                            fPath = st.nextToken();
                            //fPath="C:\\1111.txt";
                            out.println("your file path is:" + fPath);
                        
                            if (isLoggedIn(sendTo))
                            {
                                for(TCPServer client: clients) 
                                {
                                    if(client.login != null && client.login.equals(sendTo)) 
                                    {
                                        client.out.println(login + " tryind to send file " + fPath);
                                        client.out.println("to accept try /catch + save path" );
                                    }
                                }
                            
                                    shit(sendTo, fPath);
                            }
                            else 
                            {
                                out.println("user " + sendTo + " is offline");
                            }
                            
                    
                        } else 
                        {
                            out.println("You have to log in first");
                        }
                        break;
                        
                        
                        
                        
                        
                    case "/catch":
                        if(login != null) 
                        {
                            String path = st.nextToken();
                            out.println("file saved in " + path);
                            shitIn(path);
                            
                        } else 
                        {
                            out.println("You have to log in first");
                        }
                        break;
                        
///////////////////////////////////////                        
                        
                    case "/login":
                        String newLogin = null;
                        try {
                            newLogin = st.nextToken();
                            pswd = st.nextToken();
                        } catch(NoSuchElementException ex) { newLogin = null; pswd=null; out.println("you must enter the password");
                                    break;}
                        
                        if(newLogin != null && !newLogin.equals("")) {

                            if (isLoggedIn(newLogin))
                            {
                                out.println("User is already login ( " + newLogin + " )");
                                login=null;
                                //out.println("...so fuck off");
                                break;
                            }
                            
                            if(isRegistered(newLogin) ) 
                            {
                                
                                if (isPswd(newLogin, pswd))
                                {
                                    login = newLogin;
                                    out.println("Login " + login);
                                    unbufferMessages();
                                }
                                else {
                                    out.println("bad pswd motherfucker - ");
                                }
                                
                            } 
                            else {
                                out.println("Not registered " + newLogin);
                            }
                        
                        } else {
                            login = null;
                            out.println("Logout");
                        }
                        break;
                    
                    //case "/pswd":
                        
                        
                    case "/whoami":
                        if(login != null)
                            out.println(login);
                        else
                            out.println("You are not logged in");
                        break;
                    
                    case "/to":
                     
                        if(login != null)
                            out.println(login);
                        else {
                            out.println("You are not logged in");
                            break;
                        }
                        
                        newLogin = null;
                        
                        try {
                            newLogin = st.nextToken();
                        } catch(NoSuchElementException ex) { newLogin = null; }
                        
                        if(newLogin != null && !newLogin.equals("")) {
                            if(isRegistered(newLogin)) {
                                sendTo = newLogin;
                                out.println("Send to " + sendTo);
                            } else {
                                out.println("Not registered " + newLogin);
                            }
                        } else {
                            sendTo = null;
                            out.println("End of conversation");
                        }
                        break;
/////////////////////////////////////groups
                    case "/add2group":
                        
                        if(login != null)
                        {
                            String groupName = st.nextToken();
                            String idr = st.nextToken();
                            out.println("your group is:" + groupName);
                            out.println("your member is:" + idr);
                            
                            if(isRegistered(idr))
                            {
                                if (isInGroup(login, idr, groupName))
                                {
                                    if  (add2group(login, groupName, idr))
                                    {
                                        out.println(idr + " added into " + groupName);
                                    }
                                    else
                                    {
                                        out.println("gavno addgroup");
                                    }
                                
                                }
                                else out.println("user " + idr + " is already in group " + groupName);
                            }
                            else
                            {
                                out.println("Not registered " + idr);
                            }
                            
                        }
                                                    
                        else {
                            out.println("You are not logged in");
                        }
                        
                        break;
                        
                    case "/groupMembers":
                        if(login != null)
                        {
                            String groupName;
                            try {
                                groupName = st.nextToken();
                            } catch(NoSuchElementException ex) { groupName=null; out.println("you must enter the group name");}
                            
                            
                            if (groupName != null && !groupName.equals(""))
                            {
                                out.println("@" + groupName + " members: ");
                            
                            groupMembers (login, groupName);
                            }
                            
                            
                                
                            
                            
                            
                            
                        }
                                                    
                        else {
                            out.println("You are not logged in");
                        }
                        
                        break;
                        
                        
                    case "/groupList":
                        if(login != null)
                        {
                            
                            out.println(login + ", you have such groups");
                            
                            groupList (login);
                            
                        }
                                                    
                        else {
                            out.println("You are not logged in");
                        }
                        
                        break;    
                        
                    case "/delFrom":
                        if(login != null)
                        {
                            String groupName = st.nextToken();
                            String member = st.nextToken();
                            out.println("deleting " + member + " from " + groupName + "...");
                            
                            if (!isInGroup(login, member, groupName))
                            {
                                if(deleteFrom(login, member, groupName))
                                {
                                    out.println(member + " delete from " + groupName + " successfull");
                                }
                                else
                                {
                                    out.println("gavno delete");
                                }
                            }
                            
                            else
                            {
                                out.println(member + " is not exist in " + groupName);
                            }
                            
                        }
                                                    
                        else {
                            out.println("You are not logged in");
                        }
                        
                        break;
                        
                        
                        
                        /*case "/addgroup":
                            if(login != null) out.println("added to group chat" );
                        else {
                            out.println("You are not logged in");
                            break;
                        }
                        
                        newLogin = null;
                        
                        try {
                            newLogin = st.nextToken();
                        } catch(NoSuchElementException ex) { newLogin = null; }
                        
                        if(newLogin != null && !newLogin.equals("")) {
                            if(isRegistered(newLogin)) 
                            {
                                kot++;
                                
                                groupList.add (newLogin);
                                
                                for (int i = 0; i<kot; i++)
                                {
                                    
                                    out.println("Send to " + groupList.get(i));
                                }
                                
                                
                            } else {
                                out.println("Not registered " + newLogin);
                            }
                        } else {
                            sendTo = null;
                            out.println("End of conversation");
                        }
                        break;
                        
                        
                    case "/group":
                        
                        if(login != null && groupList.get(0)!=null)
                            out.println(login + " says to group");
                        else {
                            out.println("You are not logged in");
                            break;
                        }
                        
                        newLogin = null;
                        
                        try {
                            newLogin = st.nextToken();
                        } catch(NoSuchElementException ex) { newLogin = null; }
                        
                        if(newLogin != null && !newLogin.equals("")) {
                            //if(isRegistered(newLogin)) {
                                //sendTo = newLogin;
                                //out.println("Send to " + sendTo);
                                toGroup(groupList,s);
                            //} else {
                                //out.println("Not registered " + newLogin);
                            }
                        //} else 
                        //{
                        //    sendTo = null;
                        //    out.println("End of conversation");
                        //}
                        break;
                        
                        
                        
                    case "/delgroup":
                        if(login != null) out.println(login);
                        else {
                            out.println("You are not logged in");
                            break;
                        }*/
                        
                        
///////////////////////////////////////////                        
                    
                    case "/exit":
                        sock.close();
                        clients.remove(this);
                        return;
                    
                    case "/who":
                        int nli = 0;
                        for(TCPServer client: clients) {
                            if(client.login != null)
                                out.println(client.login + " -> " + (client.sendTo != null ? client.sendTo : ""));
                            else
                                nli++;
                        }
                        out.println("not logged in: " + nli);
                        break;
                    
                    case "/register":
                        newLogin = null;
                        pswd = null;
                        try {
                            newLogin = st.nextToken();
                            pswd = st.nextToken();
                        } catch(NoSuchElementException ex) { newLogin = null; }
                        
                        if (pswd == null || pswd.equals(""))
                        {
                            out.println("you must enter login + password");
                            pswd=null;
                            newLogin=null;
                            break;
                        }
                        
                        if(newLogin != null && !newLogin.equals("")) 
                        {
                            if(isRegistered(newLogin)) {
                                out.println("Already registered " + newLogin);
                            } else 
                            {
                                if(register(newLogin, pswd)) 
                                {
                                    out.println(newLogin + " registered");
                                } 
                                
                                else 
                                {
                                    out.println("Registration error on " + newLogin);                                    
                                }
                            }
                        } else {
                            out.println("No login to register");
                        }
                        break;                        
                    default:
                        out.println("Unknown command " + cmd);
                }
            } else {
                if(login != null) {
                    // out.println("You entered data [" + s + "]");
                    
                    int nrec = 0;
                    for(TCPServer client: clients) {
                        if(client.login != null && client.login.equals(sendTo)) {
                            client.out.println(login + " says: " + s);
                            nrec++;
                        }
                    }
                    out.println("messages sent: " + nrec);
                    if(nrec == 0) {
                        saveMessage(s);
                        out.println("Message buffered");
                    }
                } else {
                    out.println("You have to log in first");
                }
            }
        }
        } catch(IOException e) {}
        clients.remove(this);
    }

    @Override
    public String toString() {
        return sock + " [" + (login != null ? (login + "->" + (sendTo != null ? sendTo : "")) : "") + "]";
    }
    
//////////////////////////////////////////////myShit
    
    public void shit(String sendTo, String fPath)
    {
        FileInputStream fis = null;
        try
        {
            ServerSocket serverSocket = new ServerSocket(8889);
            Socket socket = serverSocket.accept();
            
            OutputStream socketOut = socket.getOutputStream();
            
            fis = new FileInputStream(fPath);
            
            
            
            
            byte[] buffer = new byte[64 * 1024];
            int read;
            while((read = fis.read(buffer)) != -1)
                socketOut.write(buffer, 0, read);
            socket.close();
            serverSocket.close();
        } catch(Exception ex) { ex.printStackTrace(); }
        finally
        {
            try
            {
                if(fis != null)
                fis.close();
            } catch(IOException ex) { ex.printStackTrace(); }
        }
    }
    
    public void shitIn(String path)
        {
            FileOutputStream fis = null;
            try
            {
                Socket socket = new Socket("localhost", 8889);
                
                InputStream socketIn = socket.getInputStream();
                fis = new FileOutputStream(path);
                byte[] buffer = new byte[64 * 1024];
                int read;
                while((read = socketIn.read(buffer)) != -1)
                    fis.write(buffer, 0, read);
                socket.close();
            } catch(IOException ex) { ex.printStackTrace(); }
            
            finally
            {
                try
                {
                if(fis != null)
                    fis.close();
                } catch(IOException ex) { ex.printStackTrace(); }
            }
        }
    
/////////////////////////////////////////////////////    
    public static boolean isPswd (String login, String pass)
    {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT pswd FROM users WHERE id=" + getUserId(login) + "");
            
            if(!rs.next()) { return false;}
            
            //out.println(rs.getString(1));
            if (rs.getString(1).equals(pass))    
            //if (rs.equals(pass))
                return true;
            else {return false; }//out.println(rs.getString("pswd"));  }
        } catch(SQLException ex) { ex.printStackTrace(); return false; }
    }
    
    
    public static boolean isInGroup (String login, String idr, String groupName)
    {
        try {
            
            Statement st = conn.createStatement();
            //ResultSet rs = st.executeQuery("SELECT gid FROM groups WHERE login='" + login + "' AND idr='" + idr  + "' AND groupName='" + groupName + "'" );
            //Boolean bool 
            ResultSet rs = st.executeQuery("SELECT gid FROM groups WHERE login='" + login + "' AND idr='" + idr  + "' AND groupName='" + groupName + "'" );
            rs.next();
            //try{
            //     String str = rs.getString(1);
                 //if (rs.getInt(1) > 0 ) return false;
                 //else return true;
            //     return false;    
                    
            //} catch (SQLException ex) {System.out.println("in "); } 
            
            //if (bool) return true;
            //else return false;
            System.out.println(rs.getInt(1));
            return false;
            
        } catch(SQLException ex) {ex.printStackTrace(); System.out.println("out"); return true;}   
        
        
    }
        
    
////////////////////////////////////////////////////////    

    public static boolean add2group (String login, String groupName, String idr)
    {
        int gid = getGroupId (login, groupName);
        
        try {
            
            Statement st = conn.createStatement();
            st.executeUpdate("INSERT INTO groups (gid, login, idr, groupName) VALUES (" + gid + ",'" + login +"','" + idr + "','" + groupName + "')");
        } catch(SQLException ex) {ex.printStackTrace();  return false;}    
        
        return true;
        
    }
    
    
    public static int getGroupId(String login, String groupName)
    {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT gid FROM groups WHERE login='" + login + "' AND groupName='" + groupName + "'");
            int gid = rs.getInt(1);
            return gid;
            }catch(SQLException ex) {ex.printStackTrace(); return getGNumb();}
        
    }
    
    
    public static int getGNumb()
    {
        int gNumb=1;
        //need komment
        //try{
        //    Statement st = conn.createStatement();
        //    st.executeUpdate("INSERT INTO gcount (count) VALUES (" + 0 + ")" );
        //}catch(SQLException ex) {ex.printStackTrace();}
        
        
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT count FROM gcount WHERE id=" + 1 );
            if(!rs.next()) return 0;
            gNumb = rs.getInt(1);
            gNumb++;
        
        } catch(SQLException ex) { ex.printStackTrace(); return 0; }
        upGroup(gNumb);
        
        return gNumb;
    }
    
    public static void upGroup(int gCount)
    {
        try {
            //gCount++;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("UPDATE gcount set count=" + gCount + "WHERE id=" + 1 );
            //if(!rs.next()) return 0;
            //gNumb = rs.getInt(1);
        
        } catch(SQLException ex) {ex.printStackTrace(); }
    }
    
    
    public void uList(String login, String groupName, String groupMSG)
    {
        ArrayList<String> groupList = new ArrayList<String>();
        
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT idr FROM groups WHERE login='" + login + "' AND groupName='" + groupName + "'" );
            
            while(rs.next())
            { 
                groupList.add(rs.getString("idr")); 
            }
        
        } catch(SQLException ex) {ex.printStackTrace(); }
        
        //for (int i = 0; i < groupList.size(); i++)
        //{ 
        //    out.println(groupList.get(i)); 
        //}
        
        toGroup(groupList, groupMSG);
        
        
        
        /*DatabaseConnection db = new DatabaseConnection();
        db.setSQL("Select name from Crew where nationality = 'Russian' and rank = 'Master'"); 
        db.exec(); 
        ArrayList<String> arrayList = new ArrayList<>(); 
        
        while(db.getResult().next())
        { 
            arrayList.add(db.getResult().getString("name")); 
        } 

        for (int i = 0; i < arrayList.size(); i++)
        { 
            System.out.println(arrayList.get(i)); 
        }*/
        
        
    }
    
        public void toGroup(ArrayList ggroupList, String ss)
        {
        //if(login != null) 
        //{
                    // out.println("You entered data [" + s + "]");
            
                    int nrec = 0;
                    
                    for(TCPServer client: clients) 
                    {
                        for (int i=0; i<ggroupList.size();i++)
                        {
                            if(client.login != null && client.login.equals(ggroupList.get(i))) 
                            {
                                client.out.println(login + " says: " + ss);
                                nrec++;
                            }
                            if(nrec == 0) 
                            {
                                saveMessage(ss);
                                out.println("Message buffered for: " + ggroupList.get(i));
                            }
                            
                        }
                        
                        
                    }
                    out.println("messages sent: " + nrec);
                    
                    
                //} else 
                //{
                    //out.println("You have to log in first");
                //}
        }
        
        public void groupMembers(String login, String groupName)
        {
            
            try{
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT idr FROM groups WHERE login='" + login + "' AND groupName='" + groupName + "'");
                
                while (rs.next())
                {
                    out.println(rs.getString(1));
                }
                
            }catch (SQLException ex) {}
        }
        
        public void groupList(String login)
        {
            
            try{
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT groupName FROM groups WHERE login='" + login + "'");
                //ResultSet nrs=null;
                //ArrayList<String> groupList = new ArrayList<String>();
                
                TreeSet<String> sortList = new TreeSet<String>();
                
                while (rs.next())
                {
                    sortList.add(rs.getString(1));
                    //out.println(rs.getString(1));
                }
                
                Iterator<String> itr = sortList.iterator();
                while (itr.hasNext()) 
                {
                    out.println ( itr.next().toString());
                }
                

                //rs.next();
                
                //while (rs.next())
                //{
                //    nrs = st.executeQuery("SELECT groupName FROM groups WHERE gid=" + rs.getInt("gid"));
                //    groupList.add(nrs.getString(1));
                //}
                
                
                
            }catch (SQLException ex) {ex.printStackTrace();}
            
        }
        
        
        public static boolean deleteFrom(String login, String member, String groupName)
        {
            try {
                Statement st = conn.createStatement();
                //ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE login='" + login + "'");
                st.executeUpdate("DELETE FROM groups WHERE login='" + login + "' AND idr='" + member + "' AND groupName='" + groupName + "'");
                return true;
            } catch(SQLException ex) { System.out.println ("govnobool"); return false; }
            //st.executeUpdate("DELETE FROM messages WHERE idr=" + idr);
        }


///////////////////////////////////////////////////    
    
    
    public static boolean isRegistered(String login) {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE login='" + login + "'");
            rs.next();
            return rs.getInt(1) > 0;
        } catch(SQLException ex) { return false; }
    }
    
    public static int getUserId(String login) {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT id FROM users WHERE login='" + login + "'");
            if(!rs.next()) return 0;
            return rs.getInt(1);
        } catch(SQLException ex) { return 0; }
    }

    public static boolean register(String login, String pass) {
        try {
            Statement st = conn.createStatement();
            //Statement dt = conn.createStatement();
            //int n = 
                    //st.executeUpdate("INSERT INTO users (login) VALUES ('" + login + "')"       );
                    //st.clearBatch();
                    //st.executeUpdate("UPDATE users SET pswd ='" + pass + "' WHERE login='" + login + "';" );
   
                    st.executeUpdate("INSERT INTO users (login, pswd) VALUES ('" + login + "','" + pass + "')" );
                    
            
            return true; // n > 0;
        } catch(SQLException ex)  { ex.printStackTrace(); return false; }        
    }
    
    public static boolean isLoggedIn(String login) {
        for(TCPServer client: clients) {
            if(client.login != null && client.login.equals(login))
                return true;
        }
        return false;
    }
    
    public void saveMessage(String content) {
        if(login == null || sendTo == null) return;
        try {
            int ids = getUserId(login);
            int idr = getUserId(sendTo);
            Statement st = conn.createStatement();
            st.executeUpdate("INSERT INTO messages (stamp, ids, idr, content) VALUES (CURRENT_TIMESTAMP," + ids + "," + idr + ",'" + content + "')");
        } catch(SQLException ex) {}                
    }
    
    /*
    public void createGroup (string content)
    {
        if (login==null || sendTo == null) return;
        
        try
        {
            int ids = getUserId(login);
            
            
            
        } catch (SQLException ex) {}
        
    }
    */
    
    public void unbufferMessages() {        
        try {
            if(login == null || login.equals("")) return;
            int idr = getUserId(login);
            if(idr == 0) return;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT stamp,login,content FROM messages,users WHERE idr=" + idr + " AND ids=users.id ORDER BY stamp");
            while(rs.next()) {
                out.println(rs.getString("login") + " said @" + rs.getTimestamp("stamp") + ": " + rs.getString("content"));
            }
            st.executeUpdate("DELETE FROM messages WHERE idr=" + idr);
        } catch(SQLException ex) {}
    }
}

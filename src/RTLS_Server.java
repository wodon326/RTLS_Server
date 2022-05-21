import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.*;

public class RTLS_Server implements RTLS_Variable {
    private Queue<Socket> queue;
    private HashMap<Integer, Thread> client_map;
    private int Client_num;
    private monitor_thread Monitor;
    private DB db;

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        RTLS_Server server = new RTLS_Server();
        accepter Accepter = new accepter(server);
        worker Worker = new worker(server);
        Accepter.start();
        System.out.println("Accepter 시작");
        Worker.start();
        System.out.println("Worker 시작");
    }

    public RTLS_Server() throws IOException {
        queue = new LinkedList<>();
        client_map = new HashMap<Integer, Thread>();
        Client_num = 0;
        db = new DB();

        Monitor = new monitor_thread(this);
        Monitor.start();
    }

    public void Queue_Socket_Add(Socket socket) {
        queue.add(socket);
    }

    public Socket Queue_Socket_Pop() {
        return queue.poll();
    }

    public boolean Queue_isEmpty() {
        return queue.isEmpty();
    }

    public boolean Map_ContainKey(int key) {
        return client_map.containsKey(key);
    }

    public Thread Map_getValue(int key) {
        return client_map.get(key);
    }

    public Set<Integer> Map_getkeySet() {
        return client_map.keySet();
    }

    public void Map_put(int id, Client_Thread thread) {
        client_map.put(id, thread);
    }

    public int get_Clientnum() {
        return Client_num;
    }

    public void new_Client_access(byte ID) throws IOException {
        Client_num++;
        byte[] buf_login = new byte[4];
        buf_login[0] = STX;
        buf_login[1] = CMD_LOGIN;
        buf_login[2] = ID;
        buf_login[3] = ETX;
        db.newClient(ID);
        Monitor.getOos().writeObject(buf_login);
    }

    public void addDB(int RTLS_ID, int RTLS_X, int RTLS_Y, byte RTLS_STATE) {
        db.addDB(RTLS_ID,RTLS_X,RTLS_Y,RTLS_STATE);
    }
    public ArrayList<Client_Info> getClient_path(int ID){
        return db.getClient(ID);
    }
}

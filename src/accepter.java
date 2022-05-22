import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//새로운 클라이언트가 접속했을때 지연되지 않도록 클라이언트를 받고 바로 큐로 넣기만 하는 쓰레드
public class accepter extends Thread {
    private RTLS_Server Server;
    public accepter(RTLS_Server server) {
        setName("accepter");
        Server = server;
    }

    @Override
    public void run() {
        ServerSocket listener = null;
        Socket socket = null;
        try {
            listener = new ServerSocket(3000);
            while(true)
            {
                System.out.println("연결을 기다리고있습니다....");
                socket = listener.accept();
                System.out.println("연결되었습니다.");
                Server.Queue_Socket_Add(socket);
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}

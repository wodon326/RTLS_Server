import java.io.IOException;
import java.net.Socket;

//accepter에서 받은 클라이언트에게 Client_Thread를 배정하는 쓰레드
public class worker extends Thread {
    private RTLS_Server Server;

    public worker(RTLS_Server server) {
        setName("worker");
        Server = server;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            // accepter에서 클라이언트를 큐에 집어 넣었을때 이를 감지하고 처리함
            if (!Server.Queue_isEmpty()) {
                // 클라이언트에게 Client_Thread를 배정하고 쓰레드를 시작
                Socket socket = Server.Queue_Socket_Pop();

                try {
                    Client_Thread thread = new Client_Thread(socket, Server);
                    thread.start();
                    System.out.println("쓰레드 시작.");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}

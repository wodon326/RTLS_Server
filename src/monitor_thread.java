import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

//모니터링에 모든 클라이언트들의 위치,상태를 전송하는 쓰레드
public class monitor_thread extends Thread implements RTLS_Variable {
    private ServerSocket listener_monitor;
    private Socket socket_monitor;
    private OutputStream os_monitor;
    private ObjectOutputStream oos_monitor;
    private InputStream is_monitor = null;
    private ObjectInputStream ois_monitor;
    private byte[] buf_monitor;
    private byte[] buf_monitor_recode = new byte[10];
    private byte[] int_byte = new byte[4];
    private RTLS_Server Server;
    private monitor_receiver monitor_receiver;
    public monitor_thread(RTLS_Server server) throws IOException {
        setName("monitor_thread");
        Server = server;
        //모니터링과 접속
        listener_monitor = new ServerSocket(3001);
        System.out.println("monitor의 연결을 기다리고있습니다....");
        socket_monitor = listener_monitor.accept();
        System.out.println("monitor와 연결되었습니다.");
        os_monitor = socket_monitor.getOutputStream();
        oos_monitor = new ObjectOutputStream(os_monitor);
        is_monitor = socket_monitor.getInputStream();
        ois_monitor = new ObjectInputStream(is_monitor);
        monitor_receiver = new monitor_receiver(oos_monitor,ois_monitor,Server);
        monitor_receiver.start();
    }
    @Override
    public void run() {
        try {
            //모니터링에 모든 클라이언트들의 위치,상태를 1초마다 전송
            while(true)
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                //모니터링에 클라이언트가 있으면 CMD_ALLSTAT패킷을 만들어 전송
                if(Server.get_Clientnum()>0)
                {
                    int i = 0;
                    buf_monitor = new byte[Server.get_Clientnum()*10+4];
                    buf_monitor[0]=STX;
                    buf_monitor[1]=CMD_ALLSTAT;
                    buf_monitor[2]=(byte)Server.get_Clientnum();
                    for(int key : Server.Map_getkeySet()) {
                        buf_monitor_recode[0] = (byte)key;
                        buf_monitor_recode[1] = (byte)((Client_Thread)Server.Map_getValue(key)).get_RTLS_STATE();
                        int_byte = intToBytes(((Client_Thread)Server.Map_getValue(key)).get_RTLS_X());
                        System.arraycopy(int_byte, 0, buf_monitor_recode, 2, 4);
                        int_byte = intToBytes(((Client_Thread)Server.Map_getValue(key)).get_RTLS_Y());
                        System.arraycopy(int_byte, 0, buf_monitor_recode, 6, 4);
                        System.arraycopy(buf_monitor_recode, 0, buf_monitor, 3+i, 10);
                        i+=10;
                    }
                    buf_monitor[Server.get_Clientnum()*10+3]=ETX;
                    oos_monitor.writeObject(buf_monitor);
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    public ObjectOutputStream getOos() {
        return oos_monitor;
    }

    //int -> byte[] 함수
    public static byte[] intToBytes( final int i ) {
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        bytebuffer.putInt(i);
        return bytebuffer.array();
    }
}

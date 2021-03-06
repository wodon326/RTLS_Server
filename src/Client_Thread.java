import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

//클라이언트에서 받은 패킷을 분석하고 처리하는 쓰레드
public class Client_Thread extends Thread implements RTLS_Variable {
    private int Id;
    private Socket socket;
    private InputStream is = null;
    private OutputStream os;
    private ObjectInputStream ois;
    private byte[] buf = new byte[512];
    private ObjectOutputStream oos;
    private int RTLS_STATE = 0;
    private int RTLS_X = 0;
    private int RTLS_Y = 0;
    private int Danger_Time = 0;
    private RTLS_Server Server;

    public Client_Thread(Socket socket1, RTLS_Server server) throws IOException, ClassNotFoundException {
        socket = socket1;
        os = socket.getOutputStream();
        oos = new ObjectOutputStream(os);
        is = socket.getInputStream();
        ois = new ObjectInputStream(is);
        Server = server;
        byte client_ID = 0;
        int i = 1;
        while(true){
            if(!Server.Map_ContainKey(i)) {
                client_ID = (byte) i;
                break;
            }
            i++;
        }
        if(client_ID==0)
            client_ID = (byte) (Server.get_Clientnum()+1);
        Id = client_ID;
        byte[] buf_login = new byte[4];
        buf_login[0] = STX;
        buf_login[1] = CMD_LOGIN;
        buf_login[2] = client_ID;
        buf_login[3] = ETX;

        // 새로운 클라이언트를 hashmap에 추가
        Server.Map_put(Id, this);
        Server.new_Client_access((byte)Id);
        oos.writeObject(buf_login);
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public int get_RTLS_STATE() {
        return RTLS_STATE;
    }

    public int get_RTLS_X() {
        return RTLS_X;
    }

    public int get_RTLS_Y() {
        return RTLS_Y;
    }

    @Override
    public void run() {
        try {
            while (true) {
                buf = (byte[]) ois.readObject();
                byte[] int_byte = new byte[4];
                if (buf[0] == STX && buf[buf.length - 1] == ETX) {
                    switch (buf[1]) {
                        case CMD_EXIT:
                            Id = (int) buf[2];
                            System.out.println(Id + "가 나갔습니다.");
                            Server.Remove_Client(Id);
                            this.interrupt();
                            break;
                        case CMD_LOGIN: // 클라이언트가 접속하면 전송하는 CMD_LOGIN 패킷을 처리
                            // 모든 클라이언트에게 CMD_LOGIN 패킷 전송,기존의 클라이언트의 list를 새로운 클라이언트에게 전송,hashmap에 추가
                            Id = (int) buf[2];

                            // 모든 클라이언트에게 CMD_LOGIN 패킷 전송
                            for (int key : Server.Map_getkeySet()) {
                                if (key != Id) {
                                    ObjectOutputStream send_out = ((Client_Thread) Server.Map_getValue(key)).oos;
                                    send_out.writeObject(buf);
                                }
                            }
                            break;
                        case CMD_SOS:
                            for (int key : Server.Map_getkeySet()) {
                                if (key != Id) {
                                    ObjectOutputStream send_out = ((Client_Thread) Server.Map_getValue(key)).oos;
                                    send_out.writeObject(buf);
                                }
                            }
                            Server.getMonitorOos().writeObject(buf);
                            break;
                        case CMD_MSG: // 클라이언트가 전송하는 CMD_MSG 패킷을 처리
                            for (int key : Server.Map_getkeySet()) {
                                if (key != Id) {
                                    ObjectOutputStream send_out = ((Client_Thread) Server.Map_getValue(key)).oos;
                                    send_out.writeObject(buf);
                                }
                            }
                            break;
                        case CMD_RTDATA: // 클라이언트가 전송하는 CMD_RTDATA 패킷을 처리
                            // 클라이언트에 배정된 쓰레드에 위치,상태를 저장, 데이터베이스에 클라이언트의 위치,상태를 저장

                            // 클라이언트에 배정된 쓰레드에 위치,상태를 저장
                            Id = (int) buf[2];
                            RTLS_STATE = (int) buf[3];
                            System.arraycopy(buf, 4, int_byte, 0, 4);
                            RTLS_X = ByteBuffer.wrap(int_byte).getInt();
                            System.arraycopy(buf, 8, int_byte, 0, 4);
                            RTLS_Y = ByteBuffer.wrap(int_byte).getInt();
                            if((byte)RTLS_STATE==danger)
                                Danger_Time++;
                            else
                                Danger_Time = 0;
                            if(Danger_Time>30){
                                byte[] buf_Clinet_Danger;
                                byte[] data_RTLS = new byte[10];
                                data_RTLS[0] = (byte)Id;
                                data_RTLS[1] = (byte)RTLS_STATE;
                                int_byte = intToBytes(RTLS_X);
                                System.arraycopy(int_byte, 0, data_RTLS, 2, 4);
                                int_byte = intToBytes(RTLS_Y);
                                System.arraycopy(int_byte, 0, data_RTLS, 6, 4);
                                buf_Clinet_Danger = makepacket(CMD_Client_Danger, data_RTLS);
                                Server.getMonitorOos().writeObject(buf_Clinet_Danger);
                                Danger_Time -= 15;
                            }

                            // 데이터베이스에 클라이언트의 위치,상태를 저장
                            Server.addDB(Id, RTLS_X, RTLS_Y, (byte)RTLS_STATE);
                            break;
                        default:
                            break;
                    }
                } else {
                    continue;
                }

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("클라이언트와 채팅 중 오류가 발생했습니다.");
            }
        }
    }
    public static byte[] makepacket(byte cmd, byte[] data) {
        byte[] pack = new byte[data.length + 3];
        pack[0] = STX;
        pack[1] = cmd;
        System.arraycopy(data, 0, pack, 2, data.length);
        pack[pack.length - 1] = ETX;
        return pack;
    }

    // int -> byte[] 함수
    public static byte[] intToBytes(final int i) {
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        bytebuffer.putInt(i);
        return bytebuffer.array();
    }
}

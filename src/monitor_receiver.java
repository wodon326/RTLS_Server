import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class monitor_receiver extends Thread implements RTLS_Variable {
    private ObjectOutputStream oos_monitor;
    private ObjectInputStream ois_monitor;
    private RTLS_Server server;
    private byte[] buf = new byte[512];
    public monitor_receiver(ObjectOutputStream oos_monitor,ObjectInputStream ois_monitor, RTLS_Server server){
        this.oos_monitor = oos_monitor;
        this.ois_monitor = ois_monitor;
        this.server = server;
    }
    @Override
    public void run() {
        try {
            while (true) {
                buf = (byte[]) ois_monitor.readObject();
                if (buf[0] == STX && buf[buf.length - 1] == ETX) {
                    switch (buf[1]) {
                        case CMD_PATH:
                            int id = (int) buf[2];
                            byte [] buf_path = new byte[14];
                            byte [] buf_path_data = new byte[11];
                            byte[] int_byte;
                            ArrayList<Client_Info> get_path = server.getClient_path(id);
                            for(Client_Info path : get_path){
                                buf_path_data[0] = buf[2];
                                buf_path_data[1] = path.getState();
                                int_byte = intToBytes(path.getX());
                                System.arraycopy(int_byte, 0, buf_path_data, 2, 4);
                                int_byte = intToBytes(path.getY());
                                System.arraycopy(int_byte, 0, buf_path_data, 6, 4);
                                if(path == get_path.get(get_path.size()-1)){
                                    buf_path_data[10] = (byte)1;
                                }else{
                                    buf_path_data[10] = (byte)0;
                                }
                                buf_path = makepacket(CMD_PATH, buf_path_data);
                                oos_monitor.writeObject(buf_path);
                            }
                            break;
                        case CMD_RESCUE:
                            id = (int) buf[2];
                            for (int key : server.Map_getkeySet()) {
                                if (key != id) {
                                    ObjectOutputStream send_out = ((Client_Thread) server.Map_getValue(key)).getOos();
                                    send_out.writeObject(buf);
                                }
                            }
                            break;
                        case CMD_LOCATION_ALERTS:
                            for (int key : server.Map_getkeySet()) {
                                ObjectOutputStream send_out = ((Client_Thread) server.Map_getValue(key)).getOos();
                                send_out.writeObject(buf);
                            }
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
            throw new RuntimeException(e);
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
    public static byte[] intToBytes(final int i) {
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        bytebuffer.putInt(i);
        return bytebuffer.array();
    }
}

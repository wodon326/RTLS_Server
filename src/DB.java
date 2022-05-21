import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.HashMap;

public class DB {
    private HashMap<Integer, ArrayList<Client_Info>> db;
    public DB(){
        db = new HashMap<>();
    }
    public ArrayList<Client_Info> getClient(int ID){
        return db.get(ID);
    }
    public void addDB(int ID,int x,int y, byte state){
        db.get(ID).add(new Client_Info(x,y,state));
        if(db.get(ID).size()>100){
            for(int i = 1;i<db.get(ID).size()-1;i+=2){
                db.get(ID).remove(i);
            }
        }
    }
    public void newClient(int ID){
        db.put(ID,new ArrayList<>());
    }
}
class Client_Info {
    private int x;
    private int y;
    private byte state;

    public Client_Info(int x,int y, byte state){
        this.state = state;
        this.x = x;
        this.y = y;
    }

    public byte getState() {
        return state;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
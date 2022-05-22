
public class Client_Info {
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
package lock;


public class Point {
    int x;
    boolean blocked = false;

    public Point() {
    }

    public Point(int x, boolean blocked) {
        this.x = x;
        this.blocked = blocked;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}

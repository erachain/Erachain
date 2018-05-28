package utils;

public class Pair<T, U> {

    private T a;
    private U b;

    public Pair() {
    }

    public Pair(T a, U b) {
        this.a = a;
        this.b = b;
    }

    public T getA() {
        return a;
    }

    public void setA(T a) {
        this.a = a;
    }

    public U getB() {
        return b;
    }

    public void setB(U b) {
        this.b = b;
    }

}

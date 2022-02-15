package nl.han.dea.http;

public class A404Exception extends RuntimeException {
    public A404Exception(String s, NullPointerException n) {
        super(s, n);
    }
}

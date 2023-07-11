package lk.uom.dc;

public interface Threadable extends Runnable {
    String name();

    int priority();
}

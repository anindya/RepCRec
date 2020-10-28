package nyu.adb;

import lombok.extern.slf4j.Slf4j;

//Singleton class to maintain tick across the application
//To be updated after every new instruction read.
//Initialize this as a bean sort of fashion (Spring framework).
@Slf4j
public class Tick {
    private static Tick tickInstance = new Tick();
    Integer time;

    private Tick() {
        this.time = 0;
    }

    public void increaseTick() {
        this.time += 1;
    }

    public static Tick getInstance() {
        return tickInstance;
    }
}

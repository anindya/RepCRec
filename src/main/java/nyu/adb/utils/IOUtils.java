package nyu.adb.utils;

import lombok.extern.slf4j.Slf4j;

//IOUtils singleton class to read instructions from the file or standard input.
//Returns one instruction at a time so the program cannot look ahead.
@Slf4j
public class IOUtils {
    private static final IOUtils ioUtilsInstance = new IOUtils();

    private IOUtils() {

    }

    public String inputFileName;

// Read input file supplied from main and return instructions one at a time.
//    getNextInstruction() {
//    }

    public IOUtils getInstance() {
        return ioUtilsInstance;
    }
}

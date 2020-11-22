package nyu.adb.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

//IOUtils singleton class to read instructions from the file or standard input.
//Returns one instruction at a time so the program cannot look ahead.
@Slf4j
public class IOUtils {
    private static final String LOG_TAG = "IOUtils";
    private static final IOUtils ioUtilsInstance = new IOUtils();
    private boolean stdin = false;
    private Scanner scanner = null;
    private String inputFileName;

    private BufferedReader bufferedReader = null;

    private IOUtils() {

    }

// Read input file supplied from main and return instructions one at a time.
    public String getNextLine() throws IOException{
        String nextLine;
        if (stdin) {
            nextLine = scanner.nextLine();
        } else {
            if (this.bufferedReader == null) {
                log.error("{} : Input file not open when trying to read next line", LOG_TAG);
            }
            try {
                nextLine = this.bufferedReader.readLine();
                while(StringUtils.isEmpty(nextLine)) {
                    nextLine = this.bufferedReader.readLine();
                }
            } catch (IOException e) {
                log.error("{} : IOException, file read error {}", LOG_TAG, e.getStackTrace());
                throw e;
            }
        }
        log.debug("Next line read : {}", nextLine);
        return nextLine;
    }

    public void setStdIn(boolean stdIn) {
        this.stdin = true;
        scanner = new Scanner(System.in);

    }

    /*
    // Returns true if the inputFile being set is found and opened as a fileInputStream
    * */

    public boolean setAndOpenInputFile(String fileName) {
        log.info("{} : Opening input file = {} ", LOG_TAG, fileName);

        this.inputFileName = fileName;
        try {
            this.bufferedReader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            log.error("{} : Input file = {} not found, error {}", LOG_TAG, fileName, e.getStackTrace());
            return false;
        }
        return true;
    }

    public static IOUtils getInstance() {
        return ioUtilsInstance;
    }

}



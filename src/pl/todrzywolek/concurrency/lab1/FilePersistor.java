package pl.todrzywolek.concurrency.lab1;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FilePersistor {

    public static void saveToFile(List<Integer> histogram) throws IOException {
        PrintWriter pw = new PrintWriter(new FileOutputStream("histogram.txt"));
        for (Integer result : histogram)
            pw.println(result);
        pw.close();
    }
}

package org.kurator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by lowery on 8/13/16.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader reader = null;

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Paths.get("/home/lowery/test").register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent event : key.pollEvents()) {
                System.out.println(event.kind() + " : " + event.context());
                if (event.kind() == ENTRY_CREATE) {
                    reader = new BufferedReader(new FileReader("/home/lowery/test/" + event.context()));
                }

                else if (event.kind() == ENTRY_MODIFY) {
                    String line;
                    while (reader != null && (line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }
            key.reset();
        }

    }
}

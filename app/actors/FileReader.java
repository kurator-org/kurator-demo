package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import messages.OutputData;
import messages.ReadFile;
import messages.ReadMore;
import messages.RegisterListener;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lowery on 8/2/16.
 */
public class FileReader extends UntypedActor {
    private BufferedReader reader;
    private List<ActorRef> listeners = new ArrayList<>();

    public FileReader() {
        System.out.println("Created new instance of file reader: " + self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ReadFile) {
            FileInputStream fis = new FileInputStream(((ReadFile) message).filePath);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

            self().tell(new ReadMore(), sender());
        } else if (message instanceof ReadMore) {
            String line = reader.readLine();

            if (line != null) {
                for (ActorRef listener : listeners) {
                    listener.tell(new OutputData(line), self());
                }

                getContext().system().scheduler().scheduleOnce(Duration.create(10, TimeUnit.MILLISECONDS),
                        self(), new ReadMore(), getContext().system().dispatcher(), null);

            } else {
                System.out.println("Done!");
            }
        } else if (message instanceof RegisterListener) {
            System.out.println("Registered listener");
            listeners.add(sender());
        }
    }

    public static Props props() {
        return Props.create(new Creator<FileReader>() {
            private static final long serialVersionUID = 1L;

            @Override
            public FileReader create() throws Exception {
                return new FileReader();
            }
        });
    }
}

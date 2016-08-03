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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by lowery on 8/2/16.
 */
public class WordCountFileReader extends UntypedActor {
    private BufferedReader reader;
    private ActorRef listener;

    Map<String, Long> count = new HashMap<>();
    int[] maxCounts = new int[20];

    public WordCountFileReader() {
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
                StringTokenizer tokenizer = new StringTokenizer(line, " ");
                while (tokenizer.hasMoreTokens()) {
                    String word = tokenizer.nextToken().toLowerCase();

                    if (!count.containsKey(word)) {
                        count.put(word, 1L);
                    } else {
                        count.put(word, (count.get(word) + 1));
                    }
                }

                StringBuilder sb = new StringBuilder();

                Map<String, Long> sorted = MapUtil.sortByValue(count);

                int i = 0;
                for (String word : sorted.keySet()) {
                    i++;
                    sb.append(word).append(": ").append(count.get(word)).append("<br />");
                    if (i > 24)
                        break;
                }

                    listener.tell(new OutputData(sb.toString()), self());

                getContext().system().scheduler().scheduleOnce(Duration.create(50, TimeUnit.MILLISECONDS),
                        self(), new ReadMore(), getContext().system().dispatcher(), null);

            } else {
                System.out.println("Done!");
            }
        } else if (message instanceof RegisterListener) {
            System.out.println("Registered listener");
            listener = sender();
        }
    }

    public static Props props() {
        return Props.create(new Creator<WordCountFileReader>() {
            private static final long serialVersionUID = 1L;

            @Override
            public WordCountFileReader create() throws Exception {
                return new WordCountFileReader();
            }
        });
    }
}

class MapUtil
{
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}

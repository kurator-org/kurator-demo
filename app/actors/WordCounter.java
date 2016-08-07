package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import controllers.DeregisterListener;
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
public class WordCounter extends UntypedActor {
    private ActorRef listener;

    Map<String, Long> count = new HashMap<>();
    int[] maxCounts = new int[20];

    public WordCounter() {
        System.out.println("Created new instance of WordCounter actor: " + self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof OutputData) {
            String line = ((OutputData) message).line;

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

                // Transform line into a count of words
                if (listener != null)
                    listener.tell(new OutputData(sb.toString()), self());
            }
        } else if (message instanceof RegisterListener) {
                listener = ((RegisterListener) message).listener;
            System.out.println("Registered listener " + listener + " with actor " + self());
        } else if (message instanceof DeregisterListener) {
            listener = null; // TODO: Support for multiple listeners
            System.out.println("Deregistered listener " + listener + " from actor " + self());
        }
    }

    public static Props props() {
        return Props.create(new Creator<WordCounter>() {
            private static final long serialVersionUID = 1L;

            @Override
            public WordCounter create() throws Exception {
                return new WordCounter();
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

package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import controllers.DeregisterListener;
import messages.OutputData;
import messages.RegisterListener;
import messages.SetStrategy;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.springframework.beans.factory.annotation.Configurable;
import transformers.PigLatin;
import transformers.ReverseWord;
import transformers.StringTransformerStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by lowery on 8/7/16.
 */
public class StringTransformer extends UntypedActor {
    private ActorRef listener;
    private StringTransformerStrategy strategy = new ReverseWord(); // Default
    private Map<String, StringTransformerStrategy> strategyMap;

    public StringTransformer() {
        System.out.println("Created new instance of StringTransformer: " + self() + ", defaulting to ReverseWord strategy");

        strategyMap = new HashMap<>();
        strategyMap.put("PigLatin", new PigLatin());
        strategyMap.put("ReverseWord", new ReverseWord());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof OutputData) {
            String line = ((OutputData) message).line;
            String output = strategy.transform(line);

            if (listener != null)
                listener.tell(new OutputData(output), self());
        } else if (message instanceof RegisterListener) {
            listener = ((RegisterListener) message).listener;
            System.out.println("Registered listener " + listener + " with actor " + self());
        } else if (message instanceof DeregisterListener) {
            listener = null; // TODO: Support for multiple listeners
            System.out.println("Deregistered listener " + listener + " from actor " + self());
        } else if (message instanceof SetStrategy) {
            String name = ((SetStrategy) message).strategy;
            strategy = strategyMap.get(name);
            System.out.println("Changing StringTransformer strategy to " + name);
        }
    }

    public static Props props() {
        return Props.create(new Creator<StringTransformer>() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringTransformer create() throws Exception {
                return new StringTransformer();
            }
        });
    }
}

package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import controllers.DeregisterListener;
import messages.OutputData;
import messages.RegisterListener;

import java.util.StringTokenizer;

/**
 * Created by lowery on 8/7/16.
 */
public class StringReverse extends UntypedActor {
    private ActorRef listener;

    public StringReverse() {
        System.out.println("Created new instance of StringReverse: " + self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof OutputData) {
            String line = ((OutputData) message).line;
            StringBuilder reversed = new StringBuilder();

            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken().toLowerCase();

                for (int i = word.length(); i > 0; i--) {
                    reversed.append(word.charAt(i-1));
                }

                reversed.append(" ");
            }

            reversed.append("\n");

            if (listener != null)
                listener.tell(new OutputData(reversed.toString()), self());
        } else if (message instanceof RegisterListener) {
            listener = ((RegisterListener) message).listener;
            System.out.println("Registered listener " + listener + " with actor " + self());
        } else if (message instanceof DeregisterListener) {
            listener = null; // TODO: Support for multiple listeners
            System.out.println("Deregistered listener " + listener + " from actor " + self());
        }
    }

    public static Props props() {
        return Props.create(new Creator<StringReverse>() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringReverse create() throws Exception {
                return new StringReverse();
            }
        });
    }
}

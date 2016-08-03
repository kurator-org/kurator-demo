package actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import messages.OutputData;
import messages.RegisterListener;

/**
 * Created by lowery on 8/2/16.
 */
public class ConsoleActor extends UntypedActor {

    ActorRef outputWriter;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof OutputData && outputWriter != null) {
            outputWriter.tell(message, sender());
        } else if (message instanceof RegisterListener) {
            outputWriter = sender();
        }
    }
}

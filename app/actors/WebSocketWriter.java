package actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import messages.OutputData;
import messages.RegisterListener;

/**
 * Created by lowery on 8/2/16.
 */
public class WebSocketWriter extends UntypedActor {

    private final ActorRef out;
    private final ActorRef listensTo;

    public WebSocketWriter(ActorRef out, ActorRef listensTo) {
        this.out = out;
        this.listensTo = listensTo;
    }

    @Override
    public void preStart() throws Exception {
        System.out.println("Web socket actor pre start");
        listensTo.tell(new RegisterListener(), self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof OutputData) {
            out.tell(((OutputData) message).line, ActorRef.noSender());
        }
    }
}

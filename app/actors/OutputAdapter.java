package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import controllers.DeregisterListener;
import messages.OutputData;
import messages.RegisterListener;

/**
 * Created by lowery on 8/7/16.
 */
public class OutputAdapter extends UntypedActor {
    private ActorRef listener;

    public OutputAdapter() {
        System.out.println("Created new instance of OutputAdapter: " + self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof OutputData) {
            if (listener != null)
                listener.tell(((OutputData) message).line, self());
        } else if (message instanceof RegisterListener) {
            listener = ((RegisterListener) message).listener;
            System.out.println("Registered listener " + listener + " with actor " + self());
        } else if (message instanceof DeregisterListener) {
            listener = null; // TODO: Support for multiple listeners
            System.out.println("Deregistered listener " + listener + " from actor " + self());
        }
    }

    public static Props props() {
        return Props.create(new Creator<OutputAdapter>() {
            private static final long serialVersionUID = 1L;

            @Override
            public OutputAdapter create() throws Exception {
                return new OutputAdapter();
            }
        });
    }
}

package actors;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import messages.OutputData;
import messages.ReadFile;
import messages.ReadMore;
import messages.RegisterListener;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lowery on 8/2/16.
 */
public class HelloActor extends UntypedActor {

    private ActorRef listener;

    public HelloActor() {
        System.out.println("Created new instance of hello actor: " + self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ReadFile) {
            Cancellable cancellable = getContext().system().scheduler().schedule(Duration.Zero(),
                    Duration.create(50, TimeUnit.MILLISECONDS), () -> { listener.tell(new OutputData("Testing123"), self()); } ,
                    getContext().system().dispatcher());

        } else if (message instanceof RegisterListener) {
            System.out.println("Registered listener");
            listener = sender();
        }
    }

    public static Props props() {
        return Props.create(new Creator<HelloActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public HelloActor create() throws Exception {
                return new HelloActor();
            }
        });
    }
}

package org.kurator.akka.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.kurator.akka.config.ActorConfig;
import org.kurator.akka.config.WorkflowConfig;
import org.kurator.akka.messages.RegisterListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lowery on 8/13/16.
 */
public class Workflow extends UntypedActor {
    private final Map<String, ActorRef> actors = new HashMap<>();
    private final WorkflowConfig config;

    public Workflow(WorkflowConfig config) {
        this.config = config;
    }

    @Override
    public void preStart() throws Exception {
        for (ActorConfig actorConfig : config.getActors()) {
            ActorRef actorRef;

            if (actorConfig.equals(config.getInputActor())) {
                HashMap<String, Object> settings = new HashMap<>();
                settings.put("url", "http://ipt.vertnet.org:8080/ipt/archive.do?r=ccber_mammals");
                Props props = Props.create(actorConfig.actorClass, actorConfig.getConfig(), settings);
                actorRef = getContext().system().actorOf(props);
            } else {
                Props props = Props.create(actorConfig.actorClass, actorConfig.getConfig(), new HashMap<>());
                actorRef = getContext().system().actorOf(props);
            }

            actors.put(actorConfig.getName(), actorRef);
        }

        for (ActorConfig actorConfig : config.getActors()) {
            String name = actorConfig.getName();
            ActorRef upstream = actors.get(name);

            if (actorConfig.getListeners() != null) {
                for (ActorConfig listenerConfig : actorConfig.getListeners()) {
                    ActorRef listener = actors.get(listenerConfig.getName());
                    upstream.tell(new RegisterListener(listener), self());
                }
            }
        }

        System.out.println(actors);
    }

    @Override
    public void onReceive(Object message) throws Exception {


        String input = config.getInputActor().getName();

        System.out.println(input);
        Map<String, Object> options = new HashMap<>();
        options.put("url", "http://ipt.vertnet.org:8080/ipt/archive.do?r=ccber_mammals");
        actors.get(input).tell(options, self());
    }
}

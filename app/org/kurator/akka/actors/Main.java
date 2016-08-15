package org.kurator.akka.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Created by lowery on 8/13/16.
 */
public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("Count");

        ActorRef counter = system.actorOf(Props.create(Workflow.class), "workflow");
        system.actorOf(Props.create(Terminator.class, counter), "terminator");

        system.stop(counter);
    }
}

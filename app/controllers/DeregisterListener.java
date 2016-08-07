package controllers;

import akka.actor.ActorRef;

/**
 * Created by lowery on 8/7/16.
 */
public class DeregisterListener {
    public final ActorRef listener;

    public DeregisterListener(ActorRef listener) {
        this.listener = listener;
    }
}

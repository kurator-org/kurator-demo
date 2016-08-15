package org.kurator.akka.config;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkflowConfig {
    
    private List<ActorConfig> actors = new LinkedList<ActorConfig>();
    private ActorConfig inputActor;
    private Map<String,Object> settings;
    private String name;

    public void setActors(List<ActorConfig> actors) {
        if (actors != null) {
            this.actors = actors;
        }
    }
    
    public List<ActorConfig> getActors() {
        return actors;
    }
    
    public void setInputActor(ActorConfig inputActor) {
        this.inputActor = inputActor;
    }
    
    public ActorConfig getInputActor() {
        return inputActor;
    }
    
    public void setParameters(Map<String,Object> settings) {
        this.settings = settings;
    }
    
    public Map<String,Object> getParameters() { 
        return settings;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

package org.kurator.akka.config;

import akka.actor.UntypedActor;

import java.util.*;

public class ActorConfig {

    public Class<? extends UntypedActor> actorClass;
    private List<ActorConfig> listeners;
    private Map<String,Object> defaults = new HashMap<String,Object>();
    private Map<String,Object> parameters = new HashMap<String,Object>();
    protected Map<String,Object> config = new HashMap<String,Object>();
    private Map<String, Object> inputs = new HashMap<String,Object>();

    private List<ActorConfig> listensTo = new ArrayList<>();

    public ActorConfig() {
    }

    public ActorConfig actorClass(Class<? extends UntypedActor> actorClass) {
        this.actorClass = actorClass;
        return this;
    }

    public ActorConfig setName(String name) {
        config.put("name", name);
        return this;
    }
    
    public String getName() {
        return (String) config.get("name");
    }
    
  
    @SuppressWarnings("unchecked")
    public void setActorClass(String actorClassName) throws ClassNotFoundException {
        this.actorClass = (Class<? extends UntypedActor>) Class.forName(actorClassName);
    }
    
    public Class<? extends UntypedActor> actorClass() {
        return actorClass;
    }
    
    public ActorConfig listener(ActorConfig listener) {
        if (listeners == null) {
            listeners = new LinkedList<ActorConfig>();
        }
        listeners.add(listener);
        return this;
    }

    public ActorConfig listensTo(ActorConfig sender) {
        sender.listener(this);
        return this;
    }

    public List<ActorConfig> getListensTo() {
        return listensTo;
    }
        
    public ActorConfig param(String parameter, Object value) {
        if (parameters == null) {
            parameters = new HashMap<String,Object>();
        }
        parameters.put(parameter, value);
        return this;
    }
    
    public void setDefaults(Map<String, Object> defaults) {
        this.defaults = defaults;
    }
    
    public Map<String, Object> getDefaults() {
        return defaults;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setListeners(List<ActorConfig> listeners) {
        this.listeners = listeners;
    }
    
    public List<ActorConfig> getListeners() {        
        return listeners;
    }

    
    public void setInputs(Map<String,Object> inputs) {
        this.inputs = inputs;
    }
    
    public Map<String,Object> getInputs() {
        return inputs;
    }

    public ActorConfig input(String inputName, String localName) {
        inputs.put(inputName, localName);
        return this;
    }

    public ActorConfig input(String name) {
        inputs.put(name, name);
        return this;
    }
    
    public void setListensTo(List<ActorConfig> senders) {
        for (ActorConfig sender : senders) {
            sender.listener(this);
            listensTo.add(sender);
        }
    }
    
    public Map<String, Object> getConfig() {
        return config;
    }

    public ActorConfig config(String property, Object value) {
        config.put(property, value);
        return this;
    }
    
    
    public void setPythonClass(String value)            { config.put("pythonClass", value); }
    public void setPojoClass(String value)              { config.put("pojoClass", value); }
    public void setInputStreamProperty(String value)    { config.put("inputStreamProperty", value); }
    public void setOutputStreamProperty(String value)   { config.put("outputStreamProperty", value); }
    public void setErrorStreamProperty(String value)    { config.put("errorStreamProperty", value); }
    public void setEndOnNullOutput(Boolean value)       { config.put("endOnNullOutput", value); }
}

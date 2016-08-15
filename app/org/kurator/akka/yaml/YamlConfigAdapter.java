package org.kurator.akka.yaml;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import org.kurator.akka.config.ActorConfig;
import org.kurator.akka.config.PythonActorConfig;
import org.kurator.akka.config.WorkflowConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lowery on 8/13/16.
 */
public class YamlConfigAdapter {
    private Map<String, Object> config;

    private Map<String, Object> types = new HashMap<>();
    private List<Map<String, Object>> components = new ArrayList<>();

    private Map<String, Object> actors = new HashMap<>();
    private Map<String, Object> workflow;

    public void parseYaml(InputStream yamlStream) {
        try {
            YamlReader reader = new YamlReader(new InputStreamReader(yamlStream));
            reader.getConfig().setClassTag("ref", Ref.class);
            reader.getConfig().setScalarSerializer(Ref.class, new RefSerializer());

            config = (Map<String, Object>) reader.read();

            parseTypes((List<Map<String, Object>>) config.get("types"));
            parseComponents((List<Map<String, Object>>) config.get("components"));

            loadImports();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImports() throws IOException {
        List<String> imports = (List<String>) config.get("imports");

        for (String yamlFile : imports) {
            URL resourceUrl = new URL(yamlFile);

            InputStream yamlStream = resourceUrl.openConnection().getInputStream();

            YamlReader reader = new YamlReader(new InputStreamReader(yamlStream));
            reader.getConfig().setClassTag("ref", String.class);

            Map<String, Object> importedConfig = (Map<String, Object>) reader.read();

            List<Map<String, Object>> importedTypes = (List<Map<String, Object>>) importedConfig.get("types");
            List<Map<String, Object>> importedComponents = (List<Map<String, Object>>) importedConfig.get("components");

            parseTypes(importedTypes);
            parseComponents(importedComponents);
        }
    }

    private void parseTypes(List<Map<String, Object>> conf) {
        if (conf != null) {
            for (Map<String, Object> type : conf) {
                if (type.containsKey("properties")) {
                    Map<String, Object> properties = (Map<String, Object>) type.get("properties");

                    if (properties.containsKey("actorClass")) {
                        types.put((String) type.get("id"), properties.get("actorClass"));
                    }
                } else if (type.containsKey("className")) {
                    types.put((String) type.get("id"), type.get("className"));
                }

            }
        }
    }

    private void parseComponents(List<Map<String, Object>> conf) {
        if (conf != null) {
            for (Map<String, Object> component : conf) {
                String type = (String) component.get("type");
                if (type.equals("Workflow")) {
                    workflow = component;
                } else if (type.equals("PythonActor")) {
                    actors.put((String) component.get("id"), component);
                }
            }
        }
    }

    public WorkflowConfig getWorkflowConfig() {
        Map<String, Object> properties = (Map<String, Object>) workflow.get("properties");
        List<Ref> refs = (List<Ref>) properties.get("actors");

        WorkflowConfig workflowConfig = new WorkflowConfig();
        Map<String, ActorConfig> actors = new HashMap<>();

        workflowConfig.setName((String) workflow.get("id"));

        int i = 0;
        for (Ref ref : refs) {
            String name = ref.getValue();
            
            ActorConfig actorConfig = parseActor(name);
            actors.put(name, actorConfig);

            if (i == 0) {
                workflowConfig.setInputActor(actorConfig);
            }
        }

       List<ActorConfig> actorConfigs = addListeners(actors);

        workflowConfig.setActors(actorConfigs);

        Map<String, Object> parameters =
                (Map<String, Object>) properties.get("parameters");

        for (String key : parameters.keySet()) {
            Map<String, Object> parameter = (Map<String, Object>) parameters.get(key);

            Ref paramRef = (Ref) parameter.get("actor");
            ActorConfig paramActor = parseActor(paramRef.getValue());

            parameter.put("actor", paramActor);
        }

        workflowConfig.setParameters(parameters);

        return workflowConfig;
    }

    private List<ActorConfig> addListeners(Map<String, ActorConfig> actorConfigs) {
        List<ActorConfig> actorConfigList = new ArrayList<>();

        for (String name : actorConfigs.keySet()) {
            Map<String, Object> actor = (Map<String, Object>) actors.get(name);
            Map<String, Object> properties = (Map<String, Object>) actor.get("properties");

            ActorConfig actorConfig = actorConfigs.get(name);

            List<Ref> refs = (List<Ref>) properties.get("listensTo");
            List<ActorConfig> listensTo = new ArrayList<>();
            System.out.println(refs);

            if (refs != null) {
                for (Ref ref : refs) {
                    listensTo.add(actorConfigs.get(ref.getValue()));
                }

                actorConfig.setListensTo(listensTo);
            }

            actorConfigList.add(actorConfig);
        }

        return actorConfigList;
    }

    private ActorConfig parseActor(String name) {
        Map<String, Object> actor = (Map<String, Object>) actors.get(name);
        Map<String, Object> properties = (Map<String, Object>) actor.get("properties");

        String type = (String) actor.get("type");

        if (type.equals("PythonActor")) {
            PythonActorConfig config = new PythonActorConfig();
            config.setName(name);

            try {
                config.setActorClass((String) types.get(type));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            config.setInputs((Map<String, Object>) properties.get("inputs"));
            config.setParameters((Map<String, Object>) properties.get("parameters"));

            config.setCode((String) properties.get("code"));
            config.setModule((String) properties.get("module"));

            config.setOnStart((String) properties.get("onStart"));
            config.setOnData((String) properties.get("onData"));
            config.setOnEnd((String) properties.get("onEnd"));

            return config;
        } else {
            throw new RuntimeException("Unsupported type: " + type);
        }
    }

    public void writeYaml(WorkflowConfig workflowConfig) throws IOException {
        YamlWriter writer = new YamlWriter(new FileWriter("out.yaml"));

        writer.getConfig().setClassTag("ref", Ref.class);
        writer.getConfig().setScalarSerializer(Ref.class, new RefSerializer());

        config = new HashMap<>();

        List<String> imports = new ArrayList<>();
        imports.add("classpath:/org/kurator/akka/types.yaml");

        config.put("imports", imports);

        List<ActorConfig> actorConfigs = workflowConfig.getActors();
        List<Map<String, Object>> components = new ArrayList<>();

        List<Ref> actors = new ArrayList<>();

        for (ActorConfig actorConfig : actorConfigs) {
            if (actorConfig instanceof PythonActorConfig) {
                PythonActorConfig pythonActorConfig = (PythonActorConfig) actorConfig;

                Map<String, Object> properties = new HashMap<>();
                for (String key : pythonActorConfig.getConfig().keySet()) {
                    if (pythonActorConfig.getConfig().get(key) != null) {
                        Object val = pythonActorConfig.getConfig().get(key);
                        properties.put(key, val);
                    }
                }

                if (pythonActorConfig.getListensTo() != null && !pythonActorConfig.getListensTo().isEmpty()) {
                    List<Ref> listensToRefs = new ArrayList<>();

                    for (ActorConfig listensTo : pythonActorConfig.getListensTo()) {
                        Ref ref = new Ref();
                        ref.setValue(listensTo.getName());
                        listensToRefs.add(ref);
                    }

                    properties.put("listensTo", listensToRefs);
                }

                if (pythonActorConfig.getInputs() != null) {
                    properties.put("inputs", pythonActorConfig.getInputs());
                }

                if (pythonActorConfig.getParameters() != null) {
                    properties.put("parameters", pythonActorConfig.getParameters());
                }

                Map<String, Object> actor = new HashMap<>();
                actor.put("id", pythonActorConfig.getName());
                actor.put("type", "PythonActor");
                actor.put("properties", properties);

                components.add(actor);

                Ref ref = new Ref();
                ref.setValue(pythonActorConfig.getName());
                actors.add(ref);
            }
        }

        Map<String, Object> properties = new HashMap<>();

        properties.put("actors", actors);

        Map<String, Object> parameters = workflowConfig.getParameters();
        for (String key : parameters.keySet()) {
            Map<String, Object> parameter = (Map<String, Object>) parameters.get(key);
            ActorConfig actorConfig = (ActorConfig) parameter.get("actor");

            Ref ref = new Ref();
            ref.setValue(actorConfig.getName());
            parameter.put("actor", ref);
        }

        properties.put("parameters", parameters);

        Map<String, Object> workflow = new HashMap<>();
        workflow.put("id", workflowConfig.getName());
        workflow.put("type", "Workflow");
        workflow.put("properties", properties);

        components.add(workflow);

        config.put("components", components);

        writer.write(config);
        writer.close();
    }
}

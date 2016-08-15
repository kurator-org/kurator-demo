package org.kurator;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.kurator.akka.actors.Workflow;
import org.kurator.akka.config.WorkflowConfig;
import org.kurator.akka.yaml.*;
import org.kurator.exceptions.KuratorException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by lowery on 8/13/16.
 */
public class YamlWorkflowRunner extends WorkflowRunner {
    private final InputStream yamlStream;
    private WorkflowConfig workflowConfig;

    public YamlWorkflowRunner(String yamlFile) throws KuratorException {

        try {
            ConfigurableStreamHandlerFactory factory = new ConfigurableStreamHandlerFactory("classpath", new Handler());
            URL.setURLStreamHandlerFactory(factory);

            final URL resourceUrl = new URL(yamlFile);
            yamlStream = resourceUrl.openConnection().getInputStream();

            YamlConfigAdapter reader = new YamlConfigAdapter();
            reader.parseYaml(yamlStream);
            workflowConfig = reader.getWorkflowConfig();

            YamlConfigAdapter writer = new YamlConfigAdapter();
            writer.writeYaml(workflowConfig);

        } catch (IOException e) {
            throw new KuratorException(e);
        }
    }

    public YamlWorkflowRunner(InputStream yamlStream) throws KuratorException {
        this.yamlStream = yamlStream;

        YamlConfigAdapter configAdapter = new YamlConfigAdapter();
        configAdapter.parseYaml(yamlStream);
    }

    @Override
    public WorkflowRunner run() throws Exception {
        ActorSystem system = ActorSystem.create("org/kurator");
        ActorRef workflow = system.actorOf(Props.create(Workflow.class, workflowConfig), "workflow");

        workflow.tell("start", ActorRef.noSender());
        system.awaitTermination();

        return this;
    }
}

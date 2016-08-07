package controllers;

import actors.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import javax.inject.*;

import akka.actor.Props;
import akka.actor.dsl.Creators;
import akka.japi.Creator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import messages.ReadFile;
import messages.RegisterListener;
import play.libs.Json;
import play.mvc.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import scala.concurrent.ExecutionContextExecutor;
import views.html.console;
import views.html.demo;

import static akka.pattern.Patterns.ask;

/**
 * This controller contains an action that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.
 *
 * @param actorSystem We need the {@link ActorSystem}'s
 * {@link Scheduler} to run code after a delay.
 * @param exec We need a Java {@link Executor} to apply the result
 * of the {@link CompletableFuture} and a Scala
 * {@link ExecutionContext} so we can use the Akka {@link Scheduler}.
 * An {@link ExecutionContextExecutor} implements both interfaces.
 */
@Singleton
public class AsyncController extends Controller {

    private final ActorSystem actorSystem;
    private final ExecutionContextExecutor exec;

    Map<String, Props> actorRegistry = new HashMap<>();

    Map<String, ActorRef> actors = new HashMap<>();
    List<ActorRef> workflow = new ArrayList<>();

    @Inject
    public AsyncController(ActorSystem actorSystem, ExecutionContextExecutor exec) {
        this.actorSystem = actorSystem;
        this.exec = exec;

        actorRegistry.put("FileReader", FileReader.props());
        actorRegistry.put("WordCounter", WordCounter.props());
        actorRegistry.put("StringReverse", StringReverse.props());
        actorRegistry.put("OutputAdapter", OutputAdapter.props());
    }

    public Result connect(String source, String target) {
        actors.get(source).tell(new RegisterListener(actors.get(target)), actors.get(target));
        return ok("Connect: " + target + " listensTo " + source);
    }

    public Result detach(String source, String target) {
        actors.get(source).tell(new DeregisterListener(actors.get(target)), actors.get(target));
        return ok("Detach: " + target + " listensTo " + source);
    }


    public Result demo() {
        return ok(demo.render());
    }

    public LegacyWebSocket<String> socket() {
        final ActorRef last = workflow.get(workflow.size()-1);

        return WebSocket.withActor(new Function<ActorRef, Props>() {
            @Override
            public Props apply(ActorRef actorRef) {
                return Props.create(WebSocketWriter.class, actorRef, last);
            }
        });
    }

    public Result console() {
        return ok(console.render());
    }

    public Result add(String name) {
        Props props = actorRegistry.get(name);
        ActorRef actorRef = actorSystem.actorOf(props);

        workflow.add(actorRef);
        actors.put(name, actorRef);

        ObjectNode json = Json.newObject();

        String type = "actor";

        if (name.equals("FileReader")) {
            type = "input";
        } else if (name.equals("OutputAdapter")) {
            type = "output";
        }

        json.put("actor", name);
        json.put("type", type);

        return ok(json);
    }

    public Result remove(String name) {
        ActorRef actorRef = actors.remove(name);

        //int actorPos = workflow.indexOf(actorRef);
        //ActorRef upstream = workflow.get(actorPos-1);
        //ActorRef downstream = workflow.get(actorPos+1);

        //upstream.tell(new RegisterListener(downstream), downstream);
        workflow.remove(actorRef);

        return ok("Actor removed: " + actorRef);
    }

    public Result upload() {
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> file = body.getFile("file");

        System.out.println(file.getFile().getAbsolutePath());
        ReadFile message = new ReadFile(file.getFile().getAbsolutePath());

        workflow.get(0).tell(message, ActorRef.noSender());

        return ok("File upload success!");
    }

    public Result list() {
        return ok(Json.toJson(actorRegistry.keySet()));
    }
}

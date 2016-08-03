package controllers;

import actors.FileReader;
import actors.HelloActor;
import actors.WebSocketWriter;
import actors.WordCountFileReader;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import javax.inject.*;

import akka.actor.Props;
import akka.japi.Creator;
import messages.ReadFile;
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
    List<ActorRef> workflow = new ArrayList<>();

    ActorRef outputActor;

    @Inject
    public AsyncController(ActorSystem actorSystem, ExecutionContextExecutor exec) {
        this.actorSystem = actorSystem;
        this.exec = exec;

        actorRegistry.put("HelloActor", HelloActor.props());
        actorRegistry.put("FileReader", FileReader.props());
        actorRegistry.put("WordCountFileReader", WordCountFileReader.props());
    }


    public Result demo() {
        return ok(demo.render());
    }

    public LegacyWebSocket<String> socket() {
        return WebSocket.withActor(new Function<ActorRef, Props>() {
            @Override
            public Props apply(ActorRef actorRef) {
                ActorRef last = workflow.get(workflow.size()-1);
                System.out.println("Registering actor " + last + " with websocket.");
                return Props.create(WebSocketWriter.class, actorRef, last);
            }
        });
    }

    public Result console() {
        return ok(console.render());
    }

    /*
    public CompletionStage<Result> actor(String name, String message) {
        Props props = actorRegistry.get(name);
        ActorRef actorRef = actorSystem.actorOf(props);

        return FutureConverters.toJava(ask(actorRef, message, 1000))
                .thenApply(response -> ok((String) response));
    }*/

    public Result actor(String name) {
        Props props = actorRegistry.get(name);
        ActorRef actorRef = actorSystem.actorOf(props);

        workflow.add(actorRef);

        return ok("Actor created: " + actorRef);
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

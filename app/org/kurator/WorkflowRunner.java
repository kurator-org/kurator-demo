package org.kurator;

import java.io.PrintStream;
import java.util.Map;

/**
 * Created by lowery on 8/13/16.
 */
public class WorkflowRunner {
    public WorkflowRunner apply(Map<String, Object> settings) throws Exception {
        return this;
    }

    public WorkflowRunner outputStream(PrintStream outStream) throws Exception {
        return this;
    }

    public WorkflowRunner errorStream(PrintStream errStream) throws Exception {
        return this;
    }

    public WorkflowRunner run() throws Exception {
        return this;
    }
}

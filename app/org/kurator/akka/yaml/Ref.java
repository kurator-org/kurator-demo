package org.kurator.akka.yaml;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;

/**
 * Created by lowery on 8/13/16.
 */
public class Ref {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

class RefSerializer implements ScalarSerializer<Ref> {

    @Override
    public String write(Ref ref) throws YamlException {
        return ref.getValue();
    }

    @Override
    public Ref read(String value) throws YamlException {
        Ref ref = new Ref();
        ref.setValue(value);
        return ref;
    }
}


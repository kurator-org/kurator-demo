package transformers;

import java.util.StringTokenizer;

/**
 * Created by lowery on 8/8/16.
 */
public class ReverseWord implements StringTransformerStrategy {
    @Override
    public String transform(String line) {
        StringBuilder reversed = new StringBuilder();

        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase();

            for (int i = word.length(); i > 0; i--) {
                reversed.append(word.charAt(i-1));
            }

            reversed.append(" ");
        }

        reversed.append("\n");

        return reversed.toString();
    }
}

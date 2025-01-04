package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;

public interface Command {
    /**
     * Executes the command and appends the result to the provided output.
     *
     * @param output the output array to which the result of the command execution will be appended
     */
    void execute(ArrayNode output);
}

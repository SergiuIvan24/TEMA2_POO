package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;

public interface Command {
    /**
     * Executa comanda si eventual adauga rezultatul in output.
     *
     * @param output output-ul comenzii
     */
    void execute(ArrayNode output);
}

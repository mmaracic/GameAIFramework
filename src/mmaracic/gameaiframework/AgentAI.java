/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * The class which needs to be extended in order to implement agent's (Pacman/Ghost) intelligence
 * @author Marijo
 */
public class AgentAI {
    /**
     * Method that contains the AI behaviour.
     * @param moves List of allowed moves, each move is an array containing x and y component Possible values:-1, 0, 1 for both components.
     * @param mySurroundings List of objects, agents and metadata surrounding the agent .
     * @param myInfo Information about the current agent.
     * @return index of the move that has been chosen from the offered list
     */
    public int decideMove(ArrayList<int[]> moves, PacmanVisibleWorld mySurroundings, WorldEntity.WorldEntityInfo myInfo)
    {
        Date now = new Date();
        Random r = new Random(now.getTime());
        int choice = r.nextInt(moves.size());
        return choice;
    }
    
    /**
     * Method reports debugging info to the central object tasked with application operation info
     * @param s String that contains debugging information.
     */
    public final void printStatus(String s)
    {
        MessageCollector.putMessage(this.getClass().getSimpleName(), s);
    }
}

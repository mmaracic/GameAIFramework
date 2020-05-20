/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *Class that describes the surroundings of the agent.
 * It is possible to get a list of world objects at each element of the surroundings
 * as well as the metadata that the agent left there
 * @author Marijo
 */

public final class PacmanVisibleWorld {
    /**
     * Diameter od the surroundings in the x direction 
     */
    private int gridDimensionX;
    /**
     * Diameter od the surroundings in the y direction 
     */
    private int gridDimensionY;
    
    private ArrayList<WorldEntity.WorldEntityInfo>[][] visibleWorld;
    private HashMap<Integer,Object>[][] metadata;
    private boolean[][] editableMetadata;
    
    PacmanVisibleWorld(ArrayList<WorldEntity.WorldEntityInfo>[][] visibleWorld,
                HashMap<Integer,Object>[][] metadata, boolean[][] editableMetadata)
    {
        gridDimensionX = visibleWorld.length;
        gridDimensionY = visibleWorld[0].length;
        
        this.visibleWorld = visibleWorld;
        this.metadata = metadata;
        this.editableMetadata = editableMetadata;
    }
    /**
     * Method returns the diameter od the surroundings in the x direction 
     * @return Diameter od the surroundings in the x direction
     */
    public final int getDimensionX() {return this.gridDimensionX;}
    /**
     * Method returns the diameter od the surroundings in the y direction 
     * @return Diameter od the surroundings in the y direction
     */
    public final int getDimensionY() {return this.gridDimensionY;}
    
    /**
     * Method returns the list of information objects regarding entities that reside at a visible location in the agent's surroundings.
     * Returns null for invisible locations.
     * Location 0,0 is the location of the agent.
     * @param x x-coordinate of the world location; range: -dimensionX/2...0...dimensionX/2
     * @param y y-coordinate of the world location; range: -dimensionY/2...0...dimensionY/2
     * @return List of information objects about all the world entities at that location 
     */
    public final ArrayList<WorldEntity.WorldEntityInfo> getWorldInfoAt(float x, float y)
    {
        if (editableMetadata[(int)x+gridDimensionX/2][(int)y+gridDimensionY/2])
        {
            return visibleWorld[(int)x+gridDimensionX/2][(int)y+gridDimensionY/2];
        }
        else
            return null;
    }
    
    /**
     * Method returns the map of objects left by the friendly agents at the particular visible location in the agent's surroundings (even a wall).
     * Returns null for invisible locations.
     * Location 0,0 is the location of the agent.
     * Modifications on both yours and your friends metadata are written back.
     * Each agent can have only one object at a metadata position. Any new object will overwrite the old one of the same agent.
     * Integer key is the ID of the agent. Any invalid keys and their values will be ignored.
     * @param x x-coordinate of the world location range: -dimensionX/2...0...dimensionX/2
     * @param y y-coordinate of the world location range: -dimensionY/2...0...dimensionY/2
     * @return Hashmap of objects containig the metadata left by the agents. It is up to the agents to define the form of the metadata. ID of the agent is the key of the map. 
     */
    public final HashMap<Integer,Object> getWorldMetadataAt(float x, float y)
    {
        if (editableMetadata[(int)x+gridDimensionX/2][(int)y+gridDimensionY/2])
        {
            return metadata[(int)x+gridDimensionX/2][(int)y+gridDimensionY/2];
        }
        else
        {
            return null;
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.scene.Geometry;

/**
 *
 * @author Marijo
 */
public class GhostAgent extends WorldAgent
{
    GhostAgent(float posX, float posY, float posZ, float oriX, float oriY, float oriZ,
        Geometry g, AgentAI ai, int viewRange, int timePerStepMs, int ghostIndex)
    {
        super(posX,posY,posZ,oriX,oriY,oriZ,"Ghost", g, ai, timePerStepMs, 1, Integer.MAX_VALUE, viewRange,1+ghostIndex,3);
        
    }
    
    public final void die()
    {
        Reset();
    }    
}

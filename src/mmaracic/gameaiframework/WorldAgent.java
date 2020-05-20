/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.scene.Geometry;
import java.util.ArrayList;
import mmaracic.gameaiframework.WorldEntityProperty.WorldEntityTransformer;

/**
 *
 * @author Marijo
 */
public class WorldAgent extends WorldEntity {
    
    public static final String healthPropertyName = "Health";
    public static final String livesPropertyName = "Lives";
    public static final String timePerStepMsPropertyName = "timePerStepMs";
    public static final String deathPropertyName = "Death";
    public static final String viewRangePropertyName = "ViewRange";

    private AgentAI ai=null;
    
    private int maxHealth = 0;
    private int currHealth = 0;
    private int deathTimeoutSec = 0;
    private int noLives = 0;
    private float timePerStepMs = 0;
    private int viewRange = 0;
    private int reactionSpeed = 0;
    
    WorldAgent(float posX, float posY, float posZ, float oriX, float oriY, float oriZ,
            String identifier, Geometry g, AgentAI ai, int timePerStepMs, int initHealth, int noLives, int viewRange,
            int reactionSpeed, int deathTimeoutSec)
    {
        super(posX, posY, posZ, oriX, oriY, oriZ, true, true, identifier, g, timePerStepMs);
        this.ai = ai;
        this.maxHealth = initHealth;
        this.currHealth = initHealth;
        this.deathTimeoutSec = deathTimeoutSec;
        this.noLives = noLives;
        this.timePerStepMs = timePerStepMs;
        this.viewRange = viewRange;
        this.reactionSpeed = reactionSpeed;
                
        addProperty(new WorldEntityProperty(WorldAgent.timePerStepMsPropertyName, Float.toString(timePerStepMs)));
        addProperty(new WorldEntityProperty(WorldAgent.viewRangePropertyName, Integer.toString(viewRange)));
        addProperty(new WorldEntityProperty(WorldAgent.healthPropertyName, Integer.toString(currHealth)));
        addProperty(new WorldEntityProperty(WorldAgent.livesPropertyName, Integer.toString(noLives)));
    }
    
    public int getCurrHealth() {return currHealth;}
    public int getMaxHealth() {return maxHealth;}
    public int getNoLives() {return noLives;}
    public float getTimePerStepMs() {return timePerStepMs;}
    public int getViewRange() {return viewRange;}
    public int getReactionSpeed() {return reactionSpeed;}
    
    public final int[] decideMove(ArrayList<int[]> moves, PacmanVisibleWorld mySurroundings)
    {
        try
        {
            addProperty(new WorldEntityProperty(WorldAgent.healthPropertyName, Integer.toString(currHealth), 10));
            addProperty(new WorldEntityProperty(WorldAgent.livesPropertyName, Integer.toString(noLives), 10));
            ArrayList<int[]> movesCopy = new ArrayList<>();
            for(int[] m : moves)
            {
                movesCopy.add(new int[]{m[0],m[1]});
            }
            int moveIndex = ai.decideMove(movesCopy, mySurroundings, getInfo());
            int[] move=null;
            if (moveIndex<0 || moveIndex>=moves.size())
            {
                move = new int[]{0,0};
            }
            else
            {
                move = moves.get(moveIndex);
            }
            applyTranslation(move[0], move[1], 0f);
            return move;
        }
        catch(Exception e)
        {
            StringBuilder sb = new StringBuilder(e.toString());
            sb.append(": ");
            sb.append(e.getMessage());
            sb.append("StackTrace: ");
            for (StackTraceElement el : e.getStackTrace())
            {
                sb.append(el.toString()+" ");
            }
            MessageCollector.putError(ai.getClass().getSimpleName(), sb.toString());
        }
        return new int[]{0,0};
    }
    
    public final void receiveDamage(int amount, 
            WorldEntityTransformer deathTransformer)
    {
        currHealth-=amount;
        if (currHealth<=0)
        {
            currHealth = maxHealth;
            noLives--;
            Reset();
            addProperty(new WorldEntityProperty(WorldAgent.livesPropertyName, Integer.toString(noLives)));
            addProperty(new WorldEntityProperty(deathPropertyName, propertyValueTrue, deathTimeoutSec*1000,deathTransformer,this));
        }
        addProperty(new WorldEntityProperty(WorldAgent.healthPropertyName, Integer.toString(currHealth)));
    }
}

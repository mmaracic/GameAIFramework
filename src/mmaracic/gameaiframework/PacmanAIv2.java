/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author Marijo
 */
public class PacmanAIv2 extends AgentAI{
    
    private HashSet<PacmanAI.Location> powerups = new HashSet<>();
    private HashSet<PacmanAI.Location> points = new HashSet<>();
    private PacmanAI.Location myLocation = new PacmanAI.Location(0, 0);
    private boolean amDead = false;
        
    private PacmanAI.Location targetLocation = myLocation;
    private HashSet<PacmanAI.Location> targetRoute = new HashSet<>();
    private int targetDuration = 0;
    
    private Date now = new Date();
    private Random r = new Random(now.getTime());


    @Override
    public int decideMove(ArrayList<int []> moves, PacmanVisibleWorld mySurroundings, WorldEntity.WorldEntityInfo myInfo)
    {
        int radiusX = mySurroundings.getDimensionX()/2;
        int radiusY = mySurroundings.getDimensionY()/2;
               
        boolean powerUP = myInfo.hasProperty(PacmanAgent.powerupPropertyName);
        boolean wasReset = myInfo.hasProperty(PacmanAgent.deathPropertyName);
//        Vector3f pos = myInfo.getPosition();
//        printStatus("Location x: "+pos.x+" y: "+pos.y);
        
        //determine if you know where u are
        //take into account that death takes more than one turn
        if (wasReset && !amDead)
        {
            amDead = true;
            myLocation = null;
        }
        if (!wasReset && amDead)
        {
            amDead = false;
        }
                
        //move toward the point
        //pick next if arrived
        if (targetLocation==null || targetLocation==myLocation)
        {
            if (points.iterator().hasNext())
            {
                targetLocation = points.iterator().next();
            }
            else if (powerups.iterator().hasNext())
            {
                targetLocation = powerups.iterator().next();
            }
            else
            {
                targetLocation=null;
            }
            targetDuration=0;
            targetRoute.clear();
        }
        targetDuration++;
        
        float targetPointDistance = (myLocation!=null && targetLocation!=null)?myLocation.distanceTo(targetLocation):Float.MAX_VALUE;        
        float ghostDistance = Float.MAX_VALUE;
        PacmanAI.Location ghostLocation = null;
        for (int i = -radiusX; i<=radiusX; i++)
        {
            for (int j = -radiusY; j<=radiusY; j++)
            {
                if (i==0 && j==0) continue;
                ArrayList<WorldEntity.WorldEntityInfo> neighPosInfos = mySurroundings.getWorldInfoAt(i, j);
                HashMap<Integer, Object> neighPosMetadata = mySurroundings.getWorldMetadataAt(i, j);
                
                if (neighPosMetadata!=null)
                {
                    if(myLocation==null)
                    {
                        PacmanAI.Location metaLoc = (PacmanAI.Location) neighPosMetadata.get(myInfo.getID());
                        if (metaLoc!=null)
                        {
                            myLocation = metaLoc;
                        }
                    }
                    else
                    {
                        PacmanAI.Location tempLocation = new PacmanAI.Location(myLocation.getX()+i, myLocation.getY()+j);
                        neighPosMetadata.put(myInfo.getID(),tempLocation);
                    }
                }
                if (neighPosInfos != null)
                {
                    PacmanAI.Location tempLocation = (myLocation!=null)?new PacmanAI.Location(myLocation.getX()+i, myLocation.getY()+j):new PacmanAI.Location(i, j);
                    float currPointDistance = (myLocation!=null)?myLocation.distanceTo(tempLocation):tempLocation.distanceTo(new PacmanAI.Location(0,0));
                    for (WorldEntity.WorldEntityInfo info : neighPosInfos)
                    {
                        if (info.getIdentifier().compareToIgnoreCase("Pacman")==0)
                        {
                            //Ignore myself
                        }
                        else if (info.getIdentifier().compareToIgnoreCase("Wall")==0)
                        {
                            //Its a wall, who cares!
                        }
                        else if (info.getIdentifier().compareToIgnoreCase("Point")==0)
                        {
                            //Remember where the point is!
                            points.add(tempLocation);
                            if (currPointDistance<targetPointDistance && !(targetLocation!=null && targetLocation.compareTo(tempLocation)==0))
                            {
                                targetLocation = tempLocation;
                                targetRoute.clear();
                                targetDuration=0;
                            }
                        }
                        else if (info.getIdentifier().compareToIgnoreCase("Powerup")==0)
                        {
                            //Remember where the powerup is!
                            powerups.add(tempLocation);
                        }
                        else if (info.getIdentifier().compareToIgnoreCase("Ghost")==0)
                        {
                            //Remember him!
                            if (currPointDistance<ghostDistance)
                            {
                                ghostDistance = currPointDistance;
                                ghostLocation = tempLocation;
                            }
                            //if we have powerup get him
                            if (powerUP)
                            {
                                targetLocation = tempLocation;
                                targetRoute.clear();
                                targetDuration=0;                                
                            }
                        }
                        else
                        {
                            printStatus("I dont know what "+info.getIdentifier()+" is!");
                        }
                    }
                }
            }            
        }
                     
        //sticking with target too long -> got stuck
        //dont get stuck
       if (targetDuration>10)
        {
            ArrayList<PacmanAI.Location> pointList = new ArrayList<>(points);
            int choice = r.nextInt(pointList.size());
            
            targetLocation = pointList.get(choice);
            targetRoute.clear();
            targetDuration = 0;
        }

        //select move
        float currMinPDistance = Float.MAX_VALUE;
        PacmanAI.Location nextLocation = null;
        int moveIndex = 0;
        
        for (int i=moves.size()-1; i>=0; i--)
        {
            int[] move = moves.get(i);
            PacmanAI.Location moveLocation = (myLocation!=null)?new PacmanAI.Location(myLocation.getX()+move[0], myLocation.getY()+move[1]):new PacmanAI.Location(move[0],move[1]);
            if (!targetRoute.contains(moveLocation))
            {
                float newPDistance = moveLocation.distanceTo(targetLocation);
                float newGDistance=(ghostDistance<Float.MAX_VALUE)?moveLocation.distanceTo(ghostLocation):Float.MAX_VALUE;
                if (newPDistance<=currMinPDistance && 
                        (newGDistance>1 || powerUP))
                {
                    //that way
                    currMinPDistance = newPDistance;
                    nextLocation = moveLocation;
                    moveIndex = i;
                }
            }
       }

        if (myLocation!=null)
        {
            points.remove(myLocation);
            powerups.remove(myLocation);
            myLocation = nextLocation;
        } 
        targetRoute.add(nextLocation);
        return moveIndex;
    }       
}

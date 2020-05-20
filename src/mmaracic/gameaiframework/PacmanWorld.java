/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import mmaracic.gameaiframework.WorldEntityProperty.WorldEntityTransformer;

/**
 *
 * @author Marijo
 * 
 * Dodati da ide aktivno po powerup kad ga ganjaju
 */
public class PacmanWorld {    
    /**
     * Possible movement options in 3D
     */
        
    private int gridDimensionX = 0;
    private int gridDimensionY = 0;
    private int gridDimensionZ = 0;
    
    private float gridStepX = 1.0f;
    private float gridStepY = 1.0f;
    private float gridStepZ = 1.0f;
    
    int worldTimeStepMs=0;
    
    private ArrayList<WorldEntity>[][][] world = null;
    private HashMap<Integer,Object>[][][] metadata = null;

    private HashSet<WorldEntity> worldElementList = new HashSet<>();
    private HashMap<Integer, WorldAgent> worldAgentIDList = new HashMap<>();
    private TreeMap<Integer, WorldAgent> worldAgentReactionList = new TreeMap<>();
    
    private int noTotalPoints=0;
    private int noPointsLeft=0;
    
    public enum Outcome
    {
        Win,
        Defeat,
        Tie,
        Ongoing
    }
    
    PacmanWorld(int dimensionX, int dimensionY, int dimensionZ, int worldTimeStepMs, WorldBuilder wb, HashMap<String,String> builderParams, AssetManager assetManager)
    {
        this.gridDimensionX = dimensionX;
        this.gridDimensionY = dimensionY;
        this.gridDimensionZ = dimensionZ;
        
        this.worldTimeStepMs = worldTimeStepMs;
        
        world = (ArrayList<WorldEntity>[][][]) Array.newInstance(ArrayList.class, new int[]{dimensionX,dimensionY,dimensionZ});
        metadata = (HashMap<Integer,Object>[][][]) Array.newInstance(HashMap.class, new int[]{dimensionX,dimensionY,dimensionZ});
        for(int i=0; i<dimensionX; i++)
        {
            for(int j=0; j<dimensionY; j++)
            {
                for(int k=0; k<dimensionZ; k++)
                {
                    world[i][j][k] = new ArrayList<>();
                    metadata[i][j][k] = new HashMap<>();
                }
            }
        }
        HashMap<String,String> worldInfo = wb.populateWold(world, worldTimeStepMs, builderParams, assetManager);
        noTotalPoints = Integer.parseInt(worldInfo.get("noPoints"));
        noPointsLeft = noTotalPoints;
        
        //put all the elements into a list
        for(int i=0; i<dimensionX; i++)
        {
            for(int j=0; j<dimensionY; j++)
            {
                for(int k=0; k<dimensionZ; k++)
                {
                    ArrayList<WorldEntity> currEntities = world[i][j][k];
                    worldElementList.addAll(currEntities);
                    for(WorldEntity en : currEntities)
                    {
                        if (en instanceof WorldAgent)
                        {
                            WorldAgent ag = (WorldAgent) en;
                            worldAgentIDList.put(ag.getID(),ag);
                            worldAgentReactionList.put(ag.getReactionSpeed(), ag);
                        }
                    }
                }            
            }   
        }
    
    }
    
    public final int getDimensionX() {return this.gridDimensionX;}
    public final int getDimensionY() {return this.gridDimensionY;}
    public final int getDimensionZ() {return this.gridDimensionZ;}
    
    public int getWorldTimeStepMs() {return this.worldTimeStepMs;}
    
    public final int getNoTotalPoints() {return noTotalPoints;}
    public final int getPointsEaten() {return noTotalPoints - noPointsLeft;}
    
    public final HashSet<WorldEntity> getEntities() {return worldElementList;}
    
    public final Outcome processAgents()
    {
        //check properties of world entities
        for (WorldEntity we : worldElementList)
        {
            we.checkProperties();
        }
        
        //process agents
        for (Integer reaction: worldAgentReactionList.keySet())
        {
            WorldAgent ag = worldAgentReactionList.get(reaction);
            Vector3f pos = ag.getPos();
            int locX = Math.round((pos.x-0.5f)/gridStepX+gridDimensionX/2);
            int locY = Math.round((pos.y-0.5f)/gridStepY+gridDimensionY/2);
            
            //check world conditions on agents current location
            ArrayList<WorldEntity> entities = world[locX][locY][0];
            for (int i=0;i<entities.size();i++)
            {
                WorldEntity en=entities.get(i);
                if (en.getIdentifier().compareTo("Point")==0 && ag.getIdentifier().compareTo("Pacman")==0)
                {
                    //eat point
                    noPointsLeft--;
                    entities.remove(en);
                    en.removeFromScene();
                    worldElementList.remove(en);
                }
                if (en.getIdentifier().compareTo("Powerup")==0 && ag.getIdentifier().compareTo("Pacman")==0)
                {
                    //eat powerup
                    noPointsLeft--;
                    entities.remove(en);
                    en.removeFromScene();
                    worldElementList.remove(en);
 
                    //become strong
                    ((PacmanAgent)ag).eatPowerup();
                }
                if ((en.getIdentifier().compareTo("Ghost")==0 && ag.getIdentifier().compareTo("Pacman")==0) ||
                        (ag.getIdentifier().compareTo("Ghost")==0 && en.getIdentifier().compareTo("Pacman")==0))
                {
                    PacmanAgent pacman = (PacmanAgent) ((ag.getIdentifier().compareTo("Pacman")==0)?ag:en);
                    GhostAgent ghost = (GhostAgent) ((ag.getIdentifier().compareTo("Ghost")==0)?ag:en);
//                    if (ag.getIdentifier().compareTo("Pacman")==0)
//                    {
//                            MessageCollector.putMessage("PacmanWorld", "Pacman on top of ghost"+en.getID());
//                    }
//                    else
//                    {
//                            MessageCollector.putMessage("PacmanWorld", "Ghost"+ag.getID()+" on top of pacman");                        
//                    }
                    //pacman eats ghost if he has powerup
                    //otherwise ghost eats pacman
                    if (pacman.hasProperty(PacmanAgent.powerupPropertyName))
                    {
                        //ghost dies
                        ghost.die();
                        Vector3f newPos = ghost.getPos();
                        int newLocX = Math.round ((newPos.x-0.5f)/gridStepX+gridDimensionX/2);
                        int newLocY = Math.round ((newPos.y-0.5f)/gridStepY+gridDimensionY/2);
 
                        entities.remove(ghost);
                        world[newLocX][newLocY][0].add(ghost);
                    }
                    else
                    {
                        //pacman  dies
                        pacman.die();
                        MessageCollector.putMessage("PacmanWorld", "Pacman has "+pacman.getNoLives()+" lives left.");
                        Vector3f newPos = pacman.getPos();
                        int newLocX = Math.round ((newPos.x-0.5f)/gridStepX+gridDimensionX/2);
                        int newLocY = Math.round ((newPos.y-0.5f)/gridStepY+gridDimensionY/2);
 
                        entities.remove(pacman);
                        world[newLocX][newLocY][0].add(pacman);
                        
                        if (pacman.getNoLives()<=0) return Outcome.Defeat;
                    }
                }
            }
            
            //reestablish position in case sb died etc
            pos = ag.getPos();
            locX = Math.round((pos.x-0.5f)/gridStepX+gridDimensionX/2);
            locY = Math.round((pos.y-0.5f)/gridStepY+gridDimensionY/2);

            //allowed moves
            ArrayList<int[]> moves = new ArrayList<>();
            int[][] neighbours = new int[][]{{0, 1},{1, 0},{0, -1},{-1, 0}};
            if (ag.getProperty(WorldAgent.deathPropertyName)==null)
            {
                //not dead
                for(int i=0;i<neighbours.length;i++)
                {
                    int newX = locX+neighbours[i][0];
                    int newY = locY+neighbours[i][1];

                    ArrayList<WorldEntity> elements = world[newX][newY][0];
                    boolean wallFound = false;
                    for (WorldEntity en : elements)
                    {
                        if ((en.getIdentifier().compareTo("Wall")==0) ||
                                (ag.getIdentifier().compareToIgnoreCase("Ghost")==0 && en.getIdentifier().compareTo("Ghost")==0))
                        {
                            wallFound = true;
                            break;
                        }
                     }
                    if (!wallFound)
                        moves.add(neighbours[i]);
                }
            }
            if (moves.isEmpty())
            {
                moves.add(new int[]{0,0});
//                MessageCollector.putMessage("PacmanWorld", ag.getIdentifier()+" has no available moves!");
            }
            
            //surroundings and metadata
            //agent is centered in the surroundings so he's in position
            // (ag.getViewRange(),ag.getViewRange())
            //initialization
            ArrayList<WorldEntity.WorldEntityInfo>[][] mySurroundings = (ArrayList<WorldEntity.WorldEntityInfo>[][]) Array.newInstance(ArrayList.class, new int[]{ag.getViewRange()*2+1,ag.getViewRange()*2+1});
            HashMap<Integer,Object>[][] myMetadata = (HashMap<Integer,Object>[][]) Array.newInstance(HashMap.class, new int[]{ag.getViewRange()*2+1,ag.getViewRange()*2+1});
            boolean [][] myEditableMetadata = new boolean[ag.getViewRange()*2+1][ag.getViewRange()*2+1];
            for(int i=0; i<ag.getViewRange()*2+1; i++)
            {
                for(int j=0; j<ag.getViewRange()*2+1; j++)
                {
                    mySurroundings[i][j] = new ArrayList<>();
                    myMetadata[i][j] = new HashMap<>();
                    myEditableMetadata[i][j] = false;
                }
            }
            
            //determining the visible surroundings and metadata
            LinkedList<int[]> list = new LinkedList<>();
            Set<Integer> checkedList = new HashSet<>();
            
            list.add(new int[]{locX,locY});
            int index = locX*gridDimensionY*gridDimensionZ + locY*gridDimensionZ;
            checkedList.add(index);
            while(!list.isEmpty())
            {
                int[] currPos = list.removeLast();
                int distance = Math.abs(currPos[0]-locX)+Math.abs(currPos[1]-locY);
                boolean isWall = false;
                //surroundings
                ArrayList<WorldEntity> wElements = world[currPos[0]][currPos[1]][0];
                for (WorldEntity en : wElements)
                {
                    if (en.getIdentifier().compareTo("Wall")==0) isWall = true;
//                    en.addProperty(new WorldEntityProperty(WorldEntity.selectedPropertyName,WorldEntity.propertyValueTrue,1,
//                            new WorldEntityTransformer() {
//
//                        @Override
//                        public void performTransformation(Spatial geometry) {
//                            geometry.scale(1.4f);
//                        }
//
//                        @Override
//                        public void undoTransformation(Spatial geometry) {
//                            geometry.scale(0.7143f);
//                        }
//                    },en));
                    mySurroundings[currPos[0]-locX+ag.getViewRange()][currPos[1]-locY+ag.getViewRange()].add(en.getInfo());
                }
                //metadata can be written only into the elements we can see (even walls)
                //metadata - remove the elements you give back so their removal by the owner can be noted
                HashMap<Integer,Object> currMElements = metadata[currPos[0]][currPos[1]][0];
                Set<Integer> metakeys = new HashSet(currMElements.keySet());
                for (Integer id:metakeys)
                {
                    WorldAgent fellowAg = worldAgentIDList.get(id);
                    if (fellowAg!=null && fellowAg.getIdentifier().compareToIgnoreCase(ag.getIdentifier())==0)
                    {
                        Object obj = currMElements.remove(id);
                        myMetadata[currPos[0]-locX+ag.getViewRange()][currPos[1]-locY+ag.getViewRange()].put(id,obj);
                    }
                }
                myEditableMetadata[currPos[0]-locX+ag.getViewRange()][currPos[1]-locY+ag.getViewRange()]=true;
                
                //new neighbours
                if (distance<ag.getViewRange() && !isWall)
                {
                    for(int i=0;i<neighbours.length;i++)
                    {
                        int[] newPos = new int[]{currPos[0]+neighbours[i][0],currPos[1]+neighbours[i][1]};
                        int newDistance = Math.abs(newPos[0]-locX)+Math.abs(newPos[1]-locY);
                        int newIndex = newPos[0]*gridDimensionY*gridDimensionZ + newPos[1]*gridDimensionZ;
                        if (checkedList.add(newIndex) && newDistance>distance &&
                                newPos[0]>=0 && newPos[1]>=0 && newPos[0]<gridDimensionX && newPos[1]<gridDimensionY)
                        {
                            list.addFirst(newPos);
                        }
                    }
                }
            }
            PacmanVisibleWorld pvw = new PacmanVisibleWorld(mySurroundings, myMetadata, myEditableMetadata);
            //decide move
            int[] move = ag.decideMove(moves, pvw);
            //write down the metadata he changed
            for (int i=-pvw.getDimensionX()/2; i<=pvw.getDimensionX()/2;i++)
            {
                for (int j=-pvw.getDimensionY()/2; j<=pvw.getDimensionY()/2;j++)
                {
                    HashMap<Integer,Object> meta=pvw.getWorldMetadataAt(i, j);
                    if (meta!=null && !meta.isEmpty())
                    {
                        Set<Integer> metakeys = new HashSet(meta.keySet());
                        for (Integer id: metakeys)
                        {
                            WorldAgent fellowAg = worldAgentIDList.get(id);
                            if (fellowAg!=null && fellowAg.getIdentifier().compareToIgnoreCase(ag.getIdentifier())==0)
                            {
                                metadata[locX+i][locY+j][0].put(id, meta.get(id));
                            }
                        }
                    }
                }    
            }
            //move the agent
            entities = world[locX][locY][0];
            for (int i=0;i<entities.size();i++)
            {
                WorldEntity en=entities.get(i);
                if (en.getID()==ag.getID())
                {
                    entities.remove(i);
                    world[locX+move[0]][locY+move[1]][0].add(en);
                    break;
                }
            }
        }
        //all agents processed
        //pacman win condition
        if (noPointsLeft==0)
        {
            return Outcome.Win;
        }
        else
        {
            return Outcome.Ongoing;
        }
    }    
}

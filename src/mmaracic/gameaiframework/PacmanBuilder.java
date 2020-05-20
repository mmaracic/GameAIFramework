/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Marijo
 */
public class PacmanBuilder extends WorldBuilder{
    
    private enum neighbours
    {
        all,
        top,
        bottom,
        left,
        right
    }
    private void dec2DNeigh(int[][] array, int pos1, int pos2, neighbours n, boolean check)
    {
        int[][] neighbours = null;
        switch(n)
        {
            case top: neighbours = new int[][]{{-1, 1},{0, 1},{1, 1}}; break;
            case bottom: neighbours = new int[][]{{1, -1},{0, -1},{-1, -1}}; break;
            case left: neighbours = new int[][]{{-1, -1},{-1, 0},{-1, 1}}; break;
            case right: neighbours = new int[][]{{1, 1},{1, 0},{1, -1}}; break;
            default: neighbours = new int[][]{{0, 1},{1, 1},{1, 0},{1, -1},{0, -1},{-1, -1},{-1, 0},{-1, 1}}; break;
        }
        for (int i=0; i<neighbours.length; i++)
        {
            int dim1=pos1+neighbours[i][0];
            int dim2=pos2+neighbours[i][1];
            if (!check || (dim1>0 && dim2>0 && dim1<array.length && dim2<array[0].length))
            {
                int val = array[dim1][dim2];
                array[dim1][dim2]=(val>0)?val-1:0;
            }
        }        
    }
    
    @Override
    public HashMap<String,String> populateWold(ArrayList<WorldEntity>[][][] world, int worldTimeStepMs, HashMap<String,String> builderParams, AssetManager assetManager)
    {
        //Geometries
        Box b = new Box(0.5f, 0.5f, 0.5f);
        Sphere ss = new Sphere(18, 36, 0.2f);
        Sphere sl = new Sphere(18, 36, 0.3f);
        Material matBl = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matBl.setColor("Color", ColorRGBA.Blue);
        Material matBr = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matBr.setColor("Color", ColorRGBA.Brown);
        Material matRd = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matRd.setColor("Color", ColorRGBA.Red);
        Material matGr = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matGr.setColor("Color", ColorRGBA.Green);
        Material matYl = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matYl.setColor("Color", ColorRGBA.Yellow);

        Geometry wallGeometry = new Geometry("Box", b);
        wallGeometry.setMaterial(matBr);
        Geometry powerupGeometry = new Geometry("SphereS", ss);
        powerupGeometry.setMaterial(matGr);
        Geometry pointGeometry = new Geometry("SphereS", ss);
        pointGeometry.setMaterial(matYl);
        Geometry pacmanGeometry = new Geometry("SphereL", sl);
        pacmanGeometry.setMaterial(matBl);
        Geometry ghostGeometry = new Geometry("SphereL", sl);
        ghostGeometry.setMaterial(matRd);
                         
        //Parameters
        int noGhosts=Integer.parseInt(builderParams.get("noGhosts"));
        int pitDimX=Integer.parseInt(builderParams.get("pitDimX"));
        int pitDimY=Integer.parseInt(builderParams.get("pitDimY"));

        //AgentsAIs
        String classPath = builderParams.get("classPath");
        String[] path = (classPath!=null)?new String[]{classPath}:new String[]{};
        String pacmanAIName = builderParams.get("pacmanAI");
        String ghostAIName = builderParams.get("ghostAI");
        List<String> aiNames = new ArrayList<>();
        aiNames.add(pacmanAIName);
        for (int i = 0; i< noGhosts; i++)
        {
            aiNames.add(ghostAIName);
        }
        List<AgentAI> ais = AIFactory.createAI(path,aiNames);
        
        //Create world
        int dimensionX = world.length;
        int dimensionY = world[0].length;
        
        //Transfer to image CS
        float baseX = -dimensionX/2+0.5f;
        float baseY = -dimensionY/2+0.5f;
        float baseXBox = -pitDimX/2+0.5f;
        float baseYBox = -pitDimY/2+0.5f;
        int offsetBoxX = (dimensionX-pitDimX)/2;
        int offsetBoxY = (dimensionY-pitDimY)/2;
        
        //helper info for world creation
        int[][] helper = new int[dimensionX][dimensionY];
        for(int i=0; i<dimensionX; i++)
        {
            Arrays.fill(helper[i], 8);
        }
                
        //create outer wall
        for(int i=0; i<dimensionX; i++)
        {
            WorldEntity wef = new WorldEntity(baseX+i,baseY,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
            world[i][0][0].add(wef);
            helper[i][0]=0;
            dec2DNeigh(helper,i,0,neighbours.top, true);         
            WorldEntity wel = new WorldEntity(baseX+i,baseY+dimensionY-1,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
            world[i][dimensionY-1][0].add(wel);
            helper[i][dimensionY-1]=0;
            dec2DNeigh(helper,i,dimensionY-1,neighbours.bottom, true);
        }
        for(int i=1; i<dimensionY-1; i++)
        {
            WorldEntity wef = new WorldEntity(baseX,baseY+i,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
            world[0][i][0].add(wef);
            helper[0][i]=0;
            dec2DNeigh(helper,0,i,neighbours.right, true);
            WorldEntity wel = new WorldEntity(baseX+dimensionX-1,baseY+i,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
            world[dimensionX-1][i][0].add(wel);
            helper[dimensionX-1][i]=0;
            dec2DNeigh(helper,dimensionX-1,i,neighbours.left, true);
        }
        
        //create ghost box
        for(int i=0; i<pitDimX; i++)
        {
            WorldEntity wef = new WorldEntity(baseXBox+i,baseYBox,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
            world[offsetBoxX+i][offsetBoxY+0][0].add(wef);
            helper[offsetBoxX+i][offsetBoxY+0]=0;
            dec2DNeigh(helper,offsetBoxX+i,offsetBoxY+0,neighbours.top, false);
            dec2DNeigh(helper,offsetBoxX+i,offsetBoxY+0,neighbours.bottom, false);
            WorldEntity wel = new WorldEntity(baseXBox+i,baseYBox+pitDimY-1,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
            world[offsetBoxX+i][offsetBoxY+pitDimY-1][0].add(wel);
            helper[offsetBoxX+i][offsetBoxY+pitDimY-1]=0;
            dec2DNeigh(helper,offsetBoxX+i,offsetBoxY+pitDimY-1,neighbours.top, false);
            dec2DNeigh(helper,offsetBoxX+i,offsetBoxY+pitDimY-1,neighbours.bottom, false);
        }
        for(int i=1; i<pitDimY-1; i++)
        {
            WorldEntity wef = new WorldEntity(baseXBox,baseYBox+i,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
            world[offsetBoxX+0][offsetBoxY+i][0].add(wef);
            helper[offsetBoxX+0][offsetBoxY+i]=0;
            dec2DNeigh(helper,offsetBoxX+0,offsetBoxY+i,neighbours.left, false);
            dec2DNeigh(helper,offsetBoxX+0,offsetBoxY+i,neighbours.right, false);
            if (i!=pitDimY/2)
            {
                WorldEntity wel = new WorldEntity(baseXBox+pitDimX-1,baseYBox+i,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
                world[offsetBoxX+pitDimX-1][offsetBoxY+i][0].add(wel);
                helper[offsetBoxX+pitDimX-1][offsetBoxY+i]=0;
                dec2DNeigh(helper,offsetBoxX+pitDimX-1,offsetBoxY+i,neighbours.left, false);
                dec2DNeigh(helper,offsetBoxX+pitDimX-1,offsetBoxY+i,neighbours.right, false);
            }
        }
        
        //create powerups
        WorldEntity pw1 = new WorldEntity(baseX+1,baseY+1,0.5f,0f,0f,0f,false,false,"Powerup",powerupGeometry.clone(),worldTimeStepMs);
        world[1][1][0].add(pw1);
        WorldEntity pw2 = new WorldEntity(baseX+dimensionX-2,baseY+1,0.5f,0f,0f,0f,false,false,"Powerup",powerupGeometry.clone(),worldTimeStepMs);
        world[dimensionX-2][1][0].add(pw2);
        WorldEntity pw3 = new WorldEntity(baseX+1,baseY+dimensionY-2,0.5f,0f,0f,0f,false,false,"Powerup",powerupGeometry.clone(),worldTimeStepMs);
        world[1][dimensionY-2][0].add(pw3);
        WorldEntity pw4 = new WorldEntity(baseX+dimensionX-2,baseY+dimensionY-2,0.5f,0f,0f,0f,false,false,"Powerup",powerupGeometry.clone(),worldTimeStepMs);
        world[dimensionX-2][dimensionY-2][0].add(pw4);

        //create inner walls
        Date now = new Date();
        Random r = new Random(now.getTime());
        int maxInnerWalls = (dimensionX*dimensionY)/5;
        int countInnerWalls=0;
        int countAttempts=0;
        while (countInnerWalls<maxInnerWalls && countAttempts<maxInnerWalls*5)
        {
            int randomI = 1+r.nextInt(dimensionX-2);            
            int randomJ = 1+r.nextInt(dimensionY-2);            
            if (world[randomI][randomJ][0].isEmpty())
            {
                if (randomI<offsetBoxX || randomI>=offsetBoxX+pitDimX || randomJ<offsetBoxY || randomJ>=offsetBoxY+pitDimY)
                {
                    if (helper[randomI][randomJ]>=7)
                    {
                        WorldEntity wef = new WorldEntity(baseX+randomI,baseY+randomJ,0.5f,0f,0f,0f,false,false,"Wall",wallGeometry.clone(),worldTimeStepMs);
                        world[randomI][randomJ][0].add(wef);
                        helper[randomI][randomJ]=0;
                        dec2DNeigh(helper,randomI,randomJ,neighbours.all, false);
                        countInnerWalls++;
                    }
                }
            }
            countAttempts++;
        }
        
        //put points everywhere else
        int noPoints=0;
        for(int i=1; i<dimensionX-1; i++)
        {
            for(int j=1; j<dimensionY-1; j++)
            {
                if (world[i][j][0].isEmpty())
                {
                    if (i<offsetBoxX || i>=offsetBoxX+pitDimX || j<offsetBoxY || j>=offsetBoxY+pitDimY)
                    {
                        WorldEntity we = new WorldEntity(baseX+i,baseY+j,0.5f,0f,0f,0f,false,false,"Point",pointGeometry.clone(),worldTimeStepMs);
                        world[i][j][0].add(we);
                        noPoints++;
                    }
                }
            }
        }
        //put agents
        //put ghosts
        int noPlacedGhosts=0;
        for(int i=1; i<pitDimX-1; i++)
        {
            for(int j=1; j<pitDimY-1; j++)
            {
                GhostAgent ghost = new GhostAgent(baseXBox+i,baseYBox+j,0.5f,0f,0f,0f,ghostGeometry.clone(),ais.get(1+noPlacedGhosts),3,worldTimeStepMs,++noPlacedGhosts);
                world[offsetBoxX+i][offsetBoxX+j][0].add(ghost);
                if (noPlacedGhosts>=noGhosts) break;
            }
            if (noPlacedGhosts>=noGhosts) break;
        }
        //put pacman
        while (true)
        {
            int randomI = 1+r.nextInt(dimensionX-2);            
            int randomJ = 1+r.nextInt(dimensionY-2);            
            if (randomI<offsetBoxX || randomI>=offsetBoxX+pitDimX || randomJ<offsetBoxY || randomJ>=offsetBoxY+pitDimY)
            {
                if (world[randomI][randomJ][0].get(0).getIdentifier().compareTo("Wall")!=0)
                {
                    PacmanAgent pacman = new PacmanAgent(baseX+randomI,baseY+randomJ,0.5f,0f,0f,0f,pacmanGeometry.clone(),ais.get(0),4,3,worldTimeStepMs);
                    world[randomI][randomJ][0].add(pacman);
                    break;
                }
            }
        }
        
        //return information
        HashMap<String,String> worldInfo = new HashMap<>();
        worldInfo.put("noPoints", Integer.toString(noPoints+4));
        return  worldInfo;
    }
    
}

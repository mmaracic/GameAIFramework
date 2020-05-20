/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.asset.AssetManager;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Marijo
 */
public abstract class WorldBuilder {   
    public abstract HashMap<String,String> populateWold(ArrayList<WorldEntity>[][][] world, int worldTimeStepMs, HashMap<String,String> builderParams, AssetManager assetManager);
}

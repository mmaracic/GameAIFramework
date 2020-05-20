/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.asset.AssetManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 *
 * @author Marijo
 */
public class WorldFactory {
    public static PacmanWorld createWorld(String worldBuilderName, int dimensionX, int dimensionY, int dimensionZ, int worldTimeStepMs, HashMap<String,String> builderParams, AssetManager assetManager)
    {
        try
        {
            Class wbc = WorldFactory.class.getClassLoader().loadClass(worldBuilderName);
            if (WorldBuilder.class.isAssignableFrom(wbc))
            {
                Constructor con = wbc.getConstructor();
                WorldBuilder wb = (WorldBuilder) con.newInstance();
                PacmanWorld world = new PacmanWorld(dimensionX, dimensionY, dimensionZ, worldTimeStepMs, wb, builderParams, assetManager);
                return world;
            }
            else
            {
                MessageCollector.putError("WorldFactory",worldBuilderName+" is not a subclass of WorldBuilder!");
                return null;                
            }
        }
        catch(ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex)
        {            
            MessageCollector.putError("WorldFactory", ex.getMessage());
            return null;
        }
        
    }
}

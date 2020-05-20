package mmaracic.monkeyengine;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import de.lessvoid.nifty.Nifty;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmaracic.gameaiframework.PacmanWorld;
import mmaracic.gameaiframework.WorldEntity;
import mmaracic.gameaiframework.WorldFactory;

/**
 * Main class
 * @author Marijo
 */
public class Main extends SimpleApplication {
    
    Geometry pointer=null;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp()
    {
        stateManager.attach(new GUIAppState());      
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}

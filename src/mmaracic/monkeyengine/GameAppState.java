/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.monkeyengine;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmaracic.gameaiframework.PacmanWorld;
import mmaracic.gameaiframework.WorldEntity;
import mmaracic.gameaiframework.WorldFactory;

/**
 *
 * @author Marijo
 */
public class GameAppState extends AbstractAppState {
    
    private boolean pause=false;
    private PacmanWorld.Outcome done = PacmanWorld.Outcome.Ongoing;

    private final String PAUSE_MAPPING="Pause";
    
    private SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private InputManager input;

    private Node gameNode = new Node("gameNode");
    PacmanWorld bw = null;
    GregorianCalendar gcal = null;
    ActionListener al = null;
    
    private String classPath = null;
    private String pacmanAIClass = "mmaracic.gameaiframework.PacmanAI";
    private String ghostAIClass = "mmaracic.gameaiframework.GhostAI";
    private String WorldBuilderClass = "mmaracic.gameaiframework.PacmanBuilder";
    private int worldTimeStepMs = 500;
    private int worldWidth = 20;
    private int worldHeight = 20;
    
    public GameAppState()
    {        
    }
    
    public GameAppState(String WorldBuilderClass, int worldWidth, int worldHeight, int worldTimeStepMs,
            String classPath, String pacmanAIClass, String ghostAIClass)
    {
        if (WorldBuilderClass!=null && WorldBuilderClass.compareToIgnoreCase("")!=0)
        {
            this.WorldBuilderClass = WorldBuilderClass;
        }
        if (worldWidth>=10)
        {
            this.worldWidth = worldWidth;
        }
        if (worldHeight>=10)
        {
            this.worldHeight = worldHeight;
        }
        if (worldTimeStepMs>0)
        {
            this.worldTimeStepMs = worldTimeStepMs;
        }
        if (classPath!=null && classPath.compareToIgnoreCase("")!=0)
        {
            this.classPath = classPath;
        }
        if (pacmanAIClass!=null && pacmanAIClass.compareToIgnoreCase("")!=0)
        {
            this.pacmanAIClass = pacmanAIClass;
        }
        if (ghostAIClass!=null && ghostAIClass.compareToIgnoreCase("")!=0)
        {
            this.ghostAIClass = ghostAIClass;
        }
        this.gcal = new GregorianCalendar();
    }
    
    public boolean isPaused()
    {
        return pause;
    }
    
    public PacmanWorld.Outcome getGameState()
    {
        return done;
    }
    
    public Map<String,String> getSoftGameState()
    {
        Map<String,String> state = new HashMap<>();
        state.put("Points Total", Integer.toString(bw.getNoTotalPoints()));
        state.put("Points Eaten", Integer.toString(bw.getPointsEaten()));
        return state;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        //TODO: initialize your AppState, e.g. attach spatials to rootNode
        //this is called on the OpenGL thread after the AppState has been attached
        super.initialize(stateManager, app);
        
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.input = this.app.getInputManager();
        this.gcal = new GregorianCalendar();

        HashMap<String, String> builderParams = new HashMap<>();
        builderParams.put("classPath",classPath);
        builderParams.put("pacmanAI",pacmanAIClass);
        builderParams.put("ghostAI",ghostAIClass);
        builderParams.put("noGhosts",Integer.toString(4));
        builderParams.put("pitDimX",Integer.toString(4));
        builderParams.put("pitDimY",Integer.toString(4));
        
        Trigger pauseTrigger = new KeyTrigger(KeyInput.KEY_P);
        input.addMapping(PAUSE_MAPPING, pauseTrigger);
        al = new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equalsIgnoreCase(PAUSE_MAPPING) && !isPressed)
                {
                    pause=!pause;
                }
            }
        };
        input.addListener(al, new String[]{PAUSE_MAPPING});
        
        bw = WorldFactory.createWorld(WorldBuilderClass, worldWidth, worldHeight, 1, worldTimeStepMs, builderParams, assetManager);
        HashSet<WorldEntity> entities = bw.getEntities();
        for (WorldEntity we : entities)
        {
            gameNode.attachChild(we.getSpatial());
        }
        
        rootNode.attachChild(gameNode);

        this.app.setDisplayStatView(false);
        this.app.setDisplayFps(true);
        this.cam.setLocation(new Vector3f(0,0,25));
    }
    
    @Override
    public void update(float tpf) {
        
        if (!pause)
        {
            GregorianCalendar now = new GregorianCalendar();
            if (now.compareTo(gcal)>0)
            {
                done = bw.processAgents();
                if (done!=PacmanWorld.Outcome.Ongoing)
                {
                    app.getStateManager().detach(this);        
                }
                gcal = now;
                gcal.add(GregorianCalendar.MILLISECOND, worldTimeStepMs);
            }
        }
     }
    
    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
        input.removeListener(al);
        al=null;
        input.deleteMapping(PAUSE_MAPPING);
        int detachWorld = rootNode.detachChildNamed("gameNode");
        if (detachWorld==-1)
        {
            Logger.getLogger(GameAppState.class.getName()).log(Level.SEVERE,"Pacman world deletion failed!");
        }
        this.app.setDisplayFps(false);
        this.cam.setLocation(new Vector3f(0,0,10));
    }
}

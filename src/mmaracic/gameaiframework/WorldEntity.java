/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Marijo
 */
public class WorldEntity {
    
    /**
     * The class that encapsulates the info about the world's entities to the current agent.
     */
    static public final class WorldEntityInfo
    {
        /**
         * Identifies the class of the entity
         */
        private String identifier;
        /**
         * Uniquely identifies the entity within the world
         */
        private int ID;
        
        /**
         * Position of the object within the world (3d, continuous)
         */
        private Vector3f position;
        /**
         * Orientation of the object within the world (3d, continuous)
         */
        private Vector3f orientation;
        
        /**
         * Map of the properties - key->name, value->value
         */
        private HashMap<String, String> state;
    
        public WorldEntityInfo(String identifier, int ID, Vector3f position, Vector3f orientation, HashMap<String, String> state)
        {
            this.position = position;
            this.orientation = orientation;
            this.state = state;
            this.identifier = identifier;
            this.ID = ID;
        }
        /**
         * Position of the object within the world (3d, continuous)
         * @return Position of the object within the world (3d, continuous)
         */
        public Vector3f getPosition() {return this.position;}
        /**
         * Orientation of the object within the world (3d, continuous)
         * @return Orientation of the object within the world (3d, continuous)
         */
        public Vector3f getOrientation() {return this.orientation;}
        /**
         * Method returns a string that identifies the class of the entity
         * @return Identifier of the entity's class
         */
        public String getIdentifier() {return this.identifier;}
        /**
         * Method returns a number that uniquely identifies the entity within the world
         * @return Unique identifier of the entity within the world
         */
        public int getID() {return this.ID;}
        
        /**
         * Returns the value of the property or null if it doesnt exist
         * @param name Name of the property
         * @return Value of the property
         */
        public String getProperty(String name) {return this.state.get(name);}
        
        /**
         * Returns whether the property of such name exists for this entity
         * @param name Name of the property
         * @return True if the property exists for the entity, false if not
         */
        public boolean hasProperty(String name) {return this.state.containsKey(name);}

    }
    
    public static final String selectedPropertyName = "Selected";
    public static final String movablePropertyName = "Movable";
    public static final String intelligentPropertyName = "Intelligent";
    public static final String propertyValueTrue = "True";
    public static final String propertyValueFalse = "False";

    private static int idGen = 0;
    private static int generateID() {return idGen++;}
    
    private int ID=0;
    
    private HashMap<String, WorldEntityProperty> state = new HashMap<>();
    
    private Spatial sceneEl = null;
    private WorldEntityAnimator mover = null;
    
    private String identifier = null;

    private Vector3f posCopy = null;
    private Vector3f oriCopy = null;    

    private Vector3f pos = null;
    private Vector3f ori = null;    

    public WorldEntity(float posX, float posY, float posZ, float oriX, float oriY, float oriZ,
            boolean movable, boolean intelligent, String identifier, Spatial sceneEl,
            int timePerStepMs)
    {
        ID = generateID();
        this.identifier = identifier;
        this.sceneEl = sceneEl;
        
        pos = new Vector3f(posX, posY, posZ);
        ori = new Vector3f(oriX, oriY, oriZ);
        
        this.sceneEl.move(pos);
        posCopy = new Vector3f(pos);
        oriCopy = new Vector3f(ori);
        
        
        this.mover = new WorldEntityAnimator(posCopy, idGen);
        this.sceneEl.addControl(mover);
                
        sceneEl.setLocalTranslation(pos);       
        Quaternion q = new Quaternion();
        sceneEl.setLocalRotation(q.fromAngles(ori.x, ori.y, ori.z));
        
        addProperty(new WorldEntityProperty(WorldEntity.movablePropertyName, (movable)?WorldEntity.propertyValueTrue:WorldEntity.propertyValueFalse));
        addProperty(new WorldEntityProperty(WorldEntity.intelligentPropertyName, (intelligent)?WorldEntity.propertyValueTrue:WorldEntity.propertyValueFalse));
    }
    
    public final Vector3f getPos() {return new Vector3f(pos);}
    public final Vector3f getOri() {return new Vector3f(ori);}
//    public final Vector3f getOri()
//    {
//        Quaternion loc = sceneEl.getLocalRotation();
//        return new Vector3f(loc.getX()/loc.getW(), loc.getY()/loc.getW(), loc.getZ()/loc.getW());
//    }
    public final Spatial getSpatial() {return this.sceneEl;}
    
    public final String getIdentifier() {return this.identifier;}
    public final int getID() {return ID;}
    
    public final void checkProperties()
    {
        Set<String> tempKeys = new HashSet(state.keySet());
        for (String propName: tempKeys)
        {
            WorldEntityProperty prop = state.get(propName);
            prop.checkProperty();
            if (prop.hasExpired())
            {
                state.remove(propName);
            }
        }
    }
    
    public final void Reset()
    {
        sceneEl.setLocalTranslation(posCopy);       
        Quaternion q = new Quaternion();
        sceneEl.setLocalRotation(q.fromAngles(oriCopy.x,oriCopy.y, oriCopy.z));
        pos.set(posCopy);
        ori.set(oriCopy);
    }
    
    public final void removeFromScene()
    {
        sceneEl.getParent().detachChild(sceneEl);
    }
    
    protected final void applyTranslation(float x, float y, float z)
    {
        Vector3f move = new Vector3f(x, y, z);
        pos = pos.add(move);
        mover.moveTo(pos);
    }
    
    public final WorldEntityInfo getInfo()
    {
        HashMap<String, String> exportState = new HashMap<>();
        for(WorldEntityProperty prop : state.values())
        {
            if (!prop.hasExpired())
            {
                exportState.put(prop.getName(), prop.getValue());
            }
        }
        WorldEntityInfo newInfo = new WorldEntityInfo(identifier,ID,new Vector3f(getPos()), new Vector3f(getOri()), exportState);
        return newInfo;
    }
    
    public final void addProperty(WorldEntityProperty p)
    {
        WorldEntityProperty oldP = state.put(p.getName(), p);
        if (oldP != null)
        {
            oldP.deactivateProperty();
        } 
    }

    public final WorldEntityProperty getProperty(String name)
    {
        return state.get(name);
    }
    
    public final boolean  hasProperty(String name)
    {
        return state.containsKey(name);
    }
}

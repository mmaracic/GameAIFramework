/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.scene.Spatial;
import java.util.GregorianCalendar;

/**
 *
 * @author Marijo
 */
public final class WorldEntityProperty {
    
    static public interface WorldEntityTransformer
    {
        public void performTransformation(Spatial geometry);
        public void undoTransformation(Spatial geometry);
    }

    private String name;
    private String value;
    private boolean limited;
    private boolean expired;
    private GregorianCalendar expirationTime;
    private WorldEntityTransformer transformer;
    private WorldEntity worldEntity;

    public WorldEntityProperty(String name, String value, int durationMs, WorldEntityTransformer tr, WorldEntity en)
    {
        expirationTime = new GregorianCalendar();
        expirationTime.add(GregorianCalendar.MILLISECOND, durationMs);
        this.limited = (durationMs>0)?true:false;
        this.expired = false;

        this.name = name;
        this.value = value;

        if (tr!=null && en!=null)
        {
            this.transformer = tr;
            this.worldEntity = en;
            tr.performTransformation(worldEntity.getSpatial());
        }
    }

    public WorldEntityProperty(String name, String value)
    {
        this(name,value,0,null,null);
    }

    public WorldEntityProperty(String name, String value, int durationMs)
    {
        this(name,value,durationMs,null,null);        
    }

    public WorldEntityProperty(String name, String value, WorldEntityTransformer tr, WorldEntity en)
    {
        this(name,value,0,tr,en);
    }

    public String getName(){return name;}
    public String getValue() {return value;}
    public boolean isLimited() {return limited;}
    public final boolean hasExpired() {return expired;}
    
    public final void deactivateProperty()
    {
        if (transformer!=null && worldEntity!=null)
        {
            transformer.undoTransformation(worldEntity.getSpatial());
        }
        expired = true;
    }

    public final void checkProperty()
    {
        if (limited && !expired)
        {
            GregorianCalendar now = new GregorianCalendar();
            if (now.compareTo(expirationTime)>=0)
            {
                deactivateProperty();
            }
        }
    }
}
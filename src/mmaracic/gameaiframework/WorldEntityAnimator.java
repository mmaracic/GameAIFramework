/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author Marijo
 */
public class WorldEntityAnimator extends AbstractControl {
    //Any local variables should be encapsulated by getters/setters so they
    //appear in the SDK properties window and can be edited.
    //Right-click a local variable to encapsulate it with getters and setters.
    Vector3f targetPosition=null;
    int timePerStepMs = 0;
    float timCorrection = 5f;
    
    public WorldEntityAnimator(Vector3f initPosition, int timePerStepMs)
    {
        this.targetPosition = initPosition;
        this.timePerStepMs = timePerStepMs;
    }

    @Override
    protected void controlUpdate(float tpf) {
        Vector3f currPos = spatial.getLocalTranslation();
        float distance = currPos.distance(targetPosition);
        if (distance>0)
        {
            float part = ((int)(tpf*1000*timCorrection))/(float)timePerStepMs;
            if (part>1f)
            {
                part=1f;
            }
            Vector3f difference = targetPosition.subtract(currPos);        
            Vector3f shift = difference.mult(part);
            spatial.move(shift);
        }
    }
    
    public void moveTo(Vector3f newPosition)
    {
        this.targetPosition = newPosition;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }
    
    @Override
    public Control cloneForSpatial(Spatial spatial) {
        WorldEntityAnimator control = new WorldEntityAnimator(targetPosition,timePerStepMs);
        control.spatial = spatial;
        return control;
    }
    
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }
}

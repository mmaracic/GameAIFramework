/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import static mmaracic.gameaiframework.WorldAgent.deathPropertyName;
import static mmaracic.gameaiframework.WorldEntity.propertyValueTrue;
import mmaracic.gameaiframework.WorldEntityProperty.WorldEntityTransformer;

/**
 *
 * @author Marijo
 */
public class PacmanAgent extends WorldAgent{
    
    public static final String powerupPropertyName = "PowerUp";

    PacmanAgent(float posX, float posY, float posZ, float oriX, float oriY, float oriZ,
            Geometry g, AgentAI ai, int noLives, int viewRange, int timePerStepMs)
    {
        super(posX,posY,posZ,oriX,oriY,oriZ,"Pacman", g, ai, timePerStepMs, 1, noLives, viewRange,1,3);
        
    }
    
    public final void eatPowerup()
    {
        addProperty(new WorldEntityProperty(powerupPropertyName, propertyValueTrue,5000,
                new WorldEntityTransformer(){
                    @Override
                    public void performTransformation(Spatial geometry) {
                        geometry.scale(2f);
                    }

                    @Override
                    public void undoTransformation(Spatial geometry) {
                        geometry.scale(0.5f);
                    }                    
                },this));
    }
    
    public final void die()
    {
        receiveDamage(1, new WorldEntityTransformer() {

            @Override
            public void performTransformation(Spatial geometry) {
                Material m = ((Geometry)geometry).getMaterial();
                m.setColor("Color", ColorRGBA.White);
            }

            @Override
            public void undoTransformation(Spatial geometry) {
                Material m = ((Geometry)geometry).getMaterial();
                m.setColor("Color", ColorRGBA.Blue);
            }
        });
    }
}

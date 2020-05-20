/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marijo
 * Shape of AI name: 
 */
public class AIFactory {
    public static List<AgentAI> createAI(String[] classPaths, List<String> AINames)
    {
        ClassLoader parent = AIFactory.class.getClassLoader();
        AIClassLoader customCL = new AIClassLoader(classPaths,parent);

        List<AgentAI> ais = new ArrayList<AgentAI>();
        for(int i=0; i<AINames.size(); i++)
        {
            try
            {
                Class c = customCL.loadClass(AINames.get(i));
                if (AgentAI.class.isAssignableFrom(c))
                {
                    Constructor con = c.getConstructor();
                    MessageCollector.putMessage("AI Factory","Loading AI class: "+AINames.get(i));
                    ais.add((AgentAI) con.newInstance());
                }
                else
                {
                    MessageCollector.putError("AI Factory",AINames.get(i)+" is not a subclass of AgentAI! Using default random AI");
                    ais.add(new AgentAI());                
                }
            }
            catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            {            
                MessageCollector.putError("AIFactory",ex.toString() +": "+ ex.getMessage());
                MessageCollector.putMessage("AI Factory","Using default random AI instead of "+AINames.get(i));
                ais.add(new AgentAI());                    
            }
        }
        return ais;
    }
}

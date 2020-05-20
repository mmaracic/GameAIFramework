/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
/**
 *
 * @author Marijo
 * http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
 * 
 * AI names can be class names or
 * file:D:\Projekti\MonkeyEngine\GameAIFramework\dist\DummyAI.class
 */
public class AIClassLoader extends ClassLoader {
    
    URLClassLoader loader=null;
    
    AIClassLoader(String[] classPaths, ClassLoader parent)
    {
        super(parent);
        ArrayList<URL> classURLs = new ArrayList<>();
        for (int i=0; i<classPaths.length; i++)
        {
            String path = classPaths[i];
            try
            {
                URL tempURL = new URL(path);
                classURLs.add(tempURL);
            }   
            catch (MalformedURLException ex) {
                MessageCollector.putError("AIClassLoader", "Bad url: "+path);
            }
        }
        if (!classURLs.isEmpty())
        {
            loader = new URLClassLoader((URL[]) classURLs.toArray(new URL[1]), parent);
        }
    }
    
    @Override
    public Class loadClass(String classURL) throws ClassNotFoundException
    {
        Class c = null;
        if (loader!=null)
        {
            try
            {
                c=loader.getParent().loadClass(classURL);

            }
            catch (ClassNotFoundException e)
            {
                try
                {
                    c = loader.loadClass(classURL);
                }
                catch(ClassNotFoundException e2)
                {
                    throw new ClassNotFoundException("Error while loading class "+classURL, e2);
                }
            }
        }
        else
        {
            c = this.getParent().loadClass(classURL);
        }
        return c;
    } 
}

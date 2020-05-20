/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import java.util.ArrayList;

/**
 *
 * @author Marijo
 */
public class MessageCollector {
    private static ArrayList<String> messages = new ArrayList<>();
    private static int i=0;
    
    public static void putError(String author, String error)
    {
        messages.add("Exception "+i+": "+author+" reports "+error);
        i=(++i>0)?i:0;
    }
    
    public static void putMessage(String author, String message)
    {
        messages.add("Message "+i+": " + author + " says " + message);
        i=(++i>0)?i:0;
    }
    
    public static ArrayList<String> getMessages()
    {
        ArrayList<String> tempMessages = new ArrayList<>(messages);
        messages.clear();
        return tempMessages;
    }
}

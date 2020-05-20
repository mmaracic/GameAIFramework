/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmaracic.gameaiframework;

import java.util.Locale;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 *
 * @author Marijo
 * Links:
 * http://www.informit.com/articles/article.aspx?p=2027052&seqNum=2
 * http://stackoverflow.com/questions/12952095/can-we-compile-a-java-program-inside-an-applet-or-a-swing-window
 */
public class CompilerWrapper {
    
    JavaCompiler compiler;
    
    CompilerWrapper()
    {
         compiler = ToolProvider.getSystemJavaCompiler();
         
         DiagnosticListener fManDiagListener = new DiagnosticCollector();        
         StandardJavaFileManager fMan = compiler.getStandardFileManager(fManDiagListener,null,null);
         
    }
}

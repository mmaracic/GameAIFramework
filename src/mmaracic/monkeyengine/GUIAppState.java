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
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import mmaracic.gameaiframework.MessageCollector;
import mmaracic.gameaiframework.PacmanWorld;
import static mmaracic.gameaiframework.PacmanWorld.Outcome.Defeat;
import static mmaracic.gameaiframework.PacmanWorld.Outcome.Win;

/**
 *
 * @author Marijo
 */
public class GUIAppState extends AbstractAppState implements ScreenController {

    Properties p = new Properties();

    private SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;

    GameAppState game = null;
    PacmanWorld.Outcome res = PacmanWorld.Outcome.Ongoing;
    int msgHistoryLen = 50;
    boolean locked = false;

    NiftyJmeDisplay niftyDisplay;

    private void saveProperties() {
        if (niftyDisplay.getNifty().getCurrentScreen() != null && niftyDisplay.getNifty().getCurrentScreen().getScreenId().compareTo("start") == 0) {
            TextField txtClassPath = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtClassPath", TextField.class);
            p.setProperty("classPath", txtClassPath.getRealText());
            TextField txtPacman = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtPacmanAI", TextField.class);
            p.setProperty("pacmanAI", txtPacman.getRealText());
            TextField txtGhost = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtGhostAI", TextField.class);
            p.setProperty("ghostAI", txtGhost.getRealText());
            try {
                p.store(new FileOutputStream("GameSettings.properties"), "");
            } catch (IOException ex) {
                Logger.getLogger(GUIAppState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        //TODO: initialize your AppState, e.g. attach spatials to rootNode
        //this is called on the OpenGL thread after the AppState has been attached

        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();

        //UI
        try {
            this.app.setDisplayStatView(false);
            this.app.setDisplayFps(false);
            this.app.getFlyByCamera().setDragToRotate(true);

            niftyDisplay = new NiftyJmeDisplay(assetManager, this.app.getInputManager(), this.app.getAudioRenderer(), this.app.getGuiViewPort());
            this.app.getGuiViewPort().addProcessor(niftyDisplay);
            Nifty nifty = niftyDisplay.getNifty();

            //nifty.validateXml("Interface/niftyGUIMain.xml");
            nifty.fromXml("Interface/niftyGUIMain.xml", "start", this);
            //nifty.validateXml("Interface/niftyGUIOverlay.xml");
            nifty.addXml("Interface/niftyGUIOverlay.xml");
            //nifty.validateXml("Interface/niftyGUIEnd.xml");
            nifty.addXml("Interface/niftyGUIEnd.xml");
            nifty.addXml("Interface/niftyGUIRegister.xml");
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error in GUI XML: " + ex.getMessage());

            this.app.getGuiViewPort().removeProcessor(niftyDisplay);
            this.app.setDisplayStatView(false);
            this.app.setDisplayFps(true);
            this.app.getFlyByCamera().setDragToRotate(false);
        }
    }

    @Override
    public void update(float tpf) {

        //update messages
        if (niftyDisplay.getNifty().getCurrentScreen() != null && niftyDisplay.getNifty().getCurrentScreen().getScreenId().compareTo("overlay") == 0) {
            ArrayList<String> messages = MessageCollector.getMessages();
            if (!messages.isEmpty()) {
                ListBox listBox = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("lstMessages", ListBox.class);
                for (String msg : messages) {
                    listBox.addItem(msg);
                    if (listBox.itemCount() > msgHistoryLen) {
                        listBox.removeItemByIndex(0);
                    }
                    listBox.selectItem(listBox.itemCount() - 1);
                }
            }
        }
        //check for game state
        if (game != null) {
            //If game over
            PacmanWorld.Outcome tempRes = game.getGameState();
            if (tempRes != PacmanWorld.Outcome.Ongoing) {
                ListBox listBox = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("lstMessages", ListBox.class);
                listBox.clear();

                res = tempRes;
                niftyDisplay.getNifty().gotoScreen("end");
            }

            //if game paused
            if (niftyDisplay.getNifty().getCurrentScreen().getScreenId().compareTo("overlay") == 0) {
                if (game.isPaused()) {
                    Label alert = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("alert", Label.class);
                    alert.setText("Paused");
                } else {
                    Label alert = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("alert", Label.class);
                    alert.setText("Game in progress");
                }
            }

            //if game outcome shown
            if (niftyDisplay.getNifty().getCurrentScreen().getScreenId().compareTo("end") == 0) {
                Label outcome = niftyDisplay.getNifty().getScreen("end").findNiftyControl("labOutcome", Label.class);
                //Label softOutcome = niftyDisplay.getNifty().getScreen("end").findNiftyControl("labSoftOutcome", Label.class);
                Element softOutcome = niftyDisplay.getNifty().getScreen("end").findElementByName("labSoftOutcome");

                switch (res) {
                    case Win:
                        outcome.setText("Win");
                        break;
                    case Defeat:
                        outcome.setText("Defeat");
                        break;
                    default:
                        outcome.setText("Tie");
                        break;
                }
                Map<String, String> softState = game.getSoftGameState();
                StringBuilder softInfo = new StringBuilder();
                for (String prop : softState.keySet()) {
                    softInfo.append(prop + ": ");
                    softInfo.append(softState.get(prop) + " ");
                }
                //softOutcome.setText(softInfo.toString());
                softOutcome.getRenderer(TextRenderer.class).setText(softInfo.toString());

                game = null;
                res = PacmanWorld.Outcome.Ongoing;
            }
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
        saveProperties();
        this.app.getGuiViewPort().removeProcessor(niftyDisplay);
        this.app.setDisplayStatView(false);
        this.app.setDisplayFps(true);
        this.app.getFlyByCamera().setDragToRotate(false);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
    }

    @Override
    public void onStartScreen() {
        try {
            p.load(new FileInputStream("GameSettings.properties"));
            String classPath = p.getProperty("classPath");
            if (classPath != null) {
                TextField txtClassPath = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtClassPath", TextField.class);
                if (txtClassPath != null) {
                    txtClassPath.setText(classPath);
                }
            }
            String pacmanAI = p.getProperty("pacmanAI");
            if (pacmanAI != null) {
                TextField txtPacman = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtPacmanAI", TextField.class);
                if (txtPacman != null) {
                    txtPacman.setText(pacmanAI);
                }
            }
            String ghostAI = p.getProperty("ghostAI");
            if (ghostAI != null) {
                TextField txtGhost = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtGhostAI", TextField.class);
                if (txtGhost != null) {
                    txtGhost.setText(ghostAI);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUIAppState.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GUIAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onEndScreen() {
    }

    public void startGame() {
        TextField txtClassPath = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtClassPath", TextField.class
        );
        String classPath = txtClassPath.getRealText();
        TextField txtPacman = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtPacmanAI", TextField.class);
        String pacmanAI = txtPacman.getRealText();
        TextField txtGhost = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtGhostAI", TextField.class);
        String ghostAI = txtGhost.getRealText();

        if (!locked) {
            game = new GameAppState("mmaracic.gameaiframework.PacmanBuilder", 20, 20, 500, classPath, pacmanAI, ghostAI);
        } else {
            game = new GameAppState("mmaracic.gameaiframework.PacmanBuilder", 20, 20, 500, classPath, pacmanAI, ghostAI);
        }

        saveProperties();

        niftyDisplay.getNifty()
                .gotoScreen("overlay");
        res = PacmanWorld.Outcome.Ongoing;

        app.getStateManager()
                .attach(game);

    }

    public void mainMenu() {
        niftyDisplay.getNifty().gotoScreen("start");
    }

    public void quitGame() {
        app.stop();
    }

    public void selectPath() {
        TextField txtClassPath = niftyDisplay.getNifty().getCurrentScreen().findNiftyControl("txtClassPath", TextField.class
        );

        JFileChooser fc = new JFileChooser();

        fc.setDialogTitle(
                "Select Class Path Folder");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fc.showDialog(
                null, "Select") == JFileChooser.APPROVE_OPTION) {
            File path = fc.getSelectedFile();
            txtClassPath.setText("file:" + path.getAbsolutePath() + File.separator);
        }
    }

    public void registerMenu() {

    }

    public void registerUser() {

    }

    public void submitAI() {

    }
}

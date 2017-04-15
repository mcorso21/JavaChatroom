package ServerPack.JavaFX.RunningScreen;

import ServerPack.JavaFX.LaunchScreen.LaunchScreenController;
import ServerPack.Server;
import ServerPack.ServerMain;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class RunningScreenController {
    public Server server;
    public LaunchScreenController launchScreenController;
    public TextArea chatWindow;
    public TextArea messageWindow;
    public ListView clientListView;
    public Button shutdownButton;
    public String username = "host";

    public RunningScreenController() {
    }

    public void updateChatWindow(String data) {
        chatWindow.appendText(data + "\n");
    }

    public void messageBoxOnEnter(){
        messageWindow.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER){
                    event.consume();
                    if (!messageWindow.getText().replace(" ", "").equals("")){
                        String message = String.format("%s: %s", username, messageWindow.getText());
                        messageWindow.clear();
                        try {
                            server.receivedData(null, message);
                        }
                        catch (Throwable t){
                            ServerMain.catcher("RunningScreenController messageBoxOnEnter threw: ", t);
                        }
                    }
                }
            }
        });
    }

    public void updateClientListView() {

    }

    public void shutDownServer() {

    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setLaunchScreenController(LaunchScreenController lsc) {
        this.launchScreenController = lsc;
    }
}

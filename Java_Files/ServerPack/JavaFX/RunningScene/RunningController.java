package ServerPack.JavaFX.RunningScreen;

import ServerPack.JavaFX.LaunchScreen.LaunchController;
import ServerPack.Server;
import ServerPack.ServerMain;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class RunningController {
    public Server server;
    public LaunchController launchController;
    public VBox chatAreaVBox;
    public ScrollPane chatWindowSP;
    public TextFlow chatWindow;
    public TextArea messageWindow;
    public ListView clientListView;
    public Button shutdownButton;
    public String username = "Host";
    private Stage stage;
    public ObservableList clientList = FXCollections.observableArrayList();

    public RunningController() {
    }

    public void updateChatWindow(String source, String data) {
        try {
            Text text = new Text(data + "\n");
            if (source.equals("server")) {
                text.setFill(Color.RED);
            }
            if (data.split(" ")[0].equals("server")) {
                data = data.split(" ", 2)[1];
                text.setText(data + "\n");
            }
            Platform.runLater( () -> {
                    chatWindow.getChildren().add(text);
                }
            );
        } catch (Throwable t) {
            ServerMain.catcher("RunningController updateChatWindow threw: ", t);
        }
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
                            ServerMain.catcher("RunningController messageBoxOnEnter threw: ", t);
                        }
                    }
                }
            }
        });
    }

    public void updateClientListView(ObservableList clients) {
        try {
            Platform.runLater( () -> {
                        this.clientList = null;
                        this.clientList = FXCollections.observableArrayList(clients);;
                        this.clientListView.setItems(this.clientList);
                    }
            );
        } catch (Throwable t) {
            ServerMain.catcher("RunningController addClientToListView threw: ", t);
        }
    }

    public void shutDownCloseButton() {
        try {
            this.server.shutDownServer();
            this.stage = (Stage) shutdownButton.getScene().getWindow();
            this.stage.close();
        } catch (Throwable t) {
            ServerMain.catcher("RunningController shutDownCloseButton threw: ", t);
        }
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setLaunchController(LaunchController lsc) {
        this.launchController = lsc;
    }
}

package ClientPack.JavaFX;

import ClientPack.Client;
import ClientPack.ClientMain;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;


public class RunningController {

    Client client;
    // runningFXML
    public VBox chatAreaVBox;
    public ScrollPane chatWindowSP;
    public TextFlow chatWindow;
    public TextArea messageWindow;
    public ListView<String> clientListView;
    public Button shutdownButton;
    private Stage stage;
    private String username;
    public ObservableList<String> clientList = FXCollections.observableArrayList();

    public void disconnect() {
        try {
            this.client.closeConnection();
            this.stage = (Stage) shutdownButton.getScene().getWindow();
            this.stage.close();
        } catch (Throwable t) {
            ClientMain.catcher("RunningController disconnect threw: ", t);
        }
    }

    public void updateChatWindow(String data) {
        try {
            Text text = new Text(data + "\n");
            if (data.split(" ")[0].equals("server")) {
                text.setFill(Color.RED);
                data = data.split(" ", 2)[1];
                text.setText(data + "\n");
            }
            Platform.runLater( () -> {
                        chatWindow.getChildren().add(text);
                    }
            );
        } catch (Throwable t) {
            ClientMain.catcher("RunningController updateChatWindow threw: ", t);
        }
    }

    public void messageBoxOnEnter(){
        messageWindow.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER){
                    event.consume();
                    if (!messageWindow.getText().replace(" ", "").equals("")){
                        String message = String.format("000%s: %s", username, messageWindow.getText());
                        messageWindow.clear();
                        try {
                            client.sendData(message);
                        }
                        catch (Throwable t){
                            ClientMain.catcher("RunningController messageBoxOnEnter threw: ", t);
                        }
                    }
                }
            }
        });
    }

    public void updateClientListView(String clientsAsCSV) {
        try {

            Platform.runLater( () -> {
                        String[] clients = clientsAsCSV.split(",");
                        this.clientList.clear();
                        this.clientList.addAll(clients);
                        this.clientListView.setItems(this.clientList);
                    }
            );
        } catch (Throwable t) {
            ClientMain.catcher("RunningScreenController updateClientListView threw: ", t);
        }
    }


    public void setClient(Client client) {
        this.client = client;
        this.username = this.client.getUsername();
    }
}

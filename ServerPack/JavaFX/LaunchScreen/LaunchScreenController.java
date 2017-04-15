package ServerPack.JavaFX.LaunchScreen;

import ServerPack.Server;
import ServerPack.ServerMain;
import ServerPack.JavaFX.RunningScreen.RunningScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.net.Socket;
import java.util.regex.Pattern;

public class LaunchScreenController {

    public Button startServerButton;
    public Label portTextFieldLabel;
    public TextField portTextField;
    public Text statusText;
    private Server server;
    private RunningScreenController runningScreenController;
    private Stage stage;

    public LaunchScreenController() {

    }

    public void launchServer() {
        try {
            if (portTextField.getText().equals("")) {
                statusText.setText("Please enter a port number.");
                return;
            }
            int port = -1;
            if (Pattern.matches("^[0-9]*$", this.portTextField.getText())) {
                port = Integer.parseInt(this.portTextField.getText());
                if (port >= 0 && port <= 65535) {
                    try {
                        (new Socket("localhost", port)).close();
                        statusText.setText(String.format("Port %s is unavailable.", port));
                    } catch (Throwable t) {

                    }
                }
                else {
                    statusText.setText("Port must be between 0 - 65535.");
                }
            }
            else {
                statusText.setText("Port must be numbers only.");
            }
            if (port == -1) {
                this.portTextField.clear();
                return;
            }
            // Prep server to run and create RunningScreenController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../RunningScreen/runningScreenFXML.fxml"));
            Parent root = loader.load();
            this.runningScreenController = loader.getController();
            this.server.setRunningScreenController(this.runningScreenController);
            this.runningScreenController.setServer(this.server);
            this.server.setHostPort(port);
            this.server.initServer();
            // Change scene to runningScene
            this.stage = (Stage) startServerButton.getScene().getWindow();
            this.stage.setScene(new Scene(root, 600, 400));
            this.stage.show();
        } catch (Throwable t) {
            this.portTextField.clear();
            ServerMain.catcher("launchScreenController launchServer threw: ", t);
        }
    }

    public void setServer(Server server) {
        this.server = server;
    }
}

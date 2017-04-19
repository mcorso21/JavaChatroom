package ClientPack.JavaFX;

import ClientPack.Client;
import ClientPack.ClientMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.regex.Pattern;

public class LoginController {
    private String username = "Guest";
    private String hostIP = "";
    private int hostPort = -1;
    private Client client;
    private Stage stage;
    public TextField usernameTF;
    public TextField hostIPTF;
    public TextField hostPortTF;
    public Button connectButton;
    public Text statusText;

    public void connect() {
        try {
            // Username check
            this.username = this.usernameTF.getText();
            if (this.username.replace(" ", "").equals("")) {
                this.statusText.setText("Invalid username.");
                return;
            }
            if (this.username.length() > 15) {
                this.statusText.setText("Username must be less than 15 characters long.");
                return;
            }
            // IP check
            this.hostIP = this.hostIPTF.getText();
            if (this.hostIP.replace(" ", "").equals("")) {
                this.statusText.setText("Invalid host address.");
                return;
            }
            // Port check
            if (this.hostPortTF.getText().equals("") || !Pattern.matches("^[0-9]*$", this.hostPortTF.getText())) {
                statusText.setText("Invalid port number.");
                return;
            }
            this.hostPort = Integer.parseInt(this.hostPortTF.getText());
            // Change to runningFXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("runningFXML.fxml"));
            Parent root = loader.load();
            this.client.setUsername(this.username);
            this.client.setHostIP(this.hostIP);
            this.client.setHostPort(this.hostPort);
            this.client.setRunningController(loader.getController());
            this.client.initConnection();
            // Change scene to runningScene
            this.stage = (Stage) connectButton.getScene().getWindow();
            this.stage.setScene(new Scene(root, 600, 400));
            this.stage.show();
        } catch (Throwable t) {
            ClientMain.catcher("LoginController connect threw: ", t);
        }
    }

    public void setClient(Client client) {
        this.client = client;
    }
}

package ClientPack;

import ClientPack.JavaFX.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;

public class ClientMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static void catcher(String message, Throwable t) {
        System.out.println(String.format("%s %s", message, t));
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("JavaFX/loginFXML.fxml"));
            Parent root = loader.load();
            LoginController loginController = loader.getController();
            Client client = new Client(1024, loginController);
            loginController.setClient(client);

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    client.closeConnection();
                }
            });

            primaryStage.setTitle("Chat Client");
            primaryStage.setScene(new Scene(root, 400, 200));
            primaryStage.setResizable(false);
            primaryStage.initStyle(StageStyle.UTILITY);
            primaryStage.show();
        } catch (Throwable t) {
            catcher("ClientMain start threw: ", t);
        }
    }
}

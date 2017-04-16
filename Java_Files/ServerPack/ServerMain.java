package ServerPack;

import ServerPack.JavaFX.LaunchController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;

public class ServerMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static void catcher(String message, Throwable t) {
        System.out.println(String.format("%s: %s", message, t));
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Server server = new Server(1024);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("JavaFX/launchFXML.fxml"));
            Parent root = loader.load();
            LaunchController lsc = loader.getController();
            server.setLaunchController(lsc);
            lsc.setServer(server);

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    server.shutDownServer();
                }
            });

            primaryStage.setTitle("Chat Server");
            primaryStage.setScene(new Scene(root, 400, 200));
            primaryStage.setResizable(false);
            primaryStage.initStyle(StageStyle.UTILITY);
            primaryStage.show();
        } catch (Throwable t) {
            catcher("ServerMain start threw: ", t);
        }
    }
}

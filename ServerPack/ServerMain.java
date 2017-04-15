package ServerPack;

import ServerPack.JavaFX.LaunchScreen.LaunchScreenController;
import ServerPack.JavaFX.RunningScreen.RunningScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("JavaFX/LaunchScreen/launchScreenFXML.fxml"));
            Parent root = loader.load();
            LaunchScreenController lsc = loader.getController();
            server.setLaunchScreenController(lsc);
            lsc.setServer(server);

//            Parent root = loader.load(getClass().getResource("JavaFX/LaunchScreen/launchScreenFXML.fxml"));

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

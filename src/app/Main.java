package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Assegura que o recurso FXML seja encontrado corretamente
        URL fxmlLocation = getClass().getResource("/app/view/MainView.fxml");
        if (fxmlLocation == null) {
            System.err.println("Não foi possível encontrar o arquivo FXML. Verifique o caminho.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);

        // Opcional: Carregar o CSS
        URL cssLocation = getClass().getResource("/resources/style.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        }

        primaryStage.setTitle("Simulador SGBD");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
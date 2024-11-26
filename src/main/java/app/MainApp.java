package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import app.controller.SimulationController;
import app.view.SimulationView;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Создание корневого узла и сцены
        SimulationView simulationView = new SimulationView();
        SimulationController simulationController = new SimulationController(simulationView);

        // Получение корневого узла из SimulationView
        BorderPane root = simulationView.getView();

        // Создание сцены
        Scene scene = new Scene(root, 1200, 800);

        // Подключение CSS-стиля
        String cssPath = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        // Установка сцены и отображение окна
        primaryStage.setScene(scene);
        primaryStage.setTitle("Симуляция прыжка с парашютом");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
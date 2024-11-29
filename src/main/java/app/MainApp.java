package app;

import app.controller.SimulationController;
import app.view.SimulationView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Главный класс приложения, отвечающий за запуск и отображение основного окна.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Создание корневого узла и сцены
        SimulationView simulationView = new SimulationView();
        new SimulationController(simulationView);

        // Получение корневого узла из SimulationView
        Scene scene = new Scene(simulationView.getView());

        // Подключение CSS-стиля
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Установка сцены и отображение окна в полноэкранном режиме
        primaryStage.setScene(scene);
        primaryStage.setTitle("Симуляция прыжка с парашютом");
        primaryStage.setFullScreen(true); // Запуск в полноэкранном режиме
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
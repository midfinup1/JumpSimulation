// src/main/java/app/controller/SimulationController.java

package app.controller;

import app.model.Simulation;
import app.view.AnimationPane;
import app.view.ChartsPanel;
import app.view.ControlsPanel;
import app.view.SimulationView;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Класс SimulationController отвечает за обработку событий пользовательского интерфейса
 * и управление логикой симуляции падения объекта.
 * Он взаимодействует с представлением (SimulationView) и моделью (Simulation).
 */
public class SimulationController {

    private final SimulationView view;
    private Simulation.SimulationResult simulationResult;
    private int currentIndex = 0;
    private AnimationTimer animationTimer;
    private boolean isRunning = false;
    private double timeScale = 0.1; // Скорость времени, 1.0 = реальное время
    private double accumulatedTime = 0.0; // Накопленное время для симуляции

    /**
     * Конструктор класса SimulationController.
     *
     * @param view экземпляр SimulationView, содержащий все подкомпоненты пользовательского интерфейса
     */
    public SimulationController(SimulationView view) {
        this.view = view;
        initialize();
    }

    /**
     * Инициализирует контроллер, устанавливая обработчики событий и настраивая анимацию.
     */
    private void initialize() {
        // Получение подкомпонентов представления
        ControlsPanel controls = view.getControlsPanel();
        AnimationPane animationPane = view.getAnimationPane();
        ChartsPanel chartsPanel = view.getChartsPanel();

        // Настройка обработчиков событий для кнопок
        controls.getStartButton().setOnAction(e -> handleStartButton());
        controls.getResetButton().setOnAction(e -> handleResetButton());
        controls.getRewindButton().setOnAction(e -> handleRewindButton());
        controls.getForwardButton().setOnAction(e -> handleForwardButton());

        // Настройка анимации через AnimationTimer
        animationTimer = new AnimationTimer() {
            private long lastUpdateTime = 0;

            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }

                // Рассчитываем прошедшее время в секундах с учетом масштабирования времени
                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0 * timeScale;
                accumulatedTime += deltaTime;

                // Обновляем симуляцию только тогда, когда накопилось достаточно времени для следующего шага
                if (accumulatedTime >= 1.0 / 60.0) { // 60 FPS
                    updateSimulation(accumulatedTime);
                    accumulatedTime = 0.0; // Сбрасываем накопленное время
                }

                lastUpdateTime = now;
            }
        };
    }

    /**
     * Обработчик события нажатия кнопки "Старт/Пауза".
     * Запускает или приостанавливает анимацию симуляции.
     */
    private void handleStartButton() {
        ControlsPanel controls = view.getControlsPanel();

        if (isRunning) {
            // Пауза симуляции
            animationTimer.stop();
            isRunning = false;
            controls.getStartButton().setText("Старт");
        } else {
            // Запуск симуляции
            try {
                // Чтение и парсинг входных данных
                double mass = Double.parseDouble(controls.getMassField().getText());
                double parachuteTime = Double.parseDouble(controls.getParachuteTimeField().getText());
                double area = Double.parseDouble(controls.getAreaField().getText());
                double areaParachute = Double.parseDouble(controls.getAreaParachuteField().getText());

                // Валидация параметров
                if (mass <= 0 || parachuteTime < 0 || area <= 0 || areaParachute <= 0) {
                    throw new IllegalArgumentException("Все параметры должны быть положительными числами.");
                }

                // Запуск симуляции с помощью модели
                simulationResult = Simulation.simulateJump(mass, 39000, area, areaParachute, parachuteTime);
                currentIndex = 0;

                // Очистка предыдущих данных
                view.getChartsPanel().getAltitudeChart().getData().clear();
                view.getChartsPanel().getVelocityChart().getData().clear();
                view.getChartsPanel().getAccelerationChart().getData().clear();
                view.getChartsPanel().getMachNumberChart().getData().clear();
                view.getAnimationPane().resetSimulation();

                // Предварительная отрисовка данных на графиках (опционально)
                plotInitialData();

                // Запуск анимации
                animationTimer.start();
                isRunning = true;
                controls.getStartButton().setText("Пауза");
            } catch (NumberFormatException e) {
                showAlert("Неверный ввод", "Пожалуйста, введите корректные числовые значения.");
            } catch (IllegalArgumentException e) {
                showAlert("Ошибка параметров", e.getMessage());
            }
        }
    }

    /**
     * Обработчик события нажатия кнопки "Сброс".
     * Останавливает симуляцию и сбрасывает все данные до начальных значений.
     */
    private void handleResetButton() {
        animationTimer.stop();
        isRunning = false;
        currentIndex = 0;
        view.getChartsPanel().getAltitudeChart().getData().clear();
        view.getChartsPanel().getVelocityChart().getData().clear();
        view.getChartsPanel().getAccelerationChart().getData().clear();
        view.getChartsPanel().getMachNumberChart().getData().clear();
        view.getAnimationPane().resetSimulation();
        view.getControlsPanel().getStartButton().setText("Старт");
    }

    /**
     * Обработчик события нажатия кнопки "⏪" для замедления симуляции.
     * Уменьшает скорость времени, но не ниже 0.1x.
     */
    private void handleRewindButton() {
        timeScale = Math.max(0.1, timeScale / 2); // Замедление
        System.out.println("Замедление, новая скорость времени: " + timeScale);
    }

    /**
     * Обработчик события нажатия кнопки "⏩" для ускорения симуляции.
     * Увеличивает скорость времени, но не выше 4x.
     */
    private void handleForwardButton() {
        timeScale = Math.min(4.0, timeScale * 2); // Ускорение
        System.out.println("Ускорение, новая скорость времени: " + timeScale);
    }

    /**
     * Обновляет симуляцию: обновляет позицию падающего объекта и значения аннотаций.
     *
     * @param deltaTime прошедшее время с последнего обновления
     */
    private void updateSimulation(double deltaTime) {
        if (simulationResult != null && currentIndex < simulationResult.time.size()) {
            double time = simulationResult.time.get(currentIndex);
            double altitude = simulationResult.altitude.get(currentIndex);
            double velocity = simulationResult.velocity.get(currentIndex);
            double acceleration = simulationResult.acceleration.get(currentIndex);
            double machNumber = simulationResult.machNumber.get(currentIndex);

            // Обновление графиков
            view.getChartsPanel().addDataToChart(view.getChartsPanel().getAltitudeChart(), time, altitude);
            view.getChartsPanel().addDataToChart(view.getChartsPanel().getVelocityChart(), time, velocity);
            view.getChartsPanel().addDataToChart(view.getChartsPanel().getAccelerationChart(), time, acceleration);
            view.getChartsPanel().addDataToChart(view.getChartsPanel().getMachNumberChart(), time, machNumber);

            // Обновление позиции падающего объекта и аннотаций
            view.getAnimationPane().updateSimulationMarker(time, altitude, velocity, acceleration, machNumber);

            currentIndex++;

            // Ускорение симуляции при больших deltaTime (опционально)
            if (deltaTime > 1.0) {
                currentIndex += (int) (deltaTime * 10);
            }
        } else {
            // Завершение симуляции, если все данные обработаны
            animationTimer.stop();
            isRunning = false;
            view.getControlsPanel().getStartButton().setText("Старт");
        }
    }

    /**
     * Предварительная отрисовка всех данных на графиках (может быть отключена для больших объемов данных).
     */
    private void plotInitialData() {
        if (simulationResult != null) {
            for (int i = 0; i < simulationResult.time.size(); i++) {
                double time = simulationResult.time.get(i);
                double altitude = simulationResult.altitude.get(i);
                double velocity = simulationResult.velocity.get(i);
                double acceleration = simulationResult.acceleration.get(i);
                double machNumber = simulationResult.machNumber.get(i);

                view.getChartsPanel().addDataToChart(view.getChartsPanel().getAltitudeChart(), time, altitude);
                view.getChartsPanel().addDataToChart(view.getChartsPanel().getVelocityChart(), time, velocity);
                view.getChartsPanel().addDataToChart(view.getChartsPanel().getAccelerationChart(), time, acceleration);
                view.getChartsPanel().addDataToChart(view.getChartsPanel().getMachNumberChart(), time, machNumber);
            }
        }
    }

    /**
     * Метод для отображения диалогового окна с сообщением об ошибке.
     *
     * @param title   заголовок окна
     * @param message сообщение
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
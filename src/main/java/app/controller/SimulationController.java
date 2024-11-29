package app.controller;

import app.model.Simulation;
import app.view.AnimationPane;
import app.view.ChartsPanel;
import app.view.ControlsPanel;
import app.view.SimulationView;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;

/**
 * Класс {@code SimulationController} отвечает за обработку событий пользовательского интерфейса
 * и управление логикой симуляции падения объекта.
 */
public class SimulationController {

    private final ControlsPanel controls;
    private final AnimationPane animationPane;
    private final ChartsPanel chartsPanel;

    private Simulation.SimulationResult simulationResult;
    private int currentIndex = 0;

    private AnimationTimer animationTimer;
    private boolean isRunning = false;
    private boolean isSliderBeingDragged = false;

    // Переменные для управления скоростью симуляции
    private double simulationSpeed = 0.5; // Начальная скорость симуляции
    private static final double MIN_SIMULATION_SPEED = 0.01; // Минимальная скорость
    private static final double MAX_SIMULATION_SPEED = 2.0;  // Максимальная скорость

    private double stepsAccumulator = 0.0; // Для накопления дробных шагов

    public SimulationController(SimulationView view) {
        this.controls = view.getControlsPanel();
        this.animationPane = view.getAnimationPane();
        this.chartsPanel = view.getChartsPanel();
        initialize();
    }

    private void initialize() {
        // Настройка обработчиков событий для кнопок
        controls.getStartButton().setOnAction(e -> handleStartButton());
        controls.getResetButton().setOnAction(e -> handleResetButton());
        controls.getRewindButton().setOnAction(e -> adjustSimulationSpeed(-1));
        controls.getForwardButton().setOnAction(e -> adjustSimulationSpeed(1));

        // Настройка слушателя для ползунка времени
        controls.getTimeSlider().valueProperty().addListener((obs, oldVal, newVal) -> handleSliderChange(newVal.doubleValue()));

        // Обработка событий начала и конца перетаскивания ползунка
        controls.getTimeSlider().setOnMousePressed(e -> handleSliderPress());
        controls.getTimeSlider().setOnMouseReleased(e -> handleSliderRelease());

        // Настройка анимации через AnimationTimer
        animationTimer = new AnimationTimer() {
            private long lastUpdateTime = 0;

            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }
                double deltaTime = (now - lastUpdateTime) / 1_000_000_00.0; // Конвертируем наносекунды в секунды
                updateSimulation(deltaTime);
                lastUpdateTime = now;
            }
        };
    }

    private void handleSliderChange(double sliderValue) {
        if (isSliderBeingDragged && simulationResult != null) {
            int newIndex = (int) (sliderValue * (simulationResult.time.size() - 1));
            if (newIndex != currentIndex) {
                currentIndex = newIndex;
                stepsAccumulator = 0.0; // Сбрасываем накопитель шагов
                updateSimulationDisplay();
            }
        }
    }

    private void handleSliderPress() {
        isSliderBeingDragged = true;
        if (isRunning) {
            animationTimer.stop();
        }
    }

    private void handleSliderRelease() {
        isSliderBeingDragged = false;
        if (simulationResult != null) {
            updateSimulationDisplay();
        }
        if (isRunning) {
            animationTimer.start();
        }
    }

    private void handleStartButton() {
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
                double area = Double.parseDouble(controls.getAreaField().getText());
                double areaParachute = Double.parseDouble(controls.getAreaParachuteField().getText());
                double parachuteAltitude = Double.parseDouble(controls.getParachuteAltitudeField().getText());
                double transitionTime = Double.parseDouble(controls.getTransitionTimeField().getText());

                // Валидация параметров
                if (mass <= 0 || parachuteAltitude < 0 || area <= 0 || areaParachute <= 0 || transitionTime <= 0) {
                    throw new IllegalArgumentException("Все параметры должны быть положительными числами.");
                }

                // Запуск симуляции с помощью модели
                simulationResult = Simulation.simulateJump(mass, 39000, area, areaParachute, parachuteAltitude, transitionTime);
                if (simulationResult.time.isEmpty()) {
                    showAlert("Симуляция не выполнена", "Симуляция не сгенерировала данных. Проверьте входные параметры и попробуйте снова.");
                    return;
                }

                // Отрисовка данных на графиках
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

    private void resetSimulationState() {
        animationTimer.stop();
        isRunning = false;
        currentIndex = 0;
        stepsAccumulator = 0.0;
        simulationSpeed = 0.5;

        clearCharts();
        animationPane.resetSimulation();
        controls.getStartButton().setText("Старт");
        controls.getTimeSlider().setValue(0);
        controls.getCurrentTimeLabel().setText("Текущее время: 0.0 с");
    }

    private void handleResetButton() {
        resetSimulationState();
    }

    private void adjustSimulationSpeed(int direction) {
        if (direction < 0) {
            simulationSpeed = Math.max(simulationSpeed / 2.0, MIN_SIMULATION_SPEED);
        } else {
            simulationSpeed = Math.min(simulationSpeed * 2.0, MAX_SIMULATION_SPEED);
        }
    }

    private void updateSimulation(double deltaTime) {
        if (simulationResult != null) {
            if (simulationResult.timeStep <= 0) {
                // Защита от деления на ноль или отрицательного шага
                simulationResult.timeStep = 0.1;
            }

            // Рассчитываем количество шагов, которые нужно пройти
            double stepsToAdvance = deltaTime * simulationSpeed / simulationResult.timeStep;

            // Накапливаем дробную часть шагов
            stepsAccumulator += stepsToAdvance;
            int steps = (int) stepsAccumulator;
            stepsAccumulator -= steps;

            // Увеличиваем currentIndex на количество целых шагов
            currentIndex += steps;

            if (currentIndex >= simulationResult.time.size()) {
                currentIndex = simulationResult.time.size() - 1;
                animationTimer.stop();
                isRunning = false;
                controls.getStartButton().setText("Старт");
                return;
            }

            updateSimulationDisplay();
        }
    }

    private void updateSimulationDisplay() {
        double time = simulationResult.time.get(currentIndex);
        double altitude = simulationResult.altitude.get(currentIndex);
        double velocity = simulationResult.velocity.get(currentIndex);
        double acceleration = simulationResult.acceleration.get(currentIndex);
        double machNumber = simulationResult.machNumber.get(currentIndex);

        animationPane.updateSimulationMarker(time, altitude, velocity, acceleration, machNumber);

        double sliderValue = (double) currentIndex / (simulationResult.time.size() - 1);
        Platform.runLater(() -> {
            if (!isSliderBeingDragged) {
                controls.getTimeSlider().setValue(sliderValue);
            }
            controls.getCurrentTimeLabel().setText(String.format("Текущее время: %.2f с", time));
        });

        // Обновляем текущие точки на графиках
        updateCurrentPointOnCharts(time, altitude, velocity, acceleration, machNumber);
    }

    private void plotInitialData() {
        if (simulationResult != null) {
            // Очищаем графики перед отрисовкой
            clearCharts();

            // Добавляем полные данные в серии "Full" (серые)
            for (int i = 0; i < simulationResult.time.size(); i++) {
                double time = simulationResult.time.get(i);
                double altitude = simulationResult.altitude.get(i);
                double velocity = simulationResult.velocity.get(i);
                double acceleration = simulationResult.acceleration.get(i);
                double machNumber = simulationResult.machNumber.get(i);

                chartsPanel.getAltitudeSeriesFull().getData().add(new XYChart.Data<>(time, altitude));
                chartsPanel.getVelocitySeriesFull().getData().add(new XYChart.Data<>(time, -velocity));
                chartsPanel.getAccelerationSeriesFull().getData().add(new XYChart.Data<>(time, acceleration));
                chartsPanel.getMachNumberSeriesFull().getData().add(new XYChart.Data<>(time, machNumber));
            }
        } else {
            showAlert("Симуляция не выполнена", "Проверьте входные параметры и попробуйте снова.");
        }
    }

    private void clearCharts() {
        // Очищаем данные в сериях
        chartsPanel.getAltitudeSeriesFull().getData().clear();
        chartsPanel.getAltitudeCurrentPoint().getData().clear();

        chartsPanel.getVelocitySeriesFull().getData().clear();
        chartsPanel.getVelocityCurrentPoint().getData().clear();

        chartsPanel.getAccelerationSeriesFull().getData().clear();
        chartsPanel.getAccelerationCurrentPoint().getData().clear();

        chartsPanel.getMachNumberSeriesFull().getData().clear();
        chartsPanel.getMachNumberCurrentPoint().getData().clear();
    }

    private void updateCurrentPointOnCharts(double time, double altitude, double velocity, double acceleration, double machNumber) {
        // Очищаем предыдущие точки
        chartsPanel.getAltitudeCurrentPoint().getData().clear();
        chartsPanel.getVelocityCurrentPoint().getData().clear();
        chartsPanel.getAccelerationCurrentPoint().getData().clear();
        chartsPanel.getMachNumberCurrentPoint().getData().clear();

        // Создаём новые точки
        XYChart.Data<Number, Number> altitudeData = new XYChart.Data<>(time, altitude);
        XYChart.Data<Number, Number> velocityData = new XYChart.Data<>(time, -velocity);
        XYChart.Data<Number, Number> accelerationData = new XYChart.Data<>(time, acceleration);
        XYChart.Data<Number, Number> machNumberData = new XYChart.Data<>(time, machNumber);

        // Создаём символы для точек
        altitudeData.setNode(new Circle(3, javafx.scene.paint.Color.BLUE));
        velocityData.setNode(new Circle(3, javafx.scene.paint.Color.BLUE));
        accelerationData.setNode(new Circle(3, javafx.scene.paint.Color.BLUE));
        machNumberData.setNode(new Circle(3, javafx.scene.paint.Color.BLUE));

        // Добавляем Tooltip к точкам
        addTooltipToData(altitudeData, String.format("Время: %.2f с\nВысота: %.2f м", time, altitude));
        addTooltipToData(velocityData, String.format("Время: %.2f с\nСкорость: %.2f м/с", time, -velocity));
        addTooltipToData(accelerationData, String.format("Время: %.2f с\nУскорение: %.2f м/с²", time, acceleration));
        addTooltipToData(machNumberData, String.format("Время: %.2f с\nЧисло Маха: %.2f", time, machNumber));

        // Добавляем точки на графики
        chartsPanel.getAltitudeCurrentPoint().getData().add(altitudeData);
        chartsPanel.getVelocityCurrentPoint().getData().add(velocityData);
        chartsPanel.getAccelerationCurrentPoint().getData().add(accelerationData);
        chartsPanel.getMachNumberCurrentPoint().getData().add(machNumberData);
    }

    private void addTooltipToData(XYChart.Data<Number, Number> data, String tooltipText) {
        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(data.getNode(), tooltip);
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
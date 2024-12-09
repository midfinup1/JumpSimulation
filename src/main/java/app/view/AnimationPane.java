package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

/**
 * Класс {@code AnimationPane} отвечает за создание и управление анимацией падающего объекта
 * и отображением аннотаций, связанных с объектом.
 */
public class AnimationPane {

    private final StackPane pane;
    private final Circle fallingObject;
    private final AnnotationBox annotationBox;

    // Константы для преобразования высоты в позицию на экране
    private static final double IMAGE_HEIGHT = 850; // Высота изображения в пикселях
    private static final double IMAGE_WIDTH = 300;  // Ширина изображения в пикселях
    private static final double MAX_ALTITUDE = 39000; // Максимальная высота в метрах

    private final Label maxSpeedLabel;
    private final Label freeFallTimeLabel;

    /**
     * Конструктор класса {@code AnimationPane}.
     * Инициализирует все элементы анимационной панели.
     */
    public AnimationPane() {
        pane = new StackPane();
        pane.getStyleClass().add("animation-pane");
        pane.setPrefWidth(IMAGE_WIDTH);
        pane.setPrefHeight(IMAGE_HEIGHT);
        pane.setPadding(new Insets(10));

        // Загрузка и добавление фонового изображения
        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/gradient.png")));
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(false); // Растягиваем изображение
        backgroundImageView.setFitWidth(IMAGE_WIDTH);
        backgroundImageView.setFitHeight(IMAGE_HEIGHT);
        backgroundImageView.setTranslateY(-25); // Смещение фона на 50 пикселей вверх

        pane.getChildren().add(backgroundImageView);

        // Создание падающего объекта
        fallingObject = new Circle(10);
        fallingObject.setStyle("-fx-fill: rgba(102,0,255,0.75);");
        fallingObject.getStyleClass().add("falling-object");
        fallingObject.setCenterX(IMAGE_WIDTH / 2);
        fallingObject.setCenterY(mapAltitudeToYPosition(MAX_ALTITUDE));

        // Создание аннотаций
        annotationBox = new AnnotationBox(fallingObject);

        Pane animationLayer = new Pane();
        animationLayer.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        animationLayer.getChildren().addAll(fallingObject, annotationBox.getPane());

        pane.getChildren().add(animationLayer);

        // Создание меток для максимальной скорости и времени свободного падения
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.BOTTOM_LEFT);
        infoBox.setPadding(new Insets(-15));

        maxSpeedLabel = new Label("Максимальная скорость: 0 м/с");
        maxSpeedLabel.getStyleClass().add("info-label");
        freeFallTimeLabel = new Label("Время свободного падения: 0 с");
        freeFallTimeLabel.getStyleClass().add("info-label");

        infoBox.getChildren().addAll(maxSpeedLabel, freeFallTimeLabel);

        pane.getChildren().add(infoBox);
        StackPane.setAlignment(infoBox, Pos.TOP_CENTER);
    }

    /**
     * Преобразует высоту (метры) в позицию Y на экране (пиксели).
     *
     * @param altitude высота в метрах
     * @return позиция Y в пикселях
     */
    private double mapAltitudeToYPosition(double altitude) {
        double normalizedAltitude = 1 - (altitude / MAX_ALTITUDE);
        normalizedAltitude = Math.max(0, Math.min(1, normalizedAltitude));

        // Увеличиваем нижний отступ для эффекта движения
        double yPosition = normalizedAltitude * (IMAGE_HEIGHT - 20);
        return Math.min(yPosition, IMAGE_HEIGHT);
    }

    /**
     * Возвращает панель с анимацией.
     *
     * @return StackPane с анимационными элементами
     */
    public StackPane getPane() {
        return pane;
    }

    /**
     * Обновляет позицию падающего объекта и данные аннотации.
     *
     * @param time         текущее время
     * @param altitude     текущая высота
     * @param velocity     текущая скорость
     * @param acceleration текущее ускорение
     * @param machNumber   текущее число Маха
     */
    public void updateSimulationMarker(double time, double altitude, double velocity, double acceleration, double machNumber) {
        double newY = mapAltitudeToYPosition(altitude);
        newY = Math.max(0, Math.min(IMAGE_HEIGHT, newY));
        fallingObject.setCenterY(newY);

        annotationBox.updateAnnotations(time, altitude, velocity, acceleration, machNumber);
    }

    /**
     * Сбрасывает позицию падающего объекта и аннотацию до начальных значений.
     */
    public void resetSimulation() {
        fallingObject.setCenterY(mapAltitudeToYPosition(MAX_ALTITUDE));
        fallingObject.setRadius(10);
        annotationBox.resetAnnotations();
        maxSpeedLabel.setText("Максимальная скорость: 0 м/с");
        freeFallTimeLabel.setText("Время свободного падения: 0 с");
    }

    /**
     * Обновляет отображение максимальной скорости и времени свободного падения.
     *
     * @param maxSpeed     максимальная скорость
     * @param freeFallTime время свободного падения
     */
    public void updateFinalResults(double maxSpeed, double freeFallTime) {
        maxSpeedLabel.setText(String.format("Максимальная скорость: %.1f м/с", maxSpeed));
        freeFallTimeLabel.setText(String.format("Время свободного падения: %.1f с", freeFallTime));
    }

    /**
     * Внутренний класс для управления аннотациями.
     */
    private static class AnnotationBox {
        private final VBox pane;
        private final Text timeText;
        private final Text altitudeText;
        private final Text velocityText;
        private final Text accelerationText;
        private final Text machNumberText;

        /**
         * Конструктор класса {@code AnnotationBox}.
         *
         * @param fallingObject объект, к которому привязана аннотация
         */
        public AnnotationBox(Circle fallingObject) {
            pane = new VBox(5);
            pane.getStyleClass().add("annotation-box");
            pane.setAlignment(Pos.CENTER_LEFT);

            timeText = new Text("Время: 0 с");
            altitudeText = new Text("Высота: 0 м");
            velocityText = new Text("Скорость: 0 м/с");
            accelerationText = new Text("Ускорение: 0 м/с²");
            machNumberText = new Text("Число Маха: 0");

            pane.getChildren().addAll(timeText, altitudeText, velocityText, accelerationText, machNumberText);

            pane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-padding: 5px; -fx-border-radius: 5px;");
            pane.layoutXProperty().bind(fallingObject.centerXProperty().add(20));
            pane.layoutYProperty().bind(fallingObject.centerYProperty().subtract(30));
        }

        /**
         * Обновляет текстовые метки аннотации с новыми значениями.
         */
        public void updateAnnotations(double time, double altitude, double velocity, double acceleration, double machNumber) {
            timeText.setText(String.format("Время: %.1f с", time));
            altitudeText.setText(String.format("Высота: %.1f м", altitude));
            velocityText.setText(String.format("Скорость: %.1f м/с", velocity));
            accelerationText.setText(String.format("Ускорение: %.1f м/с²", acceleration));
            machNumberText.setText(String.format("Число Маха: %.2f", machNumber));
        }

        /**
         * Сбрасывает текстовые метки аннотации до начальных значений.
         */
        public void resetAnnotations() {
            timeText.setText("Время: 0 с");
            altitudeText.setText("Высота: 0 м");
            velocityText.setText("Скорость: 0 м/с");
            accelerationText.setText("Ускорение: 0 м/с²");
            machNumberText.setText("Число Маха: 0");
        }

        /**
         * Возвращает панель аннотации.
         *
         * @return VBox с аннотацией
         */
        public VBox getPane() {
            return pane;
        }
    }
}
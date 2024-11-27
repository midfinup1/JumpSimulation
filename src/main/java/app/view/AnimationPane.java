// src/main/java/app/view/AnimationPane.java

package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Text;

/**
 * Класс AnimationPane отвечает за создание и управление анимацией падающего объекта
 * и отображением аннотаций, связанных с объектом.
 */
public class AnimationPane {

    private StackPane pane;
    private Box fallingObject;
    private AnnotationBox annotationBox;

    /**
     * Конструктор класса AnimationPane.
     * Инициализирует все элементы анимационной панели.
     */
    public AnimationPane() {
        createAnimationPane();
    }

    /**
     * Создаёт анимационную панель, включая падающий объект, фон и аннотации.
     */
    private void createAnimationPane() {
        Group animationGroup = new Group();

        // Загрузка и добавление фонового изображения
        ImageViewWrapper backgroundImageView = new ImageViewWrapper("/assets/gradient.png", 300, 800);
        animationGroup.getChildren().add(backgroundImageView.getImageView());

        // Создание падающего объекта
        fallingObject = new Box(20, 20, 20);
        fallingObject.setMaterial(new PhongMaterial(Color.RED));
        fallingObject.setTranslateX(150);
        fallingObject.setTranslateY(100); // Начальная позиция
        animationGroup.getChildren().add(fallingObject);

        // Создание аннотаций
        annotationBox = new AnnotationBox(fallingObject);
        animationGroup.getChildren().add(annotationBox.getPane());

        // Создание StackPane для размещения анимационной группы
        pane = new StackPane(animationGroup);
        pane.getStyleClass().add("animation-pane");
        pane.setPrefWidth(300);
        pane.setPadding(new Insets(10));
    }

    /**
     * Возвращает панель с анимацией.
     *
     * @return StackPane с анимационной группой
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
        // Обновление позиции падающего объекта
        double translateY = 500 - altitude / 100;
        fallingObject.setTranslateY(translateY);

        // Обновление аннотации
        annotationBox.updateAnnotations(time, altitude, velocity, acceleration, machNumber);
    }

    /**
     * Сбрасывает позицию падающего объекта и аннотацию до начальных значений.
     */
    public void resetSimulation() {
        fallingObject.setTranslateY(100);
        annotationBox.resetAnnotations();
    }

    /**
     * Внутренний класс для управления аннотациями.
     */
    private static class AnnotationBox {
        private VBox pane;
        private Text timeText;
        private Text altitudeText;
        private Text velocityText;
        private Text accelerationText;
        private Text machNumberText;

        /**
         * Конструктор класса AnnotationBox.
         *
         * @param fallingObject объект, к которому привязана аннотация
         */
        public AnnotationBox(Box fallingObject) {
            createAnnotation(fallingObject);
        }

        /**
         * Создаёт аннотацию, привязанную к падающему объекту.
         *
         * @param fallingObject объект, к которому привязана аннотация
         */
        private void createAnnotation(Box fallingObject) {
            pane = new VBox(5);
            pane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-padding: 5;");
            pane.setAlignment(Pos.CENTER_LEFT);

            // Создание текстовых меток
            timeText = new Text("Время: 0 с");
            altitudeText = new Text("Высота: 0 м");
            velocityText = new Text("Скорость: 0 м/с");
            accelerationText = new Text("Ускорение: 0 м/с²");
            machNumberText = new Text("Число Маха: 0");

            // Добавление меток в контейнер аннотаций
            pane.getChildren().addAll(timeText, altitudeText, velocityText, accelerationText, machNumberText);

            // Привязка позиции аннотации к падающему объекту с смещением
            pane.layoutXProperty().bind(fallingObject.translateXProperty().subtract(60)); // Смещение по X
            pane.layoutYProperty().bind(fallingObject.translateYProperty().add(20)); // Смещение по Y
        }

        /**
         * Обновляет текстовые метки аннотации с новыми значениями.
         *
         * @param time         текущее время
         * @param altitude     текущая высота
         * @param velocity     текущая скорость
         * @param acceleration текущее ускорение
         * @param machNumber   текущее число Маха
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
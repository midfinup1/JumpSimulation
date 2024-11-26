// src/main/java/app/view/ControlsPanel.java

package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Класс ControlsPanel отвечает за создание и управление элементами управления
 * (текстовые поля, кнопки, ползунки) в пользовательском интерфейсе.
 */
public class ControlsPanel {

    private VBox pane;
    private TextField massField;
    private TextField parachuteTimeField;
    private TextField areaField;
    private TextField areaParachuteField;
    private Button startButton;
    private Button resetButton;
    private Button rewindButton;
    private Button forwardButton;
    private Slider timeSlider;

    /**
     * Конструктор класса ControlsPanel.
     * Инициализирует все элементы управления и организует их в макете.
     */
    public ControlsPanel() {
        createControls();
    }

    /**
     * Создаёт и стилизует все элементы управления.
     */
    private void createControls() {
        // Создание текстовых полей
        massField = createStyledTextField("80");
        parachuteTimeField = createStyledTextField("240");
        areaField = createStyledTextField("0.7");
        areaParachuteField = createStyledTextField("25");

        // Создание кнопок
        startButton = createStyledButton("Старт", "button-start");
        resetButton = createStyledButton("Сброс", "button-reset");
        rewindButton = createStyledButton("⏪", "button-rewind");
        forwardButton = createStyledButton("⏩", "button-forward");

        // Создание ползунка времени
        timeSlider = new Slider(0, 1, 0);
        timeSlider.getStyleClass().add("slider");

        // Организация кнопок в горизонтальном контейнере
        HBox buttonBox = new HBox(10, rewindButton, startButton, resetButton, forwardButton);
        buttonBox.setMaxWidth(Double.MAX_VALUE);
        buttonBox.setAlignment(Pos.CENTER);

        // Организация всех элементов в вертикальном контейнере
        pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.getStyleClass().add("form-container");

        pane.getChildren().addAll(
                new Label("Масса тела (кг):"), massField,
                new Label("Время раскрытия парашюта (с):"), parachuteTimeField,
                new Label("Площадь до раскрытия парашюта (м²):"), areaField,
                new Label("Площадь после раскрытия парашюта (м²):"), areaParachuteField,
                buttonBox,
                new Label("Ползунок времени:"), timeSlider
        );
    }

    /**
     * Создаёт стилизованное текстовое поле с начальным текстом.
     *
     * @param initialText начальный текст в поле
     * @return стилизованное TextField
     */
    private TextField createStyledTextField(String initialText) {
        TextField textField = new TextField(initialText);
        textField.getStyleClass().add("text-field");
        return textField;
    }

    /**
     * Создаёт стилизованную кнопку с заданным текстом и классом стиля.
     *
     * @param text        текст на кнопке
     * @param styleClass класс стиля CSS
     * @return стилизованная Button
     */
    private Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    /**
     * Возвращает панель с элементами управления.
     *
     * @return VBox с элементами управления
     */
    public VBox getPane() {
        return pane;
    }

    // Методы доступа к элементам управления для контроллера

    public TextField getMassField() {
        return massField;
    }

    public TextField getParachuteTimeField() {
        return parachuteTimeField;
    }

    public TextField getAreaField() {
        return areaField;
    }

    public TextField getAreaParachuteField() {
        return areaParachuteField;
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getResetButton() {
        return resetButton;
    }

    public Button getRewindButton() {
        return rewindButton;
    }

    public Button getForwardButton() {
        return forwardButton;
    }

    public Slider getTimeSlider() {
        return timeSlider;
    }
}
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
 * Класс {@code ControlsPanel} отвечает за создание и управление элементами управления
 * в пользовательском интерфейсе приложения.
 */
public class ControlsPanel {

    private final VBox pane;
    private final TextField massField;
    private final TextField parachuteAltitudeField;
    private final TextField areaField;
    private final TextField areaParachuteField;
    private final TextField transitionTimeField;
    private final Button startButton;
    private final Button resetButton;
    private final Button rewindButton;
    private final Button forwardButton;
    private final Slider timeSlider;
    private final Label currentTimeLabel;

    /**
     * Конструктор класса {@code ControlsPanel}.
     */
    public ControlsPanel() {
        massField = createStyledTextField("104");
        parachuteAltitudeField = createStyledTextField("1500");
        areaField = createStyledTextField("0.5");
        areaParachuteField = createStyledTextField("25");
        transitionTimeField = createStyledTextField("4.0");

        startButton = createStyledButton("Старт");
        resetButton = createStyledButton("Сброс");
        rewindButton = createStyledButton("⏪");
        forwardButton = createStyledButton("⏩");

        timeSlider = new Slider(0, 1, 0);
        timeSlider.setShowTickLabels(true);
        timeSlider.setShowTickMarks(true);
        timeSlider.setMajorTickUnit(0.25);
        timeSlider.setMinorTickCount(4);
        timeSlider.setBlockIncrement(0.05);
        timeSlider.getStyleClass().add("slider");

        currentTimeLabel = new Label("Текущее время: 0.0 с");
        currentTimeLabel.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(10, rewindButton, startButton, resetButton, forwardButton);
        buttonBox.setAlignment(Pos.CENTER);

        pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.getStyleClass().add("form-container");

        pane.getChildren().addAll(
                new Label("Масса тела (кг):"),
                massField,
                new Label("Высота раскрытия парашюта (м):"),
                parachuteAltitudeField,
                new Label("Площадь до раскрытия парашюта (м²):"),
                areaField,
                new Label("Площадь после раскрытия парашюта (м²):"),
                areaParachuteField,
                new Label("Время раскрытия парашюта (с):"),
                transitionTimeField,
                buttonBox,
                new Label("Ползунок времени:"),
                timeSlider,
                currentTimeLabel
        );
    }

    /**
     * Создает стилизованное текстовое поле с заданным начальным текстом.
     *
     * @param initialText начальный текст
     * @return стилизованное TextField
     */
    private TextField createStyledTextField(String initialText) {
        TextField textField = new TextField(initialText);
        textField.getStyleClass().add("text-field");
        return textField;
    }

    /**
     * Создает стилизованную кнопку с заданным текстом.
     *
     * @param text текст кнопки
     * @return стилизованная Button
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button");
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

    // Методы доступа к элементам управления

    public TextField getMassField() {
        return massField;
    }

    public TextField getParachuteAltitudeField() {
        return parachuteAltitudeField;
    }

    public TextField getAreaField() {
        return areaField;
    }

    public TextField getAreaParachuteField() {
        return areaParachuteField;
    }

    public TextField getTransitionTimeField() {
        return transitionTimeField;
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

    public Label getCurrentTimeLabel() {
        return currentTimeLabel;
    }
}
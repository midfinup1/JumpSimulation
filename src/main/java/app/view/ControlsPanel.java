// src/main/java/app/view/ControlsPanel.java

package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip; // Добавлено для подсказок
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Класс {@code ControlsPanel} отвечает за создание и управление элементами управления
 * (текстовые поля, кнопки, ползунки) в пользовательском интерфейсе приложения.
 * <p>
 * Данный класс предоставляет панель с необходимыми элементами для ввода параметров симуляции,
 * управления процессом симуляции и отображения текущего состояния.
 * </p>
 *
 * <h2>Основные элементы управления:</h2>
 * <ul>
 *     <li><b>Текстовые поля:</b> Для ввода массы тела, высоты раскрытия парашюта,
 *     площади до и после раскрытия парашюта, а также времени раскрытия парашюта.</li>
 *     <li><b>Кнопки:</b> Для старта, сброса симуляции, перемотки назад и вперёд.</li>
 *     <li><b>Ползунок времени:</b> Позволяет прокручивать симуляцию во времени.</li>
 *     <li><b>Метка текущего времени:</b> Отображает текущее время симуляции.</li>
 * </ul>
 */
public class ControlsPanel {

    private VBox pane;
    private TextField massField;
    private TextField parachuteAltitudeField;
    private TextField areaField;
    private TextField areaParachuteField;
    private TextField transitionTimeField;
    private Button startButton;
    private Button resetButton;
    private Button rewindButton;
    private Button forwardButton;
    private Slider timeSlider;
    private Label currentTimeLabel; // Метка для отображения текущего времени симуляции

    /**
     * Конструктор класса {@code ControlsPanel}.
     * <p>
     * Инициализирует все элементы управления и организует их в вертикальном макете.
     * </p>
     */
    public ControlsPanel() {
        createControls();
    }

    /**
     * Создаёт и стилизует все элементы управления, а также организует их
     * в соответствующих контейнерах.
     */
    private void createControls() {
        // Создание и настройка текстовых полей с начальными значениями и подсказками
        massField = createStyledTextField("104");
//        massField.setTooltip(new Tooltip("Введите массу тела в килограммах"));

        parachuteAltitudeField = createStyledTextField("1500");
//        parachuteAltitudeField.setTooltip(new Tooltip("Введите высоту раскрытия парашюта в метрах"));

        areaField = createStyledTextField("0.5");
//        areaField.setTooltip(new Tooltip("Введите площадь тела до раскрытия парашюта в м²"));

        areaParachuteField = createStyledTextField("25");
//        areaParachuteField.setTooltip(new Tooltip("Введите площадь тела после раскрытия парашюта в м²"));

        transitionTimeField = createStyledTextField("2.0"); // Новое поле для времени раскрытия
//        transitionTimeField.setTooltip(new Tooltip("Введите время раскрытия парашюта в секундах"));

        // Создание и стилизация кнопок управления симуляцией
        startButton = createStyledButton("Старт", "button-start");
//        startButton.setTooltip(new Tooltip(""));
        resetButton = createStyledButton("Сброс", "button-reset");
        rewindButton = createStyledButton("⏪", "button-rewind");
        forwardButton = createStyledButton("⏩", "button-forward");

        // Настройка ползунка времени
        timeSlider = new Slider(0, 1, 0); // Диапазон от 0 до 1
        timeSlider.setShowTickLabels(true);
        timeSlider.setShowTickMarks(true);
        timeSlider.setMajorTickUnit(0.25);
        timeSlider.setMinorTickCount(4);
        timeSlider.setBlockIncrement(0.05);
        timeSlider.getStyleClass().add("slider");

        // Создание метки для отображения текущего времени симуляции
        currentTimeLabel = new Label("Текущее время: 0.0 с");
        currentTimeLabel.setAlignment(Pos.CENTER);

        // Организация кнопок в горизонтальном контейнере с отступами и выравниванием
        HBox buttonBox = new HBox(10, rewindButton, startButton, resetButton, forwardButton);
        buttonBox.setMaxWidth(Double.MAX_VALUE);
        buttonBox.setAlignment(Pos.CENTER);

        // Организация всех элементов в вертикальном контейнере с отступами
        pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.getStyleClass().add("form-container");

        // Добавление всех элементов на панель
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
                currentTimeLabel // Добавляем метку времени
        );
    }

    /**
     * Создаёт стилизованное текстовое поле с заданным начальным текстом.
     *
     * @param initialText начальный текст, отображаемый в поле
     * @return экземпляр {@code TextField} с применёнными стилями
     */
    private TextField createStyledTextField(String initialText) {
        TextField textField = new TextField(initialText);
        textField.getStyleClass().add("text-field");
        return textField;
    }

    /**
     * Создаёт стилизованную кнопку с указанным текстом и классом стиля.
     *
     * @param text       текст, отображаемый на кнопке
     * @param styleClass CSS-класс для стилизации кнопки
     * @return экземпляр {@code Button} с применёнными стилями
     */
    private Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    /**
     * Возвращает основную панель с элементами управления.
     *
     * @return {@code VBox}, содержащий все элементы управления
     */
    public VBox getPane() {
        return pane;
    }

    // Методы доступа к элементам управления для взаимодействия с контроллером

    /**
     * Возвращает текстовое поле для ввода массы тела.
     *
     * @return {@code TextField} для массы тела
     */
    public TextField getMassField() {
        return massField;
    }

    /**
     * Возвращает текстовое поле для ввода высоты раскрытия парашюта.
     *
     * @return {@code TextField} для высоты раскрытия парашюта
     */
    public TextField getParachuteAltitudeField() {
        return parachuteAltitudeField;
    }

    /**
     * Возвращает текстовое поле для ввода площади до раскрытия парашюта.
     *
     * @return {@code TextField} для площади до раскрытия парашюта
     */
    public TextField getAreaField() {
        return areaField;
    }

    /**
     * Возвращает текстовое поле для ввода площади после раскрытия парашюта.
     *
     * @return {@code TextField} для площади после раскрытия парашюта
     */
    public TextField getAreaParachuteField() {
        return areaParachuteField;
    }

    /**
     * Возвращает текстовое поле для ввода времени раскрытия парашюта.
     *
     * @return {@code TextField} для времени раскрытия парашюта
     */
    public TextField getTransitionTimeField() {
        return transitionTimeField;
    }

    /**
     * Возвращает кнопку старта симуляции.
     *
     * @return {@code Button} для старта симуляции
     */
    public Button getStartButton() {
        return startButton;
    }

    /**
     * Возвращает кнопку сброса симуляции.
     *
     * @return {@code Button} для сброса симуляции
     */
    public Button getResetButton() {
        return resetButton;
    }

    /**
     * Возвращает кнопку перемотки назад.
     *
     * @return {@code Button} для перемотки назад
     */
    public Button getRewindButton() {
        return rewindButton;
    }

    /**
     * Возвращает кнопку перемотки вперёд.
     *
     * @return {@code Button} для перемотки вперёд
     */
    public Button getForwardButton() {
        return forwardButton;
    }

    /**
     * Возвращает ползунок времени симуляции.
     *
     * @return {@code Slider} для управления временем симуляции
     */
    public Slider getTimeSlider() {
        return timeSlider;
    }

    /**
     * Возвращает метку, отображающую текущее время симуляции.
     *
     * @return {@code Label} с текущим временем симуляции
     */
    public Label getCurrentTimeLabel() {
        return currentTimeLabel;
    }
}
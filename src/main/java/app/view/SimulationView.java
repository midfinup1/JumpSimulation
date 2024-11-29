package app.view;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;

/**
 * Класс {@code SimulationView} отвечает за создание и организацию основных компонентов пользовательского интерфейса.
 */
public class SimulationView {

    private final BorderPane root;
    private final ControlsPanel controlsPanel;
    private final ChartsPanel chartsPanel;
    private final AnimationPane animationPane;

    /**
     * Конструктор класса {@code SimulationView}.
     * Инициализирует все подкомпоненты и строит основной макет.
     */
    public SimulationView() {
        controlsPanel = new ControlsPanel();
        chartsPanel = new ChartsPanel();
        animationPane = new AnimationPane();

        root = new BorderPane();
        root.setLeft(animationPane.getPane());
        root.setCenter(chartsPanel.getPane());
        root.setRight(controlsPanel.getPane());
        root.setPadding(new Insets(10));
    }

    /**
     * Возвращает основной корневой макет.
     *
     * @return корневой BorderPane
     */
    public BorderPane getView() {
        return root;
    }

    public ControlsPanel getControlsPanel() {
        return controlsPanel;
    }

    public ChartsPanel getChartsPanel() {
        return chartsPanel;
    }

    public AnimationPane getAnimationPane() {
        return animationPane;
    }
}
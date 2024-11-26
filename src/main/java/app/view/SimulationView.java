// src/main/java/app/view/SimulationView.java

package app.view;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;

/**
 * Класс SimulationView отвечает за создание и организацию основных компонентов пользовательского интерфейса.
 * Он объединяет панели управления, графики и анимационную панель в одном макете.
 */
public class SimulationView {

    private BorderPane root;
    private ControlsPanel controlsPanel;
    private ChartsPanel chartsPanel;
    private AnimationPane animationPane;

    /**
     * Конструктор класса SimulationView.
     * Инициализирует все подкомпоненты и строит основной макет.
     */
    public SimulationView() {
        createView();
    }

    /**
     * Создаёт основной макет, объединяя все подкомпоненты.
     */
    private void createView() {
        // Инициализация подкомпонентов
        controlsPanel = new ControlsPanel();
        chartsPanel = new ChartsPanel();
        animationPane = new AnimationPane();

        // Создание основного макета
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

    // Методы доступа к подкомпонентам для взаимодействия с контроллером

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
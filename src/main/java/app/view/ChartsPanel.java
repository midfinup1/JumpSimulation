// src/main/java/app/view/ChartsPanel.java

package app.view;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Класс ChartsPanel отвечает за создание и управление графиками в пользовательском интерфейсе.
 * Он включает четыре LineChart для отображения высоты, скорости, ускорения и числа Маха.
 */
public class ChartsPanel {

    private GridPane pane;
    private LineChart<Number, Number> altitudeChart;
    private LineChart<Number, Number> velocityChart;
    private LineChart<Number, Number> accelerationChart;
    private LineChart<Number, Number> machNumberChart;

    /**
     * Конструктор класса ChartsPanel.
     * Инициализирует все графики и организует их в сетке.
     */
    public ChartsPanel() {
        createCharts();
    }

    /**
     * Создаёт и стилизует все графики.
     */
    private void createCharts() {
        // Создание графиков
        altitudeChart = createChart("Высота (м)");
        velocityChart = createChart("Скорость (м/с)");
        accelerationChart = createChart("Ускорение (м/с²)");
        machNumberChart = createChart("Число Маха");

        // Организация графиков в GridPane
        pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);

        pane.add(altitudeChart, 0, 0);
        pane.add(velocityChart, 1, 0);
        pane.add(accelerationChart, 0, 1);
        pane.add(machNumberChart, 1, 1);

        // Установка расширяемости столбцов и строк
        GridPane.setHgrow(altitudeChart, Priority.ALWAYS);
        GridPane.setVgrow(altitudeChart, Priority.ALWAYS);
        GridPane.setHgrow(velocityChart, Priority.ALWAYS);
        GridPane.setVgrow(velocityChart, Priority.ALWAYS);
        GridPane.setHgrow(accelerationChart, Priority.ALWAYS);
        GridPane.setVgrow(accelerationChart, Priority.ALWAYS);
        GridPane.setHgrow(machNumberChart, Priority.ALWAYS);
        GridPane.setVgrow(machNumberChart, Priority.ALWAYS);
    }

    /**
     * Создаёт стилизованный график LineChart с заданным заголовком.
     *
     * @param title заголовок графика
     * @return стилизованный LineChart
     */
    private LineChart<Number, Number> createChart(String title) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Время (с)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(title);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.getStyleClass().add("line-chart");
        chart.getStyleClass().add("styled-chart"); // Класс для стилизации через CSS
        chart.setCreateSymbols(false); // Маркеры отключены по умолчанию
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        return chart;
    }

    /**
     * Возвращает панель с графиками.
     *
     * @return GridPane с графиками
     */
    public GridPane getPane() {
        return pane;
    }

    // Методы доступа к графикам для добавления данных

    public LineChart<Number, Number> getAltitudeChart() {
        return altitudeChart;
    }

    public LineChart<Number, Number> getVelocityChart() {
        return velocityChart;
    }

    public LineChart<Number, Number> getAccelerationChart() {
        return accelerationChart;
    }

    public LineChart<Number, Number> getMachNumberChart() {
        return machNumberChart;
    }

    /**
     * Добавляет данные в указанный график.
     *
     * @param chart график для добавления данных
     * @param x     значение по оси X (время)
     * @param y     значение по оси Y (соответствующий параметр)
     */
    public void addDataToChart(LineChart<Number, Number> chart, double x, double y) {
        XYChart.Series<Number, Number> series;
        if (chart.getData().isEmpty()) {
            series = new XYChart.Series<>();
            chart.getData().add(series);
        } else {
            series = chart.getData().get(0);
        }
        series.getData().add(new XYChart.Data<>(x, y));
    }
}
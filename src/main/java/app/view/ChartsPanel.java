package app.view;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;

/**
 * Класс ChartsPanel отвечает за создание и управление графиками в пользовательском интерфейсе.
 * Он включает четыре LineChart для отображения высоты, скорости, ускорения и числа Маха.
 * Добавлены возможности масштабирования и панорамирования графиков.
 */
public class ChartsPanel {

    private GridPane pane;

    private LineChart<Number, Number> altitudeChart;
    private LineChart<Number, Number> velocityChart;
    private LineChart<Number, Number> accelerationChart;
    private LineChart<Number, Number> machNumberChart;

    // Серии данных для полного графика
    private XYChart.Series<Number, Number> altitudeSeriesFull;
    private XYChart.Series<Number, Number> velocitySeriesFull;
    private XYChart.Series<Number, Number> accelerationSeriesFull;
    private XYChart.Series<Number, Number> machNumberSeriesFull;

    // Точка текущего состояния
    private XYChart.Series<Number, Number> altitudeCurrentPoint;
    private XYChart.Series<Number, Number> velocityCurrentPoint;
    private XYChart.Series<Number, Number> accelerationCurrentPoint;
    private XYChart.Series<Number, Number> machNumberCurrentPoint;

    public ChartsPanel() {
        createCharts();
    }

    private void createCharts() {
        // Создание графиков и серий
        altitudeChart = createChart("Высота (м)");
        altitudeSeriesFull = new XYChart.Series<>();
        altitudeCurrentPoint = new XYChart.Series<>();
        altitudeChart.getData().addAll(altitudeSeriesFull, altitudeCurrentPoint);

        velocityChart = createChart("Скорость (м/с)");
        velocitySeriesFull = new XYChart.Series<>();
        velocityCurrentPoint = new XYChart.Series<>();
        velocityChart.getData().addAll(velocitySeriesFull, velocityCurrentPoint);

        accelerationChart = createChart("Ускорение (м/с²)");
        accelerationSeriesFull = new XYChart.Series<>();
        accelerationCurrentPoint = new XYChart.Series<>();
        accelerationChart.getData().addAll(accelerationSeriesFull, accelerationCurrentPoint);

        machNumberChart = createChart("Число Маха");
        machNumberSeriesFull = new XYChart.Series<>();
        machNumberCurrentPoint = new XYChart.Series<>();
        machNumberChart.getData().addAll(machNumberSeriesFull, machNumberCurrentPoint);

        // Добавление функций масштабирования и панорамирования к каждому графику
        addZoomAndPan(altitudeChart);
        addZoomAndPan(velocityChart);
        addZoomAndPan(accelerationChart);
        addZoomAndPan(machNumberChart);

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

    private LineChart<Number, Number> createChart(String title) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Время (с)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(title);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setCreateSymbols(false); // Отключаем символы по умолчанию
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        return chart;
    }

    public GridPane getPane() {
        return pane;
    }

    // Методы доступа к графикам и сериям для добавления данных

    public LineChart<Number, Number> getAltitudeChart() {
        return altitudeChart;
    }

    public XYChart.Series<Number, Number> getAltitudeSeriesFull() {
        return altitudeSeriesFull;
    }

    public XYChart.Series<Number, Number> getAltitudeCurrentPoint() {
        return altitudeCurrentPoint;
    }

    public LineChart<Number, Number> getVelocityChart() {
        return velocityChart;
    }

    public XYChart.Series<Number, Number> getVelocitySeriesFull() {
        return velocitySeriesFull;
    }

    public XYChart.Series<Number, Number> getVelocityCurrentPoint() {
        return velocityCurrentPoint;
    }

    public LineChart<Number, Number> getAccelerationChart() {
        return accelerationChart;
    }

    public XYChart.Series<Number, Number> getAccelerationSeriesFull() {
        return accelerationSeriesFull;
    }

    public XYChart.Series<Number, Number> getAccelerationCurrentPoint() {
        return accelerationCurrentPoint;
    }

    public LineChart<Number, Number> getMachNumberChart() {
        return machNumberChart;
    }

    public XYChart.Series<Number, Number> getMachNumberSeriesFull() {
        return machNumberSeriesFull;
    }

    public XYChart.Series<Number, Number> getMachNumberCurrentPoint() {
        return machNumberCurrentPoint;
    }

    /**
     * Добавляет возможность масштабирования и панорамирования к LineChart.
     *
     * @param chart график, к которому добавляются функции зума и панорамирования
     */
    private void addZoomAndPan(LineChart<Number, Number> chart) {
        final double[] mouseAnchor = new double[2];
        final double[] axisScale = new double[4]; // [xLower, xUpper, yLower, yUpper]

        // Масштабирование через прокрутку колесика мыши
        chart.setOnScroll((ScrollEvent event) -> {
            event.consume();
            if (event.getDeltaY() == 0) {
                return;
            }

            double zoomFactor = (event.getDeltaY() > 0) ? 0.9 : 1.1;

            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();

            double xRange = xAxis.getUpperBound() - xAxis.getLowerBound();
            double yRange = yAxis.getUpperBound() - yAxis.getLowerBound();

            // Центр зума по координатам курсора
            double xZoom = xRange * zoomFactor;
            double yZoom = yRange * zoomFactor;

            double xCenter = xAxis.getValueForDisplay(event.getX()).doubleValue();
            double yCenter = yAxis.getValueForDisplay(event.getY()).doubleValue();

            xAxis.setLowerBound(xCenter - (xCenter - xAxis.getLowerBound()) * zoomFactor);
            xAxis.setUpperBound(xCenter + (xAxis.getUpperBound() - xCenter) * zoomFactor);

            yAxis.setLowerBound(yCenter - (yCenter - yAxis.getLowerBound()) * zoomFactor);
            yAxis.setUpperBound(yCenter + (yAxis.getUpperBound() - yCenter) * zoomFactor);
        });

        // Панорамирование через перетаскивание мыши
        chart.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY)
                return;
            mouseAnchor[0] = event.getX();
            mouseAnchor[1] = event.getY();

            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();

            axisScale[0] = xAxis.getLowerBound();
            axisScale[1] = xAxis.getUpperBound();
            axisScale[2] = yAxis.getLowerBound();
            axisScale[3] = yAxis.getUpperBound();
        });

        chart.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.PRIMARY)
                return;

            double deltaX = event.getX() - mouseAnchor[0];
            double deltaY = event.getY() - mouseAnchor[1];

            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();

            double xScale = (xAxis.getUpperBound() - xAxis.getLowerBound()) / chart.getWidth();
            double yScale = (yAxis.getUpperBound() - yAxis.getLowerBound()) / chart.getHeight();

            xAxis.setLowerBound(axisScale[0] - deltaX * xScale);
            xAxis.setUpperBound(axisScale[1] - deltaX * xScale);

            yAxis.setLowerBound(axisScale[2] + deltaY * yScale);
            yAxis.setUpperBound(axisScale[3] + deltaY * yScale);
        });
    }
}
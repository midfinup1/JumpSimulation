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
 * Класс {@code ChartsPanel} отвечает за создание и управление графиками в пользовательском интерфейсе.
 */
public class ChartsPanel {

    private final GridPane pane;

    // Серии данных для полного графика
    private final XYChart.Series<Number, Number> altitudeSeriesFull;
    private final XYChart.Series<Number, Number> velocitySeriesFull;
    private final XYChart.Series<Number, Number> accelerationSeriesFull;
    private final XYChart.Series<Number, Number> machNumberSeriesFull;

    // Точка текущего состояния
    private final XYChart.Series<Number, Number> altitudeCurrentPoint;
    private final XYChart.Series<Number, Number> velocityCurrentPoint;
    private final XYChart.Series<Number, Number> accelerationCurrentPoint;
    private final XYChart.Series<Number, Number> machNumberCurrentPoint;

    /**
     * Конструктор класса {@code ChartsPanel}.
     * Создает графики и настраивает их.
     */
    public ChartsPanel() {
        LineChart<Number, Number> altitudeChart = createChart("Высота (м)");
        altitudeSeriesFull = new XYChart.Series<>();
        altitudeCurrentPoint = new XYChart.Series<>();
        altitudeChart.getData().addAll(altitudeSeriesFull, altitudeCurrentPoint);

        LineChart<Number, Number> velocityChart = createChart("Скорость (м/с)");
        velocitySeriesFull = new XYChart.Series<>();
        velocityCurrentPoint = new XYChart.Series<>();
        velocityChart.getData().addAll(velocitySeriesFull, velocityCurrentPoint);

        LineChart<Number, Number> accelerationChart = createChart("Ускорение (м/с²)");
        accelerationSeriesFull = new XYChart.Series<>();
        accelerationCurrentPoint = new XYChart.Series<>();
        accelerationChart.getData().addAll(accelerationSeriesFull, accelerationCurrentPoint);

        LineChart<Number, Number> machNumberChart = createChart("Число Маха");
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

    /**
     * Создает LineChart с заданным названием оси Y.
     *
     * @param yAxisLabel метка для оси Y
     * @return настроенный LineChart
     */
    private LineChart<Number, Number> createChart(String yAxisLabel) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Время (с)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.getStyleClass().add("line-chart");

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

    // Методы доступа к графикам и сериям для добавления данных

    public XYChart.Series<Number, Number> getAltitudeSeriesFull() {
        return altitudeSeriesFull;
    }

    public XYChart.Series<Number, Number> getAltitudeCurrentPoint() {
        return altitudeCurrentPoint;
    }

    public XYChart.Series<Number, Number> getVelocitySeriesFull() {
        return velocitySeriesFull;
    }

    public XYChart.Series<Number, Number> getVelocityCurrentPoint() {
        return velocityCurrentPoint;
    }

    public XYChart.Series<Number, Number> getAccelerationSeriesFull() {
        return accelerationSeriesFull;
    }

    public XYChart.Series<Number, Number> getAccelerationCurrentPoint() {
        return accelerationCurrentPoint;
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
        final double[] axisStart = new double[4]; // [xLower, xUpper, yLower, yUpper]

        // Масштабирование через прокрутку колесика мыши
        chart.setOnScroll((ScrollEvent event) -> {
            event.consume();
            if (event.getDeltaY() == 0) return;

            double zoomFactor = (event.getDeltaY() > 0) ? 0.9 : 1.1;

            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();

            double xRange = xAxis.getUpperBound() - xAxis.getLowerBound();
            double yRange = yAxis.getUpperBound() - yAxis.getLowerBound();

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
            if (event.getButton() != MouseButton.PRIMARY) return;

            mouseAnchor[0] = event.getX();
            mouseAnchor[1] = event.getY();

            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();

            axisStart[0] = xAxis.getLowerBound();
            axisStart[1] = xAxis.getUpperBound();
            axisStart[2] = yAxis.getLowerBound();
            axisStart[3] = yAxis.getUpperBound();
        });

        chart.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.PRIMARY) return;

            double deltaX = event.getX() - mouseAnchor[0];
            double deltaY = event.getY() - mouseAnchor[1];

            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            NumberAxis yAxis = (NumberAxis) chart.getYAxis();

            double xScale = (xAxis.getUpperBound() - xAxis.getLowerBound()) / chart.getWidth();
            double yScale = (yAxis.getUpperBound() - yAxis.getLowerBound()) / chart.getHeight();

            xAxis.setLowerBound(axisStart[0] - deltaX * xScale);
            xAxis.setUpperBound(axisStart[1] - deltaX * xScale);

            yAxis.setLowerBound(axisStart[2] + deltaY * yScale);
            yAxis.setUpperBound(axisStart[3] + deltaY * yScale);
        });
    }
}
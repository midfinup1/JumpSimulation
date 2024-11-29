// src/main/java/app/model/Simulation.java

package app.model;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Класс Simulation отвечает за выполнение симуляции падения объекта с парашютом.
 */
public class Simulation {

    /**
     * Класс EquationsOfMotion описывает систему дифференциальных уравнений для симуляции.
     * Система включает высоту, скорость и прогресс раскрытия парашюта.
     */
    public static class EquationsOfMotion implements FirstOrderDifferentialEquations {

        private final double mass;
        private final double area;
        private final double areaParachute;
        private final double parachuteAltitude;
        private final double transitionTime; // Время для плавного раскрытия парашюта

        public EquationsOfMotion(double mass, double area, double areaParachute, double parachuteAltitude, double transitionTime) {
            this.mass = mass;
            this.area = area;
            this.areaParachute = areaParachute;
            this.parachuteAltitude = parachuteAltitude;
            this.transitionTime = transitionTime;
        }

        @Override
        public int getDimension() {
            return 3; // [altitude, velocity, deploymentProgress]
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            double altitude = y[0];
            double velocity = y[1];
            double deploymentProgress = y[2];

            double gravity = Physics.calculateGravity(altitude);
            Atmosphere.AtmosphereProperties atmosphere = Atmosphere.getAtmosphericProperties(altitude);

            double density = atmosphere.density;
            double soundSpeed = atmosphere.soundSpeed;
            double machNumber = soundSpeed > 0 ? abs(velocity) / soundSpeed : 0;
            double dragCoefficient = Physics.calculateDragCoefficient(machNumber);

            // Проверка условия раскрытия парашюта
            if (altitude <= parachuteAltitude && deploymentProgress < 1.0) {
                yDot[2] = 1.0 / transitionTime; // Скорость увеличения прогресса раскрытия
            } else {
                yDot[2] = 0.0;
            }

            // Расчет эффективной площади
            double effectiveArea = area + (areaParachute - area) * deploymentProgress;

            // Расчет силы сопротивления
            double dragForce = 0.5 * dragCoefficient * density * effectiveArea * velocity * abs(velocity);

            // Уравнения движения
            yDot[0] = velocity;
            yDot[1] = -gravity - (dragForce / mass);

            // Проверка на переполнение прогресса
            if (deploymentProgress >= 1.0 && altitude > parachuteAltitude) {
                yDot[2] = 0.0; // Остановить увеличение прогресса, если выше пороговой высоты
            }

            if (Double.isNaN(yDot[0]) || Double.isNaN(yDot[1]) || Double.isNaN(yDot[2])) {
                throw new RuntimeException(String.format(
                        "Неверный расчет производных: altitude=%.2f, velocity=%.2f, deploymentProgress=%.2f, gravity=%.2f, dragForce=%.2f",
                        altitude, velocity, deploymentProgress, gravity, dragForce));
            }
        }
    }

    /**
     * Класс SimulationResult хранит результаты симуляции.
     */
    public static class SimulationResult {
        public final List<Double> time = new ArrayList<>();
        public final List<Double> altitude = new ArrayList<>();
        public final List<Double> velocity = new ArrayList<>();
        public final List<Double> acceleration = new ArrayList<>();
        public final List<Double> machNumber = new ArrayList<>();
        public final List<Double> dragCoefficient = new ArrayList<>();

        public double timeStep; // Средний шаг времени
    }

    /**
     * Метод simulateJump выполняет симуляцию падения.
     *
     * @param mass              масса объекта (кг)
     * @param initialHeight     начальная высота (м)
     * @param area              площадь поперечного сечения до раскрытия парашюта (м²)
     * @param areaParachute     площадь поперечного сечения после раскрытия парашюта (м²)
     * @param parachuteAltitude высота, при достижении которой парашют должен раскрыться (м)
     * @param transitionTime    время раскрытия парашюта (с)
     * @return результаты симуляции
     */
    public static SimulationResult simulateJump(double mass, double initialHeight, double area, double areaParachute, double parachuteAltitude, double transitionTime) {
        EquationsOfMotion equations = new EquationsOfMotion(mass, area, areaParachute, parachuteAltitude, transitionTime);

        double[] y = new double[]{initialHeight, 0, 0}; // [altitude, velocity, deploymentProgress]

        double t0 = 0.0;
        double tMax = 1000.0;

        double minStep = 1.0e-8;
        double maxStep = 1.0;
        double absTolerance = 1.0e-8;
        double relTolerance = 1.0e-8;

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(minStep, maxStep, absTolerance, relTolerance);

        SimulationResult result = new SimulationResult();

        // Желаемый шаг вывода данных
        final double outputStep = 0.1; // Измените это значение для большего или меньшего количества точек данных

        integrator.addStepHandler(new StepHandler() {
            private double lastOutputTime = t0;

            @Override
            public void init(double t0, double[] y0, double t) {
                lastOutputTime = t0;
                // Записываем начальное состояние
                recordData(t0, y0);
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                double tStart = interpolator.getPreviousTime();
                double tEnd = interpolator.getCurrentTime();
                boolean forward = tEnd > tStart;

                // Устанавливаем направление времени
                double step = forward ? outputStep : -outputStep;

                double nextOutputTime = lastOutputTime + step;

                // Интерполируем и записываем данные на фиксированных интервалах в пределах текущего шага
                while ((forward && nextOutputTime <= tEnd) || (!forward && nextOutputTime >= tEnd)) {
                    interpolator.setInterpolatedTime(nextOutputTime);
                    double[] yInterpolated = interpolator.getInterpolatedState();
                    recordData(nextOutputTime, yInterpolated);
                    lastOutputTime = nextOutputTime;
                    nextOutputTime += step;
                }

                if (isLast) {
                    // Записываем конечное состояние
                    interpolator.setInterpolatedTime(tEnd);
                    double[] yEnd = interpolator.getInterpolatedState();
                    recordData(tEnd, yEnd);
                }
            }

            private void recordData(double t, double[] y) {
                if (Double.isNaN(y[0]) || Double.isNaN(y[1]) || Double.isNaN(y[2])) {
                    throw new RuntimeException("Неверное состояние симуляции: высота, скорость или прогресс раскрытия равны NaN");
                }

                result.time.add(t);
                result.altitude.add(abs(y[0]));
                result.velocity.add(y[1]);

                Atmosphere.AtmosphereProperties atmosphere = Atmosphere.getAtmosphericProperties(y[0]);
                double soundSpeed = atmosphere.soundSpeed;
                double machNumber = soundSpeed > 0 ? abs(y[1]) / soundSpeed : 0;
                double dragCoefficient = Physics.calculateDragCoefficient(machNumber);

                // Расчет ускорения
                double gravity = Physics.calculateGravity(y[0]);
                double effectiveArea = area + (areaParachute - area) * y[2];
                double density = atmosphere.density;
                double dragForce = 0.5 * dragCoefficient * density * effectiveArea * y[1] * abs(y[1]);
                double acceleration = -gravity - (dragForce / mass);

                result.acceleration.add(acceleration);
                result.machNumber.add(machNumber);
                result.dragCoefficient.add(dragCoefficient);
            }
        });

        // Обработчик события достижения земли
        integrator.addEventHandler(new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
                // Инициализация при начале интегрирования
            }

            @Override
            public double g(double t, double[] y) {
                if (Double.isNaN(y[0])) {
                    throw new RuntimeException("Высота равна NaN при обработке события");
                }
                return y[0]; // Высота
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP; // Остановить интегрирование при достижении земли
            }

            @Override
            public void resetState(double t, double[] y) {
                // Не требуется изменение состояния
            }
        }, 1.0e-3, 1.0e-8, 1000);

        try {
            integrator.integrate(equations, t0, y, tMax, y);
        } catch (RuntimeException e) {
            System.err.println("Ошибка во время симуляции: " + e.getMessage());
        }

        // Вычисляем средний шаг времени
        if (result.time.size() > 1) {
            double totalTime = result.time.get(result.time.size() - 1) - result.time.get(0);
            result.timeStep = totalTime / (result.time.size() - 1);
        } else {
            result.timeStep = outputStep; // Используем шаг вывода данных
        }

        return result;
    }
}
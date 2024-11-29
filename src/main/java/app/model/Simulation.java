package app.model;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс {@code Simulation} отвечает за выполнение симуляции падения объекта с парашютом.
 */
public class Simulation {

    /**
         * Класс {@code EquationsOfMotion} описывает систему дифференциальных уравнений для симуляции.
         */
        private record EquationsOfMotion(double mass, double area, double areaParachute, double parachuteAltitude,
                                         double transitionTime) implements FirstOrderDifferentialEquations {

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
                double machNumber = soundSpeed > 0 ? Math.abs(velocity) / soundSpeed : 0;
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
                double dragForce = 0.5 * dragCoefficient * density * effectiveArea * velocity * Math.abs(velocity);

                // Уравнения движения
                yDot[0] = velocity;
                yDot[1] = -gravity - (dragForce / mass);
            }
        }

    /**
     * Класс {@code SimulationResult} хранит результаты симуляции.
     */
    public static class SimulationResult {
        public final List<Double> time = new ArrayList<>();
        public final List<Double> altitude = new ArrayList<>();
        public final List<Double> velocity = new ArrayList<>();
        public final List<Double> acceleration = new ArrayList<>();
        public final List<Double> machNumber = new ArrayList<>();
        public final List<Double> dragCoefficient = new ArrayList<>();
        public final List<Double> deploymentProgress = new ArrayList<>();

        public double timeStep; // Средний шаг времени
    }

    /**
     * Выполняет симуляцию прыжка с парашютом.
     *
     * @param mass              масса объекта (кг)
     * @param initialHeight     начальная высота (м)
     * @param area              площадь до раскрытия парашюта (м²)
     * @param areaParachute     площадь после раскрытия парашюта (м²)
     * @param parachuteAltitude высота раскрытия парашюта (м)
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
        final double outputStep = 0.1;

        integrator.addStepHandler(new StepHandler() {
            private double lastOutputTime = t0;

            @Override
            public void init(double t0, double[] y0, double t) {
                lastOutputTime = t0;
                recordData(t0, y0);
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                double tStart = interpolator.getPreviousTime();
                double tEnd = interpolator.getCurrentTime();
                boolean forward = tEnd > tStart;

                double step = forward ? outputStep : -outputStep;
                double nextOutputTime = lastOutputTime + step;

                while ((forward && nextOutputTime <= tEnd) || (!forward && nextOutputTime >= tEnd)) {
                    interpolator.setInterpolatedTime(nextOutputTime);
                    double[] yInterpolated = interpolator.getInterpolatedState();
                    recordData(nextOutputTime, yInterpolated);
                    lastOutputTime = nextOutputTime;
                    nextOutputTime += step;
                }

                if (isLast) {
                    interpolator.setInterpolatedTime(tEnd);
                    double[] yEnd = interpolator.getInterpolatedState();
                    recordData(tEnd, yEnd);
                }
            }

            private void recordData(double t, double[] y) {
                result.time.add(t);
                result.altitude.add(Math.abs(y[0]));
                result.velocity.add(y[1]);
                result.deploymentProgress.add(y[2]);

                Atmosphere.AtmosphereProperties atmosphere = Atmosphere.getAtmosphericProperties(y[0]);
                double soundSpeed = atmosphere.soundSpeed;
                double machNumber = soundSpeed > 0 ? Math.abs(y[1]) / soundSpeed : 0;
                double dragCoefficient = Physics.calculateDragCoefficient(machNumber);

                // Расчет ускорения
                double gravity = Physics.calculateGravity(y[0]);
                double effectiveArea = area + (areaParachute - area) * y[2];
                double density = atmosphere.density;
                double dragForce = 0.5 * dragCoefficient * density * effectiveArea * y[1] * Math.abs(y[1]);
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
            double totalTime = result.time.getLast() - result.time.getFirst();
            result.timeStep = totalTime / (result.time.size() - 1);
        } else {
            result.timeStep = outputStep;
        }

        return result;
    }
}
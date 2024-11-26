package app.model;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import java.util.ArrayList;
import java.util.List;

public class Simulation {

    public static class EquationsOfMotion implements FirstOrderDifferentialEquations {

        private final double mass;
        private final double area;
        private final double areaParachute;
        private final double parachuteTime;

        public EquationsOfMotion(double mass, double area, double areaParachute, double parachuteTime) {
            this.mass = mass;
            this.area = area;
            this.areaParachute = areaParachute;
            this.parachuteTime = parachuteTime;
        }

        @Override
        public int getDimension() {
            return 2; // [altitude, velocity]
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            double altitude = y[0];
            double velocity = y[1];

            System.out.printf("Computing derivatives at t=%.2f, altitude=%.2f, velocity=%.2f%n", t, altitude, velocity);

            if (altitude < 0) altitude = 0;

            double gravity = Physics.calculateGravity(altitude);
            Atmosphere.AtmosphereProperties atmosphere = Atmosphere.getAtmosphericProperties(altitude);

            double density = atmosphere.density;
            double soundSpeed = atmosphere.soundSpeed;
            double machNumber = soundSpeed > 0 ? Math.abs(velocity) / soundSpeed : 0;
            double dragCoefficient = Physics.calculateDragCoefficient(machNumber);

            // Плавное увеличение площади при раскрытии парашюта
            double effectiveArea = area;
            if (t >= parachuteTime) {
                double transitionTime = 2.0; // Время раскрытия парашюта
                double deltaArea = areaParachute - area;
                effectiveArea = area + deltaArea * Math.min((t - parachuteTime) / transitionTime, 1.0);
            }

            // Расчет силы сопротивления
            double dragForce = 0.5 * dragCoefficient * density * effectiveArea * velocity * Math.abs(velocity);

//            // Ограничение на максимальную силу сопротивления
//            double maxDragForce = 50_000; // Примерное ограничение
//            dragForce = Math.min(dragForce, maxDragForce);

            // Уравнения движения
            yDot[0] = velocity;
            yDot[1] = -gravity - (dragForce / mass);

//            // Ограничение на ускорение
//            double maxAcceleration = 50.0; // Примерное ограничение
//            if (Math.abs(yDot[1]) > maxAcceleration) {
//                yDot[1] = Math.signum(yDot[1]) * maxAcceleration;
//            }

            System.out.printf("gravity=%.2f, density=%.2f, soundSpeed=%.2f, machNumber=%.2f, dragForce=%.2f%n", gravity, density, soundSpeed, machNumber, dragForce);

            if (Double.isNaN(yDot[0]) || Double.isNaN(yDot[1])) {
                throw new RuntimeException(String.format("Invalid derivative calculation: altitude=%.2f, velocity=%.2f, gravity=%.2f, dragForce=%.2f", altitude, velocity, gravity, dragForce));
            }
        }
    }

    public static class SimulationResult {
        public final List<Double> time = new ArrayList<>();
        public final List<Double> altitude = new ArrayList<>();
        public final List<Double> velocity = new ArrayList<>();
        public final List<Double> acceleration = new ArrayList<>();
        public final List<Double> machNumber = new ArrayList<>();
        public final List<Double> dragCoefficient = new ArrayList<>();
    }

    public static SimulationResult simulateJump(double mass, double initialHeight, double area, double areaParachute, double parachuteTime) {
        EquationsOfMotion equations = new EquationsOfMotion(mass, area, areaParachute, parachuteTime);

        double[] y = new double[]{initialHeight, 0}; // [altitude, velocity]

        double t0 = 0.0;
        double tMax = 1000.0;

        double minStep = 1.0e-6;
        double maxStep = 1.0;
        double absTolerance = 1.0e-6;
        double relTolerance = 1.0e-6;

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(minStep, maxStep, absTolerance, relTolerance);

        SimulationResult result = new SimulationResult();

        integrator.addStepHandler(new StepHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
                // Инициализация при начале интегрирования
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                double t = interpolator.getCurrentTime();
                double[] y = interpolator.getInterpolatedState();

                if (Double.isNaN(y[0]) || Double.isNaN(y[1])) {
                    throw new RuntimeException("Invalid simulation state: altitude or velocity is NaN");
                }

                result.time.add(t);
                result.altitude.add(y[0]);
                result.velocity.add(y[1]);

                double gravity = Physics.calculateGravity(y[0]);

                Atmosphere.AtmosphereProperties atmosphere = Atmosphere.getAtmosphericProperties(y[0]);
                double density = atmosphere.density;
                double soundSpeed = atmosphere.soundSpeed;

                double machNumber = Math.abs(y[1]) / soundSpeed;
                double dragCoefficient = Physics.calculateDragCoefficient(machNumber);
                double areaCurrent = t < parachuteTime ? area : areaParachute;
                double dragForce = 0.5 * dragCoefficient * density * areaCurrent * y[1] * Math.abs(y[1]);
                double acceleration = -gravity - dragForce / mass;

                result.acceleration.add(acceleration);
                result.machNumber.add(machNumber);
                result.dragCoefficient.add(dragCoefficient);
            }
        });

        // Событие достижения земли
        integrator.addEventHandler(new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
                // Инициализация при начале интегрирования
            }

            @Override
            public double g(double t, double[] y) {
                if (Double.isNaN(y[0])) {
                    throw new RuntimeException("Altitude is NaN during event handling");
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
            System.err.println("Error during simulation: " + e.getMessage());
        }

        return result;
    }
}
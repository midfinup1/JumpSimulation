package app.model;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Simulation {

    private static final Logger logger = Logger.getLogger(Simulation.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("simulation_data.log", false);
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(java.util.logging.LogRecord record) {
                    return record.getMessage() + "\n";
                }
            });
            logger.addHandler(fileHandler);
            logger.setLevel(Level.FINE);
        } catch (IOException e) {
            System.err.println("Не удалось настроить логгер: " + e.getMessage());
        }
    }

    /**
     * Внутренний класс для уравнений движения.
     * y[0] = h (высота)
     * y[1] = v (скорость)
     * y[2] = x (степень раскрытия парашюта от 0 до 1)
     */
    private record EquationsOfMotion(double mass, double area, double areaParachute, double parachuteAltitude,
                                     double transitionTime) implements FirstOrderDifferentialEquations {

        @Override
        public int getDimension() {
            return 3;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            double altitude = y[0];
            double velocity = y[1];
            double x = y[2]; // Степень раскрытия парашюта

            double gravity = Physics.calculateGravity(altitude);
            Atmosphere.AtmosphereProperties atmosphere = Atmosphere.getAtmosphericProperties(altitude);
            double density = atmosphere.density;
            double soundSpeed = atmosphere.soundSpeed;

            double machNumber = soundSpeed > 0 ? Math.abs(velocity) / soundSpeed : 0;
            double dragCoefficient = Physics.calculateDragCoefficient(machNumber);

            // Уравнение для раскрытия парашюта:
            // Если высота ниже заданной, парашют начинает раскрываться:
            // dx/dt = (1 - x)/transitionTime
            // Если выше - парашют не раскрывается (dx/dt=0)
            if (altitude <= parachuteAltitude) {
                yDot[2] = (1.0 - x) / transitionTime;
            } else {
                yDot[2] = 0.0;
            }

            double effectiveArea = area + (areaParachute - area) * x;
            double dragForce = 0.5 * dragCoefficient * density * effectiveArea * velocity * velocity;
            double dragAcceleration = -(dragForce / mass) * Math.signum(velocity);

            yDot[0] = velocity;
            yDot[1] = -gravity + dragAcceleration;
        }
    }

    public static class SimulationResult {
        public final List<Double> time = new ArrayList<>();
        public final List<Double> altitude = new ArrayList<>();
        public final List<Double> velocity = new ArrayList<>();
        public final List<Double> acceleration = new ArrayList<>();
        public final List<Double> machNumber = new ArrayList<>();
        public final List<Double> dragCoefficient = new ArrayList<>();
        public final List<Double> deploymentProgress = new ArrayList<>();

        public double timeStep;
    }

    public static SimulationResult simulateJump(double mass, double initialHeight, double area, double areaParachute,
                                                double parachuteAltitude, double transitionTime) {

        double initialDensity = Atmosphere.getAtmosphericProperties(initialHeight).density;
        double initialCd = Physics.calculateDragCoefficient(0);
        double terminalVelocity = Math.sqrt((2 * mass * Physics.STANDARD_GRAVITY) / (initialCd * initialDensity * area));

        EquationsOfMotion equations = new EquationsOfMotion(mass, area, areaParachute, parachuteAltitude, transitionTime);

        double[] y = new double[]{initialHeight, 0, 0};

        double t0 = 0.0;
        double tMax = 1000.0;

        double minStep = 1.0e-8;
        double maxStep = 1.0;
        double absTolerance = 1.0e-8;
        double relTolerance = 1.0e-8;

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(minStep, maxStep, absTolerance, relTolerance);

        SimulationResult result = new SimulationResult();
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
                double alt = Math.max(y[0], 0);
                double vel = y[1];
                double x = y[2];

                Atmosphere.AtmosphereProperties atmosphere = Atmosphere.getAtmosphericProperties(alt);
                double temperature = atmosphere.temperature;
                double pressure = atmosphere.pressure;
                double density = atmosphere.density;
                double soundSpeed = atmosphere.soundSpeed;

                double machNumber = soundSpeed > 0 ? Math.abs(vel) / soundSpeed : 0;
                double dragCoefficient = Physics.calculateDragCoefficient(machNumber);

                double gravity = Physics.calculateGravity(alt);
                double effectiveArea = area + (areaParachute - area) * x;
                double dragForce = 0.5 * dragCoefficient * density * effectiveArea * vel * vel;
                double dragAcc = -(dragForce / mass) * Math.signum(vel);
                double acc = -gravity + dragAcc;

                result.time.add(t);
                result.altitude.add(alt);
                result.velocity.add(vel);
                result.acceleration.add(acc);
                result.machNumber.add(machNumber);
                result.dragCoefficient.add(dragCoefficient);
                result.deploymentProgress.add(x);

                logger.info(String.format(Locale.US,
                        "%.4f; %.4f; %.4f; %.4f; %.4f; %.4f; %.4f; %.4f; %.4f; %.4f; %.4f",
                        t, alt, vel, acc, dragCoefficient, machNumber,
                        temperature, pressure, density, gravity, soundSpeed));
//                logger.info(String.format(Locale.US, "Effective Area: %.2f, Deployment Progress: %.2f", effectiveArea, x));
            }
        });

        integrator.addEventHandler(new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {}

            @Override
            public double g(double t, double[] y) {
                return y[0];
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {}
        }, 1.0e-3, 1.0e-8, 1000);

        try {
            integrator.integrate(equations, t0, y, tMax, y);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Симуляция не удалась", e);
        }

        if (result.time.size() > 1) {
            double totalTime = result.time.getLast() - result.time.getFirst();
            result.timeStep = totalTime / (result.time.size() - 1);
        } else {
            result.timeStep = outputStep;
        }

        return result;
    }
}
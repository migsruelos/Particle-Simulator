import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.RecursiveAction;

class Particle {
    double x, y; // Position
    double angle; // Angle in degrees (0 is east, increases anticlockwise)
    double velocity; // Velocity in pixels per second

    Particle(double x, double y, double angle, double velocity) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
    }

    void move(double deltaTime, List<Line2D.Double> walls) {
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(new MoveTask(this, deltaTime, walls));
    }

    private static class MoveTask extends RecursiveAction {
        private static final int THRESHOLD = 5; // Adjust as needed
        private Particle particle;
        private double deltaTime;
        private List<Line2D.Double> walls;

        MoveTask(Particle particle, double deltaTime, List<Line2D.Double> walls) {
            this.particle = particle;
            this.deltaTime = deltaTime;
            this.walls = walls;
        }

        @Override
        protected void compute() {
            // Update particle position based on velocity and angle
            double newX = particle.x + particle.velocity * Math.cos(Math.toRadians(particle.angle)) * deltaTime;
            double newY = particle.y + particle.velocity * Math.sin(Math.toRadians(particle.angle)) * deltaTime;

            // Check for collisions with walls
            for (Line2D.Double wall : walls) {
                if (wall.intersectsLine(particle.x, particle.y, newX, newY)) {
                    // Particle collided with the wall, reflect its angle
                    double wallAngle = Math.toDegrees(Math.atan2(wall.y2 - wall.y1, wall.x2 - wall.x1));

                    // Calculate the reflection angle using the law of reflection
                    double incidentAngle = Math.toDegrees(Math.atan2(newY - particle.y, newX - particle.x));
                    double reflectionAngle = 2 * wallAngle - incidentAngle;

                    particle.angle = reflectionAngle;

                    // Stop checking other walls after the first collision
                    return;
                }
            }

            // Bounce off the canvas borders
            if (newX < 0 || newX > 1260) {
                particle.angle = 180 - particle.angle;
            }
            if (newY < 0 || newY > 680) {
                particle.angle = -particle.angle;
            }

            // Update particle position based on the corrected angle
            particle.x = newX;
            particle.y = newY;
        }

    }
}

class Canvas extends JPanel {
    private List<Particle> particles;
    private List<Line2D.Double> walls; // Use Line2D for representing walls

    private int frameCount = 0;
    private int fps;
    private long lastFPSTime = System.currentTimeMillis();
    ;

    Canvas() {
        particles = new ArrayList<>();
        walls = new ArrayList<>();
        setPreferredSize(new Dimension(1280, 720));
    }

    private int calculateFPS() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastFPSTime;

        frameCount++;

        if (elapsedTime >= 500) {
            fps = (int) (frameCount * 1000 / elapsedTime);
            frameCount = 0;
            lastFPSTime = currentTime;
        }

        return fps;
    }

    void addParticles(int n, double startX, double startY, double endX, double endY,
                      double initialAngle, double velocity) {
        for (int i = 0; i < n; i++) {
            double randomX = startX + Math.random() * (endX - startX);
            double randomY = startY + Math.random() * (endY - startY);
            particles.add(new Particle(randomX, randomY, initialAngle, velocity));
        }
    }


    void addWalls(double x1, double y1, double x2, double y2) {
        // Ensure that x1 and x2 are the same to create a vertical wall
        walls.add(new Line2D.Double(x1, y1, x1, y2));
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw particles
        g.setColor(Color.GREEN);
        for (Particle particle : particles) {
            g.fillOval((int) particle.x - 5, (int) particle.y - 5, 10, 10);
        }

        // Draw walls
        g.setColor(Color.BLUE);
        for (Line2D wall : walls) {
            g.drawLine((int) wall.getX1(), (int) wall.getY1(), (int) wall.getX2(), (int) wall.getY2());
        }

        // Draw FPS
        g.setColor(Color.BLACK);
        g.drawString("FPS: " + calculateFPS(), 10, 20);

    }


    void update() {
        calculateFPS();
        // method to update particle positions using parallel processing
        double deltaTime = 0.05;
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (Particle particle : particles) {
            Future<?> future = executorService.submit(() -> particle.move(deltaTime, walls));
            futures.add(future);
        }

        // wait for all tasks to finish before shutdown
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

        repaint();
    }

    public static void main(String[] args) {
        new ParticleSimulator();
    }
}



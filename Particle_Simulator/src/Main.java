import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

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
        private static final int THRESHOLD = 5;
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
            if (walls.size() <= THRESHOLD) {
                // if the number of walls is below the threshold, perform computation directly
                computeDirectly();
            } else {
                // generate subtasks
                List<MoveTask> subtasks = createSubtasks();
                invokeAll(subtasks);
            }
        }

        private void computeDirectly() {
            double newX, newY;
            synchronized (particle) {
                // updates the particle position based on velocity and angle
                newX = particle.x + particle.velocity * Math.cos(Math.toRadians(particle.angle)) * deltaTime;
                newY = particle.y + particle.velocity * Math.sin(Math.toRadians(particle.angle)) * deltaTime;
            }

            // wall collision checker
            for (Line2D.Double wall : walls) {
                if (wall.intersectsLine(particle.x, particle.y, newX, newY)) {
                    // if the particle collides with the wall, reflect its angle
                    double wallAngle = Math.toDegrees(Math.atan2(wall.y2 - wall.y1, wall.x2 - wall.x1));

                    // calculate the reflection angle using the law of reflection
                    double incidentAngle = Math.toDegrees(Math.atan2(newY - particle.y, newX - particle.x));
                    double reflectionAngle = 2 * wallAngle - incidentAngle;

                    synchronized (particle) {
                        particle.angle = reflectionAngle;
                    }

                    // stop checking other walls after the first collision
                    return;
                }
            }

            // check for collision off the canvas borders (adjusted size so the particles don't go out of the frame)
            if (newX < 0 || newX > 1260) {
                synchronized (particle) {
                    particle.angle = 180 - particle.angle;
                }
            }
            if (newY < 0 || newY > 680) {
                synchronized (particle) {
                    particle.angle =- particle.angle;
                }
            }

            // update the particle position based on the corrected angle
            synchronized (particle) {
                particle.x = newX;
                particle.y = newY;
            }
        }

        private List<MoveTask> createSubtasks() {
            // divide the walls into sublists and create subtasks
            int size = walls.size();
            int split = size / 2;
            List<MoveTask> subtasks = new ArrayList<>();
            subtasks.add(new MoveTask(particle, deltaTime, walls.subList(0, split)));
            subtasks.add(new MoveTask(particle, deltaTime, walls.subList(split, size)));
            return subtasks;
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

    void addParticlesByAngle(int n, double startX, double startY, double velocity, double startAngle, double endAngle) {
        for (int i = 0; i < n; i++) {
            double randomAngle = startY + Math.random() * (endAngle - startAngle);
            particles.add(new Particle(startX, startY, randomAngle, velocity));
        }
    }

    void addParticlesByVelocity(int n, double startX, double startY, double angle, double startVelocity, double endVelocity) {
        for (int i = 0; i < n; i++) {
            double randomVelocity = startX + Math.random() * (endVelocity - startVelocity);
            particles.add(new Particle(startX, startY, angle, randomVelocity));
        }
    }


    void addWalls(double x1, double y1, double x2, double y2) {
        // Ensure that x1 and x2 are the same to create a vertical wall
        walls.add(new Line2D.Double(x1, y1, x1, y2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Create offscreen buffer
        Image offscreen = createImage(getWidth(), getHeight());
        Graphics offscreenGraphics = offscreen.getGraphics();

        // Draw particles on the offscreen buffer
        offscreenGraphics.setColor(Color.GREEN);
        for (Particle particle : particles) {
            offscreenGraphics.fillOval((int) particle.x - 5, (int) particle.y - 5, 10, 10);
        }

        // Draw walls on the offscreen buffer
        offscreenGraphics.setColor(Color.BLUE);
        for (Line2D wall : walls) {
            offscreenGraphics.drawLine((int) wall.getX1(), (int) wall.getY1(), (int) wall.getX2(), (int) wall.getY2());
        }

        // Draw FPS on the offscreen buffer
        offscreenGraphics.setColor(Color.BLACK);
        offscreenGraphics.drawString("FPS: " + calculateFPS(), 10, 20);

        // Copy the offscreen buffer to the screen
        g.drawImage(offscreen, 0, 0, this);
    }



    void update() {
        calculateFPS();
        double deltaTime = 0.05;

        // minimum target FPS: 60
        long desiredFrameTime = 1000 / 60;
        long currentTime = System.currentTimeMillis();

        // split rendering tasks for particles and walls
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> renderingFutures = new ArrayList<>();

        // submit rendering tasks for particles
        Future<?> particlesRenderingFuture = executorService.submit(() -> {
            for (Particle particle : particles) {
                particle.move(deltaTime, walls);
            }
        });
        renderingFutures.add(particlesRenderingFuture);

        // submit rendering task for walls
        Future<?> wallsRenderingFuture = executorService.submit(() -> {
            repaint();
        });
        renderingFutures.add(wallsRenderingFuture);

        // wait for all rendering tasks to finish
        try {
            for (Future<?> future : renderingFutures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

        // calculates the time taken for the update and rendering tasks
        long elapsedTime = System.currentTimeMillis() - currentTime;

        // sleep to maintain a consistent frame rate
        long sleepTime = Math.max(0, desiredFrameTime - elapsedTime);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParticleSimulator());
    }
}



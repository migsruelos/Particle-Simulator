import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class Particle {
    double x, y; // position
    double angle; // angle in degrees
    double velocity; // velocity in pixels per second

    Particle(double x, double y, double angle, double velocity) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocity = velocity;
    }

    void move(double deltaTime, List<Line2D.Double> walls) {
        Executor executor = Executors.newWorkStealingPool();

        // check collision concurrently
        CompletableFuture<Void>[] futures = walls.stream()
                .map(wall -> CompletableFuture.runAsync(() -> checkCollision(wall, deltaTime), executor))
                .toArray(CompletableFuture[]::new);

        // wait for computations to finish
        CompletableFuture.allOf(futures).join();

        // update particle position
        double newX = x + velocity * Math.cos(Math.toRadians(angle)) * deltaTime;
        double newY = y + velocity * Math.sin(Math.toRadians(angle)) * deltaTime;
        x = newX;
        y = newY;

        // check collision on borders
        if (newX < 0 || newX > 1260) {
            angle = 180 - angle;
        }
        if (newY < 0 || newY > 680) {
            angle = -angle;
        }
    }

    private void checkCollision(Line2D.Double wall, double deltaTime) {
        double newX, newY;
        // updates particle position based on velocity and angle
        newX = x + velocity * Math.cos(Math.toRadians(angle)) * deltaTime;
        newY = y + velocity * Math.sin(Math.toRadians(angle)) * deltaTime;

        // wall collision checker
        if (wall.intersectsLine(x, y, newX, newY)) {
            // calculate collision/reflection angle
            double wallAngle = Math.toDegrees(Math.atan2(wall.y2 - wall.y1, wall.x2 - wall.x1));
            double incidentAngle = Math.toDegrees(Math.atan2(newY - y, newX - x));
            double reflectionAngle = 2 * wallAngle - incidentAngle;

            angle = reflectionAngle;
        }
    }
}

class Canvas extends JPanel {
    private List<Particle> particles;
    private List<Line2D.Double> walls; // Line2D represents the walls

    private int frameCount = 0;
    private int fps;
    private long lastFPSTime = System.currentTimeMillis();
    ;

    Canvas() {
        particles = new ArrayList<>();
        walls = new ArrayList<>();
        setPreferredSize(new Dimension(1280, 720));

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::calculateFPS, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void updateFPS() {
        fps = frameCount * 2;  // Multiply by 2 to convert from 0.5-second intervals to 1-second intervals
        frameCount = 0;
        lastFPSTime = System.currentTimeMillis();
        repaint();
    }

    private int calculateFPS() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastFPSTime;

        frameCount++;

        // update FPS every 0.5 seconds
        if (elapsedTime >= 500) {
            updateFPS();
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
        walls.add(new Line2D.Double(x1, y1, x2, y2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);


        Image offscreen = createImage(getWidth(), getHeight());
        Graphics offscreenGraphics = offscreen.getGraphics();


        offscreenGraphics.setColor(Color.GREEN);
        for (Particle particle : particles) {
            offscreenGraphics.fillOval((int) particle.x - 5, (int) particle.y - 5, 10, 10);
        }


        offscreenGraphics.setColor(Color.BLUE);
        for (Line2D wall : walls) {
            offscreenGraphics.drawLine((int) wall.getX1(), (int) wall.getY1(), (int) wall.getX2(), (int) wall.getY2());
        }


        offscreenGraphics.setColor(Color.BLACK);
        offscreenGraphics.drawString("FPS: " + calculateFPS(), 10, 20);


        g.drawImage(offscreen, 0, 0, this);
    }



    void update() {
        calculateFPS();
        double deltaTime = 0.05;

        // minimum target FPS: 60
        long targetFrameTime = 1000 / 60;
        long currentTime = System.currentTimeMillis();

        // split rendering tasks for particles and walls
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> renderingFutures = new ArrayList<>();

        // submit rendering tasks for particles
        Future<?> renderingFuture = executorService.submit(() -> {
            for (Particle particle : particles) {
                particle.move(deltaTime, walls);
            }
            repaint();
        });
        renderingFutures.add(renderingFuture);

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
        long sleepTime = Math.max(0, targetFrameTime - elapsedTime);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}



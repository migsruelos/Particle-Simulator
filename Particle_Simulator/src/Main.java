import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

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
        // Update particle position based on velocity and angle
        double newX = x + velocity * Math.cos(Math.toRadians(angle)) * deltaTime;
        double newY = y + velocity * Math.sin(Math.toRadians(angle)) * deltaTime;

        // Check for collisions with walls
        for (Line2D.Double wall : walls) {
            if (wall.intersectsLine(x, y, newX, newY)) {
                // Particle collided with the wall, reflect its angle
                double wallAngle = Math.toDegrees(Math.atan2(wall.y2 - wall.y1, wall.x2 - wall.x1));
                double normalAngle = wallAngle + 90; // Calculate the normal angle to the wall
                double incidentAngle = angle - wallAngle;

                // Calculate the reflection angle using the law of reflection
                angle = normalAngle + incidentAngle;

                break; // Stop checking other walls after the first collision
            }
        }

        x = newX;
        y = newY;

        // Bounce off the canvas borders
        if (x < 0 || x > 1330) {
            angle = 180 - angle;
        }
        if (y < 0 || y > 680) {
            angle = -angle;
        }
    }
}

class Canvas extends JPanel {
    private List<Particle> particles;
    private List<Line2D.Double> walls; // Use Line2D for representing walls

    private int frameCount = 0;
    private int fps;
    private long lastFPSTime;

    Canvas() {
        particles = new ArrayList<>();
        walls = new ArrayList<>();
        setPreferredSize(new Dimension(1280, 720));
    }

    private int calculateFPS() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastFPSTime;

        frameCount++;

        if (elapsedTime >= 1000) {
            fps = (int) (frameCount * 1000 / elapsedTime);
            frameCount = 0;
            lastFPSTime = currentTime;
        }

        return fps;
    }

    void addParticles(int n, double startX, double startY, double endX, double endY,
                      double initialAngle, double velocity) {
        for (int i = 0; i < n; i++) {
            // Generate random starting coordinates within the specified range
            double randomX = startX + Math.random() * (endX - startX);
            double randomY = startY + Math.random() * (endY - startY);

            // Create a particle with random starting coordinates
            particles.add(new Particle(randomX, randomY, initialAngle, velocity));
        }
    }


    void addWalls(double x1, double y1, double x2, double y2) {
        walls.add(new Line2D.Double(x1, y1, x2, y2));
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
        // Update particle positions
        double deltaTime = 0.05; // You may adjust this based on your requirements
        for (Particle particle : particles) {
            particle.move(deltaTime, walls);
        }

        repaint();
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParticleSimulator simulator = new ParticleSimulator();
            simulator.setSize(1350, 720);
            simulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            simulator.setVisible(true);

            // Access the Canvas instance from ParticleSimulator
            Canvas canvas = simulator.getCanvas();

            // Adding walls to the canvas
            canvas.addWalls(50, 200, 300, 500);
            canvas.addWalls(500, 200, 300, 500);


            Timer timer = new Timer(20, e -> {
                canvas.update();
            });
            timer.start();
        });
    }
}

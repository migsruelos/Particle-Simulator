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
            // Get the distance from the particle center to the wall
            double dist = wall.ptSegDist(newX, newY);
            if (dist < 5) {
                // Particle collided with the wall, reflect its angle
                double wallAngle = Math.toDegrees(Math.atan2(wall.y2 - wall.y1, wall.x2 - wall.x1));
                double normalAngle = wallAngle + 90;
                double incidentAngle = normalAngle - angle;
                double reflectionAngle = incidentAngle;

                angle = reflectionAngle;

                break;
            }
        }

        x = newX;
        y = newY;

        // Bounce off the canvas borders
        if (x < 0 || x > 1260) {
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
    private long lastFPSTime = System.currentTimeMillis();

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
        calculateFPS();
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
            simulator.setSize(1280, 720);
            simulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            simulator.setVisible(true);

            // Access the Canvas instance from ParticleSimulator
            Canvas canvas = simulator.getCanvas();

            Timer timer = new Timer(20, e -> {
                canvas.update();
            });
            timer.start();
        });
    }
}

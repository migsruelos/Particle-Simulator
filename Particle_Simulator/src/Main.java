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

    void move(double deltaTime) {
        // Update particle position based on velocity and angle
        x += velocity * Math.cos(Math.toRadians(angle)) * deltaTime;
        y += velocity * Math.sin(Math.toRadians(angle)) * deltaTime;

        // Bounce off the walls
        if (x < 0 || x > 1280) {
            angle = 180 - angle;
        }
        if (y < 0 || y > 720) {
            angle = -angle;
        }
    }
}

class Canvas extends JPanel {
    private List<Particle> particles;
    private List<Line2D> walls; // Use Line2D for representing walls

    Canvas() {
        particles = new ArrayList<>();
        walls = new ArrayList<>();
        setPreferredSize(new Dimension(1280, 720));
    }

    void addParticles(int n, double startX, double startY, double endX, double endY,
                      double initialAngle, double velocity) {
        double distanceX = (endX - startX) / n;
        double distanceY = (endY - startY) / n;

        for (int i = 0; i < n; i++) {
            particles.add(new Particle(startX + i * distanceX, startY + i * distanceY, initialAngle, velocity));
        }
    }

    void addWalls(double x1, double y1, double x2, double y2) {
        walls.add(new Line2D.Double(x1, y1, x2, y2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw particles
        g.setColor(Color.RED);
        for (Particle particle : particles) {
            g.fillOval((int) particle.x - 5, (int) particle.y - 5, 10, 10);
        }

        // Draw walls
        g.setColor(Color.BLUE);
        for (Line2D wall : walls) {
            g.drawLine((int) wall.getX1(), (int) wall.getY1(), (int) wall.getX2(), (int) wall.getY2());
        }
    }



    void update() {
        // Update particle positions
        double deltaTime = 0.05; // You may adjust this based on your requirements
        for (Particle particle : particles) {
            particle.move(deltaTime);
        }

        repaint();
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParticleSimulator simulator = new ParticleSimulator();
            simulator.setSize(1280, 720);
            simulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


            simulator.setVisible(true);
        });
    }
}

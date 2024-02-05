import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParticleSimulator extends JFrame {
    private Canvas canvas;

    public ParticleSimulator() {
        canvas = new Canvas();
        add(canvas);

        // Example usage: adding particles and walls
        canvas.addParticles(10, 100, 100, 400, 400, 45, 50);
        canvas.addWalls(200, 200, 500, 250);

        // Timer to update the simulation regularly
        Timer timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    canvas.update();
                });
            }
        });
        timer.start();
    }

    public Canvas getCanvas() {
        return canvas;
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

class ParticleSimulator extends JFrame {
    private Canvas canvas;

    ParticleSimulator() {
        setTitle("Particle Simulator");
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        canvas = new Canvas();
        panel.add(canvas);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton particleButton = new JButton();
        particleButton.setText("Add Particle");
        particleButton.setSize(12, 16);
        particleButton.addActionListener(e -> {
            JButton button = (JButton) e.getSource();
            JFrame frame = (JFrame) SwingUtilities.windowForComponent(button);

            ParticleInputDialog particleDialog = new ParticleInputDialog(frame);
            particleDialog.setVisible(true);
        });
        buttonPanel.add(particleButton);

        JButton wallButton = new JButton();
        wallButton.setText("Add Wall");
        wallButton.setSize(12, 16);
        wallButton.addActionListener(e -> {
            JButton button = (JButton) e.getSource();
            JFrame frame = (JFrame) SwingUtilities.windowForComponent(button);

            WallInputDialog wallDialog = new WallInputDialog(frame, canvas);
            wallDialog.setVisible(true);
        });
        buttonPanel.add(wallButton);

        panel.add(buttonPanel);
        add(panel);
        setSize(1280, 720);

        // Create and execute SwingWorker in a separate thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                collectUserParticleInput();  // Collect user input for particles on a separate thread
                return null;
            }

            @Override
            protected void done() {
                // Start the main thread to display the GUI
                SwingUtilities.invokeLater(() -> {
                    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    setVisible(true);
                    startSimulation();
                });
            }
        };

        worker.execute();
    }


    private void collectUserParticleInput() {
        // Create a dialog to get user input for particles
        ParticleInputDialog particleDialog = new ParticleInputDialog(this);
        particleDialog.setVisible(true);

        // Open WallInputDialog after submitting ParticleInputDialog
        WallInputDialog wallDialog = new WallInputDialog(this, canvas);
        wallDialog.setVisible(true);
    }

    private void startSimulation() {
        // Access the Canvas instance from ParticleSimulator
        Canvas canvas = this.canvas;

        // Timer for updating the canvas
        Timer timer = new Timer(20, e -> {
            canvas.update();
        });
        timer.start();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public static void main(String[] args) {
        new ParticleSimulator();
    }
}

class ParticleInputDialog extends JDialog {
    private JTextField particleCountField;
    private JTextField startXField;
    private JTextField startYField;
    private JTextField endXField;
    private JTextField endYField;

    ParticleInputDialog(JFrame parent) {
        super(parent, "Particle Input", true);
        setLocationRelativeTo(parent);
        setSize(400, 300);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        particleCountField = createInputField();
        startXField = createInputField();
        startYField = createInputField();
        endXField = createInputField();
        endYField = createInputField();

        addRow(panel, gbc, "Particle Count:", particleCountField);
        addRow(panel, gbc, "Start X:", startXField);
        addRow(panel, gbc, "Start Y:", startYField);
        addRow(panel, gbc, "End X:", endXField);
        addRow(panel, gbc, "End Y:", endYField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            // Get user input values
            int particleCount = Integer.parseInt(particleCountField.getText());
            double startX = Double.parseDouble(startXField.getText());
            double startY = Double.parseDouble(startYField.getText());
            double endX = Double.parseDouble(endXField.getText());
            double endY = Double.parseDouble(endYField.getText());

            // Add particles to the canvas
            Canvas canvas = ((ParticleSimulator) getParent()).getCanvas();
            canvas.addParticles(particleCount, startX, startY, endX, endY, 45, 80);

            // Close the dialog
            setVisible(false);
        });

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitButton, gbc);

        add(panel);
    }

    private JTextField createInputField() {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(150, 25));
        return textField;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(textField, gbc);
    }
}

class WallInputDialog extends JDialog {
    private boolean dialogOpen = false;
    private JTextField wallX1Field;
    private JTextField wallY1Field;
    private JTextField wallX2Field;
    private JTextField wallY2Field;
    private Canvas canvas;

    WallInputDialog(JFrame parent, Canvas canvas) {
        super(parent, "Wall Input", true);
        this.canvas = canvas;
        setLocationRelativeTo(parent);
        setSize(400, 300);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        wallX1Field = createInputField();
        wallY1Field = createInputField();
        wallX2Field = createInputField();
        wallY2Field = createInputField();

        addRow(panel, gbc, "Wall X1:", wallX1Field);
        addRow(panel, gbc, "Wall Y1:", wallY1Field);
        addRow(panel, gbc, "Wall X2:", wallX2Field);
        addRow(panel, gbc, "Wall Y2:", wallY2Field);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            // Get user input values for walls
            double wallX1 = Double.parseDouble(wallX1Field.getText());
            double wallY1 = Double.parseDouble(wallY1Field.getText());
            double wallX2 = Double.parseDouble(wallX2Field.getText());
            double wallY2 = Double.parseDouble(wallY2Field.getText());

            // Add walls to the canvas
            canvas.addWalls(wallX1, wallY1, wallX2, wallY2);

            // Close the dialog
            setVisible(false);
        });

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitButton, gbc);

        add(panel);
    }

    private JTextField createInputField() {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(150, 25));
        return textField;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(textField, gbc);
    }
}

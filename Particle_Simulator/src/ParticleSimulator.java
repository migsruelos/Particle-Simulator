import javax.swing.*;
import java.awt.*;

class ParticleSimulator extends JFrame {
    private Canvas canvas;

    ParticleSimulator() {
        setTitle("Particle Simulator");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        canvas = new Canvas();
        panel.add(canvas);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton particleByDistanceButton = new JButton("Add Particle (Distance)");
        particleByDistanceButton.addActionListener(e -> {
            ParticleByDistanceInputDialog particleByDistanceDialog = new ParticleByDistanceInputDialog(this);
            particleByDistanceDialog.setVisible(true);
        });
        buttonPanel.add(particleByDistanceButton);

        JButton particleByAngleButton = new JButton("Add Particle (Angle)");
        particleByAngleButton.addActionListener(e -> {
            ParticleByAngleInputDialog particleByAngleDialog = new ParticleByAngleInputDialog(this);
            particleByAngleDialog.setVisible(true);
        });
        buttonPanel.add(particleByAngleButton);

        JButton particleByVelocityButton = new JButton("Add Particle (Velocity)");
        particleByVelocityButton.addActionListener(e -> {
            ParticleByVelocityInputDialog particleByVelocityDialog = new ParticleByVelocityInputDialog(this);
            particleByVelocityDialog.setVisible(true);
        });
        buttonPanel.add(particleByVelocityButton);

        JButton wallButton = new JButton("Add Wall");
        wallButton.addActionListener(e -> {
            WallInputDialog wallDialog = new WallInputDialog(this, canvas);
            wallDialog.setVisible(true);
        });
        buttonPanel.add(wallButton);

        panel.add(buttonPanel);
        add(panel);
        setSize(1280, 720);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // timer to update the canvas on the EDT
        Timer timer = new Timer(15, e -> {
            canvas.update();
        });
        timer.start();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParticleSimulator());
    }
}

class ParticleByDistanceInputDialog extends JDialog {
    private JTextField particleCountField;
    private JTextField startXField;
    private JTextField startYField;
    private JTextField endXField;
    private JTextField endYField;

    ParticleByDistanceInputDialog(JFrame parent) {
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
            // get user input
            int particleCount = Integer.parseInt(particleCountField.getText());
            double startX = Double.parseDouble(startXField.getText());
            double startY = Double.parseDouble(startYField.getText());
            double endX = Double.parseDouble(endXField.getText());
            double endY = Double.parseDouble(endYField.getText());

            // add particles to canvas
            Canvas canvas = ((ParticleSimulator) getParent()).getCanvas();
            canvas.addParticles(particleCount, startX, startY, endX, endY, 45, 80);

            // close the dialog
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

class ParticleByAngleInputDialog extends JDialog {
    private JTextField particleCountField;
    private JTextField startAngleField;
    private JTextField endAngleField;

    ParticleByAngleInputDialog(JFrame parent) {
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
        startAngleField = createInputField();
        endAngleField = createInputField();

        addRow(panel, gbc, "Particle Count:", particleCountField);
        addRow(panel, gbc, "Start Angle:", startAngleField);
        addRow(panel, gbc, "End Angle:", endAngleField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            // get user input
            int particleCount = Integer.parseInt(particleCountField.getText());
            double startAngle = Double.parseDouble(startAngleField.getText());
            double endAngle = Double.parseDouble(endAngleField.getText());

            // add particles to canvas
            Canvas canvas = ((ParticleSimulator) getParent()).getCanvas();
            canvas.addParticlesByAngle(particleCount, 100, 100, 80, startAngle, endAngle);

            // close the dialog
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

class ParticleByVelocityInputDialog extends JDialog {
    private JTextField particleCountField;
    private JTextField startVelocityField;
    private JTextField endVelocityField;

    ParticleByVelocityInputDialog(JFrame parent) {
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
        startVelocityField = createInputField();
        endVelocityField = createInputField();

        addRow(panel, gbc, "Particle Count:", particleCountField);
        addRow(panel, gbc, "Start Velocity:", startVelocityField);
        addRow(panel, gbc, "End Velocity:", endVelocityField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            // get user input
            int particleCount = Integer.parseInt(particleCountField.getText());
            double startVelocity= Double.parseDouble(startVelocityField.getText());
            double endVelocity = Double.parseDouble(endVelocityField.getText());

            // add particles to canvas
            Canvas canvas = ((ParticleSimulator) getParent()).getCanvas();
            canvas.addParticlesByVelocity(particleCount, 100, 100, 45, startVelocity, endVelocity);

            // close the dialog
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
            // get user input
            double wallX1 = Double.parseDouble(wallX1Field.getText());
            double wallY1 = Double.parseDouble(wallY1Field.getText());
            double wallX2 = Double.parseDouble(wallX2Field.getText());
            double wallY2 = Double.parseDouble(wallY2Field.getText());

            // add walls to canvas
            canvas.addWalls(wallX1, wallY1, wallX2, wallY2);

            // close the dialog
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
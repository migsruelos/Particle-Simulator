# Problem Set 1 : Particle Simulation

This is a particle simulation application that allows you to add particles to the environment with different initial positions, angles, and velocities. The particles will travel in a straight line, bouncing off the four walls of the canvas and any user-defined walls. The collisions are elastic, which means the particles do not lose or gain speed after bouncing. You can also add particles in batches with varying parameters. The application will show the FPS (frames per second) counter on-screen every 0.5 seconds.

# Requirements

Java 8 or higher.

# Usage

* Run the JAR file
  * This will open a window with 1280 x 720 pixel canvas. The coordinates (0, 0) is the southwest corner of the canvas, and the coordinate (1280, 720) is the northeast corner.
* In the window, the user will be presented with four buttons and the canvas for the simulation.
  * Three of the buttons are for the user to add particles
    * The X and Y inputs from the user are read as pixel coordinates.
    * When adding particles using methods addParticles, addParticlesByAngle, and addParticlesByVelocity, the angle parameter represents the initial angle of the particle in degrees.
    * Parameters startX and startY specify the minimum pixel coordinates for the starting position of the particles, while endX and endY specify the pixel coordinates for the ending position.
  * The fourth button is for the user to add walls
    * When adding walls using the addWalls method, the parameters x1, y1, x2, and y2 represent the pixel coordinates/positions of the two endpoints of a line segment on the canvas.
    * These values indicate the position of the wall in terms of pixels from the top-left corner of your canvas.
    * For example, you can add a wall with one endpoint at pixel coordinates (100, 200) and the other endpoint at (300, 400).
* Once the user has inputted their desired value, it will be visible to the user on the canvas after adding the submit button.

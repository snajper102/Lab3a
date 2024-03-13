import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * A panel that displays a two-dimensional animation that is drawn
 * using subroutines to implement hierarchical modeling.  There is a
 * checkbox that turns the animation on and off.
 */
public class SubroutineHierarchy extends JPanel {
    public static void main(String[] args) {
        JFrame window = new JFrame("Subroutine Hierarchy");
        window.setContentPane(new SubroutineHierarchy());
        window.pack();
        window.setLocation(100, 60);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    private final static int WIDTH = 800;   // The preferred size for the drawing area.
    private final static int HEIGHT = 600;

    private final static double X_LEFT = -4;    // The xy limits for the coordinate system.
    private final static double X_RIGHT = 4;
    private final static double Y_BOTTOM = -3;
    private final static double Y_TOP = 3;

    private final static Color BACKGROUND = Color.WHITE;

    private float pixelSize;

    private int frameNumber = 0;

    private void drawWorld(Graphics2D g2) {
        rotatingShape(g2, 100, -1.02, -0.05);
        rotatingShape(g2, 100, 1.04, -0.98);
        rotatingShape(g2, 80, -1.379, 1.40);
        rotatingShape(g2, 80, -3.13, 2.23);
        rotatingShape(g2, 60, 0.9, 2.05);
        rotatingShape(g2, 60, 2.12, 1.45);

        Bar(g2, 1, 1.05, 0, -0.5);
        Bar(g2, 0.85, 0.95, -2.65, 1.90);
        Bar(g2, 0.6, 0.70, 2.5, 2.5);

        Triangle(g2, 0.5, 0.5, 0, -2, Color.BLUE);
        Triangle(g2, 0.35, 0.35, -2.25, 0.75, Color.PINK);
        Triangle(g2, 0.25, 0.25, 1.5, 1, Color.GREEN);
    }

    private void Bar(Graphics2D g2, double x, double y, double offsetX, double offsetY) {
        AffineTransform saveTransform = g2.getTransform();
        g2.scale(x, y);

        g2.setColor(Color.RED);
        g2.translate(offsetX, offsetY);
        g2.rotate(-Math.PI / 8);
        g2.scale(2.3, 0.15);
        filledRect(g2);

        g2.setTransform(saveTransform);
    }

    private void Triangle(Graphics2D g2, double scaleX, double scaleY, double offsetX, double offsetY, Color color) {
        AffineTransform saveTransform = g2.getTransform();
        g2.setColor(color);
        g2.translate(offsetX, offsetY);
        g2.scale(scaleX, scaleY);
        g2.fillPolygon(new int[]{0, 1, -1}, new int[]{3, 0, 0}, 3);
        g2.setTransform(saveTransform);
    }

    private void updateFrame() {
        frameNumber++;
    }

    private void rotatingShape(Graphics2D g2, double radius, double offsetX, double offsetY) {
        AffineTransform saveTransform = g2.getTransform();
        Color saveColor = g2.getColor();
        g2.setStroke(new BasicStroke(2));

        int numOfVertices = 13;
        double angle = (Math.PI * 2) / numOfVertices;

        int[] xPoints = new int[numOfVertices];
        int[] yPoints = new int[numOfVertices];

        Polygon polygon = new Polygon();

        for (int i = 0; i < numOfVertices; i++) {
            xPoints[i] = (int) (radius * Math.sin(i * angle));
            yPoints[i] = (int) (radius * Math.cos(i * angle));

            if (i != 0)
                polygon.addPoint(xPoints[i - 1], yPoints[i - 1]);

            polygon.addPoint(xPoints[i], yPoints[i]);
            polygon.addPoint(0, 0);
        }

        polygon.addPoint(xPoints[0], yPoints[0]);
        polygon.addPoint(xPoints[numOfVertices - 1], yPoints[numOfVertices - 1]);

        g2.translate(offsetX, offsetY);
        g2.setColor(Color.black);
        g2.rotate(Math.toRadians(frameNumber));
        g2.scale(0.005, 0.005);

        g2.draw(polygon);
        g2.setColor(saveColor);
        g2.setTransform(saveTransform);
    }


    private static void filledRect(Graphics2D g2) { // Fills a square, size = 1, center = (0,0)
        g2.fill(new Rectangle2D.Double(-0.5, -0.5, 1, 1));
    }


    //--------------------------------- Implementation ------------------------------------

    private JPanel display;  // The JPanel in which the scene is drawn.

    public SubroutineHierarchy() {
        display = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                applyLimits(g2, X_LEFT, X_RIGHT, Y_TOP, Y_BOTTOM, false);
                g2.setStroke(new BasicStroke(pixelSize)); // set default line width to one pixel.
                drawWorld(g2);  // draw the world
            }
        };
        display.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        display.setBackground(BACKGROUND);
        final Timer timer = new Timer(17, new ActionListener() { // about 60 frames per second
            public void actionPerformed(ActionEvent evt) {
                updateFrame();
                repaint();
            }
        });
        final JCheckBox animationCheck = new JCheckBox("Run Animation");
        animationCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (animationCheck.isSelected()) {
                    if (!timer.isRunning())
                        timer.start();
                } else {
                    if (timer.isRunning())
                        timer.stop();
                }
            }
        });
        JPanel top = new JPanel();
        top.add(animationCheck);
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 4));
        add(top, BorderLayout.NORTH);
        add(display, BorderLayout.CENTER);
    }


    /**
     * Applies a coordinate transform to a Graphics2D graphics context.  The upper left corner of
     * the viewport where the graphics context draws is assumed to be (0,0).  The coordinate
     * transform will make a requested rectangle visible in the drawing area.  The requested
     * limits might be adjusted to preserve the aspect ratio.  (This method sets the global variable
     * pixelSize to be equal to the size of one pixel in the transformed coordinate system.)
     *
     * @param g2             The drawing context whose transform will be set.
     * @param xleft          requested x-value at left of drawing area.
     * @param xright         requested x-value at right of drawing area.
     * @param ytop           requested y-value at top of drawing area.
     * @param ybottom        requested y-value at bottom of drawing area; can be less than ytop, which will
     *                       reverse the orientation of the y-axis to make the positive direction point upwards.
     * @param preserveAspect if preserveAspect is false, then the requested rectangle will exactly fill
     *                       the viewport; if it is true, then the limits will be expanded in one direction, horizontally or
     *                       vertically, to make the aspect ratio of the displayed rectangle match the aspect ratio of the
     *                       viewport.  Note that when preserveAspect is false, the units of measure in the horizontal and
     *                       vertical directions will be different.
     */
    private void applyLimits(Graphics2D g2, double xleft, double xright,
                             double ytop, double ybottom, boolean preserveAspect) {
        int width = display.getWidth();   // The width of the drawing area, in pixels.
        int height = display.getHeight(); // The height of the drawing area, in pixels.
        if (preserveAspect) {
            // Adjust the limits to match the aspect ratio of the drawing area.
            double displayAspect = Math.abs((double) height / width);
            double requestedAspect = Math.abs((ybottom - ytop) / (xright - xleft));
            if (displayAspect > requestedAspect) {
                double excess = (ybottom - ytop) * (displayAspect / requestedAspect - 1);
                ybottom += excess / 2;
                ytop -= excess / 2;
            } else if (displayAspect < requestedAspect) {
                double excess = (xright - xleft) * (requestedAspect / displayAspect - 1);
                xright += excess / 2;
                xleft -= excess / 2;
            }
        }
        double pixelWidth = Math.abs((xright - xleft) / width);
        double pixelHeight = Math.abs((ybottom - ytop) / height);
        pixelSize = (float) Math.min(pixelWidth, pixelHeight);
        g2.scale(width / (xright - xleft), height / (ybottom - ytop));
        g2.translate(-xleft, -ytop);
    }

}

import java.awt.*;
import java.awt.event.*;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


class TouchTracking extends JPanel implements ChangeListener {


    @Override
    public void stateChanged(ChangeEvent e) {
        int N = (int) numTapsSpinner.getValue();
        lastNX = new CircularFifoQueue<Integer>(N);
        lastNY = new CircularFifoQueue<Integer>(N);
    }

    private final class DragTracker extends MouseAdapter {
        private Point lastPoint;

        @Override
        public void mousePressed(MouseEvent m) {
            lastPoint = m.getPoint();
            x = lastPoint.x;
            y = lastPoint.y;
            width = 15;
            height = 15;
            lastNX.add(lastPoint.x);
            lastNY.add(lastPoint.y);
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent m) {
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent m) {
            int dx = m.getX() - lastPoint.x;
            int dy = m.getY() - lastPoint.y;
            x += dx;
            y += dy;
            lastPoint = m.getPoint();
            lastNX.add(lastPoint.x);
            lastNY.add(lastPoint.y);
            repaint();
        }
    }

    private int x;
    private int y;
    private int width;
    private int height;

    private static JCheckBox mafButton = new JCheckBox("Moving Average Filter (RED)");
    private static JCheckBox harmonicButton = new JCheckBox("Harmonic Filter (GREEN)");

    private static JSpinner numTapsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 99, 1));

    // Queues that hold the last n points
    private static CircularFifoQueue<Integer> lastNX = new CircularFifoQueue<Integer>((Integer) numTapsSpinner.getValue());
    private static CircularFifoQueue<Integer> lastNY = new CircularFifoQueue<Integer>((Integer) numTapsSpinner.getValue());

    private DragTracker dragTracker;

    public TouchTracking() {
        setBackground(Color.WHITE);
        dragTracker = new DragTracker();
        addMouseListener(dragTracker);
        addMouseMotionListener(dragTracker);
    }

    private static int movingAverageFilter(CircularFifoQueue<Integer> list) {
        int sum = 0;
        int N = list.size();
        for (int i=0; i<N; i++) {
            sum += list.get(i);
        }
        return sum/N;
    }

    private static int harmonicFilter(CircularFifoQueue<Integer> list) {
        int sum = 0;
        int M = list.size();
        for (int i=0; i<M; i++) {
            sum += (M-i)*list.get(i);
        }
        return sum/(M*(M+1)/2);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // DRAW CURRENT MOUSE DRAG LOCATION
        g.setColor(Color.BLUE);
        g.fillOval(x, y, width, height);

        // DRAW MOVING AVERAGE FILTER POINT
        g.setColor(Color.RED);
        if(lastNX.size() > 0 && mafButton.isSelected()) {
            int avgX = movingAverageFilter(lastNX);
            int avgY = movingAverageFilter(lastNY);
            g.fillOval(avgX, avgY, width, height);
        }

        // DRAW HARMONIC FILTER POINT
        g.setColor(Color.GREEN);
        if(lastNX.size() > 0 && harmonicButton.isSelected()) {
            int avgX = harmonicFilter(lastNX);
            int avgY = harmonicFilter(lastNY);
            g.fillOval(avgX, avgY, width, height);
        }
    }

    private static void setupFrame() {
        JFrame jFrame = new JFrame();
        JPanel panel = new JPanel();
        jFrame.setSize(750, 750);
        TouchTracking touchTracking = new TouchTracking();
        panel.add(mafButton);
        panel.add(harmonicButton);
        JLabel numTapsLabel = new JLabel("Number of taps:");
        panel.add(numTapsLabel);
        panel.add(numTapsSpinner);
        panel.setSize(panel.getPreferredSize());
        jFrame.add(panel, BorderLayout.NORTH);
        jFrame.add(new TouchTracking());
        numTapsSpinner.addChangeListener(touchTracking);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        setupFrame();
    }
}

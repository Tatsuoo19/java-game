package brickBreacker;

//weak man blames his future
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Brick {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Brick::createWindow);
    }

    private static void createWindow() {
        JFrame obj = new JFrame();
        BrickBreaker gameplay = new BrickBreaker();
        obj.setTitle("BRICK BLOCK - Four Seasons");
        obj.setResizable(false);
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        obj.add(gameplay);
        obj.pack();
        obj.setLocationRelativeTo(null);
        obj.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                gameplay.pauseForFocusLoss();
            }
        });
        obj.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                gameplay.close();
            }
        });
        obj.setVisible(true);
        gameplay.requestFocusInWindow();

        // maximize the window

        // obj.setExtendedState(JFrame.MAXIMIZED_BOTH);

    }
}

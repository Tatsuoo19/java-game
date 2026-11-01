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
        obj.setBounds(10,10,800,600);
        obj.setTitle("Breakout Ball");
        obj.setResizable(false);
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        obj.add(gameplay);
        obj.setVisible(true);
        gameplay.requestFocusInWindow();

        //maximize the window

        //obj.setExtendedState(JFrame.MAXIMIZED_BOTH);

    }
}

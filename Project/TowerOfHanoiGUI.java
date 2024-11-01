import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Stack;

// Abstract base class representing common properties of any graphical game component
abstract class GameComponent extends JPanel {
    protected int numberOfDisks;
    protected int moveCount;
    protected int maxMoves;

    public GameComponent(int numberOfDisks) {
        this.numberOfDisks = numberOfDisks;
        this.moveCount = -1;
        this.maxMoves = (int) Math.pow(2, numberOfDisks) - 1; // Calculate max moves
    }

    protected abstract void resetGame(); // Abstract method to be implemented by subclasses

    public int getMoveCount() {
        return moveCount;
    }

    public void incrementMoveCount() {
        moveCount++;
    }
}

// Class for representing disks with encapsulation of properties
class Disk extends Rectangle {
    private Color color;

    public Disk(int width, int height, Color color) {
        super(width, height);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

// The main class that extends the abstract GameComponent and implements GUI behavior
public class TowerOfHanoiGUI extends GameComponent implements MouseListener, MouseMotionListener {

    private ArrayList<Stack<Disk>> rods;
    private Disk draggedDisk = null;
    private int fromRod = -1;
    private int mouseX, mouseY;
    private boolean isDragging = false;
    private JButton resetButton;

    public TowerOfHanoiGUI() {
        super(getUserSelectedDisks()); // Prompt the user to select the number of disks
        rods = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            rods.add(new Stack<>());
        }

        initializeDisks(); // Initialize disks based on the selected number
        setupGUI();
    }

    // Method to prompt the user to select the number of disks
    private static int getUserSelectedDisks() {
        String[] options = {"3", "4", "5", "6", "7"};
        String selectedValue = (String) JOptionPane.showInputDialog(
                null,
                "Select the number of disks:",
                "Tower of Hanoi Setup",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        return selectedValue != null ? Integer.parseInt(selectedValue) : 3; // Default to 3 disks if canceled
    }

    private void initializeDisks() {
        rods.get(0).clear(); // Ensure the first rod is empty before adding disks
        for (int i = numberOfDisks; i >= 1; i--) {
            rods.get(0).push(new Disk(50 + i * 20, 20, Color.ORANGE));
        }
    }

    private void setupGUI() {
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        resetButton.setBounds(250, 320, 100, 30);
        this.setLayout(null);
        this.add(resetButton);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the platform
        g.setColor(Color.GRAY);
        g.fillRect(25, 300, 550, 5); // Adjust dimensions and position as needed

        // Draw rods
        for (int i = 0; i < 3; i++) {
            g.setColor(Color.BLACK);
            g.fillRect(150 + i * 150, 100, 10, 200);
        }

        // Draw disks on each rod
        for (int i = 0; i < 3; i++) {
            int rodX = 150 + i * 150;
            int rodY = 300;
            for (Disk disk : rods.get(i)) {
                g.setColor(disk.getColor());
                g.fillRect(rodX - disk.width / 2, rodY - disk.height, disk.width, disk.height);
                g.setColor(Color.BLACK);
                g.drawRect(rodX - disk.width / 2, rodY - disk.height, disk.width, disk.height);
                rodY -= disk.height;
            }
        }

        // Draw the dragged disk if it exists
        if (draggedDisk != null && isDragging) {
            g.setColor(Color.RED);
            g.fillRect(mouseX - draggedDisk.width / 2, mouseY - draggedDisk.height / 2, draggedDisk.width, draggedDisk.height);
            g.setColor(Color.BLACK);
            g.drawRect(mouseX - draggedDisk.width / 2, mouseY - draggedDisk.height / 2, draggedDisk.width, draggedDisk.height);
        }

        // Draw the move count and max moves
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.BLACK);
        g.drawString("Moves: " + (moveCount + 1) + " / " + maxMoves, 20, 30);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (int i = 0; i < 3; i++) {
            if (!rods.get(i).isEmpty()) {
                Disk topDisk = rods.get(i).peek();
                int rodX = 150 + i * 150;
                int rodY = 300 - (rods.get(i).size() * topDisk.height);
                int padding = 10;

                if (e.getX() >= rodX - topDisk.width / 2 - padding && e.getX() <= rodX + topDisk.width / 2 + padding &&
                    e.getY() >= rodY - topDisk.height - padding && e.getY() <= rodY + padding) {
                    
                    draggedDisk = rods.get(i).pop();
                    fromRod = i;
                    isDragging = true;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    repaint();
                    break;
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (draggedDisk != null && isDragging) {
            int toRod = getRodByPosition(e.getX());
            boolean validMove = false;

            if (toRod != -1) {
                if (rods.get(toRod).isEmpty() || rods.get(toRod).peek().width > draggedDisk.width) {
                    if (fromRod != toRod) {
                        rods.get(toRod).push(draggedDisk);
                        validMove = true;
                        incrementMoveCount();
                    } else {
                        rods.get(fromRod).push(draggedDisk);
                    }
                } else {
                    rods.get(fromRod).push(draggedDisk);
                }
            } else {
                rods.get(fromRod).push(draggedDisk);
            }

            if (validMove && moveCount >= maxMoves) {
                JOptionPane.showMessageDialog(this, "Maximum moves reached!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                resetGame();
            }

            draggedDisk = null;
            isDragging = false;
            repaint();

            if (rods.get(2).size() == numberOfDisks) {
                JOptionPane.showMessageDialog(this, "You Won!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                resetGame();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedDisk != null && isDragging) {
            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
        }
    }

    @Override
    protected void resetGame() {
        rods.clear();
        for(int i = 0;i<3;i++){
            rods.add(new Stack<>());
        }
        initializeDisks();
        moveCount = -1;
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private int getRodByPosition(int x) {
        if (x >= 100 && x <= 200) return 0;
        if (x >= 250 && x <= 350) return 1;
        if (x >= 400 && x <= 500) return 2;
        return -1;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tower of Hanoi");
        TowerOfHanoiGUI game = new TowerOfHanoiGUI();
        frame.add(game);
        frame.setSize(620, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

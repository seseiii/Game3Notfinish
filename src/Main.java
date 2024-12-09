import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Pac Man");

        // Make the frame visible
        PacMan pacmanGame = new PacMan(frame); // Pass the frame to PacMan
        frame.add(pacmanGame);
        pacmanGame.requestFocus();
        frame.setVisible(true);
    }
}

class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private final int rowCount = 21;
    private final int columnCount = 19;
    private final int tileSize = 32;

    private final Image wallImage;
    private final Image lordOzaiImage;
    private final Image zukoImage;
    private final Image princessAzulaImage;
    private final Image kuviraImage;
    private final Image goldCoinImage;

    private final Image avatarUpImage;
    private final Image avatarDownImage;
    private final Image avatarLeftImage;
    private final Image avatarRightImage;


    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "X       bpo       X",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    private Image backgroundImage;


    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    class StartScreen extends JDialog {
        public StartScreen(JFrame parent) {
            super(parent, true);

            setUndecorated(true);
            getRootPane().setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

            setSize(400, 380);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(false);
            setLayout(null);

            JPanel backgroundPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;

                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    try {
                        BufferedImage image = ImageIO.read(getClass().getResource("/fireMission.png"));
                        Image scaledImage = image.getScaledInstance(380, 310, Image.SCALE_SMOOTH);
                        g2.drawImage(scaledImage, 10, 10, null);
                    } catch (IOException e) {
                        g2.setColor(Color.RED);
                        g2.drawString("Failed to load background image", 10, 20);
                    }
                }
            };
            backgroundPanel.setBounds(0, 0, 400, 380);
            backgroundPanel.setLayout(null);
            backgroundPanel.setOpaque(false);
            add(backgroundPanel);

            JButton startButton = new JButton("Start");
            startButton.setFont(new Font("Arial", Font.BOLD, 14));
            startButton.setFocusPainted(false);
            startButton.setBackground(new Color(124, 15, 15));
            startButton.setForeground(Color.WHITE);
            startButton.setBounds((400 - 100) / 2, 330, 100, 40);

            startButton.setBorderPainted(false);
            startButton.setOpaque(true);

            Color originalColor = startButton.getBackground();
            Color hoverColor = originalColor.darker();

            startButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    startButton.setBackground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    startButton.setBackground(originalColor);
                }
            });

            startButton.addActionListener(e -> dispose());
            backgroundPanel.add(startButton);
        }
    }

    PacMan(JFrame frame) {
        // Use the provided frame for the game
        goldCoinImage = new ImageIcon("src/powerFood.png").getImage();
        backgroundImage = new ImageIcon("src/firebg.png").getImage();
        new StartScreen(frame).setVisible(true);

        setLayout(new BorderLayout());
        addKeyListener(this);
        setFocusable(true);

        frame.setLayout(new BorderLayout());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        frame.add(backgroundPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        wallImage = new ImageIcon(getClass().getResource("./wall.jpg")).getImage();
        lordOzaiImage = new ImageIcon(getClass().getResource("./lordOzai.png")).getImage();
        zukoImage = new ImageIcon(getClass().getResource("./zuko.png")).getImage();
        princessAzulaImage = new ImageIcon(getClass().getResource("./princessAzula.png")).getImage();
        kuviraImage = new ImageIcon(getClass().getResource("./kuvira.png")).getImage();

        avatarUpImage = new ImageIcon(getClass().getResource("./up.png")).getImage();
        avatarDownImage = new ImageIcon(getClass().getResource("./down.png")).getImage();
        avatarLeftImage = new ImageIcon(getClass().getResource("./left.png")).getImage();
        avatarRightImage = new ImageIcon(getClass().getResource("./right.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        gameLoop = new Timer(60, this); // 20fps
        gameLoop.start();

        frame.setVisible(true);
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(lordOzaiImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(zukoImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(princessAzulaImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(kuviraImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(avatarRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(goldCoinImage, x + 14, y + 14, 6, 6);
                    foods.add(food);
                }
            }
        }
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        int mapWidth = columnCount * tileSize;
        int mapHeight = rowCount * tileSize;
        int xOffset = (getWidth() - mapWidth) / 7;
        int yOffset = (getHeight() - mapHeight) / 7;

        g.drawImage(pacman.image, pacman.x + xOffset, pacman.y + yOffset, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x + xOffset, ghost.y + yOffset, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x + xOffset, wall.y + yOffset, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.drawImage(food.image, food.x + xOffset, food.y + yOffset, food.width, food.height, null);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize / 2, tileSize / 2);
        } else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize / 2, tileSize / 2);
        }

        if (score == 700) {
            JOptionPane.showMessageDialog(null,
                    "Mission Complete!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    ghost.updateDirection(directions[random.nextInt(4)]);
                }
            }
        }

        for (Block food : foods) {
            if (collision(food, pacman)) {
                foods.remove(food);
                score += 10;
                if (foods.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Level complete!", "You Win!", JOptionPane.INFORMATION_MESSAGE);
                    loadMap();
                }
                break;
            }
        }
    }
    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction == 'U') {
            pacman.image = avatarUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = avatarDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = avatarLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = avatarRightImage;
        }
    }


    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            backgroundImage = new ImageIcon("src/firebg.png").getImage();
            System.out.println("Backgroud panel test");
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

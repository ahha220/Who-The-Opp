import java.awt.event.KeyEvent;

/* This Detective class represents the player's character in the game. It keeps track of the detective's position (x, y) on the screen and the direction they are facing. The detective can move in response to keypresses, with each direction having a corresponding key.
 */
public class Detective {
	//Instance Variables
    private int x, y; // Position of the detective on the screen
    private char direction; // Direction the detective is facing (U = Up, D = Down, L = Left, R = Right)

    /* Method Description: This special method is called Detective being the constructor. This helps to initialize the detective's starting position and direction.
     * Parameters: int startX -> Starting X position of the detective on the screen.
     *     	       int startY -> Starting Y position of the detective on the screen.
     *             char startDirection -> The initial direction the detective is facing (e.g., 'U', 'D', 'L', 'R').
     * Return Type: None
     */
    public Detective(int startX, int startY, char startDirection) {
        this.x = startX;
        this.y = startY;
        this.direction = startDirection;
    }

    /* Setter method to update the detective's position.*/
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /* Setter method to update the direction the detective is facing. */
    public void setDirection(char directionFacing) {
        this.direction = directionFacing;
    }

    /* Method Description: This method called move essentially acts upon the key movements WASD. It adjusts and moves the detective based on the key pressed. The movement is associated with specific keys:
     * [W -> Move Up, S -> Move Down, A -> Move Left, D -> Move Right --> The detective's direction is updated accordingly.]
     * Parameters: int key -> The key code representing the key that was pressed (e.g., KeyEvent.VK_W).
     * Return Type: None
     */
    public void move(int key) {
        if (key == KeyEvent.VK_W) {
            y -= 15;  // Move up by 15 units
            direction = 'U';  // Set direction facing to 'Up'
        } else if (key == KeyEvent.VK_S) {
            y += 15;  // Move down by 15 units
            direction = 'D';  // Set direction facing to 'Down'
        } else if (key == KeyEvent.VK_A) {
            x -= 15;  // Move left by 15 units
            direction = 'L';  // Set direction facing to 'Left'
        } else if (key == KeyEvent.VK_D) {
            x += 15;  // Move right by 15 units
            direction = 'R';  // Set direction facing to 'Right'
        }
    }

    /* Getter method to retrieve the detective's current X position.*/
    public int getX() {
        return x;
    }

    /* Getter method to retrieve the detective's current Y position.*/
    public int getY() {
        return y;
    }

    /* Getter method to retrieve the current direction the detective is facing.*/
    public char getDirection() {
        return direction;
    }
}

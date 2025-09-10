import java.awt.event.KeyEvent;

/* This Item class helps creates an item in the game. Items are positioned on a map and have associated properties like a description, location, importance, and status (whether they have been inspected or not).
   This class also implements the comparable interface to help organize the items found in alpha order for better searching ability.
 */
public class Item implements Comparable<Item> {
    // Instance Variables
    private String name; // Name of the item
    private int x; // x position on the map
    private int y; // y position on the map
    private String description; // Description of the item (for dialogue/evidence)
    private String location; // Location where the item is found
    private boolean isInspected; // Indicates if the item has been inspected
    private int width; // Width of the item (used for interaction area)
    private int height; // Height of the item (used for interaction area)
    private int levelOfImportance; // Importance level of the item (used to rank items)
    private int collectedItemNum; // Tracks the number of times an item has been collected

    /* Method Description: This is the special method known as the constructor that initializes an item with its name, description, location, position, dimensions, and level of importance.
     * Parameters: String name --> Name of the item
     * 			   String description --> Description of the item
     *             String location --> Location where the item is found
     *             int x --> x-coordinate of the item on the map
     * 			   int y --> y-coordinate of the item on the map
     *  		   int width --> Width of the item (for interaction area)
     *             int height --> Height of the item (for interaction area)
     *             int levelOfImportance --> Importance level of the item
     */
    public Item(String name, String description, String location, int x, int y, int width, int height, int levelOfImportance) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.isInspected = false; // Initially, the item is not inspected
        this.levelOfImportance = levelOfImportance;
        this.collectedItemNum = 0;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Getter methods
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLevelOfImportance() {
        return levelOfImportance;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public boolean isInspected() {
        return isInspected;
    }

    /**
     * Marks the item as inspected.
     */
    public void inspectItem() {
        this.isInspected = true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /* Method DescriptionL This method called getDetails provides a formatted string containing details about the item. If the item has been inspected, it returns a message indicating that the user has already looked at it.
     * Parameters: None:
     * Return Type: String --> Formatted item details string.
     */
    public String getDetails() {
        if (!isInspected) {
            return "Item: " + name + "\n" + "Description: " + formatDescription(description, 6) + "\n";
        }
        return "You already looked at this item! Please refer to notes in your book!";
    }

    /* Method Description: This method called formatDescription helps to format the description of the item by breaking it into lines, ensuring no word is cut off in the middle.
     * Parameters: String text --> The description to format
     * 			   int maxLineLength --> The maximum number of characters per line
     * Return Type: A formatted description string
     */
    public String formatDescription(String text, int maxLineLength) {
        // Split the input text by "\n" to separate name and description
        String[] parts = text.split("\n", 2);

        StringBuilder formattedText = new StringBuilder();

        // Add the name on a new line
        if (parts.length > 0) {
            formattedText.append(parts[0]).append("\n");
        }

        // Now format the description (if it exists) by breaking it into lines based on the character limit
        if (parts.length > 1) {
            String description = parts[1];
            StringBuilder line = new StringBuilder();
            String[] words = description.split("\\s+"); // Split description into words

            for (String word : words) {
                // If adding this word to the line exceeds maxLineLength, start a new line
                if (line.length() + word.length() + 1 > maxLineLength) {
                    formattedText.append(line.toString()).append("\n");
                    line.setLength(0);  // Reset the line
                }

                // Add the word to the current line (add space if not the first word in the line)
                if (line.length() > 0) {
                    line.append(" ");
                }
                line.append(word);
            }

            // Add any remaining text in the last line
            if (line.length() > 0) {
                formattedText.append(line.toString());
            }
        }

        return formattedText.toString().trim();
    }

    /* Method Description: This method compares two items by their names in alphabetical order.
     * Parmaeter: Item other --> The other item to compare
     * Return Type: int --> A negative value if this item's name is lexicographically less than the other item's name, a positive value if greater, or 0 if they are equal. (using unicodes of characters)
     */
    @Override
    public int compareTo(Item other) {
        return this.name.toLowerCase().compareTo(other.name.toLowerCase());
    }

}

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
/* This class called CaseRoom is represents a case room in the detective game. Each room has a name, description, a list of items, suspects, and a time limit for solving the case. This is helpful in organizing the correct attributes associated to each room for proper set up.
 */
public class CaseRoom {
	//Instance Variables
    private String name; // Name of the room
    private int storyNum; // Story number in the building
    private String description; // Description of the room
    private TreeSet<Item> itemsInRoom; // Sorted set of items in the room
    private List<Suspect> suspects; // List of suspects in the room
    private Time timeLimit; // Time limit for solving the case in the room

    /* Method Description: This special method is the constructor that initializes a CaseRoom object with given values for the room's name, description, a list of items, suspects, time limit, and story number.
     * 
     * Parameters: String name -> The name of the room
     *             String description -> The description of the room
     *             List<Item> items -> A list of items present in the room
     *             List<Suspect> suspects -> A list of guilty suspects associated to the case
     *             Time timeLimit -> The time limit to solve the case in the room.
     *             int storyNum -> The story/case number in the game.
     * Return Type: None
     */
    public CaseRoom(String name, String description, List<Item> items, List<Suspect> suspects, Time timeLimit, int storyNum) {
        this.name = name;
        this.description = description;
        this.itemsInRoom = new TreeSet<>(new SortItemsByImportance());
        this.itemsInRoom.addAll(items); // Adding initial items to the room
        this.suspects = new ArrayList<>(suspects); // Copying the list of suspects
        this.timeLimit = timeLimit;
        this.storyNum = storyNum;
    }

    // Getter Methods
    public String getName() {
    	
        return name;
    }

    public int getStoryNum() {
        return storyNum;
    }

    public String getDescription() {
        return description;
    }

    public TreeSet<Item> getItems() {
        return itemsInRoom;
    }

    public List<Suspect> getSuspects() {
        return suspects;
    }

    public Time getTimeLimit() {
        return timeLimit;
    }
    
    /* Method Description: This method called toString() returns a string representation of the CaseRoom, including its name and description. This is typically used for displaying room details in the detective's notebook.
     * Parameters: None
     * Return Type: String -> A string containing the name and description of the room.
     */    
    public String toString() {
        // Return the name and description of the room, along with the list of items and suspects for better clarity
        String roomNameNoCameoCasing = "";
    	if (name.equalsIgnoreCase("wongMansion")) {
    		roomNameNoCameoCasing = "Wong Mansion";
        }
    	else {
    		roomNameNoCameoCasing = "Library";
    	}
    	return "Room: " + roomNameNoCameoCasing + " Description: " + description;
    }
}

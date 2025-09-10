import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/* The TopSecretBook class is designed to manage the key elements of a case in a mystery or detective game. It contains a list of collected items, the case details to provide context for the investigation, and a set of suspects involved in the case.
 *  The class provides methods to access the collected items and suspects, facilitating the game mechanics related to item collection and suspect tracking.
 */
public class TopSecretBook {
	//Instance Variables
    private List<Item> itemsFound; // List of collected items
    private String storyLine;     // Background story or case details
    private TreeSet<Suspect> suspects; // Suspects related to the case

    /* Method Description: This is the special method called the constructor that initializes a TopSecretBook with a given story and a set of suspects. It also initializes the list of found items as an empty list.
     * Parameters: String story -> The background story or case details.
     *             TreeSet<Suspect> suspects -> The set of suspects related to the case.
     * Return Type: None
     */
    public TopSecretBook(String story, TreeSet<Suspect> suspects) {
        this.itemsFound = new ArrayList<>();  // Initialize the itemsFound list
        this.storyLine = story;
        this.suspects = suspects;
    }

    /* Getter method to retrieve the list of items found in the case.
     */
    public List<Item> getItemsFound() {
        return itemsFound; // Return the List of items found
    }

    /* Getter method to retrieve the set of suspects related to the case for organization when displaying in notebook.
     */
    public TreeSet<Suspect> getSuspects() {
        return suspects;
    }
}

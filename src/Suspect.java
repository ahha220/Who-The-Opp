/* The Suspect class represents a suspect in the investigation. It holds the suspect's information, including their name, guilty status, and location. This class also implements the comparable interface to help sort the suspects in alpha order for better organization.
 */
public class Suspect implements Comparable<Suspect> {
	//Instance Variables
    private String name;         // Name of the suspect
    private boolean isCulprit;   // Indicates if the suspect is guilty (True -> guilty, False -> innocent)
    private String location;     // Last known location of the suspect (e.g., library, mansion)

    /* Method Description: This special method is the constructor method. It initializes the suspect with the given name, alibi, dialogue, guilt status, and location.
     * Parameters: String name -> The name of the suspect.
     *     		   boolean isCulprit -> True if the suspect is guilty, false if not.
     *     		   String location -> The suspect's last known location.
     * Return Type: None
     */
    public Suspect(String name, boolean isCulprit, String location) {
        this.name = name;
        this.isCulprit = isCulprit;
        this.location = location;
    }

    // Getter Methods
    public String getName() {
        return name; // Returns the name of the suspect
    }

    public boolean isCulprit() {
        return isCulprit; // Returns true if the suspect is guilty
    }

    public String getLocation() {
        return location; // Returns the location of the suspect
    }

    /* Method Description: This method called compareTo compares this suspect's name with another suspect's name alphabetically for sorting purposes better organization.
     * Parameters: Suspect other -> The other suspect to compare with.
     * Return Type: int -> A negative value if this suspect's name is lexicographically less than the other suspect's name,  a positive value if greater, or 0 if they are equal. (using the unicovde values of characters)
     */
    public int compareTo(Suspect other) {
        return this.name.compareToIgnoreCase(other.name); // Compares names case-insensitively
    }

    /* Method Description: This method is the toString method that provides a string representation of the suspect's name for display or debugging purposes.
     * Parameters: None
     * Return Type: String -> The name of the suspect.
     */
    public String toString() {
        return name; // Returns the name of the suspect
    }
}

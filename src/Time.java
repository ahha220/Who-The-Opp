/* This class is the Time class. It is used to create Time objects specifically for the duration of each story. (aka the time limit for each story)
   This class also implements the Comparable interface, allowing comparisons between the player's high score to solve the room in the past vs if they got the right suspect the time it took for them to solve.
*/
public class Time implements Comparable<Time> {
	//Instance Variables
    private int minutes; // # of minutes
    private int seconds; // # of seconds

    /* Method Description: This is a special method called Time which is the constructor. It initializes a Time object with specified minutes and seconds.
     * Parameters: int minutes --> the number of minutes 
     * 			   int seconds --> the number of seconds
     */
    public Time(int minutes, int seconds) {
        this.minutes = minutes;
        this.seconds = seconds;
    }

    //Getters
    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    //Setters
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    /* Method Description: This method called compareTo compares the current TIme object with another (typically between the current time left vs past highscore from txt file. It helps to determine if the detective was faster than his/her previous time remaning on the map. 
     * Parameter: Time other --> this is the other Time object we are comparing with 
     * Return type: int --> a negative integer if this time is greater, zero if equal, or a positive integer if less
     */
    public int compareTo(Time other) {
    	//convert time to total seconds!
        int totalSecondsSelf = this.minutes * 60 + this.seconds;
        int totalSecondsOther = other.getMinutes() * 60 + other.getSeconds();
        // Reverse comparison to make the larger time the higher score (because we are looking at time duration how much time was remaining)
        return Integer.compare(totalSecondsOther, totalSecondsSelf);
    }

    /* Method Description: This method called durationInMap calculates the remaining time they had left after subtracting the countdown time.
     * Parameters: mapTime the time set on the map
     * Return Type: Time --> a Time object representing the remaining time duration
     */
    public Time durationInMap(Time mapTime) {
        int mapTotalSeconds = mapTime.getMinutes() * 60 + mapTime.getSeconds();
        int countdownTotalSeconds = this.minutes * 60 + this.seconds;  // Assuming 'this' refers to the current countdown time
        int resultSeconds = mapTotalSeconds - countdownTotalSeconds;
        //Convert total seconds back into minutes and seconds
        int resultMinutes = resultSeconds / 60;
        resultSeconds = resultSeconds % 60;
        return new Time(resultMinutes, resultSeconds);
    }

    /* Method Description: This method called subtractTime helps to run the map's time limit. It subtracts a specified amount of time (in seconds) from the current Time object.
     * Parameters: int timeToSubstract --> the nuber of seconds to substract from the current time
     * Return Type: void
     */
    public void subtractTime(int timeToSubtract) {
    	// convert minutes to seconds then subtract
        int totalSeconds = this.minutes * 60 + this.seconds - timeToSubtract;
        if (totalSeconds < 0) {
            this.minutes = 0;
            this.seconds = 0;
        } else {
            this.minutes = totalSeconds / 60;
            this.seconds = totalSeconds % 60;
        }
    }

    /* Method Description: This method called toString is a string representation of the Time object in the format: X min Y secs
     * Parameters: none
     * Return Type: String --> string of the time in minutes and seconds
     */
    public String toString() {
        return minutes + " min  " + seconds + " secs";
    }
}

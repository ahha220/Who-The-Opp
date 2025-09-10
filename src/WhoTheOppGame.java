import javax.swing.*;
import javax.swing.Timer;
import java.util.*;
import java.util.List;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

/* Names: Sarina Hum & Amy Huang | Date: Sunday, January 19, 2025
 * Welcome to WHO THE OPP! 
 * Description: This a single player mystery, detective, adventure game called "Who the OPP?" which is an inspiration from the storyline games Omori and Sherlock Holmes. 
 * The objective of the game is to be the investigator, be the detective to figure out who the guilty person is for each case being solved. The best bet is to use the hints 
 * around the room but remember you are limited in the Time you are allowed to investigate before someone takes over. Be smart and think carefully! (refer to more notes on Readme.txt)
 */

public class WhoTheOppGame extends JPanel implements KeyListener, ActionListener, MouseListener {
	// Global Variables`
	// GUI Components
	static JFrame frame;
	private JLabel gifLabel; // To display GIFs
	private JLabel selectedSuspectImageLabel = new JLabel(); // To display the selected suspect image
	private JLabel imageLabel; // For displaying images in secretBook
	private JButton submitButton; // Submit button
	private JButton report; // Report button

	// Timers
	private Timer gifTimer; // For GIF transitions
	private Timer timer; // General-purpose timer
	private Timer countdownTimer; // Timer for countdowns
	private Timer textTimer; // Timer for word-by-word text display

	// Gameplay State
	boolean finishedGame = false; // Tracks if the game is finished
	boolean finishedConferenceRoom = false; // Tracks if conference room gameplay is complete
	boolean isInWongMansion = false; // Tracks if the player is in Wong Mansion
	boolean isInLibrary = false; // Tracks if the player is in the library
	boolean clicked = false; // Tracks if an interactable object was clicked
	boolean submitted = false; // Tracks if a suspect report has been submitted
	boolean skipMessage = false; // Tracks if the user wants to skip the message display

	// Game Data
	int count = 0; // Counter for general use
	int storyNum = 0; // Current story number

	// Detective
	Detective detective; // The main character
	Image cursorImage; // Custom cursor image

	// Locations and Bounds
	Rectangle wongMansionBounds = new Rectangle(235, 298, 20, 18); // Bounds for Wong Mansion
	Rectangle libraryBounds = new Rectangle(570, 385, 50, 20); // Bounds for Library
	Rectangle conferenceBounds = new Rectangle(422, 130, 50, 30); // Bounds for Conference Room

	// Items and Suspects
	ArrayList<Item> items = new ArrayList<>(); // List of items
	Item currentItem; // The currently selected item
	List<Suspect> suspects = new ArrayList<>(); // List of suspects
	List<CaseRoom> caseRooms = new ArrayList<>(); // List of case rooms

	// Textbox and Messages
	List<String> message2 = new ArrayList<>(); // Message lines for word-by-word display
	int wordIndex = 0; // Current word index for text display

	// Secret Book and Search
	private ArrayList<TopSecretBook> topSecretBook = new ArrayList<>(); // Collection of TopSecretBooks
	private Map<String, String> searchCorrespondingItemImage = new HashMap<>(); // Maps items to specific hint images
	private Map<String, String> searchForValidItem = new HashMap<>(); // Maps search terms to file names (secretBook)
	String searchItem = ""; // Tracks the current search item in secretBook

	// Suspect Report
	String selectedSuspect = ""; // Tracks the currently selected suspect
	String correctSuspect = ""; // The correct suspect for the current room

	// Time Management
	Time countdownTime = new Time(100, 0); // Countdown time for gameplay

	// Current Screen/State
	String methodName = "titleScreen"; // Tracks the current screen/method

	//ADD METHOD COMMENTS
	public void initializeTimer() {
		if (textTimer != null && textTimer.isRunning()) {
			textTimer.stop(); // Stop existing timer to avoid overlapping
		}
		textTimer = new Timer(300, e -> {
			wordIndex++;
			if (wordIndex >= message2.size()) {
				textTimer.stop();
				wordIndex = message2.size() - 1; // Prevent overflow
			}
			repaint(); // Update screen to reflect the message
		});
	}

	public void autoDialogue(){
		// Changes the boolean to know the screen is clicked to paint full dialogue on screen and stops timer
		skipMessage = true;
		textTimer.stop();
	}
	/* Method Description: This method is the special method known as the constructor for the Who the OPP class. It initializes the game environment, including the detective, items, suspects, case rooms, and event listeners. Also sets up the custom cursor, title screen, and a countdown timer for certain locations.
	 * Parameters: None
	 * Return Type: None
	 */
	public WhoTheOppGame() {
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addKeyListener(this);

		initializeSearchToFileMap();

		// Cursor Display
		cursorImage = Toolkit.getDefaultToolkit().getImage("detectiveHand.png");
		Point hotspot = new Point(0, 0);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Cursor cursor = toolkit.createCustomCursor(cursorImage, hotspot, "detectiveHand.png");
		setCursor(cursor);

		// Initialize Detective
		detective = new Detective(20, 150, 'R');

		// Initialize Items, Suspects, and Case Rooms read in files in background
		initializeItems("items.txt");
		initializeSuspects("suspect.txt");
		initializeCaseRooms("caseInfo.txt");

		// Title Screen
		titleScreen();

		// Add Mouse Listener for Item Interaction
				addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						if(methodName.equalsIgnoreCase("Conferenceroom1") && !skipMessage){
							// If this is the first time the screen is clicked, it skips the dialogue
							autoDialogue();
							// Repaints the screen with text
							conferenceRoom1();

						} else if (methodName.equalsIgnoreCase("ConferenceRoom1") && skipMessage) {
							// If this is the second time the screen is clicked, it changes screens
							conferenceRoom1Suspects();
							skipMessage = false;
						} else if(methodName.equalsIgnoreCase("ConferenceRoom1Suspects")){
							// This goes to a screen depending on what icon is clicked
							if(e.getButton() == 1 && e.getX() > 70 && e.getX() < 200 && e.getY() > 435 && e.getY() < 510){
								conferenceRoom1Wong();
							} else if(e.getButton() == 1 && e.getX() > 265 && e.getX() < 450 && e.getY() > 435 && e.getY() < 500){
								conferenceRoom1Suki();
							} else if(e.getButton() == 1 && e.getX() > 485 && e.getX() < 700 && e.getY() > 435 && e.getY() < 490){
								conferenceRoom1Brothers();
							} else if(e.getButton() == 1 && e.getX() > 600  && e.getX() < 745 && e.getY() > 0 && e.getY() < 20){
								finishedConferenceRoom = true;
								transportationMapScreen();
							}
						}
						else if(((methodName.equalsIgnoreCase("conferenceroomWong") || methodName.equalsIgnoreCase("conferenceroomsuki") || methodName.equalsIgnoreCase("conferenceroomBrothers")) && e.getX() > 630 && e.getX() < 685 && e.getY() > 0 && e.getY() < 70 )){
							skipMessage = false;
							conferenceRoom1Suspects();
						} else if(methodName.equalsIgnoreCase("conferenceroomWong") && !skipMessage){
							autoDialogue();
							conferenceRoom1Wong();
						}
						else if(methodName.equalsIgnoreCase("conferenceroomsuki") && !skipMessage){
							autoDialogue();
							conferenceRoom1Suki();
						}
						else if(methodName.equalsIgnoreCase("conferenceroombrothers") && !skipMessage){
							autoDialogue();
							conferenceRoom1Brothers();
						}
						else if(methodName.equalsIgnoreCase("ConferenceRoom2") && !skipMessage){
							autoDialogue();
							conferenceRoom2();
						}else if(methodName.equalsIgnoreCase("ConferenceRoom2") && skipMessage){
							skipMessage = false;
							conferenceRoom2Suspects();
						}
						else if(methodName.equalsIgnoreCase("conferenceroom2suspects")){
							conferenceRoom2SuspectAlibi();
						}else if(methodName.equalsIgnoreCase("conferenceroom2alibis") && !skipMessage){
							
							autoDialogue();
							conferenceRoom2SuspectAlibi();
						}
						else if(methodName.equalsIgnoreCase("conferenceroom2alibis")&& e.getButton() == 1 && e.getX() > 600  && e.getX() < 745 && e.getY() > 0 && e.getY() < 20){
							finishedConferenceRoom = true;
							skipMessage = false;
							transportationMapScreen();
						}
				// Other room-specific item interactions 
				for (Item item : items) {
					// Calculate the bounds of the item
					Rectangle itemBounds = new Rectangle(item.getX(), item.getY(), item.getWidth(), item.getHeight());

					// Get the position of the detective
					int detectiveX = detective.getX();
					int detectiveY = detective.getY();

					// Check if the detective is within 50 pixels of the item (both horizontally and vertically)
					if (itemBounds.contains(e.getPoint()) && methodName.equalsIgnoreCase(item.getLocation()) &&
							Math.abs(detectiveX - item.getX()) <= 110 && Math.abs(detectiveY - item.getY()) <= 110) {
						currentItem = item;
						Collections.sort(topSecretBook.get(storyNum-1).getItemsFound());
						int index = Collections.binarySearch(topSecretBook.get(storyNum-1).getItemsFound(), new Item(currentItem.getName(), "", "", 0, 0, 0, 0, 0));

						if (index < 0) { // Found the item
							topSecretBook.get(storyNum-1).getItemsFound().add(item);  // Add to collected items list
						}
						clicked = true;
						repaint();
						break;
					}
				}

				// Check for proximity to the secret book
				if ((methodName.equalsIgnoreCase("wongMansion") && e.getPoint().getX() >= 580 && e.getPoint().getY() >= 450) || (methodName.equalsIgnoreCase("library") && e.getPoint().getX() >= 670 && e.getPoint().getX() <= 740 && e.getPoint().getY() >= 400 && e.getPoint().getY() <= 500)) {
					secretBook();
				}
			}
		});

		// Countdown Timer
		countdownTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isInWongMansion || isInLibrary) {
					countdownTime.subtractTime(1);

					// Stop the timer if time is up
					if (countdownTime.getMinutes() == 0 && countdownTime.getSeconds() == 0) {
						countdownTimer.stop();
						timeIsUpAction(); // Trigger action when time is up
					}
					repaint();
				}
			}
		});
	}

	/* Method Description: This method called intializesSearchToFileMap helps to map item names to their file name for easier navigation and calling to display item images.
	 * Parameters: none
	 * Return Type: void
	 */
	private void initializeSearchToFileMap() {
		//WongMansion Items --> FileName
		searchCorrespondingItemImage.put("cat toys", "catToys");
		searchCorrespondingItemImage.put("calendar", "calendarContent");
		searchCorrespondingItemImage.put("sports balls", "sportsBalls");
		searchCorrespondingItemImage.put("socks", "socks");
		searchCorrespondingItemImage.put("thermometer", "thermometer");
		searchCorrespondingItemImage.put("video games", "videoPic");
		searchCorrespondingItemImage.put("trash", "trashPic");
		searchCorrespondingItemImage.put("picture", "pictureHint");
		searchCorrespondingItemImage.put("plant", "plantPic");
		searchCorrespondingItemImage.put("jigglypuff", "jigglypuff");
		searchCorrespondingItemImage.put("broken plates", "brokenPlateNail");
		searchCorrespondingItemImage.put("lettuce", "lettuce");

		//Library Items --> FileName
		searchCorrespondingItemImage.put("receipt","receiptOpen");
		searchCorrespondingItemImage.put("phone", "phonepics"); 
		searchCorrespondingItemImage.put("arsenic", "arsenic");
		searchCorrespondingItemImage.put("fingerprint", "fingerprint");
		searchCorrespondingItemImage.put("purple juice", "bloodPattern");
		searchCorrespondingItemImage.put("book", "bookInfo");
		searchCorrespondingItemImage.put("notebook", "notebookletter");
	}

	/* Method Description: This method called intializeCaseRoom sets up the CaseRoom objects from a specified file called caseInfo.txt. Each CaseRoom contains a name, description, story number, time limit, associated items, and suspects. It also creates the TopSecretBook for each CaseRoom containing the description and suspects.
	 * Parameteres: String filename --> the name of the file containing the CaseRoom details
	 * The file format should follow:
	 * - Room name (String)
	 * - Story number (int)
	 * - Room description (String)
	 * - Time limit in minutes (int)
	 * - (repeats for each room)
	 * Return Type: void
	 */
	public void initializeCaseRooms(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				String name = line.trim(); // Room name
				int storyNum = Integer.parseInt(br.readLine().trim()); // Story number
				String description = br.readLine().trim(); // Description of the room
				int timeLimit = Integer.parseInt(br.readLine().trim()); // Time limit in minutes

				// Collect items for the room (items should match the room's name)
				List<Item> itemsInRoom = new ArrayList<>();
				for (Item item : items) {
					if (item.getLocation().equalsIgnoreCase(name)) {
						itemsInRoom.add(item);
					}
				}

				// Collect suspects for the room (suspects should match the room's name)
				List<Suspect> suspectsInRoom = new ArrayList<>();
				for (Suspect suspect : suspects) {
					if (suspect.getLocation().equalsIgnoreCase(name)) {
						suspectsInRoom.add(suspect);
					}
				}

				// Create and add the CaseRoom
				CaseRoom caseRoom = new CaseRoom(name, description, itemsInRoom, suspectsInRoom, new Time(timeLimit,0), storyNum);
				caseRooms.add(caseRoom);

				// Create a TopSecretBook for this case room
				topSecretBook.add(new TopSecretBook (description, new TreeSet<>(suspectsInRoom)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Method Description: This method called initializeSuspects sets up the Suspect objects from a specified file called suspect.txt. Each Suspect contains a name, guilt status, and last known location.
	 * Parameters: String filename --> the name of the file containing the Suspect details
	 * The file format should follow:
	 * - Suspect's name (String)
	 * - Is guilty (boolean)
	 * - Last known location (String)
	 * - (repeats for each suspect)
	 * Return Type: void
	 */
	public void initializeSuspects(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				String name = line.trim(); // Suspect's name
				boolean isCulprit = Boolean.parseBoolean(br.readLine().trim()); // Is the suspect guilty?
				String location = br.readLine().trim(); // Last known location (e.g., library or wongMansion)
				// Create a Suspect object
				Suspect suspect = new Suspect(name,isCulprit, location);
				suspects.add(suspect); //adds to list
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Method Description: This method called initializeItems sets up the Item objects from a specified file. Each Item contains a name, description, location, position, dimensions, and importance level.
	 * Parameters: String filename --> the name of the file containing the Item details
	 * The file format should follow:
	 * - Item name (String)
	 * - Item description (String)
	 * - Location (String)
	 * - X position (int)
	 * - Y position (int)
	 * - Width (int)
	 * - Height (int)
	 * - Level of importance (int)
	 * - (repeats for each item)
	 */
	public void initializeItems(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				// Read the item data in groups of 8 lines
				String itemName = line.trim();
				String description = br.readLine().trim();
				String location = br.readLine().trim();
				int xPos = Integer.parseInt(br.readLine().trim());
				int yPos = Integer.parseInt(br.readLine().trim());
				int width = Integer.parseInt(br.readLine().trim());
				int height = Integer.parseInt(br.readLine().trim());
				int levelOfImportance = Integer.parseInt(br.readLine().trim());

				// Create the Item and add it to the list
				Item item = new Item(itemName, description, location, xPos, yPos, width, height, levelOfImportance);
				items.add(item);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.err.println("Error parsing number from file: " + e.getMessage());
		}
	}

	//ADD METHOD COMMENTS
	public void renderMessage(Graphics g, int x, int y) {
		int wordsPerLine = 12; // Maximum words per line
		int linesToDisplay = (wordIndex / wordsPerLine) + 1;
		int currentY = y;

		for(int i = 0; i < linesToDisplay; i++){
			int startIndex = i *wordsPerLine;
			int endIndex = Math.min((i+1)*wordsPerLine, wordIndex);

			if(startIndex < wordIndex){
				String line = String.join(" ", message2.subList(startIndex, endIndex + 1));
				g.drawString(line, x, currentY);
				currentY += g.getFontMetrics().getHeight();
			}

		}

	}
	public void startMessageTimer(String newMessage) {
		if (message2 == null || !newMessage.equals(String.join(" ", message2))) {
			message2 = Arrays.asList(newMessage.split(" "));
			wordIndex = 0;
			initializeTimer();
			textTimer.start(); // Start the timer for new message
		}
	}


	//Few of these methods below are specifically for the case room to avoid repetitiveness.
	/* Method Description: This method called drawRoomBackground draws the background image of the specified room.
	 * Parameters: Graphisc g --> the Graphics object used for drawing.
	 *             String roomName the name of the room (e.g., "wongMansion" or "library").
	 * Return type: void
	 */
	private void drawRoomBackground(Graphics g, String roomName) {
	    g.drawImage(new ImageIcon(roomName + ".png").getImage(), 0, 0, getWidth(), getHeight(), this);
	}

	/* Method Description: This method called drawRoomItems draws all the items located in the specified room.
	 * Parameters: Graphics g --> the Graphics object used for drawing.
	 * 			   String roomName --> the name of the room (e.g., "wongMansion" or "library").
	 * Return type: void
	 */
	private void drawRoomItems(Graphics g, String roomName) {
	    int totalItems = 0;
	    int collectedCount = 0;

	    for (Item item : items) {
	        if (item.getLocation().equalsIgnoreCase(roomName)) {
	            totalItems++;
	            if (topSecretBook.get(storyNum - 1).getItemsFound().contains(item)) {
	                collectedCount++;
	            } else {
	                ImageIcon itemIcon = new ImageIcon(item.getName().toLowerCase() + ".png");
	                g.drawImage(itemIcon.getImage(), item.getX(), item.getY(), item.getWidth(), item.getHeight(), this);
	            }
	        }
	    }
	}

	/* Method Description: This method called drawDetective draws the detective's icon on the screen.
	 * Parameters: Graphics g --> the Graphics object used for drawing.
	 * Return type: void
	 */
	private void drawDetective(Graphics g) {
	    ImageIcon detectiveIcon = getDetectiveIcon();
	    g.drawImage(detectiveIcon.getImage(), detective.getX(), detective.getY(), 80, 80, null);
	}

	/* Method Description: This method called drawCountdownTwiner draws the countdown timer for the specified room.
	 * Parameters: Graphics g --> the Graphics object used for drawing.
	 * 		       boolean isInRoom --> a boolean indicating whether the player is in the room.
	 * 			   String roomName --> the name of the room (e.g., "wongMansion" or "library").
	 * Return type: void
	 */
	private void drawCountdownTimer(Graphics g, boolean isInRoom, String roomName) {
	    if (isInRoom) {
	        String timeText = String.format("%02d:%02d", countdownTime.getMinutes(), countdownTime.getSeconds());
	        g.setColor(Color.WHITE);
	        g.setFont(new Font("American Typewriter", Font.BOLD, 20));

	        if (roomName.equals("wongMansion")) {
	            g.drawString("Time: " + timeText, 92, 290);
	        } else if (roomName.equals("library")) {
	            g.drawString("Time: " + timeText, 460, 90);
	        }
	    }
	}

	/* Method Description: This method called drawCollectedItemsCount draws the count of collected out of the total items for the specified room.
	 * Parameters: Graphics g --> the Graphics object used for drawing.
	 * 			   String roomName --> the name of the room (e.g., "wongMansion" or "library").
	 * Return Type: void
	 */
	private void drawCollectedItemsCount(Graphics g, String roomName) {
	    int totalItems = 0; 
	    int collectedCount = 0;

	    for (Item item : items) {
	        if (item.getLocation().equalsIgnoreCase(roomName)) {
	            totalItems++;
	            if (topSecretBook.get(storyNum - 1).getItemsFound().contains(item)) {
	                collectedCount++;
	            }
	        }
	    }

	    g.setColor(Color.WHITE);
	    g.setFont(new Font("American Typewriter", Font.BOLD, 25));
	    if (roomName.equals("wongMansion")) {
	        g.drawString("Collected", getWidth() - 140, 80);
	        g.drawString(collectedCount + "/" + totalItems, getWidth() - 140, 110);
	        g.drawString("Items", getWidth() - 140, 140);
	    } else if (roomName.equals("library")) {
	        g.drawString("Collected: " + collectedCount + "/" + totalItems + " Items", 520, 550);
	    }
	}

	/* Method Description: This method called drawItemInspectionScreen draws the item inspection screen with details for the selected item.
	 * Parameters: Graphics g --> the Graphics object used for drawing
	 * Return Type: void
	 */
	private void drawItemInspectionScreen(Graphics g) {
	    remove(report); //remove report button
	    g.drawImage(new ImageIcon("page.png").getImage(), 0, 0, getWidth(), getHeight(), this); //draw the page
	    bookBackButtons(new ImageIcon("bookButton.png"), 0, 0, 60, 60); //draw the back button

	    String imageName = searchCorrespondingItemImage.get(currentItem.getName().toLowerCase());
	    if (imageName != null) {
	        g.drawImage(new ImageIcon(imageName + ".png").getImage(), 90, 170, 300, 200, this);
	    }

	    String formattedDetails = currentItem.formatDescription(currentItem.getDetails(), 40); //display information about item
	    String[] lines = formattedDetails.split("\n");
	    int x = getWidth() - 380;
	    int y = getHeight() - 400;
	    int lineHeight = 30;

	    Font font = new Font("American Typewriter", Font.BOLD, 13);
	    g.setFont(font);

	    for (String line : lines) {
	        g.drawString(line, x, y);
	        y += lineHeight;
	    }
	}

	/* Method Description: This method called paintComponent is responsible for painting the components on the screen. The method uses the Graphics object to render the visual elements (such as images, text, and other graphics) needed to be displayed depending on what is happening.
	 * Parameters: Graphics g -->  The Graphics object used for drawing. It provides methods for rendering images, shapes, and text on the component.
	 * Return Type: void--> It performs the drawing operation directly.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (methodName.equalsIgnoreCase("titleScreen")) { //Title Screen
			g.drawImage(new ImageIcon("title.png").getImage(), 0, 0, getWidth(), getHeight(), this);
		} else if (methodName.equalsIgnoreCase("creditsScreen")) { //Credits Screen
			g.drawImage(new ImageIcon("credits.png").getImage(), 0, 0, getWidth(), getHeight(), this);
		} else if (methodName.equalsIgnoreCase("menuPg2Screen")) { //menuPg2 Screen
			g.drawImage(new ImageIcon("menuPg2.png").getImage(), 0, 0, getWidth(), getHeight(), this);
		} 
		else if (methodName.equalsIgnoreCase("menuScreen")) { //menuScreen
			g.drawImage(new ImageIcon("menu.png").getImage(), 0, 0, getWidth(), getHeight(), this);
		} 
		else if (methodName.equalsIgnoreCase("transportationMapScreen")) {

			// Draw Wong's Mansion
			g.setColor(Color.RED);
			g.fillRect(wongMansionBounds.x, wongMansionBounds.y, wongMansionBounds.width, wongMansionBounds.height);

			//Draw Library
			g.setColor(Color.RED);
			g.fillRect(libraryBounds.x, libraryBounds.y, libraryBounds.width, libraryBounds.height);

			// Draw Conference
			g.setColor(Color.RED);
			g.fillRect(conferenceBounds.x, conferenceBounds.y, conferenceBounds.width, conferenceBounds.height);
			g.drawImage(new ImageIcon("transportationMap.png").getImage(), 0, 0, getWidth(), getHeight(), this);
			createButton("bookButton.png", 10, 10, 30, 30, "back");
			// Draw the detective at the current position
			ImageIcon detectiveIcon = getDetectiveIcon();
			// add one for conference room
			if (storyNum == 1) {// && is completedLevel
				//set tehre postion outside the house
			} else if (storyNum == 2) { //&& is completedLevel
				// all cases solved streen don't need to step placement outside wongHouseset congradulation screen have a home button (more cases coming your way)
			}
			g.drawImage(detectiveIcon.getImage(), detective.getX(), detective.getY(), 80, 80, null);
			// Depending on what the player chooses, the arrow and dialogue will change
			// If a game has been completed, it means the player has to go back to the conference room
			if(finishedGame || storyNum == 0){
				startMessageTimer("GO TO THE CONFERENCE ROOM");
				g.drawImage(new ImageIcon("Arrow.png").getImage(), 415, 70, getWidth()-750, getHeight()-520, this);
			}
			// If the player chooses case 1, they have to go to the library
			else if(!finishedGame && storyNum == 1){
				startMessageTimer("GO TO THE LIBRARY");
				g.drawImage(new ImageIcon("Arrow.png").getImage(), 562, 297, getWidth()-750, getHeight()-520, this);
			}
			// If the player chooses case 2, they have to go to the Wong Mansion
			else if(!finishedGame && storyNum == 2){
				startMessageTimer("GO TO THE WONG MANSION");
				g.drawImage(new ImageIcon("Arrow.png").getImage(), 235, 240, getWidth()-750, getHeight()-520, this);
			}
			// Draw the word-by-word message near the top-left corner
			g.setColor(Color.WHITE);  // Set text color
			g.setFont(new Font("American Typewriter", Font.BOLD, 20));  // Set font size and type
			g.setFont(new Font("American Typewriter", Font.BOLD, 20));
			renderMessage(g, 50, 35); // Draw message
		}  else if (methodName.equalsIgnoreCase("wongMansion") || methodName.equalsIgnoreCase("library")) {
			String roomName;
			boolean isInRoom;

			if (methodName.equalsIgnoreCase("wongMansion")) {
				roomName = "wongMansion";
				isInRoom = isInWongMansion;
			} else {
				roomName = "library";
				isInRoom = isInLibrary;
			}

			if (!clicked || currentItem == null) {
				drawRoomBackground(g, roomName);
				drawRoomItems(g, roomName);
				drawDetective(g);
				drawCountdownTimer(g, isInRoom, roomName);
				drawCollectedItemsCount(g, roomName);
			} else if (!currentItem.isInspected()) {
				drawItemInspectionScreen(g);
			}
		} 	else if (methodName.equalsIgnoreCase("conferenceRoom")) {

			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), this);
			startMessageTimer("PICK THE CASE YOU WANT TO SOLVE");
			g.setColor(Color.WHITE);  // Set text color

			g.setFont(new Font("American Typewriter", Font.BOLD, 30));
			renderMessage(g, 95, 60);

		} else if (methodName.equalsIgnoreCase("conferenceRoom1")) {
			storyNum = 2;
			// Draw Images and Texts
			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), null);
			g.setFont(new Font("American Typewriter", Font.BOLD, 20));  // Set font size and type
			g.setColor(Color.WHITE);  // Set text color
			g.drawString("Click to skip dialogue", 80, 70);
			g.drawString("Click again to continue", 480,70);

			// If the user clicks the screen, the text automatically displays
			// Each line has 12 words
			// If not, the dialogue displays word by word
			if(skipMessage){
				g.setFont(new Font("American Typewriter", Font.PLAIN, 20));  // Set font size and type
				g.setColor(Color.WHITE);  // Set text color
				g.drawString("Ms. Wong's husband came home from a long day at work on",80, 450);
				g.drawString("a Saturday. When he got home, he discovered a huge mess in",80, 475);
				g.drawString("the house! Before going to work, the house was still clean...He",80, 500);
				g.drawString("wants to find out who made the mess and is hiring you!",80, 525);

			}else{
				startMessageTimer("Ms. Wong's husband came home from a long day at work on a Saturday. When he got home, he discovered a huge mess in the house! Before going to work the house was still clean...He wants to find out who made the mess and is hiring you!");
				g.setColor(Color.WHITE);  // Set text color
				g.setFont(new Font("American Typewriter", Font.PLAIN, 20));  // Set font size and type
				renderMessage(g, 80, 450);
			}


		}
		else if(methodName.equalsIgnoreCase("conferenceroom1suspects")){
			// Draw the images
			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), null);
			g.drawImage(new ImageIcon("mari.png").getImage(), 70, 435, getWidth()-720, getHeight()-480, null);
			g.drawImage(new ImageIcon("suki.png").getImage(), 265, 435, getWidth()-720, getHeight()-490, null);
			g.drawImage(new ImageIcon("brother.png").getImage(), 475, 435, getWidth()-720, getHeight()-480, null);
			g.drawImage(new ImageIcon("brother.png").getImage(), 535, 435, getWidth()-720, getHeight()-480, null);
			g.setFont(new Font("American Typewriter", Font.BOLD, 20));

			g.setColor(Color.white);
			g.drawString("Ms. Wong", 150,500);
			g.drawString("Suki the Cat", 338,500);
			g.drawString("Brother Duo", 605,500);
			//g.drawRect(700,10,200,10);
			g.drawString("Ready to Play", 620,20);
			g.drawString("Click on the suspect to interview them", 20, 20);

			startMessageTimer("LET'S MEET THE SUSPECTS");
			g.setColor(Color.WHITE);  // Set text color
			g.setFont(new Font("American Typewriter", Font.BOLD, 30));  // Set font size and type
			renderMessage(g, 160, 60) ;
		}
		else if(methodName.equalsIgnoreCase("conferenceroomwong")){
			// Draw the Images
			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), null);
			g.drawImage(new ImageIcon("back.png").getImage(), 650, 53, getWidth()  -750, getHeight()  -550, null);
			g.drawImage(new ImageIcon("mari.png").getImage(), 90, 435, getWidth()-720, getHeight()-480, null);
			g.setFont(new Font("American Typewriter", Font.BOLD,16));
			g.setColor(Color.white);
			g.drawString("Go Back to Suspect List", 450, 68);
			g.drawString("Click to skip dialogue", 80, 70);
			g.setColor(Color.WHITE);  // Set text color
			g.setFont(new Font("American Typewriter", Font.BOLD, 30));  // Set font size and type

			// If the screen is clicked, the dialogue is automatically/hard coded onto the screen
			// If not, the screen displays it word by word
			if(skipMessage){
				g.setColor(Color.WHITE);  // Set text color
				g.setFont(new Font("American Typewriter", Font.BOLD, 16));  // Set font size and type
				g.drawString("Why would I make the mess?! I was at the grocery store", 170, 460);
				g.drawString("and marked tests all day! All my students love to hand things", 170, 480);
				g.drawString("on the weekends!", 170, 500);

			}
			else{
				startMessageTimer("Why would I make the mess?! I was at the grocery store and marked tests all day! All my students love to hand things on the weekends!");
				g.setColor(Color.WHITE);  // Set text color
				g.setFont(new Font("American Typewriter", Font.BOLD, 16));  // Set font size and type
				renderMessage(g, 170, 460);
			}
		}
		else if(methodName.equalsIgnoreCase("conferenceroomsuki")){
			// Draw Images
			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), null);
			g.drawImage(new ImageIcon("back.png").getImage(), 650, 53, getWidth()  -750, getHeight()  -550, null);
			g.drawImage(new ImageIcon("suki.png").getImage(), 70, 435, getWidth()-720, getHeight()-490, null);
			g.setFont(new Font("American Typewriter", Font.BOLD,16));
			g.setColor(Color.WHITE);
			g.drawString("Go Back to Suspect List", 450, 68);
			g.drawString("Click to skip dialogue", 80, 70);

			// If the screen is clicked, the dialogue is automatically/hard coded onto the screen
			// If not, the screen displays it word by word
			if(skipMessage){
				g.setColor(Color.WHITE);  // Set text color
				g.setFont(new Font("American Typewriter", Font.BOLD, 16));  // Set font size and type
				g.drawString("meow meow meow meowwwwww",170,460);
			}
			else{
				startMessageTimer("meow meow meow meowwwwww");
				g.setColor(Color.WHITE);  // Set text color
				g.setFont(new Font("American Typewriter", Font.BOLD, 16));  // Set font size and type
				renderMessage(g, 170, 460);
			}

		}
		else if(methodName.equalsIgnoreCase("conferenceroombrothers")){
			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), null);
			g.drawImage(new ImageIcon("back.png").getImage(), 650, 53, getWidth()  -750, getHeight()  -550, null);
			g.drawImage(new ImageIcon("brother.png").getImage(), 20, 435, getWidth()-720, getHeight()-480, null);
			g.drawImage(new ImageIcon("brother.png").getImage(), 80, 435, getWidth()-720, getHeight()-480, null);
			g.setFont(new Font("American Typewriter", Font.BOLD,16));
			g.setColor(Color.WHITE);
			g.drawString("Go Back to Suspect List", 450, 68);
			g.drawString("Click to skip dialogue", 80, 70);
			
			if(skipMessage){
				g.setColor(Color.WHITE);  // Set text color
				g.setFont(new Font("American Typewriter", Font.BOLD, 15));
				g.drawString("We didn't even know the mess happened! We were upstairs playing games", 160, 460);
				g.drawString("all day...You know we had no work and Among Us had", 160, 480);
				g.drawString("a new update! Why would we waste a precious free Saturday?", 160, 500);
			}
			else{
				startMessageTimer("We didn't even know the mess happened! We were upstairs playing games all day...You know we had no work and Among Us had a new update! Why would we waste a precious free Saturday?");
				g.setColor(Color.WHITE);  // Set text color
				g.setFont(new Font("American Typewriter", Font.BOLD, 15));  // Set font size and type
				renderMessage(g, 160, 460);
			}

			// text
		}
		else if(methodName.equalsIgnoreCase("conferenceroom2")){
			storyNum = 1;
			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), null);
			g.setColor(Color.white);  // Set text color
			g.setFont(new Font("American Typewriter", Font.BOLD, 20));  // Set font size and type
			g.drawString("Click to skip dialogue", 50, 70);
			g.drawString("Click again to continue", 470, 70);


			if(skipMessage){
				g.setColor(Color.white);
				g.setFont(new Font("American Typewriter", Font.PLAIN, 23));  // Set font size and type
				g.drawString("Fafa's body was found in the library at 5PM. You are hired", 60, 470);
				g.drawString("to find who is the killer!", 60, 500 );
			}
			else{
				startMessageTimer("Fafa's body was found in the library at 5PM. You are hired to find who is the killer!");
				g.setColor(Color.WHITE);  // Set text color
				g.setFont(new Font("American Typewriter", Font.PLAIN, 23));  // Set font size and type
				renderMessage(g, 60, 470);
			}

		}
		else if(methodName.equalsIgnoreCase("conferenceroom2suspects")){
			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), null);
			g.drawImage(new ImageIcon("gaga.png").getImage(), 70, 455, getWidth()-720, getHeight()-500, null);
			g.drawImage(new ImageIcon("wawa.png").getImage(), 300, 455, getWidth()-690, getHeight()-490, null);
			g.drawImage(new ImageIcon("jaja.png").getImage(), 550, 455, getWidth()-700, getHeight()-490, null);
			g.setFont(new Font("American Typewriter", Font.BOLD, 20));

			g.setColor(Color.white);
			g.drawString("Click the screen to learn about them", 80, 70);

			g.drawString("Gaga", 150,520);
			g.drawString("Wawa", 400,520);
			g.drawString("Jaja", 650,520);

			startMessageTimer("They all claimed to not be at the scene of the crime!");
			g.setFont(new Font("American Typewriter", Font.BOLD,16));
			g.setColor(Color.white);
			renderMessage(g, 180, 450);

		}
		else if(methodName.equalsIgnoreCase("conferenceroom2alibis")){
			g.drawImage(new ImageIcon("conferenceRoom.png").getImage(), 0, 0, getWidth(), getHeight(), null);
			g.setColor(Color.white);
			g.setFont(new Font("American Typewriter", Font.BOLD,16));
			g.drawString("Click to skip dialogue", 50, 70);
			g.drawString("Ready to Play", 620,20);
			if(skipMessage){
				g.setColor(Color.white);
				g.setFont(new Font("American Typewriter", Font.PLAIN,14));
				g.drawString("They are all university students. They claimed to be at home the", 60, 460);
				g.drawString("whole day studying! Gaga is currently studying chemical engineering. Wawa is studying", 60, 480);
				g.drawString("chemical engineering. Wawa is studying forensic science! (interesting...) Jaja is a WWE", 60, 500);
				g.drawString("pro wrestler. Jaja is pretty strong...", 60, 520);
			}
			else{
				startMessageTimer("They are all university students. They claimed to be at home the whole day studying! Gaga is currently studying chemical engineering. Wawa is studying forensic science! (interesting...). Jaja is a WWE pro wrestler. Jaja is pretty strong...");
				g.setFont(new Font("American Typewriter", Font.PLAIN,14));
				g.setColor(Color.white);
				renderMessage(g, 60, 460);
			}

		}

		else if (methodName.equalsIgnoreCase("secretBook")) { //Secret Book
			// Initial case room description and image will be drawn
			g.drawImage(new ImageIcon("page.png").getImage(), 0, 0, getWidth(), getHeight(), this);
			bookBackButtons(new ImageIcon("bookButton.png"), 0, 0, 60, 60);

			// Sort the list before searching (in case it's not already sorted)
			Collections.sort(topSecretBook.get(storyNum - 1).getItemsFound());
			if (searchItem.isEmpty()) {
				// Set the font to American Typewriter
				g.setFont(new Font("American Typewriter", Font.BOLD, 16));

				// Split the description into lines of 30 characters
				int maxLineLength = 30;
				int xPos = 420; // Start X position for text
				int yPos = 150; // Start Y position for text
				int lineHeight = 20; // Vertical space between lines

				// Get the description to format
				String description = caseRooms.get(storyNum - 1).toString();

				// Split the description into words and process each one
				String[] words = description.split(" ");
				StringBuilder currentLine = new StringBuilder();

				for (String word : words) {
					// Check if adding this word exceeds the max line length
					if (currentLine.length() + word.length() + 1 > maxLineLength) {
						// If it exceeds, draw the current line and move to the next line
						g.drawString(currentLine.toString(), xPos, yPos);
						yPos += lineHeight; // Move down for the next line
						currentLine.setLength(0); // Reset the current line
					}
					// Add the word to the current line
					if (currentLine.length() > 0) {
						currentLine.append(" "); // Add space between words
					}
					currentLine.append(word);
				}

				// Draw the last line if any text remains
				if (currentLine.length() > 0) {
					g.drawString(currentLine.toString(), xPos, yPos);
				}
				int xPosition = 98; 
				// If searchItem is empty, display suspect images
				// Assuming topSecretBook.get(storyNum - 1) gives the correct TopSecretBook object.
				TreeSet<Suspect> suspects = topSecretBook.get(storyNum - 1).getSuspects();
				for (Suspect suspect : suspects) {
					String suspectName = suspect.getName(); // Load and draw the image for the suspect
					g.drawImage(new ImageIcon(suspectName + "-removebg-preview.png").getImage(), xPosition, 360, 90, 100, this);
					xPosition += 95;  // Increment xPosition to place the next image
				}

			}

			// If a search query has been entered
			else if (!searchItem.isEmpty()) {
				remove(imageLabel);
				// Perform binary search for the search item
				int index = Collections.binarySearch(topSecretBook.get(storyNum-1).getItemsFound(), new Item(searchItem, "", "", 0, 0, 0, 0, 0));
				if (index >= 0) { // Found the item
					String imageName = searchCorrespondingItemImage.get(searchItem.toLowerCase());

					// Draw the image based on the mapped search query
					g.drawImage(new ImageIcon(imageName + ".png").getImage(), 70, 100, 300, 300, this);
					g.setColor(Color.BLACK); // Optionally, draw a description or hint based on the search item
					
					// Get the description and split into words
					String description = topSecretBook.get(storyNum-1).getItemsFound().get(index).getDescription();
					String[] words = description.split(" ");
					
					// Font settings
					g.setFont(new Font("American Typewriter", Font.BOLD, 10)); // Adjust font size as needed
					int x = getWidth() - 380; // X-coordinate for text
					int y = getHeight() - 400; // Starting Y-coordinate for text
					int maxLineLength = 40; // Maximum characters per line
					int lineHeight = 30; // Spacing between lines

					// Build and draw lines with 40 characters per line
					StringBuilder line = new StringBuilder();

					for (String word : words) { // Add the current word and a space
						line.append(word).append(" ");
						// If the line exceeds 40 characters, draw the current line and start a new one
						if (line.length() > maxLineLength) {
							g.drawString(line.toString().trim(), x, y); // Draw the line at the current position
							y += lineHeight; // Move to the next line (increase Y position)
							line.setLength(0); // Reset the line for the next set of words
						}
					}

					// Draw any remaining words
					if (line.length() > 0) {
						g.drawString(line.toString().trim(), x, y);
					}

				} else {// Item not found
					g.setColor(Color.RED); // Set a larger font and in red
					Font bigFont = new Font("American Typewriter", Font.BOLD, 100); // Adjust font type and size as needed
					g.setFont(bigFont);
					String message = "Item not found"; // The message to display
					int textWidth = message.length() * 60; // Estimate 60 pixels per character for this font size
					int textHeight = 100;                 // Approximate text height is the font size
					// Calculate the x and y coordinates for centering
					int x = (getWidth() - textWidth) / 2 + 50;
					int y = (getHeight() + textHeight) / 2 - 50;
					g.drawString(message, x, y); // Draw the text at the calculated position
				}
			} 
		}

		else if (methodName.equalsIgnoreCase("reportScreen")){ // File Report Screen
			g.drawImage(new ImageIcon("report.png").getImage(), 0, 0, getWidth(), getHeight(), this);
			g.setColor(Color.WHITE); // Set text color for visibility
			g.setFont(new Font("American Typewriter", Font.BOLD, 20)); // Set font style and size
			g.drawString(storyNum +"", 202, 173);
		}
		else if (methodName.equalsIgnoreCase("conclusionScreen")) { // Conclusion Screen
			g.drawImage(new ImageIcon("conclusion.png").getImage(), 0, 0, getWidth(), getHeight(), this);
			String storyline = ""; // Display Storyline on the Left
			if (storyNum == 1) {
				storyline = "Gaga gave Fafa some arsenic powder, and Fafa started to cough blood out. Gaga is a slightly coocoo scientist, and she wanted to test out her new chemical (arsenic powder!). It just so happens that Gaga was tutoring Fafa in the library... Although Wawa's receipt was in the library, she simply forgot she was there. On the phone, arsenic powder can be seen in Gaga's lab!";
			} else if (storyNum ==2) {
				storyline = "Little did you know, it was Ms.Wong all along. She herself didn't know it was her because she was very sick. All she said was actually all a dream. Though she was sick, she did the laundry but dropped the bros socks along the way. She had stumbled upon Suki's toys and JigglyPuff her fav! She also knocked over the plant and Suki decided to just run over it too. Ms Wong forgot about this but she also wanted to try toss the tennis balls in the basketball net but one swing and her nail flew off and the balls hit the plates. After that she was nice enough to still make Suki food in the sink of course and then fell in a deep sleep upstairs.";
			}

			String[] words = storyline.split(" "); // Split the storyline by spaces
			int x = 100; // Starting X position
			int y = 220; // Starting Y position
			int maxLineLength = 40;  // Maximum characters per line
			StringBuilder line = new StringBuilder(); // To accumulate characters for each line

			for (int i = 0; i < words.length; i++) {
				// Add the current word to the line with a space
				line.append(words[i]).append(" ");
				// If the current line exceeds the maxLineLength, draw it and start a new line
				if (line.length() > maxLineLength || i == words.length - 1) {
					g.setColor(Color.BLACK); // Set text color for visibility
					g.setFont(new Font("American Typewriter", Font.BOLD, 13)); // Set font style and size
					g.drawString(line.toString(), x, y); // Draw the line at the current position
					y += 20; // Move to the next line (increase Y position)
					line.setLength(0); // Reset the line for the next set of words
				}
			}
		}
	}

	/* Method Description: This method called getDetectiveIcon returns the appropriate ImageIcon for the detective's character based on the current direction facing.
	 * Parameters: none
	 * Return Type: ImageIcon --> an ImageIcon representing the detective facing a specific direction ('U' for up, 'D' for down, 'L' for left, 'R' for right).
	 */
	public ImageIcon getDetectiveIcon() {
		// Check the detective's current direction and return the corresponding icon
		if (detective.getDirection() == 'U') {
			return new ImageIcon("detectiveU.png"); // Up direction
		} else if (detective.getDirection() == 'D') {
			return new ImageIcon("detectiveD.png"); // Down direction
		} else if (detective.getDirection() == 'L') {
			return new ImageIcon("detectiveL.png"); // Left direction
		} else if (detective.getDirection() == 'R') {
			return new ImageIcon("detectiveR.png"); // Right direction
		} else {
			return new ImageIcon("detectiveD.png"); // Default to down direction if no direction is set
		}
	}

	/* Method Description: This method is called titleScreen. It helps to set up the title screen by removing any existing components and adding the Play, Menu, and Credits buttons with appropriate icons, positions, and action commands.
	 * Parameters: none
	 * Return Type: none
	 */
	public void titleScreen() {
		removeAll();  // Remove all existing components
		methodName = "titleScreen";
		setLayout(null);
		titleScreenButtons("playButton.png", 303, 496, "play"); 	    // Create Start/Play Button
		titleScreenButtons("menuButton.png", 23, 497, "menu1"); 	    // Create Menu/Rules button
		titleScreenButtons("creditsButton.png", 569, 495, "credits");	// Create Credits Button
		repaint(); // Repaint to reflect the changes 
	}

	/* Method Description: This method called titleScreenButtons links to above. It helps to create and add the title buttons with an image icon, position, and action command.
	 * Parameters: String imagePath --> image file name to use for the button icon.
	 *			   int x --> The x-coordinate for the button's position.
	 *      	   int y --> The y-coordinate for the button's position.
	 * 			   String actionCommand --> The action command that will be associated with the button's action listener.
	 */
	public void titleScreenButtons(String imagePath, int x, int y, String actionCommand) {
		ImageIcon titleButtonIcon = new ImageIcon(imagePath);
		Image image = titleButtonIcon.getImage();
		Image newImage = image.getScaledInstance(211, 40, java.awt.Image.SCALE_SMOOTH);  // Scale the image to a specific size
		titleButtonIcon = new ImageIcon(newImage);

		JButton titleButton = new JButton(titleButtonIcon);
		titleButton.setBounds(x, y, newImage.getWidth(null), newImage.getHeight(null));  // Adjust size and position based on the new image
		titleButton.addActionListener(this);
		titleButton.setActionCommand(actionCommand); //set the actionCommand
		add(titleButton); //add it to the frame
	}

	/* Method Description: This method called transportationMapScreen() updates the screen for the transportation map, setting the detective's position based on the current story the detective is going to investigate and setting up a countdown timer if a time limit is defined for the case chosen.
	 * Parameters: none
	 * Return Type: none
	 */
	public void transportationMapScreen() {
		removeAll(); // Remove all current components on the screen
		methodName = "transportationMapScreen"; // Set the current method name to "transportationMapScreen" for later use (e.g., for handling navigation and paintcomponent)

		if (storyNum != 0) { // Check if there is a valid story number to set up the time limit for the case (as the conference room does not require a Time)
			Time timeLimit = caseRooms.get(storyNum - 1).getTimeLimit();
			// If a time limit is present, set the countdown timer accordingly
			if (timeLimit != null) {
				countdownTime.setMinutes(timeLimit.getMinutes());
				countdownTime.setSeconds(timeLimit.getSeconds());
			} else { // If no time limit exists, reset the countdown timer to 0 minutes and 0 seconds
				countdownTime.setMinutes(0);
				countdownTime.setSeconds(0);
			}
		}

		// Adjust the detective's position and direction based on the story number and whether the conference room has been completed (finished dialogue info)
		if (finishedConferenceRoom) { // If the detecitve is finished at the conference room, set the detective's position and direction
			detective.setPosition(422, 140);
			detective.setDirection('D');  // 'D' represents a downward direction
			finishedConferenceRoom = false;  // Reset the conference room completion flag

		} else if (storyNum == 1) { // If it's the first story (library), position the detective accordingly and reset the story number
			detective.setPosition(570, 410);
			detective.setDirection('D');
			storyNum = 0;  // Reset story number to prevent re-triggering

		} else if (storyNum == 2) { // If it's the second story, position the detective accordingly and reset the story number
			detective.setPosition(230, 340);
			detective.setDirection('D');
			storyNum = 0;  // Reset story number to prevent re-triggering

		} else { // For other scenarios, position the detective at a default starting position and set the direction to 'R' (right) --> this being from title screen to transportation screen
			detective.setPosition(25, 150);
			detective.setDirection('R');
		}
		revalidate();  // Revalidate the layout of the components and repaint the screen to reflect changes
		repaint();
	}

	/* Method Description: This method called setUpStoryScene helps to set up the scene based on the specified detective's position and direction, creates the file report button with the specified image path, position, and size, and starts the countdown timer. It also updates the depending on which location is selected.
	 * Parameter: String sceneName --> The name of the scene ("library" or "wongMansion")
	 *		      int detectivePosX --> The X-coordinate for the detective's position.
	 *			  int detectivePosY -->The Y-coordinate for the detective's position.
	 * 			  char detectiveDirection --> The direction of the detective ('U', 'D', 'L', 'R').
	 *            int fileReportButtonX --> The X-coordinate for the file report button's position.
	 *            int fileReportButtonY --> The Y-coordinate for the  file report button's  position.
	 * 			  int fileReportButtonWidth --> The width of the  file report button.
	 * 			  int fileReportButtonHeight --> The height of the  file report button.
	 * 			  boolean isWongMansionSelected --> this flag indicates whether WongMansion is the current scene
	 *			  boolean isLibrarySelected --> this flag indicates whether Library is the current scene
	 * Return Type: void
	 */
	public void setUpStoryScene(String sceneName, int detectivePosX, int detectivePosY, char detectiveDirection, int fileReportButtonX, int fileReportButtonY, int fileReportButtonWidth, int fileReportButtonHeight, boolean isWongMansionSelected, boolean isLibrarySelected) {
		removeAll();  // Remove any previous components
		methodName = sceneName;  // Set the current method name for debugging
		detective.setPosition(detectivePosX, detectivePosY);  // Set detective's position
		detective.setDirection(detectiveDirection);  // Set detective's direction
		isInWongMansion = isWongMansionSelected;  // Set flag for Wong's Mansion scene
		isInLibrary = isLibrarySelected;  // Set flag for Library scene
		ImageIcon reportButton = new ImageIcon("reportButton.png");  // Create the report button icon
		Image image = reportButton.getImage();
		Image newimg = image.getScaledInstance(fileReportButtonWidth, fileReportButtonHeight, java.awt.Image.SCALE_SMOOTH);  // Scale the image for button
		reportButton = new ImageIcon(newimg);  // Update the icon with the scaled image
		report = new JButton(reportButton);  // Create the button with the icon
		report.setBounds(fileReportButtonX, fileReportButtonY, fileReportButtonWidth, fileReportButtonHeight);  // Set button position and size
		report.addActionListener(this);  // Add action listener for button click
		report.setActionCommand("report");  // Set action command to identify the button
		add(report);  // Add the button to the screen
		countdownTimer.start();  // Start the countdown timer
		revalidate();  // Revalidate the layout
		repaint();  // Repaint the screen to reflect the changes
	}

	/* Method Description: This method called library initializes and calls the setUpStoryScene for case #1.
	 * Parameters: none
	 * Return Type: void
	 */
	public void library() {
		setUpStoryScene("library", detective.getX(), detective.getY(), detective.getDirection(), 200, 10, 70, 80, false, true);
	}

	/* Method Description: This method called wongMansion initializes and calls the setUpStoryScene for case #2.
	 * Parameters: none
	 * Return Type: void
	 */
	public void wongMansion() {
		setUpStoryScene("wongMansion", detective.getX(), detective.getY(), detective.getDirection(), 635, 192, 55, 80, true, false);
	}

	//ADD METHOD COMMENTS
	public void conferenceRoom() {
		removeAll();
		methodName = "conferenceRoom";

		ImageIcon caseIcon = new ImageIcon("case1Button.png");
		Image image = caseIcon.getImage();
		Image newimg = image.getScaledInstance(267, 250, java.awt.Image.SCALE_SMOOTH);
		caseIcon = new ImageIcon(newimg);
		JButton button = new JButton(caseIcon);
		button.setBounds(150, 434, 200, 95);
		button.addActionListener(this);
		button.setActionCommand("case1");
		add(button);

		ImageIcon caseIcon2 = new ImageIcon("case2button.png");
		Image image2 = caseIcon2.getImage();
		Image newimg2 = image2.getScaledInstance(267, 250, java.awt.Image.SCALE_SMOOTH);
		caseIcon2 = new ImageIcon(newimg2);
		JButton button2 = new JButton(caseIcon2);
		button2.setBounds(440, 434, 200, 95);
		button2.addActionListener(this);
		button2.setActionCommand("case2");
		add(button2);

		finishedConferenceRoom = false;
		finishedGame = false;
		textTimer.start();
		repaint();
	}

	public void conferenceRoom1() {
		removeAll();
		methodName = "conferenceRoom1";
		count = 1;
		storyNum = 1;
		if(skipMessage){
			removeAll();
		}
		else{
			textTimer.start();
		}
		repaint();
	}
	public void conferenceRoom1Suspects() {
		removeAll();
		methodName = "conferenceRoom1Suspects";
		textTimer.start();
		repaint();
	}
	public void conferenceRoom1Wong(){
		removeAll();
		methodName = "conferenceroomWong";
		if(skipMessage){
			removeAll();
		} else{
			textTimer.start();
		}
		repaint();
	}
	public void conferenceRoom1Suki(){
		removeAll();
		methodName = "conferenceroomSuki";
		if(skipMessage){
			removeAll();
		}else{
			textTimer.start();
		}
		repaint();
	}
	public void conferenceRoom1Brothers(){
		removeAll();
		methodName = "conferenceroomBrothers";
		if(skipMessage){
			removeAll();
		}
		else{
			textTimer.start();
		}
		repaint();
	}
	public void conferenceRoom2(){
		removeAll();
		methodName = "conferenceRoom2";
		if(skipMessage){
			removeAll();
		}
		else{
			textTimer.start();
		}
		repaint();
	}
	public void conferenceRoom2Suspects(){
		removeAll();
		methodName = "conferenceRoom2Suspects";
		repaint();
	}
	public void conferenceRoom2SuspectAlibi(){
		removeAll();
		methodName = "conferenceRoom2alibis";
		if(skipMessage){
			removeAll();
		}
		else{
			textTimer.start();
		}
		repaint();
	}
	/* Method Description: This method called secretBook displays the detective's book screen, where the player can search for specific item they have collected to review relevant information. This method sets up the search bar and initializes mapping of terms valid.
	 * Parameteres: none
	 * Return Type: void
	 */
	public void secretBook() {
		removeAll(); // Clear any previous content from the screen
		methodName = "secretBook"; // Set the method name for drawing purposes

		// Initialize search term mappings
		initializeSearchTerms();

		// Create the search bar for user input
		JTextField searchBar = new JTextField();
		searchBar.setBounds(80, 10, 600, 40); // Position and size of the search bar
		searchBar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String query = searchBar.getText().trim().toLowerCase(); // Get the query entered by the user, trim it, and convert it to lowercase
				// Map the query to its corresponding file name if it exists in the map
				if (searchForValidItem.containsKey(query)) {
					searchItem = searchForValidItem.get(query); // Get the mapped file name
				} else {
					searchItem = query; // Default to the raw query if no mapping exists --> this MAY indicate that the item is not found
				}
				repaint(); // Repaint the screen to reflect the search results
			}
		});

		setLayout(null); // Set layout manager to null for absolute positioning
		add(searchBar); // Add the search bar to the screen

		// Load the default image based on the current room name
		ImageIcon defaultIcon = new ImageIcon(caseRooms.get(storyNum - 1).getName() + ".png");
		Image scaledImage = defaultIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH); // Scale the image
		ImageIcon scaledIcon = new ImageIcon(scaledImage); // Create a new ImageIcon with the scaled image
		imageLabel = new JLabel(scaledIcon); // Create a JLabel to display the scaled image
		imageLabel.setBounds(110, 100, 250, 250); // Set position and size of the image label
		add(imageLabel); // Add the image label to the screen
		repaint(); // Repaint the screen to display initial content
	}

	/* Method Description: This method called initializeSearchTerms helps create a mapping of searchable items to the corresponding file name for the item to be displayed correctly.
	 * Parameter: none
	 * Return Type: void
	 */
	private void initializeSearchTerms() {
		// Wong Mansion search terms
		searchForValidItem.put("cat toys", "cat toys"); //fix
		searchForValidItem.put("calendar", "calendar");
		searchForValidItem.put("sports balls", "sports balls");
		searchForValidItem.put("socks", "socks");
		searchForValidItem.put("thermometer", "thermometer");
		searchForValidItem.put("video games", "video games"); //fix
		searchForValidItem.put("trash", "trash");
		searchForValidItem.put("picture", "picture");
		searchForValidItem.put("plant", "plant");
		searchForValidItem.put("jigglypuff", "jigglypuff");
		searchForValidItem.put("broken plates", "broken plates");
		searchForValidItem.put("lettuce", "lettuce");

		// Library search terms
		searchForValidItem.put("arsenic", "arsenic");
		searchForValidItem.put("book", "book");
		searchForValidItem.put("fingerprint", "fingerprint");
		searchForValidItem.put("phone", "phone");
		searchForValidItem.put("purple juice", "purple juice");
		searchForValidItem.put("notebook", "notebook");
		searchForValidItem.put("receipt", "receipt");
	}

	/* Method Description: This method called creditsScreen displays the credits screen and adds a back button to go back to the title screen.
	 * Parameters: none
	 * Return Type: void
	 */
	public void creditsScreen() {
		removeAll();
		methodName = "creditsScreen";
		createButton("creditsMenuBackButton.png", 25, 32, 60, 60, "back");
		repaint();
	}

	/* Method Description: This method called menuScreen displays the how to play menu screen (so instructions) and adds a back button for use to return back to title screen and a right/next button to read the key notes.
	 * Parameters: none
	 * Return Type: void
	 */
	public void menuScreen() {
		removeAll();
		methodName = "menuScreen";
		createButton("creditsMenuBackButton.png", 25, 32, 60, 60, "back");
		createButton("menuRightButton.png", 735, 32, 60, 60, "menu2");
		repaint();
	}

	/* Method Description: This method called menuPg2Screen displays the extra key notes to look out for (such as some special icons to refer back to detective's notes or to file a report on the case) and adds a back button for use to return back to the previous page about how to play.
	 * Parameters: none
	 * Return Type: void
	 */
	public void menuPg2Screen() {
		removeAll();
		methodName = "menuPg2Screen";
		createButton("creditsMenuBackButton.png", 25, 32, 60, 60, "menu1");
		repaint();
	}

	/* Method Description: This method called createButton is a generalized method for creating buttons with customizable icons, positions, sizes, and actions. These are the specific buttons after titleScreen used for credits, instructions menu, and transportation screen to go back to the title screen)
	 * Parameters: String iconPath --> file name of image to be used as the button icon
	 *			   int x --> the x-coordinate of the button's position.
	 * 			   int y --> The y-coordinate of the button's position.
	 * 			   int w --> The width of the button.
	 * 			   int h --> The height of the button.
	 * 			   String actionCommand --> The action command to be associated with the button.
	 * Return Type: void
	 */
	public void createButton(String iconPath, int x, int y, int w, int h, String actionCommand) {
		ImageIcon buttonIcon = new ImageIcon(iconPath);
		Image image = buttonIcon.getImage();
		Image newimg = image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
		buttonIcon = new ImageIcon(newimg);

		JButton button = new JButton(buttonIcon);
		button.setBounds(x, y, w, h);
		button.addActionListener(this);
		button.setActionCommand(actionCommand);
		add(button);
	}

	/* Method Description: This method called bookBackButton creates the back button with customizable icon, position, size, and action command for the secretBook. For the action command specifically, it is determined based on the current story number.
	 * Parameters: ImageIcon button --> The ImageIcon representing the back button's icon.
	 * 			   int x --> The x-coordinate of the back button's position.
	 * 			   int y --> The y-coordinate of the back button's position.
	 * 			   int w --> The width of the back button.
	 * 			   int h --> The height of the back button.
	 * Return Type: void
	 */
	public void bookBackButtons(ImageIcon button, int x, int y, int w, int h) {
		setLayout(null);
		Image image = button.getImage();
		Image newimg = image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
		button = new ImageIcon(newimg);

		JButton backButton = new JButton(button);
		backButton.setBounds(x, y, w, h);
		backButton.addActionListener(this);

		// Set the action command based on story number
		if (storyNum == 1) {
			backButton.setActionCommand("library");
		} else if (storyNum == 2) {
			backButton.setActionCommand("wongMansion");
		}
		add(backButton);
	}


	/* Method Description: This method called setCorrectSuspect initializes and sets the correct guilty suspect of the current room. It iterates through the suspects in the current room and finds the one marked as the culprit. It then sets the correctSuspect to the name of the suspect identified as the culprit.
	 * Parameters: none
	 * Return type: void
	 */
	public void setCorrectSuspect() {
		// Iterate through caseRooms to find the matching room by name
		for (int i = 0; i < caseRooms.get(storyNum - 1).getSuspects().size(); i++) {
			Suspect suspect = caseRooms.get(storyNum - 1).getSuspects().get(i);
			if (suspect.isCulprit()) {
				correctSuspect = suspect.getName(); // Set the correct suspect
				break;
			}
		}
	}

	/* Method Description: This method called reportScreen displays the report screen where the player can view collected items and select a suspect to report.
	 * Parameters: none
	 * Return Type: void
	 */
	public void reportScreen() {
		removeAll();
		methodName = "reportScreen"; // Set the current method name for reference
		setLayout(null);

		// Stop the countdown timer if it's running
		if (countdownTimer != null) {
			countdownTimer.stop();
		}

		setCorrectSuspect();  // Set the correct suspect based on the room's information

		// Create the submit button (initially disabled)
		ImageIcon submitIcon = new ImageIcon("submitButton.png");
		Image submitImage = submitIcon.getImage();
		Image submitNewImg = submitImage.getScaledInstance(200, 80, java.awt.Image.SCALE_SMOOTH);
		submitIcon = new ImageIcon(submitNewImg);
		submitButton = new JButton(submitIcon);
		submitButton.setBounds(480, 410, 200, 80); // Position the submit button
		submitButton.setEnabled(false); // Disable submit button initially
		submitButton.addActionListener(this);
		submitButton.setActionCommand("submit");
		add(submitButton);

		// Display collected item images in a grid layout
		int xPos = 110; // Initial X position
		int yPos = 220; // Initial Y position
		int itemWidth = 60; // Width of each item image
		int itemHeight = 60; // Height of each item image
		int itemsPerRow = 6; // Number of items per row

		// Loop through the collected items for the current story number
		for (int i = 0; i < topSecretBook.get(storyNum - 1).getItemsFound().size(); i++) {
			Item item = topSecretBook.get(storyNum - 1).getItemsFound().get(i);
			String imageName = searchCorrespondingItemImage.get(item.getName().toLowerCase()); // Get the image file name from the map using the item name

			if (imageName != null) {
				ImageIcon itemIcon = new ImageIcon(imageName + ".png"); // Create and scale the item image
				Image itemImage = itemIcon.getImage().getScaledInstance(itemWidth, itemHeight, Image.SCALE_SMOOTH);
				itemIcon = new ImageIcon(itemImage);

				JLabel itemLabel = new JLabel(itemIcon); // Create a JLabel with the image and set its position
				itemLabel.setBounds(xPos, yPos, itemWidth, itemHeight);
				add(itemLabel);
			}

			xPos += itemWidth;  // Update position for the next item

			// Move to the next row after 'itemsPerRow' items
			if ((i + 1) % itemsPerRow == 0) {
				xPos = 110; // Reset X position
				yPos += itemHeight; // Move to the next row
			}
		}

		// Retrieve the list of suspects for the current room to create button for each suspect
		List<Suspect> suspects = caseRooms.get(storyNum - 1).getSuspects();
		int suspectButtonX = 110; // Initial X position for suspect buttons
		int suspectButtonY = 400; // Fixed Y position for all suspect buttons
		int buttonSpacing = 110; // Space between buttons
		for (Suspect suspect : suspects) {
			String suspectImageName = "select" + suspect.getName().replace(" ", "") + ".png";
			createSuspectButton(suspectImageName, suspect.getName(), suspectButtonX, suspectButtonY);
			suspectButtonX += buttonSpacing; // Move X position for the next button
		}
		revalidate(); // Revalidate and repaint the screen to apply the changes
		repaint();
	}

	/* Method Description: This method called actionPerformed handles all action events triggered by user interactions with buttons other components. This method processes different commands (e.g., navigation, suspect selection, game flow) and updates the game state accordingly.
	 * Parameters: ActionEvent e -->  The ActionEvent triggered by user interaction.
	 * Return Type: void
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Retrieve the action command associated with the event
		String action = e.getActionCommand();

		// Navigate back to the title screen
		if (action.equals("back")) {
			detective.setPosition(25, 150); // Reset detective's position
			detective.setDirection('R');   // Set detective's direction
			titleScreen();                 // Navigate to the title screen
		} 
		// Handle navigation to menu screens
		else if (action.equals("menu2")) {
			menuPg2Screen();               // Display the key notes menu page
		} else if (action.equals("menu1")) {
			menuScreen();                  // Navigate to the general instructions on how to play menu screen
		} 
		// Display credits screen
		else if (action.equals("credits")) {
			creditsScreen();               // Show credits screen
		} 
		// Start the game and navigate to the transportation map
		else if (action.equals("play")) {
			storyNum = 0;                  // Initialize story progression
			transportationMapScreen();     // Navigate to the transportation map screen
		} 
		// Navigate to Wong's Mansion
		else if (action.equals("wongMansion")) {
			submitted = false;             // Reset submission state
			clicked = false;               // Reset click state of items
			searchItem = "";               // Clear search items in secret book
			wongMansion();                 // Navigate to Wong's Mansion
		} 
		// Navigate to the library
		else if (action.equals("library")) {
			submitted = false; // Reset submission state
			clicked = false;  // Reset click state of items
			searchItem = "";  // Clear searching of items in secret book
			library();  // Navigate to the library
		} 
		// Navigate to specific case rooms
		else if (action.equals("case1")) {
			conferenceRoom2(); // Navigate to Conference Room 2
		} else if (action.equals("case2")) {
			conferenceRoom1(); // Navigate to Conference Room 1
		} 
		// Navigate to the report screen
		else if (action.equals("report")) {
			submitted = false; // Reset submission state
			reportScreen();  // Show the report screen for the current room
		} 
		// Handle selected suspect being reported
		else if (submitted == false && (action.equals("Suki") || action.equals("DuoBrothers") || action.equals("MsWong") || action.equals("Jaja") || action.equals("Wawa") || action.equals("Gaga"))) {
			selectedSuspect = action;      // Set the selected suspect
			submitButton.setEnabled(true); // Enable the submit button

			String imagePath = getSuspectImage(selectedSuspect); // Update the displayed image of the selected suspect
			if (!imagePath.isEmpty()) {
				ImageIcon suspectIcon = new ImageIcon(imagePath);
				Image suspectImage = suspectIcon.getImage().getScaledInstance(100, 120, Image.SCALE_SMOOTH);
				selectedSuspectImageLabel.setBounds(530, 250, 100, 120);
				add(selectedSuspectImageLabel);
				selectedSuspectImageLabel.setIcon(new ImageIcon(suspectImage));
			}
		} 
		// Handle submission of reported file
		else if (submitted == false && action.equals("submit")) {
			boolean isCorrect = isCorrectSuspect(selectedSuspect); // Check if suspect is correct
			submitted = true; // Mark as submitted

			String gifPath;
			if (isCorrect) {
				gifPath = "correct.gif";
			} else {
				gifPath = "wrong.gif";
			}
			displayGIF(gifPath); // Show appropriate GIF based on correct answer

			// Create a timer for transition to the conclusion screen
			timer = new Timer(3000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timer.stop(); // Stop the transition timer
					conclusionScreen(); // Navigate to conclusion screen
				}
			});

			timer.start(); // Start the transition timer
			revalidate();  // Refresh
		} 
		// Exit the current room and navigate to the transportation map
		else if (action.equals("exit")) {
			finishedConferenceRoom = false; // Reset room completion state
			transportationMapScreen(); // Navigate to the transportation map
		}
	}

	/* Method Description: This method called createSuspectButton creates a button for a suspect with a given image, action command, and position.
	 * Parameters: String imagePath The file path of the suspect's image.
	 * 			   String suspectActionCommand --> The action command to be associated with the button, typically the suspect's name.
	 * 			   int x --> The x-coordinate for the button's position on the panel.
	 * 			   int y --> The y-coordinate for the button's position on the panel.
	 * Return Type: void
	 */
	public void createSuspectButton(String imagePath, String suspectActionCommand, int x, int y) {
		ImageIcon suspect = new ImageIcon(imagePath);
		Image suspectImage = suspect.getImage().getScaledInstance(90, 100, Image.SCALE_SMOOTH);  // Adjust size
		suspect = new ImageIcon(suspectImage);

		JButton suspectButton = new JButton(suspect);
		suspectButton.setBounds(x, y, 90, 100); // Set size and location for the button
		suspectButton.addActionListener(this);
		suspectButton.setActionCommand(suspectActionCommand); //Set command
		add(suspectButton);
	}

	public void conclusionScreen() {
		removeAll();
		methodName = "conclusionScreen";
		setLayout(null);  // Ensure the layout is set to null for absolute positioning

		// Read the high score from the file
		Time currentHighScore = readHighScore(caseRooms.get(storyNum-1).getName());
		String durationStr = "";

		// Only display the actual time if the suspect is correct
		if (isCorrectSuspect(selectedSuspect)) {
			// Use the durationInMap method to calculate the actual time spent
			Time actualDuration = countdownTime.durationInMap(caseRooms.get(storyNum-1).getTimeLimit());
			durationStr = "Actual Time: " + actualDuration.toString();

			// Compare and update the high score if necessary
			if (countdownTime.compareTo(currentHighScore) < 0) {
				saveHighScore(caseRooms.get(storyNum-1).getName(), countdownTime);
				// Save the new high score to file

				// Create labels to display the text on screen
				JLabel bestTimeLabel = new JLabel("New HighScore: " + actualDuration.toString());
				bestTimeLabel.setBounds(480, 420, 400, 30);    // Adjust position as needed
				bestTimeLabel.setFont(new Font("American Typewriter", Font.BOLD, 15));
				bestTimeLabel.setForeground(Color.BLACK);  // Set the text color to white

				add(bestTimeLabel);
			} else {
				JLabel actualTimeLabel = new JLabel(durationStr);
				actualTimeLabel.setBounds(480, 420, 400, 30);  // Adjust position as needed
				actualTimeLabel.setFont(new Font("American Typewriter", Font.BOLD, 15));
				actualTimeLabel.setForeground(Color.BLACK);  // Set the text color to black

				add(actualTimeLabel);
			}
		}

		// Display the guilty person's image on the top-right corner
		ImageIcon guiltyIcon = new ImageIcon(correctSuspect + "-removebg-preview.png");
		Image guiltyImage = guiltyIcon.getImage().getScaledInstance(80, 100, Image.SCALE_SMOOTH); // Resize the image
		JLabel guiltyLabel = new JLabel(new ImageIcon(guiltyImage));
		guiltyLabel.setBounds(520, 180, 80, 100); // Position on top-right corner
		add(guiltyLabel); // Add it to the panel

		// Display Key Hints (Top 3 most important items)
		List<Item> sortedItems = new ArrayList<>();
		sortedItems.addAll(caseRooms.get(storyNum-1).getItems()); // Collect all items from each room

		// Sort the items based on importance (highest importance first)
		Collections.sort(sortedItems, new SortItemsByImportance()); 

		int xPos = 480; // Starting Y position for key hints
		for (int i = 0; i < Math.min(3, sortedItems.size()); i++) { // Display top 3 hints
			Item item = sortedItems.get(i);  // Get the most important items (sorted first)

			// Debug: Check the image path for each hint item
			String hintImagePath = searchCorrespondingItemImage.get(item.getName().toLowerCase()) + ".png";

			// Create ImageIcon for the hint image
			ImageIcon originalIcon = new ImageIcon(hintImagePath);

			// Scale the image to the desired size (60x60 in this case)
			Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);

			// Create a new ImageIcon with the scaled image
			ImageIcon scaledIcon = new ImageIcon(scaledImage);

			// Create a JLabel with the scaled ImageIcon
			JLabel hintLabel = new JLabel(scaledIcon);
			hintLabel.setBounds(xPos, 340, 50, 50); // Set position for the image label

			// Add the JLabel to the panel (assuming 'this' is a JPanel or JFrame)
			add(hintLabel);

			xPos += 50; // Adjust Y position for next hint
		}


		// Load and scale the exit button image
		ImageIcon exitIcon = new ImageIcon("exitButton.png");
		Image exitImage = exitIcon.getImage().getScaledInstance(105, 100, Image.SCALE_SMOOTH); // Adjust size
		exitIcon = new ImageIcon(exitImage);

		// Create the exit button
		JButton exitButton = new JButton(exitIcon);
		exitButton.setBounds(getWidth() - 103, getHeight() - 99, 105, 100); // Position at the bottom-right
		exitButton.addActionListener(this);
		exitButton.setActionCommand("exit");

		// Add the exit button to the screen
		add(exitButton);

		revalidate();  // Revalidate to ensure that the layout updates properly
		repaint();  // Repaint the screen to show the exit button and labels
	}


	/* Method Description: This method called readHighScore reads the high score for a given room from the "highscore.txt" file. If no high score is found for the room, the method returns a default high score based on the time the detective goes if they solve the case right.
	 * Parameters: String roomName --> The name of the room whose high score is being read.
	 * Return Type: Time --> The high score for the specified room as a Time object.
	 */
	public Time readHighScore(String roomName) {
		File file = new File("highscore.txt"); // The file where high scores are stored

		Time highScore = new Time(Integer.MAX_VALUE, Integer.MAX_VALUE); // Default high score if no score is found for the room (representing an invalid score)

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) { // Read the file line by line
			String line;
			boolean foundRoom = false;  // Flag to indicate whether the room was found in the file

			// Iterate through each line in the file
			while ((line = reader.readLine()) != null) {
				// Check if the line matches the room name
				if (line.trim().equalsIgnoreCase(roomName)) {
					foundRoom = true;  // Mark the room as found
				} else if (foundRoom && line.contains(":")) { // If we are past the room name and on a line with the time format (minutes:seconds)
					String[] timeParts = line.split(":");  // Split the time by the colon (:) character
					int minutes = Integer.parseInt(timeParts[0].trim());  // Parse the minutes
					int seconds = Integer.parseInt(timeParts[1].trim());  // Parse the seconds
					highScore = new Time(minutes, seconds);  // Create a Time object to represent the high score
					break; // Exit the loop once the high score is found
				}
			}
		} catch (IOException e) {  // Catch and log any IO exceptions that occur while reading the file
			System.out.println("Error reading high score: " + e.getMessage());
		}
		return highScore;  // Return the high score, either the found score or the default value if not found
	}

	/* Method Description: This method called saveHighScore saves a new high score for the given room into the "highscore.txt" file.
	 * Parameter: String roomName  --> The name of the room for which the high score is being saved.
	 *            Time newHighScore --> The new high score to be saved (represented by a Time object).
	 * Return Type: void
	 */
	public void saveHighScore(String roomName, Time newHighScore) {
		File file = new File("highscore.txt"); // The file where high scores are stored

		// StringBuilder to hold the modified content of the file
		StringBuilder fileContent = new StringBuilder();
		boolean roomFound = false; // Flag to check if the room has been found in the file

		// Reading the existing content of the file
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			String currentRoomName = null;

			// Loop through each line of the file
			while ((line = reader.readLine()) != null) {
				// If the room name is found, update the high score
				if (line.trim().equalsIgnoreCase(roomName)) {
					roomFound = true;
					fileContent.append(line).append("\n");  // Add the room name to the content
					reader.readLine();
					fileContent.append(newHighScore.getMinutes()).append(":").append(newHighScore.getSeconds()).append("\n"); // Add the new high score
					continue; // Skip the current high score line to avoid duplication
				}
				fileContent.append(line).append("\n");  // For all other lines, simply append them as they are
			}
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {  // Write the modified content back into the file
				writer.write(fileContent.toString());
			}
		} catch (IOException e) {// Catch any IO exceptions and print an error message
			System.out.println("Error saving high score: " + e.getMessage());
		}
	}


	/* Method Description: This method called displayGIF helps to show the GIF on the screen for 3 seconds before being removed.
	 * Parameters: String gifPath --> The file name to the GIF that needs to be displayed.
	 * Return Type: void
	 */
	private void displayGIF(String gifPath) {
		// If there is an active GIF being displayed, remove it first
		if (gifLabel != null) {
			JLayeredPane layeredPane = getRootPane().getLayeredPane();
			layeredPane.remove(gifLabel); // Remove the old GIF from the layered pane
			revalidate(); // Revalidate the layout after removing the GIF
			repaint();    // Repaint the panel to update the display
		}

		ImageIcon gifIcon = new ImageIcon(gifPath);
		gifLabel = new JLabel(gifIcon); // Create a new JLabel for the GIF

		// Set the position and size of the GIF (covering the entire panel)
		gifLabel.setBounds(0, 0, getWidth(), getHeight()); 

		JLayeredPane layeredPane = getRootPane().getLayeredPane();
		layeredPane.add(gifLabel, JLayeredPane.MODAL_LAYER);   // Add the GIF to the layered pane, ensuring it's in the topmost layer

		revalidate();
		repaint(); // Revalidate and repaint to ensure everything is updated and visible

		// If a timer exists, stop it before starting a new one
		if (gifTimer != null && gifTimer.isRunning()) {
			gifTimer.stop(); // Stop any active timer
		}

		// Create and start a new timer to remove the GIF after 3 seconds
		gifTimer = new Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {  // Remove the GIF from the layered pane after 3 seconds
				layeredPane.remove(gifLabel);
				revalidate();  // Revalidate the layout after removing the GIF
				repaint();     // Repaint the view to update the UI
			}
		});

		gifTimer.start();// Start the timer to trigger the GIF removal after 3 seconds
	}

	/* Method Description: This method called isCorrectSuspect checks if the selected suspect chosen by the detective matches the correct suspect for the current room. This method compares the provided suspect name selected with the correct suspect for the current room. If they match, it returns true; otherwise, it returns false.
	 * Parameters: String selectedSuspect --> The name of the suspect selected by the user.
	 * Return Type: boolean --> true if the selected suspect is correct, otherwise false
	 */
	private boolean isCorrectSuspect(String selectedSuspect) {
		return selectedSuspect.equals(correctSuspect); // Compare with the correct suspect for the current room
	}

	/* Method Description: This method called getSuspectImage retrieves the file name of the image associated with the given suspect name.
	 * Parameters: String suspectName --> The name of the suspect.
	 * Return type: String --> It returns the generated image file name for suspect.
	 */
	public String getSuspectImage(String suspectName) {
		return "select" + suspectName + ".png";
	}

	/* Method Description: This method called timeIsUpAction is triggered when the countdown timer finishes. It handles the action when the timer runs out. It stops the timer and displays the file report screen.
	 * Parameters: none
	 * Return Type: void
	 */
	public void timeIsUpAction() { // Timer stopped and show report screen
		reportScreen();
	}

	@Override
	/* Method Description: This method called keyPressed handles the key press event to move the detective and handle room transitions. It checks the bounds of the detective's new position to ensure they stay within the valid areas and prevents movement if the detective tries to go out of bounds. It also handles room transitions based on the detective's position.
	 * Parameters: KeyEvent e --> The KeyEvent triggered by the key press.
	 * Return Type: void
	 */
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode(); // Get the key code from the event
		int prevX = detective.getX(); // Store the detective's previous X position
		int prevY = detective.getY(); // Store the detective's previous Y position

		detective.move(key); // Move the detective based on the key pressed
		
		// Define a rectangle for the detective's new position to check for collisions
		Rectangle newDetectiveBounds = new Rectangle(detective.getX(), detective.getY(), 40, 40);

		// Check if the detective is out of bounds in the transportation map screen
		if (methodName.equalsIgnoreCase("transportationMapScreen") && (newDetectiveBounds.x < 2 || newDetectiveBounds.y < 3 || newDetectiveBounds.x + newDetectiveBounds.width > getWidth() || newDetectiveBounds.y + newDetectiveBounds.height > getHeight() || newDetectiveBounds.x <= 10 || newDetectiveBounds.y <= 140  || (newDetectiveBounds.x <= 420 && (newDetectiveBounds.y >= 190 && newDetectiveBounds.y <= 300)) 
				|| ((newDetectiveBounds.x >= 410 && newDetectiveBounds.x >= 450) && (newDetectiveBounds.y >= 250 && newDetectiveBounds.y <= 275)) || ((newDetectiveBounds.x >= 670) && (newDetectiveBounds.y >= 100)) || ((newDetectiveBounds.x >= 420 && newDetectiveBounds.x >= 450) && (newDetectiveBounds.y <= 200)) || ((newDetectiveBounds.x >= 150 && newDetectiveBounds.x <= 400) && (newDetectiveBounds.y >= 350)) || ((newDetectiveBounds.x >= 230 && newDetectiveBounds.x <= 330) && (newDetectiveBounds.y >= 340))
				|| ((newDetectiveBounds.x <= 120) && (newDetectiveBounds.y >= 300)) || ((newDetectiveBounds.x >= 420 && newDetectiveBounds.x >= 450) && (newDetectiveBounds.y >= 275 && newDetectiveBounds.y <= 385)) || ((newDetectiveBounds.x <= 200 && newDetectiveBounds.x <= 300) && (newDetectiveBounds.y >= 458)) || ((newDetectiveBounds.x <= 455 && newDetectiveBounds.x <= 500) && (newDetectiveBounds.y >= 450)) || ((newDetectiveBounds.x >= 500) && (newDetectiveBounds.y >= 450)))) {
			detective.setPosition(prevX, prevY); // Reset position 
			return;
		} 
		// Check if the detective is out of bounds in the library
		else if (methodName.equalsIgnoreCase("library") && 
				( (newDetectiveBounds.x >= 470 && newDetectiveBounds.x <= 600 && newDetectiveBounds.y <= 220)|| (newDetectiveBounds.x >= 200 && newDetectiveBounds.x <= 440) && (newDetectiveBounds.y > 495) || (newDetectiveBounds.x < 240 && newDetectiveBounds.y > 495) || (newDetectiveBounds.x > 440 && newDetectiveBounds.y > 495)  || (newDetectiveBounds.x > 440 && newDetectiveBounds.x <= 600 && newDetectiveBounds.y > 435)  || (newDetectiveBounds.x <235 && newDetectiveBounds.y > 440)  || (newDetectiveBounds.x <92) || (newDetectiveBounds.x <130 && newDetectiveBounds.y <210)  || (newDetectiveBounds.x >92 && newDetectiveBounds.x < 125 && newDetectiveBounds.y <210)  || (newDetectiveBounds.x >530 && newDetectiveBounds.x < 560 && newDetectiveBounds.y <210)  || (newDetectiveBounds.x > 560 && newDetectiveBounds.y <270) 
						|| (newDetectiveBounds.x > 180 && newDetectiveBounds.x <= 525 && newDetectiveBounds.y > 265  && newDetectiveBounds.y < 390) || (newDetectiveBounds.x > 287 && newDetectiveBounds.x <= 500 && newDetectiveBounds.y <255)  || (newDetectiveBounds.x > 180 && newDetectiveBounds.x <= 300 && newDetectiveBounds.y <210) || (newDetectiveBounds.x > 635 && newDetectiveBounds.y <300) 
						|| (newDetectiveBounds.x > 705) || (newDetectiveBounds.x > 630 && newDetectiveBounds.y >=320) || (newDetectiveBounds.x < 500 && newDetectiveBounds.x > 315 && newDetectiveBounds.y <=255) || (newDetectiveBounds.x <= 137 && newDetectiveBounds.y >=375) || (newDetectiveBounds.x >= 555 && newDetectiveBounds.y >=375) || (newDetectiveBounds.x >= 590 && newDetectiveBounds.y >=315) || (newDetectiveBounds.x >= 122 && newDetectiveBounds.y <=150) 				)){
			detective.setPosition(prevX, prevY); // Reset position
			return;
		} // Check if the detective is out of bounds in Wong's Mansion
		else if (methodName.equalsIgnoreCase("wongMansion") && ((newDetectiveBounds.x >180 && newDetectiveBounds.x < 280) && (newDetectiveBounds.y > 380) || (newDetectiveBounds.x >90 && newDetectiveBounds.x < 150) && (newDetectiveBounds.y > 350 && newDetectiveBounds.y < 400) || (newDetectiveBounds.x >40 && newDetectiveBounds.x < 120) && (newDetectiveBounds.y > 420) || (newDetectiveBounds.x >40 && newDetectiveBounds.x < 210) && (newDetectiveBounds.y > 190 && newDetectiveBounds.y < 330) || (newDetectiveBounds.x >270 && newDetectiveBounds.x < 400) && (newDetectiveBounds.y > 150 && newDetectiveBounds.y < 330) || (newDetectiveBounds.x >40 && newDetectiveBounds.x < 115) && (newDetectiveBounds.y < 200)
				|| (newDetectiveBounds.x > 90 && newDetectiveBounds.x < 280) && (newDetectiveBounds.y < 60) || (newDetectiveBounds.x > 260) && (newDetectiveBounds.y < 40) || (newDetectiveBounds.x > 420 ) && (newDetectiveBounds.y < 120) || (newDetectiveBounds.x > 280 ) && (newDetectiveBounds.y > 145 && newDetectiveBounds.y < 230) || (newDetectiveBounds.x > 420 ) && (newDetectiveBounds.y > 230 && newDetectiveBounds.y < 310)
				|| (newDetectiveBounds.x > 630 ) && (newDetectiveBounds.y > 220 ) || (newDetectiveBounds.x > 320 && newDetectiveBounds.x < 380) && (newDetectiveBounds.y > 300  && newDetectiveBounds.y < 360) || (newDetectiveBounds.x > 420 && newDetectiveBounds.x < 450) && (newDetectiveBounds.y > 310  && newDetectiveBounds.y < 330) || (newDetectiveBounds.x > 360) && (newDetectiveBounds.y > 470)|| (newDetectiveBounds.x < 310 ) && (newDetectiveBounds.y > 470) || (newDetectiveBounds.x >= 310 && newDetectiveBounds.x < 360) && (newDetectiveBounds.y > 500) || (newDetectiveBounds.x < 80) || (newDetectiveBounds.x >= 430 && newDetectiveBounds.x < 540) && (newDetectiveBounds.y > 440) || (newDetectiveBounds.x >= 580 && newDetectiveBounds.x < 700) && (newDetectiveBounds.y > 400) || (newDetectiveBounds.x > 240 && newDetectiveBounds.x < 430) && (newDetectiveBounds.y < 80) || (newDetectiveBounds.x > 580) && (newDetectiveBounds.y > 100 && newDetectiveBounds.y < 210) || (newDetectiveBounds.x <= 150 && newDetectiveBounds.y > 410))) {
			detective.setPosition(prevX, prevY); // Reset position 
			return;
		}

		// Check for room transitions based on the detective's position
		if (methodName.equalsIgnoreCase("transportationMapScreen")) {
			if (storyNum == 2 && newDetectiveBounds.intersects(wongMansionBounds)) {
				storyNum = 2;
				detective.setPosition(325, 480);
				detective.setDirection('U');
				wongMansion(); // Move detective and transition to Wong's Mansion
				return;
			}
			else if (storyNum == 1 && newDetectiveBounds.intersects(libraryBounds)) {
				storyNum = 1;
				detective.setPosition(392, 465);
				detective.setDirection('U');
				library(); // Move detective and transition to library
				return;
			} else if (storyNum == 0 && newDetectiveBounds.intersects(conferenceBounds)) {
				storyNum = 0;
				conferenceRoom(); // Transition to conference room
				return;
			}
		} 
		repaint();
	}

	/* Method Description: This method is the main method. It is entry point that creates a new JFrame window to run "Who The OPP?".
	 * Parameters: String[] args --> Command-line arguments
	 * Return type: void
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("Who The Opp?");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		WhoTheOppGame gamePanel = new WhoTheOppGame(); // Creates an instance of the game panel
		frame.getContentPane().add(gamePanel);
		frame.setVisible(true); // Makes the window visible
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}

}
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * CrawlGui is to extend javafx.application.Application. When run, it expects a
 * single command line argument which is the name of a map file to load. If this
 * argument is missing the message \Usage: java CrawlGui mapname" is to be
 * printed to standard error and the program will exit with status 1. If the
 * argument is present but the map can not be loaded for some reason, \Unable to
 * load file" is to be printed to standard error and the program will exit with
 * status 2. (Any output to standard error should be followed by a newline).
 * There are three main parts at the top level. Button area: The area to the
 * right, it should keep a fixed width when the window is resized. Message area:
 * The area at the bottom displays text messages (It should occupy the full
 * width of the window and resize with the window). Map area: displays the map.
 * It should resize as the window is resized.
 */
public class CrawlGui extends Application {

    //start room to load
    private Room startRoom;
    private Player player;
    private Room originalRoot;

    public static void main(String args) {
        //if something goes wrong with start()
        launch(args);
    }

    /**
     * load the main with canvas, buttons, and textarea. Set button actions for
     * all buttons north, south, east, west buttons call private method
     * movePlayer() look button displays all contents in that room examine,
     * drop, take, fight, save call displayDialog() from Dialog class examine
     * accept user input and display Thing in room that match with input
     */
    @Override
    public void start(Stage primaryStage) {
        //set the title of the window(Stage)
        primaryStage.setTitle("Crawl - Explore");
        //getting commend-line parameter
        final Parameters params = getParameters();
        final List<String> parameters = params.getRaw();
        final String mapName;
        //expect only one argument
        if (parameters.size() == 1) {
            mapName = parameters.get(0);
        } else {
            mapName = "";
        }
        //loadMap to get start room and player
        // Use the parameter as filename
        Object[] map = MapIO.loadMap(mapName);
        //check whether map file can be loaded or not
        if (mapName.equals("")) {
            System.err.println("Usage: java CrawlGui mapname");
            System.exit(1);
        } else if (map == null) {
            System.err.println("Unable to load file");
            System.exit(2);
        } else {
            //Declaring Nodes
            BorderPane borderPane;
            GridPane gridPane;
            TextArea textArea;
            Button north, east, south, west,
                    look, examine, drop, take,
                    fight, save;
            Scene scene;
            BoundsMapper bm;
            Cartographer cartographer;
            //pass the start room and player to separate variables
            startRoom = (Room) map[1];
            originalRoot = (Room) map[1];
            player = (Player) map[0];
            //add the player in the start room
            startRoom.enter(player);
            //Call BoundsMapper walk method to add coordinates to all rooms
            bm = new BoundsMapper(originalRoot);
            bm.walk();
            //Pass that BoundsMapper to Cartographer to draw map
            cartographer = new Cartographer(bm);
            //after successful map load, display "You find yourself in "
            // followed by the description of the start room.
            textArea = new TextArea("You found yourself in the "
                    + startRoom.getDescription());
            //Creating Buttons with their titles
            north = new Button("North");
            south = new Button("South");
            east = new Button("East");
            west = new Button("West");
            look = new Button("Look");
            examine = new Button("Examine");
            take = new Button("Take");
            drop = new Button("Drop");
            fight = new Button("Fight");
            save = new Button("Save");
            //call movePlayer() helper method in east,north,south,west actions
            //I use lambda expression
            east.setOnAction(e -> {
                movePlayer(player, "East", textArea);
                cartographer.update(bm);
            });
            north.setOnAction(e -> {
                movePlayer(player, "North", textArea);
                cartographer.update(bm);
            });
            south.setOnAction(e -> {
                movePlayer(player, "South", textArea);
                cartographer.update(bm);
            });
            west.setOnAction(e -> {
                movePlayer(player, "West", textArea);
                cartographer.update(bm);
            });

            // display "room_desc - You see:" and list the Things in the room
            // display "You are carrying:" and
            // list the Things from player inventory
            // display "worth total_value_of_things in total" at the end
            look.setOnAction(e -> {
                String desc = startRoom.getDescription();
                textArea.appendText("\n" + desc + " - You see:");
                for (Thing t : startRoom.getContents()) {
                    textArea.appendText("\n " + t.getShortDescription());
                }
                textArea.appendText("\nYou are carrying:");
                double total = 0.0;
                for (Thing t : player.getContents()) {
                    textArea.appendText("\n " + t.getShortDescription());
                    if (t instanceof Treasure) {
                        total += ((Treasure) t).getValue();
                    } else if (t instanceof Critter) {
                        total += ((Critter) t).getValue();
                    }
                }
                textArea.appendText(
                        "\nworth " + String.format("%.1f", total) + ""
                                + " in total");
            });

            //call displayDialog() from Dialog class to know Examine What
            //if Thing found in the room, display the long desc of that Thing
            //if not found, display "Nothing found with that name"
            examine.setOnAction(e -> {
                String input = Dialog.displayDialog("Examine "
                        + "What?");
                boolean foundDesc = false;
                for (Thing t : startRoom.getContents()) {
                    if (t.getShortDescription()
                            .equals(input)) {
                        textArea.appendText("\n" + t.getDescription());
                        foundDesc = true;
                        break;
                    }
                }
                if (!foundDesc && input != null) {
                    textArea.appendText("\nNothing found with that name");
                }
            });

            //call diaplayDialog() from Dialog class to know Item to drop
            //if Thing found in the player inventory, drop that Thing
            //if not found, display "Nothing found with that name"
            drop.setOnAction(e -> {
                String input = Dialog.displayDialog("Item to drop?");
                boolean foundDesc = false;
                for (Thing t : player.getContents()) {
                    if (t.getShortDescription()
                            .equals(input)) {
                        player.drop(t);
                        startRoom.enter(t);
                        foundDesc = true;
                        cartographer.update(bm);
                        break;
                    }
                }
                if (!foundDesc && input != null) {
                    textArea.appendText("\nNothing found with that name");
                }
            });

            //call diaplayDialog() from Dialog class to know Take what
            //if Thing found in the room and it is not a Player,
            //make sure it is not a mob and it is not alive.
            //Otherwise, fail silently.
            //if Thing not found, display "Nothing found with that name"
            take.setOnAction(e -> {
                String input = Dialog.displayDialog("Take what?");
                boolean foundDesc = false;
                for (Thing t : startRoom.getContents()) {
                    if (t.getShortDescription().equals(input)
                            && !(t instanceof Player)) {
                        if (!(t instanceof Mob) || !((Mob) t).isAlive()) {
                            if (startRoom.leave(t)) {
                                player.add(t);
                                startRoom.leave(t);
                            }
                        }
                        foundDesc = true;
                        cartographer.update(bm);
                        break;
                    }
                }
                if (!foundDesc && input != null) {
                    textArea.appendText("\nNothing found with that name");
                }
            });

            //adding all buttons into allBtn[] array
            //will pass this to disableAllBtns() method
            Button allBtn[] = {north, east, south, west, look, drop, fight,
                    save,
                    examine, take};
            //call diaplayDialog() from Dialog class to know Fight what
            //if alive Critter found in the room, fight that Critter
            //if Critter dead, display "You won"
            //if Player dead, display "Game over" and
            // call disableAllBtns() to disable all buttons
            fight.setOnAction(e -> {
                String input = Dialog.displayDialog("Fight what?");
                for (Thing t : startRoom.getContents()) {
                    if (t.getShortDescription().equals(input)
                            && t instanceof Critter
                            && ((Critter) t).isAlive()) {
                        player.fight((Critter) t);
                        if (player.getHealth() > 0) {
                            textArea.appendText("\nYou won");
                            cartographer.update(bm);
                        } else {
                            textArea.appendText("\nGame over");
                            cartographer.update(bm);
                            disableAllBtns(true, allBtn);
                        }
                    }
                }
            });

            //call diaplayDialog() from Dialog class to know Save filename
            //pass the current room and filename to MapIO.saveMap()
            //display "Saved" if success
            //display "Unable to save" if not success
            save.setOnAction(e -> {
                String input = Dialog.displayDialog("Save filename?");
                if (MapIO.saveMap(originalRoot, input)) {
                    textArea.appendText("\nSaved");
                } else {
                    textArea.appendText("\nUnable to save");
                }

            });

            //Use GridPane for buttons
            gridPane = new GridPane();
            gridPane.add(north, 1, 0);
            gridPane.add(west, 0, 1);
            gridPane.add(east, 2, 1);
            gridPane.add(south, 1, 3);
            gridPane.add(look, 0, 4);
            gridPane.add(drop, 0, 5);
            gridPane.add(fight, 0, 6);
            gridPane.add(save, 0, 7);
            gridPane.add(examine, 1, 4);
            gridPane.add(take, 1, 5);

            //set the cartographer to stackpane to get center position
            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(cartographer);
            //BorderPane to contain gridlayout,stackpane and text Area
            borderPane = new BorderPane();
            borderPane.setCenter(stackPane);
            borderPane.setBottom(textArea);
            borderPane.setRight(gridPane);
            //Passing borderPane to scene
            scene = new Scene(borderPane);
            //set that scene to primaryStage
            primaryStage.setScene(scene);
            primaryStage.show();

        }

    }

    /**
     * Player move to other room if player leave failed, show "Something
     * prevents you from leaving" if player leave the room from wrong exit, show
     * "No door that way" if player success moving to another room, show "You
     * Enter " with the room description
     */
    private void movePlayer(Player player, String direction,
            TextArea textArea) {
        //if player leave success
        if (!startRoom.leave(player)) {
            textArea.appendText(
                    "\nSomething prevents you from leaving");
        } else if (startRoom.getExits().containsKey
                (direction)) {
            Room destination = startRoom.getExits().get(direction);
            textArea.appendText("\nYou Enter " + destination
                    .getDescription());
            //enter to desitination room
            destination.enter(player);
            //make that room a start room
            startRoom = destination;
        } else {
            //if player leave success, but there's no exit, then
            //put the player to start room again
            textArea.appendText("\nNo door that way");
            startRoom.enter(player);
        }
    }

    /**
     * Disable all buttons after Gave Over
     */
    private void disableAllBtns(boolean disabled, Button allBtn[]) {
        for (Button btn : allBtn) {
            btn.setDisable(disabled);
        }
    }
}



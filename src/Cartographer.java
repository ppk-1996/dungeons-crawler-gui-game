import java.util.Map.Entry;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * Cartographer is to extend javafx.scene.canvas.Canvas. This class will be
 * responsible for rendering the \map" in the GUI. It will draw all reachable
 * rooms starting from a \start room". Each room is rendered as a square with
 * small strokes into the interior at the midpoints of edges where there is an
 * exit to an adjacent room (ignore exits which are not labelled North, South,
 * East or West). For this assignment, you may assume that all exits are two
 * way. The player is to be indicated with a @ in the top left quadrant (ie the
 * top left corner) of the room. If any treasure is present in the room, a $ is
 * drawn in the top right quadrant. If any live critters are present in the
 * room, a M is drawn in the bottom left quadrant. Any dead critters are
 * indicated with a m in the bottom right quadrant. Your cartographer must be
 * large enough to show the entire loaded map. That is, your initial window size
 * will vary depending on the map being loaded.
 */
public class Cartographer extends Canvas {

    private int roomWidth = 40;
    private int roomHeight = 40;
    private int exitLength = roomWidth / 8;
    private int wMidPoint = roomWidth / 2;
    private int hMidPoint = roomHeight / 2;
    private int extraPixel = 1;
    private int xOffset;
    private int yOffset;
    private GraphicsContext gc = this.getGraphicsContext2D();

    /**
     * Cartographer is to extend javafx.scene.canvas.Canvas. This class will be
     * responsible for rendering the "map" in the GUI. It will draw all
     * reachable rooms starting from a "start room".
     *
     * @param bm BoundsMapper to get coordinates and rooms
     */
    Cartographer(BoundsMapper bm) {
        super();
        drawMap(bm);
    }

    private void drawMap(BoundsMapper bm) {
        setOffset(bm);
        setCanvasArea(bm);
        //loop through all room and pair
        for (Entry<Room, Pair> rp : bm.coords.entrySet()) {
            Room room = rp.getKey(); // get room
            Pair pair = rp.getValue(); //get pair
            //add offsets to x * width and y * height
            double x = xOffset + (pair.x * roomWidth);
            double y = yOffset + (pair.y * roomHeight);
            //draw room as rectangle
            drawRooms(x, y);
            //draw exits of the room as short lines in the middle
            drawExits(room, x, y);
            //draw things in the room
            drawThings(room, x, y);
        }
    }

    private void setOffset(BoundsMapper bm) {
        // -xMin * room width to get x coordinate offset
        xOffset = (-bm.xMin * roomWidth);
        // -yMin * room height to get y coordinate offset
        yOffset = (-bm.yMin * roomHeight);
    }

    /*
    Canvas width is produced by (xMin + xMax )* room width
    We need to plus one for (0,0) room
    extra pixel is added for displaying full rectangles
    Canvas height is also calculated the same
    */
    private void setCanvasArea(BoundsMapper bm) {
        int canvasWidth = (-bm.xMin + bm.xMax + 1) * roomWidth + extraPixel;
        int canvasHeight = (-bm.yMin + bm.yMax + 1) * roomHeight + extraPixel;
        this.setWidth(canvasWidth);
        this.setHeight(canvasHeight);
    }

    //draw player,treasure, critter(alive) and critter(dead)
    private void drawThings(Room room, double x, double y) {
        for (Thing t : room.getContents()) {
            //The player is to be indicated with a @ in the top left quadrant
            //(ie the top left corner) of the room.
            if (t instanceof Player) {
                gc.fillText("@", x + extraPixel * 2,
                        y + hMidPoint - (hMidPoint / 2));
            }
            //If any treasure is present in the room,
            // a $ is drawn in the top right quadrant.
            if (t instanceof Treasure) {
                gc.fillText("$", x + wMidPoint + extraPixel * 2,
                        y + hMidPoint - (hMidPoint / 2));
            }
            //If any live critters are present in the room, a M is drawn in
            // the bottom left quadrant.
            if (t instanceof Critter && ((Critter) t).isAlive()) {
                gc.fillText("M", x + extraPixel * 2,
                        y + hMidPoint + (hMidPoint / 2));
            }
            //Any dead critters are indicated with a m in the bottom right
            // quadrant.
            if (t instanceof Critter && !((Critter) t).isAlive()) {
                gc.fillText("m", x + wMidPoint + extraPixel * 2,
                        y + hMidPoint + (hMidPoint / 2));
            }
        }
    }

    private void drawExits(Room room, double x, double y) {
        if (room.getExits().containsKey("North")) {
            //draw line from x mid point to up
            gc.strokeLine(x + wMidPoint, y,
                    x + wMidPoint, y + exitLength);
            //draw line from x mid point to down
            //need to add one extra pixel to balance
            gc.strokeLine(x + wMidPoint, y,
                    x + wMidPoint, y - exitLength + extraPixel);
        }
        if (room.getExits().containsKey("West")) {
            //draw line from y mid point to right
            gc.strokeLine(x + exitLength, y + hMidPoint,
                    x, y + hMidPoint);
            //draw line from y mid point to left
            //need to add one extra pixel to balance
            gc.strokeLine(x - exitLength + extraPixel, y + hMidPoint,
                    x, y + hMidPoint);
        }
    }

    //draw a room as a rectangle with after offsetting x and y value
    private void drawRooms(double x, double y) {
        gc.strokeRect(x - 0.5 + extraPixel,
                y - 0.5 + extraPixel,
                roomWidth,
                roomHeight);
    }

    //call by buttons actions in CrawlGui
    //clear the canvas and draw the map again
    void update(BoundsMapper bm) {
        gc.clearRect(0, 0, this.getWidth(), this.getHeight());
        drawMap(bm);
    }

}


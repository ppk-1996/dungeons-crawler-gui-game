import java.util.Optional;
import javafx.scene.control.TextInputDialog;
import javafx.stage.StageStyle;

/**
 * Helper class to display TextInputDialog in button actions
 */
class Dialog {
    //to return the user input
    private static String input;

    /**
     * display TextInputDialog
     * @param title title of the dialog box
     * @return user input or null on cancel
     */
    static String displayDialog(String title) {
        //if user click cancel, input will return null
        input=null;
        TextInputDialog dialog = new TextInputDialog();
        // this wasn't changing anything in my PC
        dialog.initStyle(StageStyle.UTILITY);
        //set the tile of the dialog
        dialog.setTitle(title);
        //no header, content, graphic
        dialog.setHeaderText(null);
        dialog.setContentText(null);
        dialog.setGraphic(null);
        //get result of the user input when user click ok
        Optional<String> result = dialog.showAndWait();
        //if user click ok, get result string from textfield
        result.ifPresent(e -> input = result.get());
        //if user click cancel, return null
        return input;
    }
}
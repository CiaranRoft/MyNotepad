/*
 * Programmer : Ciaran Roft
 * Date: 22/08/2017
 * Description : V1.0 - A clone of the notepad application. MyNotepad can, save open and create new txt files. 
 *      If the user tries to exit the application or tries to open another document, 
 *      it will display a alert box asking if they want to save. 
 *      
 */
package mynotepad;

/**
 *
 * @author Roft
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

public class MyNotepad extends Application {
    boolean fresh = true; // Boolean to hold wether or not a file has been changed 
    String filename = "Untitled"; // Global string to hold the file name of the file that is open
    File file = new File("");
    @Override
    public void start(Stage primaryStage) throws Exception{  
        
        // Creating the text area where text is entered
        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(100);
        textArea.setPrefColumnCount(100);
        
        // Creating a lisener on the textArea to mark the document once a change has been made
        textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if(fresh){
                    Mark(); // Calling the mark method to flipp the boolean once
                }
            }
        });
        
        // Method to ensure greater control once the RED X button is pressed
        primaryStage.setOnCloseRequest(evt -> {
            //prevent window from closing
            evt.consume();
            //Shutdown method for shuting down window
            Shutdown(primaryStage, textArea);
        });
        
        BorderPane root = new BorderPane(textArea); // Allows children to be placed top, left, right, and center positions
        Scene scene = new Scene(root,1200, 600); // Creatibng the container that will houses all content
        
        // Menu Bar at the top of the application
        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());// Ensuring the width of the MenuBar is the width of the application
        root.setTop(menuBar); // Setting MenuBar to top of the BorderPane
        
        // File menu - new open, save, exit
        Menu fileMenu = new Menu("File");// Creaing the file menu button
        
        // Creating a new file button
        MenuItem newMenuItem = new MenuItem("New");
        newMenuItem.setOnAction(actionEvent -> CreateNewFile(primaryStage, textArea));
        
        // Opening existing file button
        MenuItem openMenuItem = new MenuItem("Open");
        openMenuItem.setOnAction(actionEvent -> Open(primaryStage, textArea));
        
        // Saving a file button
        MenuItem saveMenuItem = new MenuItem("Save");
        saveMenuItem.setOnAction(actionEvent -> Save(primaryStage, textArea.getText()));
               
        //Handeler for the CTRL N : New, CTRl O : Open, and CTRL S : Save
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            final KeyCombination newKeyComb = new KeyCodeCombination(KeyCode.N,KeyCombination.CONTROL_DOWN); // CTRL N
            final KeyCombination openKeyComb = new KeyCodeCombination(KeyCode.O,KeyCombination.CONTROL_DOWN); // CTRL O
            final KeyCombination saveKeyComb = new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN); // CTRL S
            
            public void handle(KeyEvent ke) {
                if (newKeyComb.match(ke)) {
                    CreateNewFile(primaryStage, textArea);
                }
                else if(openKeyComb.match(ke)){
                    Open(primaryStage, textArea);
                }
                else if(saveKeyComb.match(ke)){
                    Save(primaryStage, textArea.getText());
                }
            }
        });
        
        // Exiting the application
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(actionEvent -> Shutdown(primaryStage, textArea));
        
        // Adding all of the button to the file menu button
        fileMenu.getItems().addAll(newMenuItem, openMenuItem, saveMenuItem,
            new SeparatorMenuItem(), exitMenuItem);
        
        menuBar.getMenus().addAll(fileMenu); // Addign the file button to the menu bar
        
        primaryStage.setTitle("Untitled Document - MyNotepad");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /*
        Method to handle the alert box that appears if the user could close a document before saving the files.
        Alert will appear when user trys to open a new file or try to exit the application
    */
    public void SaveAlertBox(Stage primaryStage, TextArea textArea, String message){
        // Initilizing the AlertBox
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("MyNotepad");
        alert.setHeaderText("Do you want to save changes to " + filename);
        
        // Initilizing the three buttons of the AlertBox
        ButtonType saveButton = new ButtonType("Save");
        ButtonType dontSaveButton = new ButtonType("Dont Save");
        ButtonType cancelButton = new ButtonType("Cancel");
        
        // Adding buttons to the AllertBox and storing the button type that was pressed
        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        
        // If statement to preform different actions depending on the button press.  
        // "Don't Save" - the apllication will continue with opening a new file of closing the application depending on the message
        if (result.get() == saveButton){
            Save(primaryStage, textArea.getText());
        }else if (result.get() == dontSaveButton && message == "new") {
            textArea.setText("");
            primaryStage.setTitle("Untitled Document - MyNotepad");
            fresh = true;
        }else if (result.get() == dontSaveButton && message == "close") {
            Platform.exit();
        } else if (result.get() == dontSaveButton && message == "open") {
            fresh = true;
            Open(primaryStage, textArea);
        } else{
            alert.close();   
        }
    }
    
    /*
        Method to shutdown the application
    */
    public void Shutdown(Stage primaryStage, TextArea textArea){
        // Check the global fresh boolean to see if a change has been made
        if(!fresh){
             SaveAlertBox(primaryStage, textArea, "close"); // If a change has been made AlertBox is displayed to ask them to save
        }
        else{
            Platform.exit(); // If no change has been made then the application is closed
        }   
    }
    
    /*
        Method to flip the fresh boolean
    */
    public  void Mark(){
        fresh = false;
    }
    
    /*
        Method to create a new file
    */
    public void CreateNewFile(Stage primaryStage, TextArea textArea){
        // Check the global fresh boolean to see if a change has been made before new file is opened
        if(!fresh){
             SaveAlertBox(primaryStage, textArea, "new"); // If a change has been made AlertBox is displayed to ask them to save
        } 
        else{
            // If no change has been made then the TextArea is cleared 
            textArea.setText("");
            primaryStage.setTitle("Untitled Document - MyNotepad");
        }
    }
    
    /*
        Method to open a file
    */
    public void Open(Stage primaryStage, TextArea textArea){
        if(!fresh){
             SaveAlertBox(primaryStage, textArea, "open"); // If a change has been made AlertBox is displayed to ask them to save
        } 
        else{
            // Initilizing the file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");

            // Selects the file chossen reads the text inside and sets the text of the text area to the text
            file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                textArea.setText(readFile(file));
                filename = file.getName();
                primaryStage.setTitle(filename + " - MyNotepad");
                fresh = true; //File just opened so no change could be made
            }  
        } 
    }
    /*
        Method to read the file chossen and return the contents
    */    
    private String readFile(File file){
        StringBuilder stringBuffer = new StringBuilder();
        BufferedReader bufferedReader = null;
         
        try {
 
            bufferedReader = new BufferedReader(new FileReader(file)); // Buffered reader that will read file
            //System.out.println(bufferedReader);
            String text; // String to hold each line 
            while ((text = bufferedReader.readLine()) != null) {
                //System.out.print(text);
                stringBuffer.append(text+"\n"); // .readLine does not return the terminating string.
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyNotepad.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyNotepad.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                Logger.getLogger(MyNotepad.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
         
        return stringBuffer.toString();
    }
    
    /*
        Method to Save the TXT files
    */
    public void Save(Stage primaryStage, String text2save){
        if (file.getName().equals("")){
            // Initilizing the file chooser
            FileChooser fileChooser = new FileChooser();

            //Set extension filter  - ONLY TXT FILES NOW
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Save File");
            file = fileChooser.showSaveDialog(primaryStage);//Show save file dialog
        }
        
        try{

            FileWriter fstream = new FileWriter(file.getAbsoluteFile());// Initilizing the file writer to read a stream of characters
            BufferedWriter out = new BufferedWriter(fstream); // Buffered Writer to read stream and, 
            out.write(text2save); // Writing the text to the file 
            out.close(); //Close the output stream
            primaryStage.setTitle(file.getName() + " - MyNotepad"); // Renaming the title
            fresh = true;
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /*
        Main method
    */
    public static void main(String [] args){
        launch(args); // Launching the APP
    }
}

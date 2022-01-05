package com.kenzie.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.Scanner;

public class Main extends Application {
    public static final String GAME_TITLE = "Kenzie Quiz Bowl";
    public static final String WELCOME_MESSAGE = "Welcome to the " + GAME_TITLE + "!";
    public static final String GOODBYE_MESSAGE = "Thank you for playing with us today :D\nGoodbye!";

    private static GameDaemon gameDaemon = null;

    private Scene welcomeScene;
    private Button btnToConsole;

    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);
        gameDaemon = GameDaemon.getInstance();

        //MainFrame mainFrame = new MainFrame(gameDaemon); // deprecated from swift ui
        launch(args);

        switch (gameDaemon.getGameState()) {
            case QUIT:
                return;
            case TO_CONSOLE:
                break;
            default:
                System.out.println("Error: Unexpected input from getGameState");
        }

        // Console execution ----------------------------------------------------------------------
        // welcome message
        System.out.println(WELCOME_MESSAGE);

        String inputResponse = "";
        boolean continueGame = true;
        do {
            try {
                inputResponse = inputScanner.nextLine();
            } catch (IllegalStateException illegalStateException) {
                System.out.println("Fatal Error: Could not open Scanner.");
                return;
            } catch (java.util.NoSuchElementException noSuchElementException) {
                System.out.println("Error: No line was found.");
            }

            continueGame = gameDaemon.reportInput(inputResponse);
        } while (continueGame);

        // output goodbye
        System.out.println(GOODBYE_MESSAGE);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(GAME_TITLE);
        generateWelcomeScene();

        primaryStage.setScene(this.welcomeScene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if(gameDaemon.getGameState() == GameState.TO_CONSOLE) {
            return;
        }
        gameDaemon.setGameState(GameState.QUIT);
    }

    private void generateWelcomeScene() {
        if (welcomeScene != null) {
            return;
        }
        this.btnToConsole = new Button("Go To Console Mode");
        this.btnToConsole.setOnAction(e -> {
            gameDaemon.setGameState(GameState.TO_CONSOLE);
            Platform.exit();
        });
        //this.btnToConsole.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");

        StackPane layout = new StackPane();
        layout.getChildren().add(btnToConsole);
        //layout.setBackground(new Background(new BackgroundFill(Color.color(0.1, 0.6, 0.7), CornerRadii.EMPTY, Insets.EMPTY)));
        this.welcomeScene = new Scene(layout, 400, 200);
        welcomeScene.getStylesheets().add(GAME_TITLE.replaceAll(" ", "") + ".css");
    }

//    @Override
//    public void handle(ActionEvent event) {
//        System.out.println("hey");
//        if (event.getSource() == this.btnToConsole) {
//            gameDaemon.setGameState(GameState.TO_CONSOLE);
//            Platform.exit();
//        }
//    }
}


package com.kenzie.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main extends Application {
    public static final String GAME_TITLE = "Kenzie Quiz Bowl"; // also used for resource file names
    public static final int GAME_INPUT_TIMEOUT = 5; // in seconds

    public static final String CONSOLE_WELCOME_MESSAGE = "Welcome to the " + GAME_TITLE + "!";
    public static final String CONSOLE_GOODBYE_MESSAGE = "Thank you for playing with us today :D\nGoodbye!";

    private static GameDaemon gameDaemon = null;

    private Scene welcomeScene;
    @FXML
    private Button btnToConsole;

    public static void main(String[] args) {
        gameDaemon = GameDaemon.getInstance();

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
        System.out.println(CONSOLE_WELCOME_MESSAGE);

        Scanner inputScanner = new Scanner(System.in);
        String inputResponse = "";
        GameState gameState;
        while ((gameState = gameDaemon.getGameState()) != GameState.QUIT) {
            switch (gameState) {
                case TO_CONSOLE:
                    gameDaemon.setGameState(GameState.QUIT);
                    break;
                case GET_NUM_PLAYERS:
                    System.out.print("Please enter the number of players (1 or 2): ");
                    break;
                case GET_GAME_TYPE:
                    System.out.print("What type of game would you like to play?\n" +
                                     "  1) Single Category\n" +
                                     "  2) Mixed Random Categories\n" +
                                     "  3) Full Jeopardy Round\n: ");
                    break;
                case GET_CATEGORY:
                    break;
                case QUIT:
                    break;
                default:
                    // This shouldn't happen
                    System.out.println("ERROR, UNEXPECTED SWITCH " + gameState + " in main()");
            }
            try {
                if ((gameState.getValue() & GameState.TIMEOUT.getValue()) == GameState.TIMEOUT.getValue()) {
                    inputResponse = getConsoleInputWithTimeout(inputScanner);
                } else {
                    inputResponse = getConsoleInput(inputScanner);
                }
            } catch (TimeoutException e) {

            } catch (NoSuchElementException | IllegalStateException e) {
                System.out.println("Error: Scanner could not open or had another internal error.");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            gameDaemon.reportInput(inputResponse);
        }

        System.out.println(CONSOLE_GOODBYE_MESSAGE);
    }

    // GUI Methods --------------------------------------------------------------------------------

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(GAME_TITLE);

        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().
                    getResource("/" + GAME_TITLE.replaceAll(" ", "") + ".fxml")));
        } catch (Exception e) {
            String s = e.getMessage();
        }
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
//        generateWelcomeScene();
//
//        primaryStage.setScene(this.welcomeScene);

        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (gameDaemon.getGameState() == GameState.TO_CONSOLE) {
            return;
        }
        gameDaemon.setGameState(GameState.QUIT);
    }


    // This function seems as it will be deprecated shortly
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

    public void btnToConsole(ActionEvent actionEvent) {
        gameDaemon.setGameState(GameState.TO_CONSOLE);
        Platform.exit();
    }
//    @Override
//    public void handle(ActionEvent event) {
//        System.out.println("hey");
//        if (event.getSource() == this.btnToConsole) {
//            gameDaemon.setGameState(GameState.TO_CONSOLE);
//            Platform.exit();
//        }
//    }

    // Console Methods ----------------------------------------------------------------------------

    private static String getConsoleInput(Scanner inputScanner)
            throws NoSuchElementException, IllegalStateException
    {
        return inputScanner.nextLine();
    }

    // Ref: https://stackoverflow.com/questions/61807890/user-input-with-a-timeout-in-java
    private static String getConsoleInputWithTimeout(Scanner inputScanner) throws TimeoutException {
        Callable<String> callable = () -> inputScanner.nextLine();
        ExecutorService service = Executors.newFixedThreadPool(1);
        Future<String> inputFuture = service.submit(callable);

        try {
            return inputFuture.get(GAME_INPUT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            service.shutdown();
        }
    }
}


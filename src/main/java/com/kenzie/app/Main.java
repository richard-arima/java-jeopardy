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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.*;

public class Main extends Application {
    public static final String GAME_TITLE = "Kenzie Quiz Bowl"; // also used for resource file names
    public static final int GAME_INPUT_TIMEOUT = 5; // in seconds

    public static final String CONSOLE_WELCOME_MESSAGE = "Welcome to the " + GAME_TITLE + "!";
    public static final String CONSOLE_GOODBYE_MESSAGE = "Thank you for playing with us today :D\nGoodbye!";
    public static final String CONSOLE_PADDING = "\n";

    private static GameDaemon gameDaemon = null;

    private Scene welcomeScene;
    @FXML
    private Button btnToConsole;

    public static void main(String[] args) {
        gameDaemon = GameDaemon.getInstance();

        // GUI execution --------------------------------------------------------------------------
        launch(args);

        switch (gameDaemon.getGameState()) {
            case QUIT:
                return;
            case TO_CONSOLE:
                gameDaemon.setGameState(GameState.GET_NUM_PLAYERS);
                break;
            default:
                System.out.println("Error: Unexpected gameState from getGameState");
        }

        // Console execution ----------------------------------------------------------------------
        System.out.println(CONSOLE_WELCOME_MESSAGE + CONSOLE_PADDING);

        String inputResponse = "";
        GameState gameState;
        while ((gameState = gameDaemon.getGameState()) != GameState.QUIT) {
            switch (gameState) {
                case TO_CONSOLE:
                    gameDaemon.setGameState(GameState.GET_NUM_PLAYERS);
                case GET_NUM_PLAYERS:
                    System.out.print("Please enter the number of players (1 or 2)\n=> ");
                    break;
                case GET_GAME_TYPE:
                    System.out.print("What type of game would you like to play?\n" +
                            "  1) Mixed Random Categories\n" +
                            "  3) Full Jeopardy Round\n=> ");
                    break;
                case GET_CATEGORY:
                    // todo: get categories
                    // todo: format them and show to users and ask for response
                    switch (gameDaemon.getGameType()) {
                        case MIXED_RANDOM_CATEGORIES:
                            System.out.println("Mixed Random Category incomplete");
                            break;
                        case FULL_JEOPARDY:
                            System.out.println("Full Jeopardy incomplete");
                            break;
                        default:
                    }
                case SHOW_RULES:
                case GET_ANSWER_TIMEOUT:
                    System.out.print("Your Answer is => ");
                    break;
                case QUIT:
                    break;
                default:
                    // This shouldn't happen
                    System.out.println("ERROR, UNEXPECTED SWITCH " + gameState + " in main()");
            }
            try {
                if ((gameState.getValue() & GameState.TIMEOUT.getValue()) == GameState.TIMEOUT.getValue()) {
                    inputResponse = getConsoleInputWithTimeout();
                } else {
                    inputResponse = getConsoleInput();
                }
            } catch (TimeoutException e) {
                // todo: User has timed out for response,
                System.out.println("TIMEOUT");
                System.out.println(e.getMessage());
                inputResponse = null;
            } catch (NoSuchElementException | IllegalStateException | InterruptedException e) {
                System.out.println("Error: Scanner could not open or had another internal error.");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            if (!gameDaemon.reportInput(inputResponse)) {
                System.out.println("Invalid Entry, please try again.\n");
            }
        }

        System.out.println(CONSOLE_PADDING + CONSOLE_GOODBYE_MESSAGE);
        System.exit(0);
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


    // Console Methods ----------------------------------------------------------------------------

    private static String getConsoleInput()
            throws NoSuchElementException, IllegalStateException
    {
        try {
            return new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            System.out.println("Error: IOException in getConsoleInput()");
            return null;
        }
    }

    // based on: https://pretagteam.com/question/user-input-with-a-timeout-in-java
    public static String getConsoleInputWithTimeout() throws InterruptedException, TimeoutException {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        String input = null;
        Callable<String> stringCallable = () -> {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                try {
                    while (!bufferedReader.ready()) {
                        Thread.sleep(50);
                    }
                    return bufferedReader.readLine();
                } catch (IOException e) {
                    System.out.println("Error: IOException in getConsoleInputWithTimeout() Lambda");
                    return null;
                }
            };

        try {
            Future<String> result = ex.submit(stringCallable);
            try {
                input = result.get(GAME_INPUT_TIMEOUT, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            } catch (TimeoutException e) {
                result.cancel(true);
                throw new TimeoutException("Timeout Encountered in getConsoleInputWithTimeout()");
            }
        } catch (RejectedExecutionException e) {
            System.out.println("Error: RejectedExecutionException in getConsoleInputWithTimeout()");
            e.printStackTrace();
        } finally {
            ex.shutdownNow();
        }
        return input;
    }
}
package com.kenzie.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.*;

public class Main extends Application {
    public static final String GAME_TITLE = "Kenzie Quiz Bowl"; // also used for resource file names
    public static final int GAME_INPUT_TIMEOUT = 10; // in seconds

    public static final String CONSOLE_WELCOME_MESSAGE = "Welcome to the " + GAME_TITLE + "!";
    public static final String CONSOLE_GOODBYE_MESSAGE =
            "Thank you for playing with us today :D\nGoodbye!";

    private static GameDaemon gameDaemon = null;
    private static boolean playConsole = false;


    public static void main(String[] args) {
        gameDaemon = GameDaemon.getInstance();

        launch(args);
        if (playConsole) {
            playConsole();
        }
    }

    // Console Specific ----------------------------------------------------------------------------

    private static boolean playConsole() {
        System.out.println(CONSOLE_WELCOME_MESSAGE + "\n");

        do {
            getNumPlayers();
            getGameType();
            System.out.print("\nPreparing game...");
            if (!gameDaemon.setupGame()) {
                System.out.println("\rError: Could not setup game, Terminating.");
                return false;
            }
            System.out.println("\rGame ready!" + "\n");
            printRules();

            // Game play
            do {
                int currentPlayer = gameDaemon.getCurrentPlayer();
                ClueDTO currentClueDTO = gameDaemon.getCurrentClueDTO();
                System.out.println("\nPlayer " + (currentPlayer + 1) +
                        " - round " + (gameDaemon.getCurrentQuestion() + 1) +
                        ") The category is, " + currentClueDTO.getCategory().getTitle() +
                        ".\nPress enter when ready.");
                getConsoleInput();

                System.out.println( "Q: " + currentClueDTO.getQuestion());
                System.out.println("A --> " + currentClueDTO.getAnswer()); // DEBUG ===================== DEBUG

                String userAnswer;
                try {
                    userAnswer = getConsoleInputWithTimeout().trim();
                    if (gameDaemon.reportAnswer(userAnswer)) {
                        System.out.println("Correct!");
                    } else {
                        System.out.println("Incorrect!\nThe correct answer was... " +
                                currentClueDTO.getAnswer());
                    }
                } catch (TimeoutException e) {
                    System.out.println("Times up! Sorry!!\nThe correct answer was... " +
                            currentClueDTO.getAnswer());
                    gameDaemon.reportAnswer(null);
                }
            } while (gameDaemon.getCurrentPlayer() != -1);

            printResults();
            gameDaemon.resetGame();
        } while (getContinuePlay());

        System.out.println("\n" + CONSOLE_GOODBYE_MESSAGE);
        return true;
    }

    private static void getNumPlayers() {
        while (true) {
            System.out.print("Please enter the number of players (1 or 2)\n=> ");
            int inputAsInt;
            try {
                inputAsInt = Integer.parseInt(getConsoleInput().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid Entry, please try again.\n");
                continue;
            }

            if (gameDaemon.setNumberOfPlayers(inputAsInt)) {
                break;
            } else {
                System.out.println("Invalid Entry, please try again.\n");
            }
        }
    }

    private static void getGameType() {
        boolean valid = false;
        while (!valid) {
            System.out.print("What type of game would you like to play?\n" +
                             "  1) Mixed Values Random Categories\n" +
                             "  2) Full Jeopardy Round\n=> ");

            int inputAsInt;
            try {
                inputAsInt = Integer.parseInt(getConsoleInput().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid Entry, please try again.\n");
                continue;
            }

            switch (inputAsInt) {
                case 1:
                    gameDaemon.setGameType(GameType.RANDOM);
                    valid = true;
                    break;
                case 2:
                    gameDaemon.setGameType(GameType.FULL_JEOPARDY);
                    valid = true;
                    break;
                default:
                    System.out.println("Invalid Entry, please try again.\n");
            }
        }
    }

    private static boolean getContinuePlay() {
        while (true) {
            System.out.print("\nWould you like to play again? (y/n)\n=> ");
            String playAgain = getConsoleInput().trim();
            if (playAgain.compareToIgnoreCase("y") == 0) {
                System.out.print("\n\n");
                return true;
            } else if (playAgain.compareToIgnoreCase("n") == 0) {
                return false;
            } else {
                System.out.println("Invalid Entry, please try again.\n");
            }
        }
    }

    private static void printRules() {
        System.out.println("\n\n" + gameDaemon.getGameRules() + "\n");
    }

    private static void printResults() {
        int[] scores = gameDaemon.getPlayersScore();
        int winningScore;

        if (scores != null) {
            winningScore = scores[0];
            ArrayList <Integer> winningPlayers = new ArrayList<>();
            winningPlayers.add(0);
            if (gameDaemon.getNumberOfPlayers() > 1) {
                for (int i = 1; i < gameDaemon.getNumberOfPlayers(); i++) {
                    if (scores[i] > winningScore) {
                        winningScore = scores[i];
                        winningPlayers.clear();
                        winningPlayers.add(i);
                    } else if (scores[i] == winningScore) {
                        winningPlayers.add(i);
                    }
                }

                StringBuilder sb = new StringBuilder("Player " + (winningPlayers.get(0) + 1) + ",");
                for (int i = 1; i < winningPlayers.size() - 1; i++) {
                    sb.append(" ").append("Player ").append(winningPlayers.get(i) + 1).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                if (winningPlayers.size() > 1) {
                    sb.append(" & ");
                    sb.append("Player ").append(winningPlayers.get(winningPlayers.size() - 1) + 1);
                }
                System.out.println("\n\nThe winning score is " + winningScore + ", held by " +
                        sb + "!! " + getExpression(winningScore));
            } else {
                System.out.println("\n\nYour score is " + winningScore +
                        "!! " + getExpression(winningScore));
            }
        }
    }

    private static String getExpression(int score) {
        String expression = "";
        int percentCorrect = (int)(((double)score /
                (double)GameDaemon.GAME_RANDOM_NUM_QUESTIONS_PER_PLAYER) * 10);
        switch (percentCorrect) {
            case 10:
                expression = "Wow!!! PERFECT ROUND";
                break;
            case 9:
            case 8:
                expression = "Very well done";
                break;
            case 7:
            case 6:
                expression = "Not bad :)";
                break;
            case 5:
            case 4:
                expression = "Hope you had fun at least!";
                break;
            case 3:
            case 2:
            case 1:
                expression = "I mean it could be worse... :{";
                break;
            case 0:
                expression = "Ouch... Maybe read more often";
                break;
            default:
        }
        return expression;
    }

    private static String getConsoleInput() {
        try {
            return new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            System.out.println("Error: IOException in getConsoleInput()");
            return null;
        }
    }

    // based on: https://pretagteam.com/question/user-input-with-a-timeout-in-java
    public static String getConsoleInputWithTimeout() throws TimeoutException {
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
            } catch (ExecutionException | InterruptedException e) {
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

    // GUI Specific --------------------------------------------------------------------------------

    private static Stage stage = null;
    private static Scene welcomeScene = null;
    private static Scene getPlayersScene = null;

    private static int gameReady;

    @FXML
    public ToggleButton btnOnePlayer;
    @FXML
    public ToggleButton btnTwoPlayer;
    @FXML
    public ToggleButton btnRandom;
    @FXML
    public ToggleButton btnFullJeopardy;
    @FXML
    public Text txtWelcomeFeedback;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(GAME_TITLE);
        stage = primaryStage;
        Parent welcome = null;
        Parent getPlayers = null;
        try {
            welcome = FXMLLoader.load(Objects.requireNonNull(getClass().
                    getResource("/" + GAME_TITLE.replaceAll(" ", "") + "Welcome.fxml")));
            getPlayers = FXMLLoader.load(Objects.requireNonNull(getClass().
                    getResource("/" + GAME_TITLE.replaceAll(" ", "") + "GetPlayers.fxml")));
        } catch (IOException e) {
            System.out.println("Error: Could not load FXML file: " + e.getMessage());
            playConsole = true;
            Platform.exit();
        }
        welcomeScene = new Scene(welcome, 600, 400);
        getPlayersScene = new Scene(getPlayers, 600, 400);
        primaryStage.setScene(welcomeScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
    }

    public void btnToConsole(ActionEvent actionEvent) {
        playConsole = true;
        Platform.exit();
    }

    public void btnSwap(ToggleButton btnAction, ToggleButton btnDormant, int mask) {
        gameReady &= (~mask);
        if (btnAction.isSelected()) {
            btnDormant.setSelected(false);
            gameReady += mask;
        }
        txtWelcomeFeedback.setVisible(false);
    }

    public void btnOnePlayerSelected(ActionEvent actionEvent) {
        btnSwap(btnOnePlayer, btnTwoPlayer, 1);
        gameDaemon.setNumberOfPlayers(1);
    }

    public void btnTwoPlayerSelected(ActionEvent actionEvent) {
        btnSwap(btnTwoPlayer, btnOnePlayer, 1);
        gameDaemon.setNumberOfPlayers(2);
    }

    public void btnRandomSelected(ActionEvent actionEvent) {
        btnSwap(btnRandom, btnFullJeopardy, (1<<1));
        gameDaemon.setGameType(GameType.RANDOM);
    }

    public void btnFullJeopardySelected(ActionEvent actionEvent) {
        btnSwap(btnFullJeopardy, btnRandom, (1<<1));
        gameDaemon.setGameType(GameType.FULL_JEOPARDY);
    }

    public void btnBegin(ActionEvent actionEvent) {
        if (gameReady == 3) {
            txtWelcomeFeedback.setVisible(true);
            txtWelcomeFeedback.setText("Preparing game...");
            if (!gameDaemon.setupGame()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not setup game, Terminating.", ButtonType.OK);
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add("/" + GAME_TITLE.replaceAll(" ", "") + ".css");
                alert.showAndWait();
                Platform.exit();
            }
            System.out.println("\rGame ready!" + "\n");
            return;
        }

        switch (gameReady) {
            case 0:
                txtWelcomeFeedback.setText("Please select number of players and type of game");
                break;
            case 1:
                txtWelcomeFeedback.setText("Please select type of game");
                break;
            case 2:
                txtWelcomeFeedback.setText("Please select number of players");
                break;
            case 3: // Already handled
            default:
                // nothing, this is here for code style adherence
        }
        txtWelcomeFeedback.setVisible(true);
    }
}

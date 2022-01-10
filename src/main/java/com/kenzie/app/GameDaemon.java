package com.kenzie.app;

import java.util.ArrayList;
import java.util.Random;

enum GameState {
    SETUP(1),
    TO_CONSOLE(2),
    RESTART(3),

    TIMEOUT(1<<31),         // this bit flag will fall through instead of accept response
    FALLTHROUGH(1<<30),     // this bit flag has a timeout circumstance

    GET(1<<23),
    GET_NUM_PLAYERS(GET.value | 1),
    GET_GAME_TYPE(GET.value | 2),
    GET_CATEGORY(GET.value | 3),
    GET_ANSWER(GET.value | 4),
    GET_ANSWER_TIMEOUT(GET.value | TIMEOUT.value | 1),

    PRESENT(1<<22),
    PRESENT_QUESTION(PRESENT.value | 1),

    PROCESS(1<<21),
    PROCESS_ANSWER(PROCESS.value | 1),

    SHOW(1<<20),
    SHOW_RULES(SHOW.value | 1),
    SHOW_RESULTS(SHOW.value | 2),

    QUIT(1<<15);

    private final int value;

    GameState(int value) {
        this.value= value;
    }

    public int getValue() {
        return this.value;
    }
}

enum GameType {
    MIXED_RANDOM_CATEGORIES,
    FULL_JEOPARDY,
    NONE
}

public class GameDaemon {
    private static GameDaemon gameDaemon;
    private GameState gameState;
    private CustomHttpClient httpClient;

    private int numberOfPlayers = -1; // either 1 or 2
    private int numberOfCluesOnJService = -1;
    private GameType gameType = GameType.NONE; // enum GameType
    private int currentPlayer = -1;
    private String currentClue = null;
    private String currentCategory = null;

    private ArrayList<ArrayList<ClueDTO>> playersWithClues;
    private ArrayList<Integer> cluesUsed;

    private Random randomNumberGenerator;

    public static int GAME_MIXED_RANDOM_CATEGORIES_NUM_QUESTIONS_PRE_PERSON = 10;

    private GameDaemon() {
        this.gameState = GameState.SETUP; // This is here for future implementation of setup and asynchronous methods
        // get info from web
        httpClient = new CustomHttpClient();
        cluesUsed = new ArrayList<>();
        randomNumberGenerator = new Random();

        numberOfCluesOnJService = httpClient.getTotalNumClues();
        if (numberOfCluesOnJService == -1) {
            System.out.println("Error: Could not find number of total clues available from server");
            // Since the server is unreachable, might as well quit while we're behind :D
            this.gameState = GameState.QUIT;
            return;
        }

        CategoryListDTO categories = httpClient.getCategories();
        ClueListDTO clues = httpClient.getCluesWithParameters("category", "1", "value", "100");

        this.gameState = GameState.GET_NUM_PLAYERS;
    }

    public static GameDaemon getInstance() {
        if (gameDaemon == null) {
            gameDaemon = new GameDaemon();
        }
        return gameDaemon;
    }

    // processes input and returns true if success or false if invalid input, state prob doesn't change
    public boolean reportInput(String input) {
        int inputAsInt = -1;
        switch (gameState) {
            case TO_CONSOLE:
                this.gameState = GameState.GET_NUM_PLAYERS;
            case GET_NUM_PLAYERS:
                inputAsInt = parseStringInNumberRange(input, 1, 2);
                if (inputAsInt == -1) {
                    return false;
                }
                this.numberOfPlayers = inputAsInt;
                this.gameState = GameState.GET_GAME_TYPE;
                return true;
            case GET_GAME_TYPE:
                inputAsInt = parseStringInNumberRange(input, 1, 2);
                if (inputAsInt == -1) {
                    return false;
                }
                switch (inputAsInt) {
                    case 1:
                        this.gameType = GameType.MIXED_RANDOM_CATEGORIES;
                        break;
                    case 2:
                        this.gameType = GameType.FULL_JEOPARDY;
                        break;
                    default:
                }
                this.gameState = GameState.GET_CATEGORY;
                return setupGame();
                //return true;
            case GET_CATEGORY:
                setGameState(GameState.GET_ANSWER_TIMEOUT);
                // todo: if valid input, update categories or generate questions for player(s)
                // todo: set currentPlayer to 1
                break;
            case GET_ANSWER_TIMEOUT:
                // todo: see if current player has correct answer for their currentclue
                if (input == null) return false;
                gameState = GameState.QUIT;
                return true;
                //setGameState(GameState.QUIT);
            case QUIT:
                return true;
            case RESTART:
                // todo: reset all internal variables for players and game setup
            default:
                // This shouldn't happen, yet here we are. Nothing defined so far.
        }
        return true;
    }

    private boolean setupGame() {
        switch (this.gameType) {
            case MIXED_RANDOM_CATEGORIES:
                return setupRandomCategoriesGame();
            case FULL_JEOPARDY:
                break;
            default:
        }
        return false;
    }

    public boolean setupRandomCategoriesGame() {
        if ((this.numberOfCluesOnJService == -1) || (this.numberOfPlayers < 1) || (this.gameType == GameType.NONE)) {
            return false;
        }

        playersWithClues = new ArrayList<>();
        for (int i = 0; i < this.numberOfPlayers; i++) {
            playersWithClues.add(new ArrayList<ClueDTO>());
        }

        int currentPlayerIndex = 0;
        int currentQuestionIndex = 0;
        while (playersWithClues.get(this.numberOfPlayers - 1).size() != GAME_MIXED_RANDOM_CATEGORIES_NUM_QUESTIONS_PRE_PERSON) {
            int randomNum = randomNumberGenerator.nextInt(numberOfCluesOnJService + 1);
            ClueDTO clue = httpClient.getClueById(randomNum);
            if (cluesUsed.contains(randomNum)) {
                continue;
            }
            if (currentPlayerIndex == 0) {
                // this is the case we are doing the first and the rest match
                playersWithClues.get(currentPlayerIndex).add(clue);
            } else {
                if (clue.getValue() != playersWithClues.get(0).get(currentQuestionIndex).getValue()) {
                    continue;
                } else {
                    playersWithClues.get(currentPlayerIndex).add(clue);
                }
            }
            cluesUsed.add(randomNum);
            currentPlayerIndex = ++currentPlayerIndex % numberOfPlayers;
            if (currentPlayerIndex == 0) {
                currentQuestionIndex++;
            }
        }
        return true;
    }

    // Parses string to a positive integer within range specified. -1 on any error otherwise
    private int parseStringInNumberRange(String inputString, int rangeMin, int rangeMax) {
        try {
            int inputAsInt = Integer.parseInt(inputString);
            if ((inputAsInt < rangeMin) || (inputAsInt > rangeMax)) {
                return -1;
            }
            return inputAsInt;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean checkValidRange(int input, int rangeMin, int rangeMax) {
        return ((input >= rangeMin) && (input <= rangeMax));
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getNumberOfPlayers() {
        return this.numberOfPlayers;
    }

    public boolean setNumberOfPlayers(int numberOfPlayers) {
        if (checkValidRange(numberOfPlayers, 1, 2)) {
            this.numberOfPlayers = numberOfPlayers;
            return true;
        }
        return false;
    }

    public boolean setNumberOfPlayers(String numberOfPlayers) {
        int inputAsInt = -1;
        try {
            inputAsInt = Integer.parseInt(numberOfPlayers);
        } catch (NumberFormatException e) {
            return false;
        }
        if (checkValidRange(inputAsInt, 1, 2)) {
            this.setNumberOfPlayers(inputAsInt);
            return true;
        }
        return false;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public int getCurrentPlayer() {
        return this.currentPlayer;
    }

    public String getCurrentClue() {
        return this.currentClue;
    }

    public String getCurrentCategory() {
        return this.currentCategory;
    }
}

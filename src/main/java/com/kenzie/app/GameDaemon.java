package com.kenzie.app;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum GameType {
    RANDOM,
    FULL_JEOPARDY,
    NONE
}

public class GameDaemon {
    private static GameDaemon gameDaemon;
    private final CustomHttpClient httpClient;
    private final Random randomNumberGenerator;
    private final ArrayList<Integer> cluesUsed;

    private final ArrayList<ArrayList<ClueDTO>> playersWithClues;

    private int numberOfCluesOnJService = -1;
    private GameType gameType = GameType.NONE;
    private int numberOfPlayers = -1; // either 1 or 2 for now, -1 for none set
    private int currentPlayer = -1;
    private int currentQuestion = -1;
    private int[] playersScore;

    public static final int GAME_RANDOM_NUM_QUESTIONS_PER_PLAYER = 4;
    public static final int GAME_INPUT_TIMEOUT = 10; // in seconds
    public static final String GAME_RANDOM_RULES =
            "Player(s) given " + GAME_RANDOM_NUM_QUESTIONS_PER_PLAYER + " clues. " +
            "For each given clue you will have " + GAME_INPUT_TIMEOUT +
            " seconds to enter a response. " +
            "1 Point will be awarded for each correct answer.\nGood Luck :)";
    public static final String GAME_FULL_JEOPARDY_RULES = "";

    private GameDaemon() {
        httpClient = new CustomHttpClient();
        cluesUsed = new ArrayList<>();
        playersWithClues = new ArrayList<>();
        playersScore = null;
        randomNumberGenerator = new Random();
        numberOfCluesOnJService = httpClient.getTotalNumClues();
        if (numberOfCluesOnJService == -1) {
            // Since the server is unreachable, might as well quit while we're behind :D
            System.out.println("Error: Could not find number of total clues available from server");
            return;
        }
        numberOfPlayers = -1;
        currentPlayer = -1;
        currentQuestion = -1;
        gameType = GameType.NONE;

        // ================================ REMOVE BELOW LATER =========================================================
//        CategoryListDTO categories = httpClient.getCategories();
//        ClueListDTO clues = httpClient.getCluesWithParameters("category", "1", "value", "100");
    }

    public static GameDaemon getInstance() {
        if (gameDaemon == null) {
            gameDaemon = new GameDaemon();
        }
        return gameDaemon;
    }

    public boolean setupGame() {
        switch (this.gameType) {
            case RANDOM:
                return setupRandomCategoriesGame(true);
            case FULL_JEOPARDY:
                break;
            default:
        }
        return false;
    }

    private boolean setupRandomCategoriesGame(boolean mixedValues) {
        if ((this.numberOfCluesOnJService == -1) || (this.numberOfPlayers < 1) || (this.gameType == GameType.NONE)) {
            return false;
        }

        playersWithClues.clear();
        for (int i = 0; i < this.numberOfPlayers; i++) {
            playersWithClues.add(new ArrayList<ClueDTO>());
        }

        int currentPlayerIndex = 0;
        int currentQuestionIndex = 0;
        while (playersWithClues.get(this.numberOfPlayers - 1).size() != GAME_RANDOM_NUM_QUESTIONS_PER_PLAYER) {
            int randomNum = randomNumberGenerator.nextInt(numberOfCluesOnJService + 1);
            if (cluesUsed.contains(randomNum)) {
                continue;
            }
            ClueDTO clue = httpClient.getClueById(randomNum);
            clue.setAcceptableAnswers(generateAcceptableAnswers(clue.getAnswer()));

            if (currentPlayerIndex == 0) {
                // this is the case we are doing the first and the rest match
                playersWithClues.get(currentPlayerIndex).add(clue);
            } else {
                if (!mixedValues && (clue.getValue() != playersWithClues.get(0).get(currentQuestionIndex).getValue())) {
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
        this.currentPlayer = 0;
        this.currentQuestion = 0;
        return true;
    }

    public void resetGame() {
        this.numberOfPlayers = -1;
        this.currentPlayer = -1;
        this.currentQuestion = -1;
        this.playersScore = null;
        this.gameType = GameType.NONE;
        this.playersWithClues.clear();
    }

    // DEBUG ============================================================ MAKE THIS PRIVATE LATER =====
    public ArrayList<String> generateAcceptableAnswers(String answer) {
        ArrayList<String> acceptableAnswers = new ArrayList<>();
        answer = answer.toLowerCase(Locale.ROOT);

        // Remove ( ) from answer and use as acceptable answer
        acceptableAnswers.add(answer.replaceAll("\\(.*?\\)", "").trim());

        // Add everything within ( ) as a possible answer
        Matcher m = Pattern.compile("\\((.*?)\\)").matcher(answer);
        while (m.find()) {
            acceptableAnswers.add(m.group(1));
        }

        // take out grammar articles and punctuation from extracted words
        for (int i = 0; i < acceptableAnswers.size(); i++ ) {
            acceptableAnswers.add(i, acceptableAnswers.remove(i).
                    replaceAll("^(the|an|a|and)\\s|\\s+(the|an|a|and)\\s+|\\s+", " ").trim().
                    replaceAll("'|,|\\.", ""));

        }

        return acceptableAnswers;
    }

    public boolean reportAnswer(String userAnswer) {
        boolean isCorrect = false;
        if (userAnswer == null) {
            isCorrect = false;
        } else {
            // check to see if answer is correct
            userAnswer = userAnswer.toLowerCase(Locale.ROOT);
            userAnswer = userAnswer.replaceAll("'|,|\\.|\"", "");
            for (String answerOption : getCurrentClueDTO().getAcceptableAnswers()) {
                boolean answerOk = true;
                for (String answerOptionSplit : answerOption.split(" ")) {
                    if (!userAnswer.contains(answerOptionSplit)) {
                        System.out.println();
                        answerOk = false;
                        break;
                    }
                }
                if (answerOk) {
                    isCorrect = true;
                    break;
                }
            }
            if (userAnswer.trim().compareToIgnoreCase(getCurrentClueDTO().getAnswer()) == 0) {
                isCorrect = true;
                this.playersScore[getCurrentPlayer()]++;
            }
        }

        this.currentPlayer = ++this.currentPlayer % this.numberOfPlayers;
        if (this.currentPlayer == 0) {
            this.currentQuestion++;
        }
        if (this.currentQuestion == GAME_RANDOM_NUM_QUESTIONS_PER_PLAYER) {
            // create results
            this.currentPlayer = -1;
        }

        return isCorrect;
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

    private boolean checkValidRange(int input, int rangeMin, int rangeMax) {
        return ((input >= rangeMin) && (input <= rangeMax));
    }

    public String getGameRules() {
        switch (gameType) {
            case RANDOM:
                return GAME_RANDOM_RULES;
            case FULL_JEOPARDY:
                return GAME_FULL_JEOPARDY_RULES;
            default:
                return "Error: Unknown game in getGameRules().";
        }
    }

    public int getNumberOfQuestions() {
        switch (gameType) {
            case RANDOM:
                return GAME_RANDOM_NUM_QUESTIONS_PER_PLAYER;
            case FULL_JEOPARDY:
                return 0;
            default:
        }
        return 0;
    }

    public int getNumberOfPlayers() {
        return this.numberOfPlayers;
    }

    public boolean setNumberOfPlayers(int numberOfPlayers) {
        if (checkValidRange(numberOfPlayers, 1, 2)) {
            this.numberOfPlayers = numberOfPlayers;
            this.playersScore = new int[this.numberOfPlayers];
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
        return setNumberOfPlayers(inputAsInt);
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

    public ClueDTO getCurrentClueDTO() {
        if ((currentQuestion == -1) || (currentPlayer == -1)) {
            return null;
        }
        return playersWithClues.get(this.currentPlayer).get(currentQuestion);
    }

    public int getCurrentQuestion() {
        return this.currentQuestion;
    }

    public int[] getPlayersScore() {
        return this.playersScore;
    }

    public int getPlayersWithCluesSize() {
        return playersWithClues.get(0).size();
    }
}

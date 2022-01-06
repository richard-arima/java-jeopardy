package com.kenzie.app;

import java.util.List;

/* GAMEPLAY
    Game loads - SETUP
        download categories
    choose number of players - GET_NUM_PLAYERS
    choose game type (single category, mixed random,
 */

enum GameState {
    SETUP(1),
    TIMEOUT(1<<8),
    GET(1<<7),
    GET_NUM_PLAYERS(GET.value| 1),
    GET_GAME_TYPE(GET.value | 2),
    GET_CATEGORY(GET.value | 3),
    GET_ANSWER(GET.value | 4),
    GET_ANSWER_TIMEOUT(GET.value | TIMEOUT.value | 1),
    PRESENT(1<<6),
    PRESENT_QUESTION(PRESENT.value | 1),
    PROCESS(1<<5),
    PROCESS_ANSWER(PROCESS.value | 1),
    TO_CONSOLE(2),
    QUIT(-1);

    private final int value;

    GameState(int value) {
        this.value= value;
    }

    public int getValue() {
        return this.value;
    }
}

public class GameDaemon {
    private static GameDaemon gameDaemon;
    private GameState gameState;
    private CustomHttpClient httpClient;

    private GameDaemon() {
        this.gameState = GameState.SETUP;
        // get info from web
        httpClient = new CustomHttpClient();
        CategoriesListDTO categories = httpClient.getCategories();
    }

    public static GameDaemon getInstance() {
        if (gameDaemon == null) {
            gameDaemon = new GameDaemon();
        }
        return gameDaemon;
    }

    // processes input and returns true if success or false if invalid input, state prob doesn't change
    public boolean reportInput(String input) {
        switch (gameState) {
            case QUIT:
                return true;
            default:
                // This shouldn't happen, yet here we are. Nothing defined so far.
        }
        return true;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}


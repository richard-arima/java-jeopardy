package com.kenzie.app;

enum GameState {
    SETUP,
    GET_NUM_PLAYERS,
    GET_CATEGORY,
    PRESENT_QUESTION,
    PROCESS_ANSWER,
    TO_CONSOLE,
    QUIT
}

public class GameDaemon {
    private static GameDaemon gameDaemon;
    private GameState gameState;

    private GameDaemon() {
        this.gameState = GameState.SETUP;
        // get info from web
    }

    public static GameDaemon getInstance() {
        if (gameDaemon == null) {
            gameDaemon = new GameDaemon();
        }
        return gameDaemon;
    }

    public boolean reportInput(String input) {
        switch (gameState) {
            case QUIT:
                return false;
            default:
                // This shouldn't happen, yet here we are. Nothing defined so far.
        }
        return false;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}


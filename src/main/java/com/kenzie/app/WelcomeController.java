package com.kenzie.app;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class WelcomeController {
    private GameDaemon gameDaemon;
    private int gameReady;

    ScenesAndStages scenesAndStages;
    private Runnable setConsolePlayTrue;

    @FXML
    public ToggleButton btnOnePlayer;
    @FXML
    public ToggleButton btnTwoPlayer;
    @FXML
    public ToggleButton btnRandom;
    @FXML
    public ToggleButton btnFullJeopardy;
    @FXML
    public Button btnBegin;
    @FXML
    public Text txtWelcomeFeedback;

    @FXML
    public void initialize() {
        Platform.runLater(() -> btnBegin.requestFocus());
    }

    public void initData(ScenesAndStages scenesAndStages, GameDaemon gameDaemon) {
        this.scenesAndStages = scenesAndStages;
        this.gameDaemon = gameDaemon;
    }

    public void setSetConsolePlayTrue(Runnable method) {
        this.setConsolePlayTrue = method;
    }

    public void showRules() {
        ((TransitionController)scenesAndStages.transitionScene.getUserData()).transitionTo(GUITransitionType.RULES);
        scenesAndStages.stage.setScene(scenesAndStages.transitionScene);
    }

    public void btnToConsole(ActionEvent actionEvent) {
        setConsolePlayTrue.run();
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
            txtWelcomeFeedback.setText("Preparing game...");
            txtWelcomeFeedback.setVisible(true);
            if (!gameDaemon.setupGame()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not setup game, Terminating.", ButtonType.OK);
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add("/" + Main.GAME_TITLE.replaceAll(" ", "") + ".css");
                alert.showAndWait();
                Platform.exit();
            }
            showRules();
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

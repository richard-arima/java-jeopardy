package com.kenzie.app;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class PlayRandomController {
    @FXML
    public Text txtPlayerAndCategory;
    @FXML
    public Text txtQuestion;
    @FXML
    public Text txtAnswer;
    @FXML
    public Text txtTimeout;

    private ScenesAndStages scenesAndStages;
    private GameDaemon gameDaemon;
    private int currentPlayer;
    private ClueDTO currentClueDTO;
    private boolean proceedOnSpace = false;
    private boolean proceedNewGame = false;

    public void initData(ScenesAndStages scenesAndStages, GameDaemon gameDaemon) {
        this.scenesAndStages = scenesAndStages;
        this.gameDaemon = gameDaemon;
    }

    public void setCurrentQuestion() {
        this.currentPlayer = gameDaemon.getCurrentPlayer();
        if (currentPlayer == -1) {
            setResults();
            return;
        }
        this.currentClueDTO = gameDaemon.getCurrentClueDTO();

        txtPlayerAndCategory.setText("Player " + (currentPlayer + 1) + " Round " +
                (gameDaemon.getCurrentQuestion() + 1) + "\nCategory: " +
                currentClueDTO.getCategory().getTitle());
        txtQuestion.setText("");
        txtAnswer.setText("Press space for clue");
        scenesAndStages.playRandomScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.SPACE) {
                        txtAnswer.setText("");
                        txtQuestion.setText(currentClueDTO.getQuestion());
                        scenesAndStages.playRandomScene.setOnKeyPressed(this::handleInput);
                        System.out.println(currentClueDTO.getAnswer()); // =================================== DEBUG ======
                    }
                });
    }

    public void setResults() {
        int[] scores = gameDaemon.getPlayersScore();
        int winningScore;

        txtPlayerAndCategory.setText(Main.getResultsAsString());
        txtQuestion.setText("");
        txtAnswer.setText("Would you like to play again? y/n");
        proceedNewGame = true;
        gameDaemon.resetGame();
    }

    public void handleInput(KeyEvent value) {
        if (proceedOnSpace) {
            if (value.getCode() == KeyCode.SPACE) {
                proceedOnSpace = false;
                setCurrentQuestion();
            }
            return;
        }

        if (proceedNewGame) {
            if (value.getText().equalsIgnoreCase("y")) {
                ((WelcomeController)scenesAndStages.welcomeScene.getUserData()).reset();
                scenesAndStages.stage.setScene(scenesAndStages.welcomeScene);
                proceedNewGame = false;
                return;
            } else if (value.getText().equalsIgnoreCase("n")) {
                proceedNewGame = false;
                Platform.exit();
                return;
            }
            return;
        }

        if (value.getCode() == KeyCode.BACK_SPACE) {
            if (!txtAnswer.getText().isEmpty()) {
                String txt = txtAnswer.getText();
                txtAnswer.setText(txt.substring(0, txt.length() - 1));
            }
        } else if (value.getCode() == KeyCode.ENTER) {
            if (gameDaemon.reportAnswer(txtAnswer.getText())) {
                txtAnswer.setText("Correct!\nPress space to continue");
            } else {
                txtAnswer.setText("Incorrect! The correct answer was, " +
                        currentClueDTO.getAnswer() + "\nPress space to continue");
            }
            proceedOnSpace = true;
        } else {
            txtAnswer.setText(txtAnswer.getText() + value.getText());
        }
    }
}

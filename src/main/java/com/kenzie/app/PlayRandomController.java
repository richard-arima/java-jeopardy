package com.kenzie.app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

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
    private ClueDTO currentClueDTO;
    private boolean proceedOnSpace = false;
    private boolean proceedNewGame = false;
    private int timeLeft;
    private Timeline timeout;

    private Font txtPlayerAndCategoryOriginalFont;
    private Font txtQuestionOriginalFont;
    private Font txtAnswerOriginalFont;

    @FXML
    public void initialize() {
        txtPlayerAndCategoryOriginalFont = txtPlayerAndCategory.getFont();
        txtQuestionOriginalFont = txtQuestion.getFont();
        txtAnswerOriginalFont = txtAnswer.getFont();
    }

    public void initData(ScenesAndStages scenesAndStages, GameDaemon gameDaemon) {
        this.scenesAndStages = scenesAndStages;
        this.gameDaemon = gameDaemon;
    }

    public void setCurrentQuestion() {
        resetText();

        int currentPlayer = gameDaemon.getCurrentPlayer();
        if (currentPlayer == -1) {
            setResults();
            return;
        }
        this.currentClueDTO = gameDaemon.getCurrentClueDTO();

        if (gameDaemon.getNumberOfPlayers() == 1) {
            txtPlayerAndCategory.setText("Round " +
                    (gameDaemon.getCurrentQuestion() + 1) + "\nCategory: " +
                    currentClueDTO.getCategory().getTitle());
        } else {
            txtPlayerAndCategory.setText("Player " + (currentPlayer + 1) + " Round " +
                    (gameDaemon.getCurrentQuestion() + 1) + "\nCategory: " +
                    currentClueDTO.getCategory().getTitle());
        }
        txtQuestion.setText("");
        txtAnswer.setText("Press space for clue");
        txtTimeout.setText("");

        fitText();

        scenesAndStages.playRandomScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.SPACE) {
                        txtAnswer.setText("");
                        txtQuestion.setText(currentClueDTO.getQuestion());

                        fitText();

                        scenesAndStages.playRandomScene.setOnKeyPressed(this::handleInput);
                        timeLeft = GameDaemon.GAME_INPUT_TIMEOUT;
                        timeout = new Timeline(
                                new KeyFrame(Duration.seconds(1),
                                        e2 -> {
                                            txtTimeout.setText(Integer.toString(timeLeft--));
                                            if (timeLeft <= -1) {
                                                timeout.stop();
                                                setTimeout();
                                            }
                                        }));
                        timeout.setCycleCount(GameDaemon.GAME_INPUT_TIMEOUT + 1);
                        timeout.play();

                        System.out.println("Heading Size: " + txtPlayerAndCategory.getText().length());
                        System.out.println("Question Size: " + txtQuestion.getText().length());
                        System.out.println("Answer size: " + txtAnswer.getText().length());
                        System.out.println("Category: " + currentClueDTO.getCategory().getTitle()); // ============DEBUG
                        System.out.println("Value: " + currentClueDTO.getValue());
                        System.out.println(currentClueDTO.getAnswer()); // =================================== DEBUG ======
                    }
                });
    }

    public void setTimeout() {
        timeout.stop();
        gameDaemon.reportAnswer(null);
        txtAnswer.setText("Timeout! The correct answer was,\n" +
                currentClueDTO.getAnswer() + "\nPress space to continue");
        proceedOnSpace = true;

        fitText();
    }

    public void setResults() {
        txtPlayerAndCategory.setText(Main.getResultsAsString());
        txtQuestion.setText("");
        txtAnswer.setText("Would you like to play again? y/n");
        txtTimeout.setText("");
        proceedNewGame = true;
        gameDaemon.resetGame();
    }

    public void handleInput(KeyEvent value) {
        if (proceedOnSpace) {
            if (value.getCode() == KeyCode.SPACE) {
                System.out.println("Heading Size: " + txtPlayerAndCategory.getText().length()); // =========DEBUG
                System.out.println("Question Size: " + txtQuestion.getText().length());
                System.out.println("Answer size: " + txtAnswer.getText().length());       // =================DEBUG

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
            timeout.stop();
            if (gameDaemon.reportAnswer(txtAnswer.getText())) {
                txtAnswer.setText("Correct!\nPress space to continue");
            } else {
                txtAnswer.setText("Incorrect! The correct answer was,\n" +
                        currentClueDTO.getAnswer() + "\nPress space to continue");
            }
            proceedOnSpace = true;

            fitText();
        } else {
            txtAnswer.setText(txtAnswer.getText() + value.getText());
        }
    }

    private void fitText() {
        if (txtPlayerAndCategory.getText().length() > 40) {
            txtPlayerAndCategory.setFont(
                    Font.font(txtPlayerAndCategoryOriginalFont.getFamily(), 30));
        }
        if (txtQuestion.getText().length() > 90) {
            txtQuestion.setFont(Font.font(txtQuestionOriginalFont.getFamily(), 30));
        }
        if (txtAnswer.getText().length() > 30) {
            txtAnswer.setFont(Font.font(txtAnswerOriginalFont.getFamily(), 25));
        }
    }

    private void resetText() {
        txtPlayerAndCategory.setFont(txtPlayerAndCategoryOriginalFont);
        txtQuestion.setFont(txtQuestionOriginalFont);
        txtAnswer.setFont(txtAnswerOriginalFont);
    }
}

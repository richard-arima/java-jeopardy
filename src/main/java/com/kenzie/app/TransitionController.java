package com.kenzie.app;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class TransitionController {
    private ScenesAndStages scenesAndStages;
    private GameDaemon gameDaemon;

    @FXML
    public Text txtTransitionHeading;
    @FXML
    public Text txtTransitionBody;
    @FXML
    public Text txtTransitionFooter;

    @FXML
    public void initialize() {
    }

    public void initData(ScenesAndStages scenesAndStages, GameDaemon gameDaemon) {
        this.scenesAndStages = scenesAndStages;
        this.gameDaemon = gameDaemon;

        scenesAndStages.transitionScene.setOnKeyPressed(e -> {
            scenesAndStages.stage.setScene(scenesAndStages.playRandomScene);
            ((PlayRandomController)scenesAndStages.playRandomScene.getUserData()).setCurrentQuestion();
        });
    }

    public void transitionTo(GUITransitionType transitionType) {
        switch (transitionType) {
            case RULES:
                txtTransitionHeading.setText("Rules");
                txtTransitionBody.setText(GameDaemon.GAME_RANDOM_RULES +
                        "\n\nYou may begin typing when question is shown");
                txtTransitionFooter.setText("Press any key when ready");
                break;
            case RESULTS:
                break;
            default:
        }
    }
}

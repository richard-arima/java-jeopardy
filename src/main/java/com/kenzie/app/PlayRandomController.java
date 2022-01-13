package com.kenzie.app;

public class PlayRandomController {
    private ScenesAndStages scenesAndStages;
    private GameDaemon gameDaemon;

    public void initData(ScenesAndStages scenesAndStages, GameDaemon gameDaemon) {
        this.scenesAndStages = scenesAndStages;
        this.gameDaemon = gameDaemon;
    }
}

package com.kenzie.app;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClueDTO {
    @JsonProperty("canon")
    private boolean canon;
    @JsonProperty("game")
    private GameDTO game;
    @JsonProperty("category")
    private CategoryDTO category;
    @JsonProperty("invalidCount")
    private int invalidCount;
    @JsonProperty("gameId")
    private int gameId;
    @JsonProperty("categoryId")
    private int categoryId;
    @JsonProperty("value")
    private int value;
    @JsonProperty("question")
    private String question;
    @JsonProperty("answer")
    private String answer;
    @JsonProperty("id")
    private int id;

    public boolean getCanon() {
        return this.canon;
    }

    public void setCanon(boolean canon) {
        this.canon = canon;
    }

    public GameDTO getGame() {
        return this.game;
    }

    public void setGame(GameDTO game) {
        this.game = game;
    }

    public CategoryDTO getCategory() {
        return this.category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public int getInvalidCount() {
        return this.invalidCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public int getGameId() {
        return this.gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClueDTO{" +
                "canon=" + canon +
                ", game=" + game +
                ", category=" + category +
                ", invalidCount=" + invalidCount +
                ", gameId=" + gameId +
                ", categoryId=" + categoryId +
                ", value=" + value +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", id=" + id +
                '}';
    }
}

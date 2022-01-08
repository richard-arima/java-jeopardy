package com.kenzie.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ClueListDTO {
    @JsonProperty("clues")
    private List<ClueDTO> clues;

    public List<ClueDTO> getClues() {
        return this.clues;
    }

    public void setCategories(List<ClueDTO> clues) {
        this.clues = clues;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CluesListDTO{\nclues=\n");
        for(ClueDTO c : clues) {
            sb.append(c.toString());
            sb.append("\n");
        }
        return sb.append("\n} End CluesListDTO").toString();
    }
}
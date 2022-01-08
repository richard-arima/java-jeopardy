package com.kenzie.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CategoryListDTO {
    @JsonProperty("categories")
    private List<CategoryDTO> categories;

    public List<CategoryDTO> getCategories() {
        return this.categories;
    }

    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CategoriesListDTO{\ncategories=\n");
        for(CategoryDTO c : categories) {
            sb.append(c.toString());
            sb.append("\n");
        }
        return sb.append("\n} End CategoriesListDTO").toString();
    }
}

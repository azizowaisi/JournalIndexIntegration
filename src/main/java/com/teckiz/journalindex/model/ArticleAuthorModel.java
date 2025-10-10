package com.teckiz.journalindex.model;

/**
 * Model for article author data
 * Mirrors the PHP ArticleAuthorModel
 */
public class ArticleAuthorModel {
    
    private String name;
    
    public ArticleAuthorModel() {
    }
    
    public ArticleAuthorModel(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "ArticleAuthorModel{" +
                "name='" + name + '\'' +
                '}';
    }
}


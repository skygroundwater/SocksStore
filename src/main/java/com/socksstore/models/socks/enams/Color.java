package com.socksstore.models.socks.enams;

public enum Color {

    WHITE("WHITE"), BLACK("BLACK"), RED("RED"),
    YELLOW("YELLOW"), ORANGE("ORANGE"), GREEN("GREEN"),
    BLUE("BLUE"), PURPLE("PURPLE"), PINK("PINK"),
    BROWN("BROWN"), GREY("GREY");

    private final String nameToString;

    Color(String russianName){
        this.nameToString = russianName;
    }

    public String getNameToString() {
        return nameToString;
    }
}

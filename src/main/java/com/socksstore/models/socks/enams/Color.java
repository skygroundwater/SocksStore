package com.socksstore.models.socks.enams;

import com.socksstore.exceptions.InvalidValueException;

public enum Color {

    WHITE("WHITE"), BLACK("BLACK"), RED("RED"),
    YELLOW("YELLOW"), ORANGE("ORANGE"), GREEN("GREEN"),
    BLUE("BLUE"), PURPLE("PURPLE"), PINK("PINK"),
    BROWN("BROWN"), GREY("GREY");

    private final String nameToString;

    Color(String name) {
        this.nameToString = name;
    }

    public String getNameToString() {
        return nameToString;
    }

    public static Color getColor(String colorName){
        for(Color color: values()){
            if((colorName.toUpperCase()).equals(color.nameToString)){
                return color;
            }
        }
        throw new InvalidValueException();
    }
}
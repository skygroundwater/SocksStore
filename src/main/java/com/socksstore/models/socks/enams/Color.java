package com.socksstore.models.socks.enams;

public enum Color {

    WHITE("Белые"), BLACK("Черные"), RED("Красные"),
    YELLOW("Желтые"), ORANGE("Оранжевые"), GREEN("Зелёные"),
    BLUE("Голубые"), PURPLE("Фиолетовые"), PINK("Розовые"),
    BROWN("Коричневые"), GREY("Серые");

    private final String russianName;

    Color(String russianName){
        this.russianName = russianName;
    }

    public String getRussianName() {
        return russianName;
    }
}

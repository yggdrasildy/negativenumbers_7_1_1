package com.test

class Item {

    static constraints = {
        name nullable: false, unique: true
        stock nullable: false
        itemValue nullable: false
    }

    String name
    Long stock
    BigDecimal itemValue

}

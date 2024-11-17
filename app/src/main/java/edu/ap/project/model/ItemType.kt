package edu.ap.project.model


enum class ItemType(val typeName: String) {
    HOUSEHOLD("Huishoudelijk Artikel"),
    DRILL("Tuin- en Gazonartikelen"),
    VACUUM("Gereedschap en Uitrusting"),
    ELECTRONICS("Elektronica"),
    RECREATIONAL("Recreatief Artikel"),
    AUTOMOTIVE("Automotive Artikel"),
    OFFICE_SUPPLIES("Kantoorbenodigdheden"),
    PARTY_SUPPLIES("Feestartikelen"),
    OTHERS("Overig");

    // You can add additional methods or properties if needed
    override fun toString(): String = typeName
}

package ru.rutoken.tech.ui.ca.generateobjects.certificate

/**
 * Creates a list of DN (Distinguished Name) fields were CN=[owner]
 */
fun createDN(owner: String) = listOf(
    "CN",
    owner,
    "1.2.840.113549.1.9.1",
    "ivanova_ekaterina@rutoken.ru",
    "O",
    "АО \"Актив Софт\"",
    "OGRN",
    "1037700094541",
    "OU",
    "Аналитика",
    "title",
    "Руководитель отдела",
    "SNILS",
    "12345678900",
    "INNLE",
    "7729361030",
    "C",
    "RU",
    "ST",
    "Москва",
    "L",
    "г. Москва",
    "street",
    "Шарикоподшипниковская ул, д. 1"
)

/**
 * List of extension fields
 */
val EXTENSIONS = listOf(
    "1.2.643.100.114", // identification_kind
    "ASN1:INT:0", // personal
    "2.5.29.32", // cert_policies
    "1.2.643.100.113.1", // Класс средства ЭП КС1
    "extendedKeyUsage",
    "1.3.6.1.5.5.7.3.2,1.3.6.1.5.5.7.3.4",
    "subjectSignTool",
    "ASN1:UTF8String:Средство электронной подписи: СКЗИ \"Рутокен ЭЦП 3.0\"",
    "keyUsage",
    "digitalSignature,nonRepudiation,keyEncipherment,dataEncipherment"
)

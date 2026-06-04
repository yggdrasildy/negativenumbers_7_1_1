# negativenumbers_7_1_1
Grails 7.1.1 application with domain containing long and bigdecimal fields for demonstrating bug in edit, update and save negative numbers when browser has locale set to nb-NO.

This is the second iteration of the bug. There are still issues with the update and save operations.

## The issue location
The issue has been located to the NumberFormat(Locale) which is used in LocaleAwareNumberConverter. Most locales use '-' as minus sign, but there are exceptions.

## Locales with non ASCII hyphen minus sign
These exceptions are documented in the test LocaleAwareNumberConverterSpec."document locales with non ascii hyphen minus sign"()

```
Locales where DecimalFormatSymbols.minusSign is not ASCII hyphen-minus (-):
et                                  U+2212 − Estonian
et-EE                               U+2212 − Estonian (Estonia)
et-Latn-EE                          U+2212 − Estonian (Latin, Estonia)
eu                                  U+2212 − Basque
eu-ES                               U+2212 − Basque (Spain)
eu-Latn-ES                          U+2212 − Basque (Latin, Spain)
fa                                  U+2212 − Persian
fa-AF                               U+2212 − Persian (Afghanistan)
fa-Arab-IR                          U+2212 − Persian (Arabic, Iran)
fa-IR                               U+2212 − Persian (Iran)
fi                                  U+2212 − Finnish
fi-FI                               U+2212 − Finnish (Finland)
fi-Latn-FI                          U+2212 − Finnish (Latin, Finland)
fo                                  U+2212 − Faroese
fo-DK                               U+2212 − Faroese (Denmark)
fo-FO                               U+2212 − Faroese (Faroe Islands)
fo-Latn-FO                          U+2212 − Faroese (Latin, Faroe Islands)
gsw                                 U+2212 − Swiss German
gsw-CH                              U+2212 − Swiss German (Switzerland)
gsw-FR                              U+2212 − Swiss German (France)
gsw-LI                              U+2212 − Swiss German (Liechtenstein)
gsw-Latn-CH                         U+2212 − Swiss German (Latin, Switzerland)
hr                                  U+2212 − Croatian
hr-BA                               U+2212 − Croatian (Bosnia & Herzegovina)
hr-HR                               U+2212 − Croatian (Croatia)
hr-Latn-HR                          U+2212 − Croatian (Latin, Croatia)
ksh                                 U+2212 − Colognian
ksh-DE                              U+2212 − Colognian (Germany)
ksh-Latn-DE                         U+2212 − Colognian (Latin, Germany)
lt                                  U+2212 − Lithuanian
lt-LT                               U+2212 − Lithuanian (Lithuania)
lt-Latn-LT                          U+2212 − Lithuanian (Latin, Lithuania)
nb                                  U+2212 − Norwegian Bokmål
nb-Latn-NO                          U+2212 − Norwegian Bokmål (Latin, Norway)
nb-NO                               U+2212 − Norwegian Bokmål (Norway)
nb-SJ                               U+2212 − Norwegian Bokmål (Svalbard & Jan Mayen)
nn                                  U+2212 − Norwegian Nynorsk
nn-Latn-NO                          U+2212 − Norwegian Nynorsk (Latin, Norway)
nn-NO                               U+2212 − Norwegian (Norway, Nynorsk)
nn-NO                               U+2212 − Norwegian Nynorsk (Norway)
no                                  U+2212 − Norwegian
no-Latn-NO                          U+2212 − Norwegian (Latin, Norway)
no-NO                               U+2212 − Norwegian (Norway)
rm                                  U+2212 − Romansh
rm-CH                               U+2212 − Romansh (Switzerland)
rm-Latn-CH                          U+2212 − Romansh (Latin, Switzerland)
se                                  U+2212 − Northern Sami
se-FI                               U+2212 − Northern Sami (Finland)
se-Latn-NO                          U+2212 − Northern Sami (Latin, Norway)
se-NO                               U+2212 − Northern Sami (Norway)
se-SE                               U+2212 − Northern Sami (Sweden)
sl                                  U+2212 − Slovenian
sl-Latn-SI                          U+2212 − Slovenian (Latin, Slovenia)
sl-SI                               U+2212 − Slovenian (Slovenia)
sv                                  U+2212 − Swedish
sv-AX                               U+2212 − Swedish (Åland Islands)
sv-FI                               U+2212 − Swedish (Finland)
sv-Latn-SE                          U+2212 − Swedish (Latin, Sweden)
sv-SE                               U+2212 − Swedish (Sweden)
```

## The solution
I copied the LocaleAwareNumberConverter into a new LoggingLocaleAwareNumberConverter and added a log statement to the format method. Using the new LocaleAwareNumberConverter was configured in resources.groovy. With extensive logging I was able to create the method `convertToLocaleMinusIfNecessary`

```groovy
    /**
     * replaces minus prefix of stringValue if stringValue is prefixed with '-' and the localeMinusSign is not '-'
     * @param numberFormatter
     * @param stringValue
     * @return
     */
    protected String convertToLocaleMinusIfNecessary(NumberFormat numberFormatter, String stringValue) {
        if (numberFormatter instanceof DecimalFormat && stringValue?.startsWith('-')) {
            String localeMinusSign = ((DecimalFormat) numberFormatter).decimalFormatSymbols.minusSign
            if (localeMinusSign != '-') {
                stringValue = localeMinusSign + stringValue.substring(1)
            }
        }
        stringValue
    }
```

## LoggingLocaleAwareBigDecimalConverter notes
The LoggingLocaleAwareBigDecimalConverter is a copy of LocaleAwareBigDeciamlConverter. The only difference is added logging.

## AI assistance
I used Junie to help locate the issue in grails, wire up the LoggingConverters and test the application.
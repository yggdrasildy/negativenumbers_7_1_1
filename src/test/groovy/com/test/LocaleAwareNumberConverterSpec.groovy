package com.test

import groovy.util.logging.Slf4j
import org.grails.databinding.converters.web.LocaleAwareNumberConverter
import spock.lang.Specification

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition

@Slf4j
class LocaleAwareNumberConverterSpec extends Specification {

    void "negative long parsing fails for Norwegian locale in Grails converter"() {
        given:
        Locale defaultLocale = Locale.default
        Locale.default = new Locale('nb', 'NO')
        def converter = new LocaleAwareNumberConverter(targetType: Long)

        when:
        converter.convert('-1')

        then:
        NumberFormatException e = thrown()
        e.message == 'Unable to parse number [-1]'

        cleanup:
        Locale.default = defaultLocale
    }

    void "Norwegian number format does not consume ascii hyphen minus"() {
        given:
        def position = new ParsePosition(0)

        when:
        Number parsed = NumberFormat.getInstance(new Locale('nb', 'NO')).parse('-1', position)

        then:
        parsed == null
        position.index == 0
    }

    void "document locales with non ascii hyphen minus sign"() {
        when:
        List<Map<String, String>> localesWithOtherMinusSign = Locale.availableLocales
                .collect { Locale locale ->
                    NumberFormat numberFormat = NumberFormat.getInstance(locale)
                    if (!(numberFormat instanceof DecimalFormat)) {
                        return null
                    }
                    char minusSign = ((DecimalFormat) numberFormat).decimalFormatSymbols.minusSign
                    if (minusSign == '-') {
                        return null
                    }
                    [
                            locale   : locale.toLanguageTag(),
                            name     : locale.getDisplayName(Locale.ENGLISH),
                            minusSign: minusSign,
                            codePoint: String.format('U+%04X', (int) minusSign)
                    ]
                }
                .findAll()
                .sort { Map locale -> locale.locale }

        then:
        log.debug('Locales where DecimalFormatSymbols.minusSign is not ASCII hyphen-minus (-):')
        localesWithOtherMinusSign.each { Map locale ->
            log.debug("${locale.locale.padRight(35)} ${locale.codePoint} ${locale.minusSign} ${locale.name}")
        }
        println('\nLocales where DecimalFormatSymbols.minusSign is not ASCII hyphen-minus (-):')
        localesWithOtherMinusSign.each { Map locale ->
            println("${locale.locale.padRight(35)} ${locale.codePoint} ${locale.minusSign} ${locale.name}")
        }
        localesWithOtherMinusSign.every { Map locale -> locale.minusSign != '-' }
    }

}
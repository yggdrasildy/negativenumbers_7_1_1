package com.test.databinding

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.text.NumberFormat

@CompileStatic
@Slf4j
class LoggingLocaleAwareBigDecimalConverter extends LoggingLocaleAwareNumberConverter {

    @Override
    protected NumberFormat getNumberFormatter() {
        /*
         * Original Grails 7.1.1 LocaleAwareBigDecimalConverter.getNumberFormatter():
         *
         * NumberFormat.getNumberInstance(getLocale())
         */
        Locale locale = getLocale()
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale)
        log.debug('LoggingLocaleAwareBigDecimalConverter.getNumberFormatter selected formatter={} locale={} targetType={} format={}',
                numberFormat.getClass().name,
                locale,
                targetType?.name,
                describeFormat(numberFormat))
        numberFormat
    }

    @Override
    Object convert(Object value) {
        /*
         * Original Grails 7.1.1 LocaleAwareBigDecimalConverter.convert(Object) before this local subclass
         * delegated through LoggingLocaleAwareNumberConverter for instrumentation:
         *
         * Number number = (Number) super.convert(value)
         * number == null ? null : new BigDecimal(number.toString())
         */
        Number number = (Number) super.convert(value)
        number == null ? null : new BigDecimal(number.toString())
    }
}
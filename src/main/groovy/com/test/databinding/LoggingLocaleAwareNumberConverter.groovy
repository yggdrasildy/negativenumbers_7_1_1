package com.test.databinding

import grails.databinding.converters.ValueConverter
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition

@CompileStatic
@Slf4j
class LoggingLocaleAwareNumberConverter implements ValueConverter {

    Class<?> targetType
    LocaleResolver localeResolver

    @Override
    boolean canConvert(Object value) {
        value instanceof CharSequence
    }

    @Override
    Object convert(Object value) {
        /*
         * Original Grails 7.1.1 LocaleAwareNumberConverter.convert(Object) before local instrumentation:
         *
         * String stringValue = value?.toString()?.trim()
         * NumberFormat numberFormatter = getNumberFormatter()
         * ParsePosition position = new ParsePosition(0)
         * Number parsedNumber = numberFormatter.parse(stringValue, position)
         * if (stringValue == null || parsedNumber == null || position.index != stringValue.length()) {
         *     throw new NumberFormatException("Unable to parse number [${value}]")
         * }
         * if (targetType == Long || targetType == Long.TYPE) {
         *     return parsedNumber.longValue()
         * }
         * if (targetType == Integer || targetType == Integer.TYPE) {
         *     return parsedNumber.intValue()
         * }
         * if (targetType == Short || targetType == Short.TYPE) {
         *     return parsedNumber.shortValue()
         * }
         * if (targetType == Float || targetType == Float.TYPE) {
         *     return parsedNumber.floatValue()
         * }
         * if (targetType == Double || targetType == Double.TYPE) {
         *     return parsedNumber.doubleValue()
         * }
         * parsedNumber
         */
        String stringValue = value?.toString()?.trim()
        NumberFormat numberFormatter = getNumberFormatter()
        ParsePosition position = new ParsePosition(0)

        stringValue = convertToLocaleMinusIfNecessary(numberFormatter, stringValue)

        Number parsedNumber = numberFormatter.parse(stringValue, position)

        // Local instrumentation added to understand locale-aware parsing failures for Norwegian locale.
        log.debug('LoggingLocaleAwareNumberConverter input=({}) targetType={} locale={} formatter={} parsedNumber={} parseIndex={} errorIndex={} length={} bytes={} ordinals={}',
                stringValue,
                targetType?.name,
                getLocale(),
                numberFormatter.getClass().name,
                parsedNumber,
                position.index,
                position.errorIndex,
                stringValue?.length(),
                stringValue?.getBytes('UTF-8')?.encodeHex()?.toString(),
                ordinals(stringValue))

        if (stringValue == null || parsedNumber == null || position.index != stringValue.length()) {
            log.error("Unable to parse number [${value}]")
            throw new NumberFormatException("Unable to parse number [${value}]")
        }

        if (targetType == Long || targetType == Long.TYPE) {
            return parsedNumber.longValue()
        }
        if (targetType == Integer || targetType == Integer.TYPE) {
            return parsedNumber.intValue()
        }
        if (targetType == Short || targetType == Short.TYPE) {
            return parsedNumber.shortValue()
        }
        if (targetType == Float || targetType == Float.TYPE) {
            return parsedNumber.floatValue()
        }
        if (targetType == Double || targetType == Double.TYPE) {
            return parsedNumber.doubleValue()
        }
        parsedNumber
    }

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

    protected NumberFormat getNumberFormatter() {
        Locale locale = getLocale()
        NumberFormat numberFormat = NumberFormat.getInstance(locale)
        log.debug('LoggingLocaleAwareNumberConverter.getNumberFormatter selected formatter={} locale={} targetType={} format={}',
                numberFormat.getClass().name,
                locale,
                targetType?.name,
                describeFormat(numberFormat))
        numberFormat
    }

    protected void logNumberFormatter(String localeName) {
        Locale locale = Locale.forLanguageTag(localeName.replace('_', '-'))
        NumberFormat numberFormat = NumberFormat.getInstance(locale)
        log.debug('LoggingLocaleAwareNumberConverter.logNumberFormatter {} formatter={} locale={} targetType={} format={}',
                localeName,
                numberFormat.getClass().name,
                locale,
                targetType?.name,
                describeFormat(numberFormat))
    }

    protected Locale getLocale() {
        def requestAttributes = RequestContextHolder.requestAttributes
        if (localeResolver != null && requestAttributes instanceof ServletRequestAttributes) {
            Locale resolvedLocale = localeResolver.resolveLocale(((ServletRequestAttributes) requestAttributes).request)
            log.debug('LoggingLocaleAwareNumberConverter.getLocale selected locale={} source=localeResolver resolver={} requestAttributes={}',
                    resolvedLocale,
                    localeResolver.getClass().name,
                    requestAttributes.getClass().name)
            return resolvedLocale
        }
        log.debug('LoggingLocaleAwareNumberConverter.getLocale selected locale={} source=Locale.default localeResolverPresent={} requestAttributes={}',
                Locale.default,
                localeResolver != null,
                requestAttributes?.getClass()?.name)
        Locale.default
    }

    private static String ordinals(String value) {
        // Local helper added for instrumentation; not present in the original Grails converter.
        if (value == null) {
            return null
        }
        List<String> result = []
        for (int i = 0; i < value.length(); i++) {
            result << String.valueOf((int) value.charAt(i))
        }
        result.join(', ')
    }

    protected static String describeFormat(NumberFormat numberFormat) {
        // Local helper added for instrumentation; not present in the original Grails converter.
        if (numberFormat instanceof DecimalFormat) {
            DecimalFormat decimalFormat = (DecimalFormat) numberFormat
            return "pattern=${decimalFormat.toPattern()}, localizedPattern=${decimalFormat.toLocalizedPattern()}, positivePrefix=${decimalFormat.positivePrefix}, negativePrefix=${decimalFormat.negativePrefix}, positiveSuffix=${decimalFormat.positiveSuffix}, negativeSuffix=${decimalFormat.negativeSuffix}, decimalSeparator=${decimalFormat.decimalFormatSymbols.decimalSeparator}, groupingSeparator=${decimalFormat.decimalFormatSymbols.groupingSeparator}, minusSign=${decimalFormat.decimalFormatSymbols.minusSign}, parseIntegerOnly=${decimalFormat.parseIntegerOnly}, groupingUsed=${decimalFormat.groupingUsed}, minimumFractionDigits=${decimalFormat.minimumFractionDigits}, maximumFractionDigits=${decimalFormat.maximumFractionDigits}"
        }
        "parseIntegerOnly=${numberFormat.parseIntegerOnly}, groupingUsed=${numberFormat.groupingUsed}, minimumFractionDigits=${numberFormat.minimumFractionDigits}, maximumFractionDigits=${numberFormat.maximumFractionDigits}"
    }
}
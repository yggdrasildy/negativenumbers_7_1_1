import com.test.databinding.LoggingLocaleAwareBigDecimalConverter
import com.test.databinding.LoggingLocaleAwareNumberConverter

// Place your Spring DSL code here
beans = {

    defaultGrailsBigDecimalConverter(LoggingLocaleAwareBigDecimalConverter) {
        targetType = BigDecimal
        localeResolver = ref('localeResolver')
    }

    defaultLongConverter(LoggingLocaleAwareNumberConverter) {
        targetType = Long
        localeResolver = ref('localeResolver')
    }

}

package com.test

import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import org.springframework.validation.FieldError

import static org.springframework.http.HttpStatus.*

@Slf4j
class ItemController {

    ItemService itemService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond itemService.list(params), model:[itemCount: itemService.count()]
    }

    def show(Long id) {
        respond itemService.get(id)
    }

    def create() {
        respond new Item(params)
    }

    def save(Item item) {
        if (item == null) {
            notFound()
            return
        }
        logItem(item, 'save.autoPopulated')
        logParams(params)
        logBindingErrors(item, 'save.afterAutoPopulate')
        try {
            itemService.save(item)
        } catch (ValidationException e) {

            log.error("e: ${e.message}")
            respond item.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'item.label', default: 'Item'), item.id])
                redirect item
            }
            '*' { respond item, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond itemService.get(id)
    }

    def update(Item item) {
        if (item == null) {
            notFound()
            return
        }

        logItem(item, 'update.autoPopulated')
        logParams(params)
        logBindingErrors(item, 'update.afterAutoPopulate')

        try {
            itemService.save(item)
        } catch (ValidationException e) {
            respond item.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'item.label', default: 'Item'), item.id])
                redirect item
            }
            '*'{ respond item, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        itemService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'item.label', default: 'Item'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'item.label', default: 'Item'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
    protected static void logItem(Item item, String when) {
        log.debug("item.${when}.properties: ${item.properties}")
        log.debug("item.${when}.stock: ${item.stock}")
        log.debug("item.${when}.itemValue: ${item.itemValue}")
    }
    protected static void logParams(Map params) {
        log.debug("params: ${params}")
        log.debug "RAW stock=(${params?.stock})}"
        log.debug "RAW stock bytes=${params?.stock?.getBytes('UTF-8')?.encodeHex()}"
        log.debug "RAW stock class ${params?.stock?.class}"
        log.debug "RAW stock ordinals ${getOrdinals(params?.stock as String)}"
        log.debug "RAW itemValue=(${params?.itemValue})}"
        log.debug "RAW itemValue bytes=${params?.itemValue?.getBytes('UTF-8')?.encodeHex()}"
        log.debug "RAW itemValue class ${params?.itemValue?.class}"
        log.debug "RAW itemValue ordinals ${getOrdinals(params?.itemValue as String)}"
    }
    static String getOrdinals(String inString){
        if (inString == null) {
            return null
        }
        String result = new String();
        for (int i = 0; i < inString.length(); i++){
            if (!result.isEmpty()){
                result += ", ";
            }
            char c = inString.charAt(i);

            String thisChar = String.valueOf((int) c);
            result += thisChar;
        }

        return result;
    }

    protected static void logBindingErrors(Item item, String when) {
        if (item == null || !item.hasErrors()) {
            return
        }
        item.errors.allErrors.each { error ->
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error
                log.debug("item.${when}.fieldError field=${fieldError.field} bindingFailure=${fieldError.bindingFailure} rejectedValue=(${fieldError.rejectedValue}) rejectedValueClass=${fieldError.rejectedValue?.getClass()?.name} codes=${fieldError.codes as List}")
                if (fieldError.rejectedValue instanceof CharSequence) {
                    String rejected = fieldError.rejectedValue.toString()
                    log.debug("item.${when}.fieldError ${fieldError.field} bytes=${rejected.getBytes('UTF-8').encodeHex()}")
                    log.debug("item.${when}.fieldError ${fieldError.field} ordinals=${getOrdinals(rejected)}")
                }
            } else {
                log.debug("item.${when}.error code=${error.code} object=${error.objectName} codes=${error.codes as List}")
            }
        }
    }

}

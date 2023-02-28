package plugins.org.craftercms.plugin.docusign.utils

import groovy.json.JsonSlurper

class HttpHelpers {
    static def MSG_BODY_EMPTY = 'Body is empty.'
    static def MSG_INVALID_TOKEN = 'Invalid token.'
    static def getPostParams(request) {
        def result = [:]
        def reader = request.getReader()
        def body = ''

        def content = reader.readLine()
        while (content != null) {
            body += content
            content = reader.readLine()
        }

        if (!body) {
            return null
        }

        return new JsonSlurper().parseText(body)
    }
}
package com.clouway.clientsinfo

class ConsoleDisplay: Display {
    override fun print(content: String?) {
        if(content != null) {
            println(content)
        }
    }
}
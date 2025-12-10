package ticklock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TicklockApplication

fun main(args: Array<String>) {
    runApplication<TicklockApplication>(*args)
}
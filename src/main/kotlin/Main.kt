import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.io.File

// Функция, читающая файл посимвольно и отправляющая символы в канал
fun CoroutineScope.readFileToChannel(filePath: String, channel: SendChannel<Char>) = launch {
    File(filePath).bufferedReader().use { reader ->
        var char = reader.read()
        while (char != -1) {
            channel.send(char.toChar())
            char = reader.read()
        }
    }
    channel.close()
}

// Функция преобразования канала символов в канал строк
fun CoroutineScope.transformCharChannelToStringChannel(
    charChannel: ReceiveChannel<Char>,
    stringChannel: SendChannel<String>
) = launch {
    val buffer = StringBuilder()
    for (char in charChannel) {
        if (char == '\n') {
            stringChannel.send(buffer.toString())
            buffer.clear()
        } else {
            buffer.append(char)
        }
    }
    if (buffer.isNotEmpty()) {
        stringChannel.send(buffer.toString())
    }
    stringChannel.close()
}

// Пример использования
fun main() = runBlocking {
    val charChannel = Channel<Char>()
    val stringChannel = Channel<String>()

    readFileToChannel("src/res/file.txt", charChannel)
    transformCharChannelToStringChannel(charChannel, stringChannel)

    for (string in stringChannel) {
        println(string)
    }
}

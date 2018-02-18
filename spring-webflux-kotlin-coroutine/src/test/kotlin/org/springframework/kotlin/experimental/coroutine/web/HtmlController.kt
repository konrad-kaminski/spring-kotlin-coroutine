package org.springframework.kotlin.experimental.coroutine.web

import kotlinx.coroutines.experimental.channels.Channel
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
open class HtmlController {
    @GetMapping("/blog")
    suspend fun blog(model: Model): String {
        model["articles"] = Channel<Article>(1).apply {
            send(Article("TestTitle", "TestText"))
            close()
        }

        return "blog"
    }

    @GetMapping("/blogEndpoint")
    suspend fun blogEndpoint(model: Model): String = blog(model)
}

data class Article(
    val title: String,
    val text: String
)

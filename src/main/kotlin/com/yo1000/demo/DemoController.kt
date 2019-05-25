package com.yo1000.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author yo1000
 */
@RequestMapping("/")
@RestController
class DemoController {
    @GetMapping
    fun getIndex(): Any = "OK"
}

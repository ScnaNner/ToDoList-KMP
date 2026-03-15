package com.example.todolist


import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Taskbar
import javax.imageio.ImageIO

fun main() {
    // 🍎 Tell the macOS Dock to use our icon!
    try {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("mac")) {
            // We grab the icon right out of your resources folder
            val imageStream = Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png")
            if (imageStream != null) {
                val image = ImageIO.read(imageStream)
                Taskbar.getTaskbar().iconImage = image
            }
        }
    } catch (e: Exception) {
        println("Mac Dock icon hack failed: ${e.message}")
    }

    // Launch the actual app
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MyTasks",
            // We keep this here so it works on Windows and Linux too!
            icon = painterResource("icon.png")
        ) {
            App()
        }
    }
}
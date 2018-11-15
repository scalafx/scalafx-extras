package org.scalafx.extras.pong

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}

object PongApp extends JFXApp {

  val game = new Pong()

  stage = new PrimaryStage {
    title = "Pong Demo"
    scene = new Scene(500, 500) {
      fill = LinearGradient(
        startX = 0.0,
        startY = 0.0,
        endX = 0.0,
        endY = 1.0,
        proportional = true,
        cycleMethod = CycleMethod.NoCycle,
        stops = List(Stop(0.0, Color.Black), Stop(0.0, Color.Gray)))
      content = game.pongComponents
    }
  }

  game.initialize()
}
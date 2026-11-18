package com.example.math

enum class AngleMode(val shortLabel: String, val fullName: String) {
  DEG("DEG", "Degrees"),
  RAD("RAD", "Radians"),
  GRAD("GRAD", "Gradians");

  fun next(): AngleMode =
    when (this) {
      DEG -> RAD
      RAD -> GRAD
      GRAD -> DEG
    }
}

package br.com.casaclara.ui

import androidx.compose.runtime.mutableStateOf

/** Estados globais de exibição lidos durante a composição (mudam sem recriar as telas). */
object UiFlags {
    val hideValues = mutableStateOf(false)
    val reduceMotion = mutableStateOf(false)
    val confetti = mutableStateOf(true)
    val haptics = mutableStateOf(true)

    init { br.com.casaclara.domain.Money.masked = { hideValues.value } }
}

package com.equipo.sanmarkfood.restaurante

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

fun proveedorAppCheck(): AppCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()

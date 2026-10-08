package com.equipo.sanmarkfood.restaurante

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

fun proveedorAppCheck(): AppCheckProviderFactory = PlayIntegrityAppCheckProviderFactory.getInstance()

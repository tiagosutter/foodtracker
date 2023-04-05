package br.dev.tiagosutter.foodtracker.dependencyinjection.app

import dagger.hilt.migration.AliasOf
import javax.inject.Scope
import javax.inject.Singleton

// It makes more sense to call it Application Scope (App Scope for short) than calling it Singleton

@Scope
@AliasOf(Singleton::class)
annotation class AppScope {
}
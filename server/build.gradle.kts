plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "2.1.0"
    application
}

group = "id.nearyou.app"
version = "1.0.0"
application {
    mainClass.set("id.nearyou.app.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)

    // Authentication & Security
    implementation("io.ktor:ktor-server-auth:3.3.0")
    implementation("io.ktor:ktor-server-auth-jwt:3.3.0")
    implementation("org.mindrot:jbcrypt:0.4")

    // Serialization
    implementation("io.ktor:ktor-server-content-negotiation:3.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.48.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.48.0")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("net.postgis:postgis-jdbc:2.5.1")

    // Redis
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE")

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

// Task untuk test database connection
tasks.register<JavaExec>("testDb") {
    group = "verification"
    description = "Test database connection and PostGIS"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("id.nearyou.app.TestDatabaseConnectionKt")
}
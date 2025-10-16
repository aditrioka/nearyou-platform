plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
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

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.48.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("net.postgis:postgis-jdbc:2.5.1")

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
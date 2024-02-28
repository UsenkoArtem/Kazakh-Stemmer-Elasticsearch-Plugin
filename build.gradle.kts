import java.nio.file.Paths

buildscript {
    val esVersion = project.properties["esVersion"] ?: "7.13.4"
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.elasticsearch.gradle:build-tools:$esVersion")
    }
}

plugins {
    java
    idea
    id("org.ajoberstar.grgit") version "4.1.0"
    id("nebula.ospackage") version "8.5.6"
    `maven-publish`
}

apply {
    plugin("elasticsearch.esplugin")
}

group = "dev.evo.elasticsearch"

val lastTag = grgit.describe(mapOf("match" to listOf("v*"), "tags" to true)) ?: "v0.0.0"
val pluginVersion = lastTag.split('-', limit = 2)[0].trimStart('v')
val versions = org.elasticsearch.gradle.VersionProperties.getVersions() as Map<String, String>
version = "$pluginVersion-es${versions["elasticsearch"]}"

val distDir = Paths.get(buildDir.path, "distributions")

repositories {
    mavenCentral()
}

dependencies {
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val pluginName = "kazakh-stemmer-plugin"

configure<org.elasticsearch.gradle.plugin.PluginPropertiesExtension> {
    name = pluginName
    description = "kazakh-stemmer-plugin"
    classname = "org.elasticsearch.plugin.KazakhStemmerPlugin"
    version = project.version.toString()
    licenseFile = project.file("LICENSE.txt")
    noticeFile = project.file("NOTICE.txt")
}

tasks.withType<JavaCompile> {
    options.isWarnings = true
    options.compilerArgs.add("-Werror")
}

tasks.named("validateElasticPom") {
    enabled = false
}

tasks.named("assemble") {
    dependsOn("deb")
}

tasks.register("deb", com.netflix.gradle.plugins.deb.Deb::class) {
    dependsOn("bundlePlugin")

    packageName = project.name

    requires("elasticsearch", versions["elasticsearch"])
        .or("elasticsearch-oss", versions["elasticsearch"])

    from(zipTree(tasks["bundlePlugin"].outputs.files.singleFile))
    into("/usr/share/elasticsearch/plugins/$pluginName")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.named("deb"))
        }
    }
    repositories {
        maven {
            name = "GitLab"
            url = uri("https://gitlab.com/api/v4/projects/3351/packages/maven")
            val gitlabToken = project.properties["gitlabToken"]?.toString()
                ?: System.getenv("CI_JOB_TOKEN")
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = gitlabToken
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}
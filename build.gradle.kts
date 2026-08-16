plugins {
    `java-library`
    `maven-publish`
    id("org.cyclonedx.bom") version "2.3.1"
}

group = "org.xq"
version = providers.gradleProperty("version").orElse("0.1.0-SNAPSHOT").get()

allprojects { repositories { mavenCentral() } }
subprojects {
    group = rootProject.group
    version = rootProject.version
    plugins.withId("java-library") {
        apply(plugin = "maven-publish")
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    artifactId = "xq-test-sdk-jvm-${project.name}"
                }
            }
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/ExperienceQuality/xq-svctest-kit")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
    plugins.withId("java") {
        tasks.withType<Test>().configureEach {
            useTestNG {
                suiteName = System.getenv("XQ_MEDIUM_SUITE") ?: "xq-svctest-kit-medium"
                listeners.add("org.xq.testsdk.core.TestNgLifecycleDiagnostics")
            }
            systemProperty("xq.medium.suite", System.getenv("XQ_MEDIUM_SUITE") ?: "xq-svctest-kit-medium")
            systemProperty("xq.hub.ticket", System.getenv("XQ_HUB_TICKET") ?: "67")
            systemProperty("xq.pull-request", System.getenv("GITHUB_REF_NAME") ?: "local")
            testLogging { showStandardStreams = true }
        }
    }
}

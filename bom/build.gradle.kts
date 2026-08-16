plugins { `java-platform`; `maven-publish` }
javaPlatform { allowDependencies() }
dependencies { constraints { api(project(":core")); api(project(":http")); api(project(":openapi")); api(project(":postgres")) } }

publishing {
    publications {
        create<MavenPublication>("mavenBom") {
            from(components["javaPlatform"])
            artifactId = "xq-test-sdk-jvm-bom"
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

plugins { `java-library` }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
dependencies { api("org.testng:testng:7.10.2") }

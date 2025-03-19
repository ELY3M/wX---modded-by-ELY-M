def version_value = sh(returnStdout: true, script: "cat build.gradle | grep -o 'version = [^,]*'").trim()
sh "echo Project in version value: $version_value"
def version = version_value.split(/=/)[1]
sh "echo final version: $version"
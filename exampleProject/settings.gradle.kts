rootProject.name = "exampleProject"

//includeBuild("../plugin")

include("system1")
include("system2", "system2:moduleA")
include("docs")

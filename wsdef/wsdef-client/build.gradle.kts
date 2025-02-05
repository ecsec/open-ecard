import io.mateo.cxf.codegen.wsdl2java.Wsdl2Java

description = "wsdef-client"

plugins {
	id("openecard.lib-conventions")
	alias(libs.plugins.cxf)
}

dependencies {
	api(libs.jaxb.api)
	api(libs.jaxb.ws.api)
}

// the generated java files produce errors in modern javadoc versions
tasks.named("javadoc").configure { enabled = false }

sourceSets.main.configure {
	resources {
		srcDir("src/main/wsdl")
	}
}


cxfCodegen {
	cxfVersion = libs.versions.cxf
}

tasks.register("ecard", Wsdl2Java::class) {
	var outDir: DirectoryProperty? = null
	toolOptions {
		outDir = outputDir
		wsdl.set(projectDir.resolve("src/main/wsdl/ALL.wsdl").absolutePath)
		wsdlLocation.set("ALL.wsdl")
		bindingFiles.add(projectDir.resolve("src/main/wsdl-binding/serialization.xjc").absolutePath)
		bindingFiles.add(projectDir.resolve("src/main/wsdl-binding/wrapperStyle.xjc").absolutePath)
		xjcArgs.add("-npa")
	}

	doLast {
		// I'm sure there is a more elegant way to delete files, but this works for now
		val oecWsDir = outDir?.dir("org/openecard/ws")?.get()
		oecWsDir?.let {
			it.asFile.listFiles().forEach {
				if (it.name.endsWith(".java")) {
					it.delete()
				}
			}
		}
	}
}

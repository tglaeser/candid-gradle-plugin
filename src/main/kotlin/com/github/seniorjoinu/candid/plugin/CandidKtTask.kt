package com.github.seniorjoinu.candid.plugin

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import senior.joinu.candid.CandidCodeGenerator
import java.io.File

/**
 * The Candid to Kotlin task.
 */
abstract class CandidKtTask : SourceTask() {

    @get:Internal internal abstract var sourceSetName: String
    @get:Internal internal val sourceSet: CandidSourceSet
        get() = project.candidExtension.sourceSets.getByName(sourceSetName)

    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE) @get:Option(option = "didFiles", description = "A file collection of Candid sources; if not explicitly set resolved from the matching Candid source set.") internal var didFiles: FileCollection = project.files()
    @get:Input @get:Optional @get:Option(option = "genPackage", description = "The Kotlin package name; if not explicitly set derived from the Candid source directory layout.") abstract val genPackage: Property<String>

    @TaskAction fun execute() {
        val prettyTag = "[${genPackage.orNull ?: "empty"}]"
        val destinationDir = sourceSet.candid.destinationDirectory.asFile.get()

        didFiles.forEach { didFile ->
            val packageName = getPackageName(didFile)
            logger.info("$prettyTag pkgName :: $packageName")
            logger.info("$prettyTag didPath :: $didFile")
            logger.info("$prettyTag genPath :: $destinationDir")

            val src = CandidCodeGenerator.Source.File(didFile.toPath())
            CandidCodeGenerator.generate(src, destinationDir, packageName)
        }
    }

    private fun getPackageName(didFile: File): String {
        sourceSet.candid.sourceDirectories.forEach { srcDir ->
            val packagePath = didFile.relativeTo(srcDir).parent
            if (packagePath != null && !packagePath.startsWith("..")) return packagePath.toString().replace(File.separatorChar, '.')
        }
        return genPackage.getOrElse("")
    }
}

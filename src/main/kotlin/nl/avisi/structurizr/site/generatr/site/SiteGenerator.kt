package nl.avisi.structurizr.site.generatr.site

import com.structurizr.Workspace
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import nl.avisi.structurizr.site.generatr.includedSoftwareSystems
import nl.avisi.structurizr.site.generatr.site.model.*
import nl.avisi.structurizr.site.generatr.site.views.*
import java.io.File
import java.nio.file.Path

fun copySiteWideAssets(exportDir: File) {
    val css = object {}.javaClass.getResource("/assets/css/style.css")?.readText()
        ?: throw IllegalStateException("CSS file not found on classpath")
    val cssFile = File(exportDir, "style.css")

    cssFile.writeText(css)
}

fun generateRedirectingIndexPage(exportDir: File, defaultBranch: String) {
    val htmlFile = File(exportDir, "index.html")
    htmlFile.writeText(
        buildString {
            appendLine("<!doctype html>")
            appendHTML().html {
                attributes["lang"] = "en"
                head {
                    meta {
                        httpEquiv = "refresh"
                        content = "0; url=$defaultBranch/"
                    }
                    title { +"Structurizr site generatr" }
                }
                body()
            }
        }
    )
}

fun generateSite(
    version: String,
    workspace: Workspace,
    assetsDir: File?,
    exportDir: File,
    branches: List<String>,
    currentBranch: String
) {
    val generatorContext = GeneratorContext(version, workspace, branches, currentBranch) { name, baseUrl ->
        val view = workspace.views.views.single { view -> view.key == name }
        generateDiagramWithElementLinks(workspace, exportDir, view, baseUrl)
    }

    if (assetsDir != null) copyAssets(assetsDir, File(exportDir, currentBranch))
    generateHtmlFiles(generatorContext, exportDir)
}

private fun copyAssets(assetsDir: File, exportDir: File) {
    assetsDir.copyRecursively(exportDir, overwrite = true)
}

private fun generateHtmlFiles(context: GeneratorContext, exportDir: File) {
    val branchDir = File(exportDir, context.currentBranch)
    buildList {
        add { writeHtmlFile(branchDir, HomePageViewModel(context)) }
        add { writeHtmlFile(branchDir, WorkspaceDecisionsPageViewModel(context)) }
        add { writeHtmlFile(branchDir, SoftwareSystemsPageViewModel(context)) }

        context.workspace.documentation.sections
            .filter { it.order != 1 }
            .forEach {
                add { writeHtmlFile(branchDir, WorkspaceDocumentationSectionPageViewModel(context, it)) }
            }
        context.workspace.documentation.decisions
            .forEach {
                add { writeHtmlFile(branchDir, WorkspaceDecisionPageViewModel(context, it)) }
            }

        context.workspace.model.includedSoftwareSystems.forEach {
            add { writeHtmlFile(branchDir, SoftwareSystemHomePageViewModel(context, it)) }
            add { writeHtmlFile(branchDir, SoftwareSystemContextPageViewModel(context, it)) }
            add { writeHtmlFile(branchDir, SoftwareSystemContainerPageViewModel(context, it)) }
            add { writeHtmlFile(branchDir, SoftwareSystemComponentPageViewModel(context, it)) }
            add { writeHtmlFile(branchDir, SoftwareSystemDeploymentPageViewModel(context, it)) }
            add { writeHtmlFile(branchDir, SoftwareSystemDependenciesPageViewModel(context, it)) }
            add { writeHtmlFile(branchDir, SoftwareSystemDecisionsPageViewModel(context, it)) }
            add { writeHtmlFile(branchDir, SoftwareSystemSectionsPageViewModel(context, it)) }

            it.documentation.decisions.forEach { decision ->
                add { writeHtmlFile(branchDir, SoftwareSystemDecisionPageViewModel(context, it, decision)) }
            }

            it.documentation.sections.filter { section -> section.order != 1 }.forEach { section ->
                add { writeHtmlFile(branchDir, SoftwareSystemSectionPageViewModel(context, it, section)) }
            }
        }
    }
        .parallelStream()
        .forEach { it.invoke() }
}

private fun writeHtmlFile(exportDir: File, viewModel: PageViewModel) {
    val htmlFile = File(exportDir, Path.of(viewModel.url, "index.html").toString())
    htmlFile.parentFile.mkdirs()
    htmlFile.writeText(
        buildString {
            appendLine("<!doctype html>")
            appendHTML().html {
                when (viewModel) {
                    is HomePageViewModel -> homePage(viewModel)
                    is SoftwareSystemsPageViewModel -> softwareSystemsPage(viewModel)
                    is SoftwareSystemHomePageViewModel -> softwareSystemHomePage(viewModel)
                    is SoftwareSystemContextPageViewModel -> softwareSystemContextPage(viewModel)
                    is SoftwareSystemContainerPageViewModel -> softwareSystemContainerPage(viewModel)
                    is SoftwareSystemComponentPageViewModel -> softwareSystemComponentPage(viewModel)
                    is SoftwareSystemDeploymentPageViewModel -> softwareSystemDeploymentPage(viewModel)
                    is SoftwareSystemDependenciesPageViewModel -> softwareSystemDependenciesPage(viewModel)
                    is SoftwareSystemDecisionPageViewModel -> softwareSystemDecisionPage(viewModel)
                    is SoftwareSystemDecisionsPageViewModel -> softwareSystemDecisionsPage(viewModel)
                    is SoftwareSystemSectionPageViewModel -> softwareSystemSectionPage(viewModel)
                    is SoftwareSystemSectionsPageViewModel -> softwareSystemSectionsPage(viewModel)
                    is WorkspaceDecisionPageViewModel -> workspaceDecisionPage(viewModel)
                    is WorkspaceDecisionsPageViewModel -> workspaceDecisionsPage(viewModel)
                    is WorkspaceDocumentationSectionPageViewModel -> workspaceDocumentationSectionPage(viewModel)
                }
            }
        }
    )
}

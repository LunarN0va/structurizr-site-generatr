package nl.avisi.structurizr.site.generatr.site.views

import kotlinx.html.*
import nl.avisi.structurizr.site.generatr.site.model.TableViewModel

fun FlowContent.table(viewModel: TableViewModel) {
    viewModel.headerRows.forEach { rowViewModel ->
        if (rowViewModel.columns.size > 2) {
            table (classes = "table is-fullwidth large-table") {
                thead {
                    viewModel.headerRows.forEach {
                        row(it)
                    }
                }
                tbody {
                    viewModel.bodyRows.forEach {
                        row(it)
                    }
                }
            }
        } else {
            table (classes = "table is-fullwidth") {
                thead {
                    viewModel.headerRows.forEach {
                        row(it)
                    }
                }
                tbody {
                    viewModel.bodyRows.forEach {
                        row(it)
                    }
                }
            }
        }
    }
}

private fun THEAD.row(viewModel: TableViewModel.RowViewModel) {
    tr {
        viewModel.columns.forEach {
            cell(it)
        }
    }
}

private fun TBODY.row(viewModel: TableViewModel.RowViewModel) {
    tr {
        viewModel.columns.forEach {
            cell(it)
        }
    }
}

private fun TR.cell(viewModel: TableViewModel.CellViewModel) {
    when (viewModel) {
        is TableViewModel.TextCellViewModel ->
            if (viewModel.isHeader && viewModel.greyText)
                th { span(classes = "has-text-grey") { +viewModel.title } }
            else if (viewModel.isHeader)
                th { +viewModel.title }
            else if (viewModel.boldText && viewModel.oneTenthWidth)
                td(classes = "is-one-tenth") { span(classes = "has-text-weight-bold") { +viewModel.title } }
            else if (viewModel.boldText)
                td { span(classes = "has-text-weight-bold") { +viewModel.title } }
            else if (viewModel.oneTenthWidth)
                td(classes = "is-one-tenth") { +viewModel.title }
            else
                td { +viewModel.title }
        is TableViewModel.LinkCellViewModel ->
            if (viewModel.isHeader)
                th { link(viewModel.link) }
            else
                td { link(viewModel.link) }
        is TableViewModel.ExternalLinkCellViewModel ->
            if (viewModel.isHeader)
                th { externalLink(viewModel.link) }
            else
                td { externalLink(viewModel.link) }
    }
}

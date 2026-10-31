package org.flexstore.infra.invoice

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.FontFactory
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.flexstore.domain.invoice.*
import java.awt.Color
import java.io.ByteArrayOutputStream

// Lays out the content of an invoice on an A4 page, with OpenPDF: the title and the dates, the seller and the
// customer side by side, the table of the lines, the totals, then the footer
class OpenPdfInvoiceRenderer : InvoiceRenderer {

    override fun render(invoiceDocument: InvoiceDocument): InvoicePdf {
        val output = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 48f, 48f, 48f, 48f)
        PdfWriter.getInstance(document, output)
        document.open()
        document.add(Paragraph(invoiceDocument.title().value, font(TITLE_SIZE, Font.BOLD)))
        document.add(linesOf(invoiceDocument.dates(), SMALL_SIZE))
        document.add(partiesOf(invoiceDocument))
        document.add(tableOf(invoiceDocument))
        document.add(totalsOf(invoiceDocument.totals()))
        document.add(linesOf(invoiceDocument.footer(), SMALL_SIZE).apply { spacingBefore = 24f })
        document.close()
        return InvoicePdf(output.toByteArray())
    }

    // The seller on the left, the billing then the delivery address on the right
    private fun partiesOf(invoiceDocument: InvoiceDocument) = PdfPTable(2).apply {
        widthPercentage = 100f
        setSpacingBefore(24f)
        addCell(borderless(linesOf(invoiceDocument.seller(), TEXT_SIZE)))
        val customer = linesOf(invoiceDocument.billedTo(), TEXT_SIZE).apply {
            add(linesOf(invoiceDocument.deliveredTo(), TEXT_SIZE).apply { spacingBefore = 12f })
        }
        addCell(borderless(customer))
    }

    private fun tableOf(invoiceDocument: InvoiceDocument) = PdfPTable(floatArrayOf(5f, 1.5f, 2f, 2f)).apply {
        widthPercentage = 100f
        setSpacingBefore(24f)
        headerRows = 1
        invoiceDocument.tableHeader().asList().forEachIndexed { column, text ->
            addCell(cellOf(text, font(TEXT_SIZE, Font.BOLD), column).apply { backgroundColor = HEADER_COLOR })
        }
        invoiceDocument.tableRows().asList().forEach { row ->
            row.asList().forEachIndexed { column, text ->
                addCell(cellOf(text, font(TEXT_SIZE, Font.NORMAL), column))
            }
        }
    }

    // The totals, under the last two columns of the table; the last one in bold
    private fun totalsOf(totals: InvoiceRows) = PdfPTable(floatArrayOf(6.5f, 2f, 2f)).apply {
        widthPercentage = 100f
        setSpacingBefore(8f)
        val rows = totals.asList()
        rows.forEachIndexed { index, row ->
            val style = if (index == rows.lastIndex) Font.BOLD else Font.NORMAL
            val (label, amount) = row.asList()
            addCell(borderless(Phrase("")))
            addCell(borderless(Phrase(label.value, font(TEXT_SIZE, style))))
            addCell(borderless(Phrase(amount.value, font(TEXT_SIZE, style))).apply {
                horizontalAlignment = Element.ALIGN_RIGHT
            })
        }
    }

    private fun linesOf(lines: InvoiceLines, size: Float) = Paragraph().apply {
        lines.asList().forEachIndexed { index, line ->
            val style = if (index == 0 && size == TEXT_SIZE) Font.BOLD else Font.NORMAL
            add(Phrase(line.value + "\n", font(size, style)))
        }
    }

    // The first column (the designation) on the left, the numbers on the right
    private fun cellOf(text: InvoiceText, font: Font, column: Int) = PdfPCell(Phrase(text.value, font)).apply {
        horizontalAlignment = if (column == 0) Element.ALIGN_LEFT else Element.ALIGN_RIGHT
        setPadding(6f)
        borderColor = BORDER_COLOR
    }

    private fun borderless(element: Element) = PdfPCell().apply {
        border = PdfPCell.NO_BORDER
        addElement(element)
    }

    // Helvetica in Windows-1252: the accents and the euro sign
    private fun font(size: Float, style: Int) =
        FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, size, style)
}

private const val TITLE_SIZE = 20f
private const val TEXT_SIZE = 10f
private const val SMALL_SIZE = 9f
private val HEADER_COLOR = Color(0xF1, 0xF5, 0xF9)
private val BORDER_COLOR = Color(0xCB, 0xD5, 0xE1)

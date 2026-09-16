package com.tgcrongai.givingapp.export

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Writes a minimal, valid .docx (a plain OOXML zip) containing a title and a
 * table, with no external dependency. Apache POI was in the original spec,
 * but its `poi-ooxml` artifact drags in java.awt / javax.xml.stream classes
 * that are unavailable on Android and routinely break at runtime, so this
 * hand-rolled writer is used instead — same output format, no dependency risk.
 */
object MinimalDocxWriter {

    fun write(file: File, title: String, headers: List<String>, rows: List<List<String>>) {
        FileOutputStream(file).use { fos ->
            ZipOutputStream(fos).use { zip ->
                zip.putNextEntry(ZipEntry("[Content_Types].xml"))
                zip.write(contentTypesXml().toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("_rels/.rels"))
                zip.write(relsXml().toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("word/document.xml"))
                zip.write(documentXml(title, headers, rows).toByteArray())
                zip.closeEntry()
            }
        }
    }

    private fun esc(text: String) = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    private fun contentTypesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        |<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
        |<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
        |<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
        |</Types>""".trimMargin()

    private fun relsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        |<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        |<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
        |</Relationships>""".trimMargin()

    private fun documentXml(title: String, headers: List<String>, rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
        sb.append("<w:body>")

        // Title paragraph
        sb.append("<w:p><w:r><w:rPr><w:b/><w:sz w:val=\"32\"/></w:rPr><w:t>${esc(title)}</w:t></w:r></w:p>")

        // Table
        sb.append("<w:tbl>")
        sb.append(tableRow(headers, bold = true))
        rows.forEach { sb.append(tableRow(it, bold = false)) }
        sb.append("</w:tbl>")

        sb.append("</w:body></w:document>")
        return sb.toString()
    }

    private fun tableRow(cells: List<String>, bold: Boolean): String {
        val rPr = if (bold) "<w:rPr><w:b/></w:rPr>" else ""
        val cellsXml = cells.joinToString(separator = "") { cell ->
            "<w:tc><w:p><w:r>$rPr<w:t>${esc(cell)}</w:t></w:r></w:p></w:tc>"
        }
        return "<w:tr>$cellsXml</w:tr>"
    }
}

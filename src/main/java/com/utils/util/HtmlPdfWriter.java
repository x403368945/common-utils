package com.utils.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.utils.enums.Charsets;
import com.utils.excel.entity.Range;
import com.utils.util.Num.RangeInt;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;

import static com.utils.enums.Charsets.UTF_8;

/**
 * @author Jason Xie on 2017/12/11.
 */
@Slf4j
public final class HtmlPdfWriter {
    private HtmlPdfWriter() {
    }

    public static HtmlPdfWriter of() {
        return new HtmlPdfWriter();
    }

    /**
     * css文件绝对路径
     */
    private File css;
    /**
     * 写入pdf文件绝对路径
     */
    private File pdf;
    /**
     * RectangleReadOnly 每页大小示例：new RectangleReadOnly(PageSize.A3.getWidth(), PageSize.A3.getHeight() / 2)
     */
    private RectangleReadOnly rectangle = new RectangleReadOnly(PageSize.A3);
    /**
     * 水印内容， 为 null 表示不加水印
     */
    private String watermark;
    /**
     * 页面写入事件监听,当设置了此参数，watermark 参数则无效，水印需在此参数中自行添加
     */
    private PdfPageEvent pageEvent;

    public HtmlPdfWriter setCss(File css) {
        this.css = css;
        return this;
    }

    public HtmlPdfWriter setPdf(File pdf) {
        this.pdf = pdf;
        return this;
    }

    public HtmlPdfWriter setRectangle(final RectangleReadOnly rectangle) {
        this.rectangle = rectangle;
        return this;
    }

    public HtmlPdfWriter setWatermark(final String watermark) {
        this.watermark = watermark;
        return this;
    }

    public HtmlPdfWriter setPageEvent(final PdfPageEvent pageEvent) {
        this.pageEvent = pageEvent;
        return this;
    }

    /**
     * html写入pdf, 默认A3大小
     *
     * @param file {@link File} html文件对象
     * @return {@link File} pdf文件绝对路径
     */
    @SneakyThrows
    public final File write(final File file) {
        return write(new FileInputStream(file));
    }

    /**
     * 将 html 写入到 PDF
     *
     * @param html {@link String} html文件内容
     * @return {@link File} 写入 PDF 文件路径
     */
    public final File write(final String html) {
        return write(new ByteArrayInputStream(html.getBytes(UTF_8.charset)));
    }

    /**
     * 将 html 写入到 PDF
     *
     * @param inputStream {@link InputStream} html文件数据流
     * @return {@link File} 写入 PDF 文件路径
     */
    @SneakyThrows
    public final File write(final InputStream inputStream) {
        if (!pdf.getParentFile().exists()) {
            FPath.of(pdf.getParentFile()).mkdirs();
        }
        final Document document = new Document(rectangle);
        document.setMargins(0, 0, 0, 0);
        final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdf));

        if (Objects.nonNull(pageEvent)) {
            writer.setPageEvent(pageEvent);
        } else {
            if (Objects.nonNull(watermark)) {
                writer.setPageEvent(new PdfPageEventHelper() {
                    private Phrase phrase = new Phrase(watermark, new Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 30, Font.BOLD, new GrayColor(0.95f)));

                    @Override
                    public void onEndPage(final PdfWriter writer, final Document document) {
                        RangeInt.of(0, 2).forEach(x ->
                                RangeInt.of(0, 2).forEach(y ->
                                        ColumnText.showTextAligned(writer.getDirectContentUnder(), Element.ALIGN_CENTER, phrase, (50.5f + x * 350), (50.0f + y * 300), 45)
                                )
                        );
                    }
                });
            }
        }
        document.open();
        XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                inputStream,
                new FileInputStream(css),
                UTF_8.charset);

        document.close();
        FPath.of(pdf).chmod(644);
        return pdf;
    }
}

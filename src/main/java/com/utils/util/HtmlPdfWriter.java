package com.utils.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 *
 * @author Jason Xie on 2017/12/11.
 */
@Slf4j
public class HtmlPdfWriter {
    private HtmlPdfWriter() {}
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
     * 是否添加水印
     */
    private boolean addWatermark = true;
    /**
     * 页面写入事件监听,当设置了此参数，addWatermark参数则无效，水印需在此参数中自行添加
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

    public HtmlPdfWriter setRectangle(RectangleReadOnly rectangle) {
        this.rectangle = rectangle;
        return this;
    }

    public HtmlPdfWriter setAddWatermark(boolean addWatermark) {
        this.addWatermark = addWatermark;
        return this;
    }

    public HtmlPdfWriter setPageEvent(PdfPageEvent pageEvent) {
        this.pageEvent = pageEvent;
        return this;
    }

    /**
     * html写入pdf, 默认A3大小
     * param htmlPath String html文件绝对路径
     * param cssPath String css文件绝对路径
     * param pdfPath String 写入pdf文件绝对路径
     * param rectangle RectangleReadOnly 每页大小示例：new RectangleReadOnly(PageSize.A3.getWidth(), PageSize.A3.getHeight() / 2)
     * param addWatermark boolean 是否添加水印
     * param pageEvent PdfPageEvent 页面写入事件监听,当设置了此参数，addWatermark参数则无效，水印需在此参数中自行添加
     * @throws Exception 写入失败
     * @return pdf文件绝对路径
     */
    public final File write(File file) throws Exception {
        return write(new FileInputStream(file));
    }
    public final File write(String html) throws Exception {
        return write(new ByteArrayInputStream(html.getBytes(Charsets.UTF_8)));
    }
    public final File write(InputStream inputStream) throws Exception {
        if (!pdf.getParentFile().exists()) {
            FPath.of(pdf.getParentFile()).mkdirs();
        }
        final Document document = new Document(rectangle);
        document.setMargins(0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdf));

        if (null != pageEvent) {
            writer.setPageEvent(pageEvent);
        } else {
            if(addWatermark) {
                writer.setPageEvent(new PdfPageEventHelper(){
                    private Phrase phrase = new Phrase("www.anavss.com", new Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 30, Font.BOLD, new GrayColor(0.95f)));
                    @Override
                    public void onEndPage(PdfWriter writer, Document document) {
                        for (int x = 0; x < 3; x++) {
                            for (int y = 0; y < 3; y++) {
                                ColumnText.showTextAligned(writer.getDirectContentUnder(),  Element.ALIGN_CENTER, phrase, (50.5f + x * 350), (50.0f + y * 300), 45);
                            }
                        }
                    }
                });
            }
        }
        document.open();
        XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                inputStream,
                new FileInputStream(css),
                Charsets.UTF_8);

        document.close();
        FPath.of(pdf).chmod(644);
        return pdf;
    }
}

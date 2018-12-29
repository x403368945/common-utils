package com.utils.excel;//package com.core.util.excel;
//
//import org.apache.poi.ss.formula.*;
//import org.apache.poi.ss.formula.ptg.AreaErrPtg;
//import org.apache.poi.ss.formula.ptg.AreaPtg;
//import org.apache.poi.ss.formula.ptg.Ptg;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.ss.usermodel.helpers.RowShifter;
//import org.apache.poi.ss.util.CellRangeAddress;
//import org.apache.poi.util.Internal;
//import org.apache.poi.util.POILogFactory;
//import org.apache.poi.util.POILogger;
//import org.apache.poi.xssf.streaming.*;
//import org.apache.poi.xssf.usermodel.*;
//import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * @author 谢长春 on 2017/11/20.
// */
//public class SXSSFRowShifter  extends RowShifter {
//    private static final POILogger logger = POILogFactory.getLogger(SXSSFRowShifter.class);
//
//    public SXSSFRowShifter(SXSSFSheet sh) {
//        super(sh);
//    }
//
//    /** @deprecated */
//    public List<CellRangeAddress> shiftMerged(int startRow, int endRow, int n) {
//        return this.shiftMergedRegions(startRow, endRow, n);
//    }
//
//    public void updateNamedRanges(FormulaShifter shifter) {
//        Workbook wb = this.sheet.getWorkbook();
//        SXSSFEvaluationWorkbook fpb = SXSSFEvaluationWorkbook.create((SXSSFWorkbook)wb);
//        Iterator i$ = wb.getAllNames().iterator();
//
//        while(i$.hasNext()) {
//            Name name = (Name)i$.next();
//            String formula = name.getRefersToFormula();
//            int sheetIndex = name.getSheetIndex();
//            Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.NAMEDRANGE, sheetIndex, -1);
//            if(shifter.adjustFormula(ptgs, sheetIndex)) {
//                String shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
//                name.setRefersToFormula(shiftedFmla);
//            }
//        }
//
//    }
//
//    public void updateFormulas(FormulaShifter shifter) {
//        this.updateSheetFormulas(this.sheet, shifter);
//        Workbook wb = this.sheet.getWorkbook();
//        Iterator i$ = wb.iterator();
//
//        while(i$.hasNext()) {
//            Sheet sh = (Sheet)i$.next();
//            if(this.sheet != sh) {
//                this.updateSheetFormulas(sh, shifter);
//            }
//        }
//
//    }
//
//    private void updateSheetFormulas(Sheet sh, FormulaShifter shifter) {
//        Iterator i$ = sh.iterator();
//
//        while(i$.hasNext()) {
//            Row r = (Row)i$.next();
//            SXSSFRow row = (SXSSFRow)r;
//            this.updateRowFormulas(row, shifter);
//        }
//
//    }
//
//    @Internal
//    public void updateRowFormulas(Row row, FormulaShifter shifter) {
//        SXSSFSheet sheet = (SXSSFSheet)row.getSheet();
//        Iterator i$ = row.iterator();
//
//        while(i$.hasNext()) {
//            Cell c = (Cell)i$.next();
//            SXSSFCell cell = (SXSSFCell)c;
//            CTCell ctCell = cell.getCTCell();
//            if(ctCell.isSetF()) {
//                CTCellFormula f = ctCell.getF();
//                String formula = f.getStringValue();
//                if(formula.length() > 0) {
//                    String shiftedFormula = shiftFormula(row, formula, shifter);
//                    if(shiftedFormula != null) {
//                        f.setStringValue(shiftedFormula);
//                        if(f.getT() == STCellFormulaType.SHARED) {
//                            int si = (int)f.getSi();
//                            CTCellFormula sf = sheet.getSharedFormula(si);
//                            sf.setStringValue(shiftedFormula);
//                            this.updateRefInCTCellFormula(row, shifter, sf);
//                        }
//                    }
//                }
//
//                this.updateRefInCTCellFormula(row, shifter, f);
//            }
//        }
//
//    }
//
//    private void updateRefInCTCellFormula(Row row, FormulaShifter shifter, CTCellFormula f) {
//        if(f.isSetRef()) {
//            String ref = f.getRef();
//            String shiftedRef = shiftFormula(row, ref, shifter);
//            if(shiftedRef != null) {
//                f.setRef(shiftedRef);
//            }
//        }
//
//    }
//
//    private static String shiftFormula(Row row, String formula, FormulaShifter shifter) {
//        Sheet sheet = row.getSheet();
//        Workbook wb = sheet.getWorkbook();
//        int sheetIndex = wb.getSheetIndex(sheet);
//        int rowIndex = row.getRowNum();
//        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create((XSSFWorkbook)wb);
//
//        try {
//            Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, sheetIndex, rowIndex);
//            String shiftedFmla = null;
//            if(shifter.adjustFormula(ptgs, sheetIndex)) {
//                shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
//            }
//
//            return shiftedFmla;
//        } catch (FormulaParseException var10) {
//            logger.log(5, new Object[]{"Error shifting formula on row ", Integer.valueOf(row.getRowNum()), var10});
//            return formula;
//        }
//    }
//
//    public void updateConditionalFormatting(FormulaShifter shifter) {
//        XSSFSheet xsheet = (XSSFSheet)this.sheet;
//        XSSFWorkbook wb = xsheet.getWorkbook();
//        int sheetIndex = wb.getSheetIndex(this.sheet);
//        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
//        CTWorksheet ctWorksheet = xsheet.getCTWorksheet();
//        CTConditionalFormatting[] conditionalFormattingArray = ctWorksheet.getConditionalFormattingArray();
//
//        for(int j = conditionalFormattingArray.length - 1; j >= 0; --j) {
//            CTConditionalFormatting cf = conditionalFormattingArray[j];
//            ArrayList<CellRangeAddress> cellRanges = new ArrayList();
//            Iterator i$ = cf.getSqref().iterator();
//
//            int i$;
//            while(i$.hasNext()) {
//                Object stRef = i$.next();
//                String[] regions = stRef.toString().split(" ");
//                String[] arr$ = regions;
//                i$ = regions.length;
//
//                for(int i$ = 0; i$ < i$; ++i$) {
//                    String region = arr$[i$];
//                    cellRanges.add(CellRangeAddress.valueOf(region));
//                }
//            }
//
//            boolean changed = false;
//            List<CellRangeAddress> temp = new ArrayList();
//            Iterator i$ = cellRanges.iterator();
//
//            while(i$.hasNext()) {
//                CellRangeAddress craOld = (CellRangeAddress)i$.next();
//                CellRangeAddress craNew = shiftRange(shifter, craOld, sheetIndex);
//                if(craNew == null) {
//                    changed = true;
//                } else {
//                    temp.add(craNew);
//                    if(craNew != craOld) {
//                        changed = true;
//                    }
//                }
//            }
//
//            if(changed) {
//                int nRanges = temp.size();
//                if(nRanges == 0) {
//                    ctWorksheet.removeConditionalFormatting(j);
//                    continue;
//                }
//
//                List<String> refs = new ArrayList();
//                Iterator i$ = temp.iterator();
//
//                while(i$.hasNext()) {
//                    CellRangeAddress a = (CellRangeAddress)i$.next();
//                    refs.add(a.formatAsString());
//                }
//
//                cf.setSqref(refs);
//            }
//
//            CTCfRule[] arr$ = cf.getCfRuleArray();
//            int len$ = arr$.length;
//
//            for(i$ = 0; i$ < len$; ++i$) {
//                CTCfRule cfRule = arr$[i$];
//                String[] formulaArray = cfRule.getFormulaArray();
//
//                for(int i = 0; i < formulaArray.length; ++i) {
//                    String formula = formulaArray[i];
//                    Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, sheetIndex, -1);
//                    if(shifter.adjustFormula(ptgs, sheetIndex)) {
//                        String shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
//                        cfRule.setFormulaArray(i, shiftedFmla);
//                    }
//                }
//            }
//        }
//
//    }
//
//    public void updateHyperlinks(FormulaShifter shifter) {
//        int sheetIndex = this.sheet.getWorkbook().getSheetIndex(this.sheet);
//        List<? extends Hyperlink> hyperlinkList = this.sheet.getHyperlinkList();
//        Iterator i$ = hyperlinkList.iterator();
//
//        while(i$.hasNext()) {
//            Hyperlink hyperlink = (Hyperlink)i$.next();
//            XSSFHyperlink xhyperlink = (XSSFHyperlink)hyperlink;
//            String cellRef = xhyperlink.getCellRef();
//            CellRangeAddress cra = CellRangeAddress.valueOf(cellRef);
//            CellRangeAddress shiftedRange = shiftRange(shifter, cra, sheetIndex);
//            if(shiftedRange != null && shiftedRange != cra) {
//                xhyperlink.setCellReference(shiftedRange.formatAsString());
//            }
//        }
//
//    }
//
//    private static CellRangeAddress shiftRange(FormulaShifter shifter, CellRangeAddress cra, int currentExternSheetIx) {
//        AreaPtg aptg = new AreaPtg(cra.getFirstRow(), cra.getLastRow(), cra.getFirstColumn(), cra.getLastColumn(), false, false, false, false);
//        Ptg[] ptgs = new Ptg[]{aptg};
//        if(!shifter.adjustFormula(ptgs, currentExternSheetIx)) {
//            return cra;
//        } else {
//            Ptg ptg0 = ptgs[0];
//            if(ptg0 instanceof AreaPtg) {
//                AreaPtg bptg = (AreaPtg)ptg0;
//                return new CellRangeAddress(bptg.getFirstRow(), bptg.getLastRow(), bptg.getFirstColumn(), bptg.getLastColumn());
//            } else if(ptg0 instanceof AreaErrPtg) {
//                return null;
//            } else {
//                throw new IllegalStateException("Unexpected shifted ptg class (" + ptg0.getClass().getName() + ")");
//            }
//        }
//    }
//}

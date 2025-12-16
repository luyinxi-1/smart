package com.upc.modular.course.export;

import com.upc.modular.course.param.vo.CourseInfoExportVO;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class CourseInfoDocxBuilder {

    public byte[] buildDocument(CourseInfoExportVO vo) throws Exception {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // 标题
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun tr = title.createRun();
            tr.setText(safe(vo.getCourseName()) + " 课程");
            tr.setFontFamily("宋体");
            tr.setFontSize(16);
            tr.setBold(true);

            // 计算总行数
            List<CourseInfoExportVO.TextbookInfo> tbs = vo.getTextbooks() == null ? Collections.emptyList() : vo.getTextbooks();
            int tbRows = tbs.size() > 0 ? tbs.size() : 1;
            
            // 计算多班级学生行数和有学生的班级数
            List<CourseInfoExportVO.ClassSection> classSections = vo.getClassSections();
            int totalStudentCount = 0;
            if (classSections != null && !classSections.isEmpty()) {
                for (CourseInfoExportVO.ClassSection section : classSections) {
                    List<CourseInfoExportVO.StudentInfo> stus = section.getStudents();
                    if (stus != null && !stus.isEmpty()) {
                        totalStudentCount += stus.size();
                    }
                }
            } else {
                List<CourseInfoExportVO.StudentInfo> stus = vo.getStudents();
                if (stus != null && !stus.isEmpty()) {
                    totalStudentCount = stus.size();
                }
            }

            // 总行数计算
            int totalRows = 3 + 1 + tbRows;
            
            // 班级基本信息行数
            if (classSections != null && !classSections.isEmpty()) {
                totalRows += classSections.size();
            } else {
                totalRows += 1;
            }
            
            // 学生信息行数
            if (totalStudentCount > 0) {
                totalRows += 1 + totalStudentCount;
            }

            XWPFTable table = doc.createTable(totalRows, 4);
            setTableWidth100(table);
            setTableBorders(table);
            setTableGrid(table, 1800, 3000, 2200, 2200);

            int r = 0;

            // 1) 课程名称
            setCell(table, r, 0, "课程名称", true, true);
            setCell(table, r, 1, safe(vo.getCourseName()), false, false);
            setCell(table, r, 2, "", false, false);
            setCell(table, r, 3, "", false, false);
            mergeCellsHorizontally(table, r, 1, 3);
            table.getRow(r).setHeight(600);
            r++;

            // 2) 授课老师 + 联系方式
            setCell(table, r, 0, "授课老师", true, true);
            setCell(table, r, 1, safe(vo.getTeacherName()), false, false);
            setCell(table, r, 2, "联系方式", true, true);
            setCell(table, r, 3, safe(vo.getTeacherContact()), false, false);
            table.getRow(r).setHeight(600);
            r++;

            // 3) 课程简介（合并1~3列）
            setCell(table, r, 0, "课程简介", true, true);
            setCell(table, r, 1, safe(vo.getCourseIntro()), false, false);
            setCell(table, r, 2, "", false, false);
            setCell(table, r, 3, "", false, false);
            mergeCellsHorizontally(table, r, 1, 3);
            table.getRow(r).setHeight(1200);
            r++;

            // 4) 教材表头
            int tbStartRow = r;
            setCell(table, r, 0, "包含教材", true, true);
            setCell(table, r, 1, "教材名称", true, true);
            setCell(table, r, 2, "", true, true);
            setCell(table, r, 3, "作者", true, true);
            mergeCellsHorizontally(table, r, 1, 2);
            table.getRow(r).setHeight(600);
            r++;

            // 5) 教材行
            for (int i = 0; i < tbRows; i++) {
                CourseInfoExportVO.TextbookInfo item = i < tbs.size() ? tbs.get(i) : null;
                setCell(table, r, 0, "", false, false);
                setCell(table, r, 1, item == null ? "" : safe(item.getTextbookName()), false, false);
                setCell(table, r, 2, "", false, false);
                setCell(table, r, 3, item == null ? "" : safe(item.getAuthorName()), false, false);
                mergeCellsHorizontally(table, r, 1, 2);
                table.getRow(r).setHeight(600);
                r++;
            }
            mergeCellsVertically(table, 0, tbStartRow, r - 1);

            // 6) 班级列表
            if (classSections != null && !classSections.isEmpty()) {
                // 先列出所有班级名称和组织
                for (CourseInfoExportVO.ClassSection section : classSections) {
                    setCell(table, r, 0, "班级名称", true, true);
                    setCell(table, r, 1, safe(section.getClassName()), false, false);
                    setCell(table, r, 2, "组织", true, true);
                    setCell(table, r, 3, safe(section.getInstitutionName()), false, false);
                    table.getRow(r).setHeight(600);
                    r++;
                }
                
                // 按班级顺序列出学生信息
                if (totalStudentCount > 0) {
                    // 学生表头
                    setCell(table, r, 0, "班级", true, true);
                    setCell(table, r, 1, "姓名", true, true);
                    setCell(table, r, 2, "学号", true, true);
                    setCell(table, r, 3, "联系电话", true, true);
                    table.getRow(r).setHeight(600);
                    r++;

                    // 学生行
                    for (CourseInfoExportVO.ClassSection section : classSections) {
                        List<CourseInfoExportVO.StudentInfo> stus = section.getStudents();
                        if (stus != null && !stus.isEmpty()) {
                            for (CourseInfoExportVO.StudentInfo s : stus) {
                                setCell(table, r, 0, safe(section.getClassName()), false, false);
                                setCell(table, r, 1, safe(s.getStudentName()), false, false);
                                setCell(table, r, 2, safe(s.getStudentNo()), false, false);
                                setCell(table, r, 3, safe(s.getPhone()), false, false);
                                table.getRow(r).setHeight(600);
                                r++;
                            }
                        }
                    }
                }
            } else {
                // 兼容旧逻辑（单班级）
                setCell(table, r, 0, "班级名称", true, true);
                setCell(table, r, 1, safe(vo.getClassName()), false, false);
                setCell(table, r, 2, "组织", true, true);
                setCell(table, r, 3, safe(vo.getInstitutionName()), false, false);
                table.getRow(r).setHeight(600);
                r++;
                
                // 学生详细信息
                List<CourseInfoExportVO.StudentInfo> stus = vo.getStudents();
                if (stus != null && !stus.isEmpty()) {
                    // 学生表头
                    setCell(table, r, 0, "班级", true, true);
                    setCell(table, r, 1, "姓名", true, true);
                    setCell(table, r, 2, "学号", true, true);
                    setCell(table, r, 3, "联系电话", true, true);
                    table.getRow(r).setHeight(600);
                    r++;

                    // 学生行
                    for (CourseInfoExportVO.StudentInfo s : stus) {
                        setCell(table, r, 0, safe(vo.getClassName()), false, false);
                        setCell(table, r, 1, safe(s.getStudentName()), false, false);
                        setCell(table, r, 2, safe(s.getStudentNo()), false, false);
                        setCell(table, r, 3, safe(s.getPhone()), false, false);
                        table.getRow(r).setHeight(600);
                        r++;
                    }
                }
            }

            doc.write(bos);
            return bos.toByteArray();
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static void setCell(XWPFTable table, int row, int col, String text, boolean center) {
        setCell(table, row, col, text, center, false);
    }
    
    private static void setCell(XWPFTable table, int row, int col, String text, boolean center, boolean bold) {
        XWPFTableCell cell = table.getRow(row).getCell(col);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

        int pSize = cell.getParagraphs().size();
        for (int i = pSize - 1; i >= 0; i--) cell.removeParagraph(i);

        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(center ? ParagraphAlignment.CENTER : ParagraphAlignment.LEFT);

        XWPFRun run = p.createRun();
        run.setText(text == null ? "" : text);
        run.setFontFamily("宋体");
        run.setFontSize(14);
        if (bold) {
            run.setBold(true);
        }
    }

    private static CTTcPr tcPr(XWPFTableCell cell) {
        return cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
    }

    private static void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            CTTcPr pr = tcPr(cell);
            CTVMerge vMerge = pr.isSetVMerge() ? pr.getVMerge() : pr.addNewVMerge();
            vMerge.setVal(rowIndex == fromRow ? STMerge.RESTART : STMerge.CONTINUE);
        }
    }

    private static void mergeCellsHorizontally(XWPFTable table, int row, int fromCol, int toCol) {
        for (int colIndex = fromCol; colIndex <= toCol; colIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(colIndex);
            CTTcPr pr = tcPr(cell);
            CTHMerge hMerge = pr.isSetHMerge() ? pr.getHMerge() : pr.addNewHMerge();
            hMerge.setVal(colIndex == fromCol ? STMerge.RESTART : STMerge.CONTINUE);
        }
    }

    private static void setTableBorders(XWPFTable table) {
        table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
        table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
        table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
        table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
        table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
        table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
    }

    private static void setTableWidth100(XWPFTable table) {
        CTTblPr tblPr = table.getCTTbl().getTblPr() != null ? table.getCTTbl().getTblPr() : table.getCTTbl().addNewTblPr();
        CTTblWidth w = tblPr.getTblW() != null ? tblPr.getTblW() : tblPr.addNewTblW();
        w.setType(STTblWidth.PCT);
        w.setW(BigInteger.valueOf(5000));
    }

    private static void setTableGrid(XWPFTable table, int... colWidths) {
        CTTblGrid grid = table.getCTTbl().getTblGrid() != null ? table.getCTTbl().getTblGrid() : table.getCTTbl().addNewTblGrid();
        // 清空现有列定义
        while (grid.sizeOfGridColArray() > 0) {
            grid.removeGridCol(0);
        }
        // 添加新的列定义
        for (int w : colWidths) {
            grid.addNewGridCol().setW(BigInteger.valueOf(w));
        }
    }
}
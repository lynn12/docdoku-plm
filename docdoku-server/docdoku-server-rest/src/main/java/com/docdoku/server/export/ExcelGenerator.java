/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server.export;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.PathDataIteration;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceListOfValuesAttribute;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartLinkList;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.query.QueryContext;
import com.docdoku.core.query.QueryField;
import com.docdoku.core.query.QueryResultRow;
import com.docdoku.core.util.DateUtils;
import com.docdoku.core.util.Tools;
import com.docdoku.server.helpers.LangHelper;
import com.docdoku.server.rest.collections.QueryResult;
import com.docdoku.server.rest.dto.InstanceAttributeDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chadid Asmae
 */
public class ExcelGenerator {

    private static final Logger LOGGER = Logger.getLogger(ExcelGenerator.class.getName());

    private Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
    public File generateXLSResponse(QueryResult queryResult, Locale locale, String baseURL) {
        File excelFile = new File("export_parts.xls");
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Parts Data");

        String header = String.join(";", queryResult.getQuery().getSelects());
        String[] columns = header.split(";");

        Map<Integer, String[]> data = new HashMap<>();
        String[] headerFormatted = createXLSHeaderRow(header, columns, locale);
        data.put(1, headerFormatted);

        Map<Integer, String[]> commentsData = new HashMap<>();
        String[] headerComments = createXLSHeaderRowComments(header, columns);
        commentsData.put(1, headerComments);

        List<String> selects = queryResult.getQuery().getSelects();
        int i = 1;
        for (QueryResultRow row : queryResult.getRows()) {
            i++;
            data.put(i, createXLSRow(selects, row, baseURL));
            commentsData.put(i, createXLSRowComments(selects, row));
        }

        //Iterate over data and write to sheet
        Set<Integer> keySet = data.keySet();
        int rowNum = 0;

        for (Integer key : keySet) {

            Row row = sheet.createRow(rowNum++);
            String[] objArr = data.get(key);
            int cellNum = 0;
            for (String obj : objArr) {
                Cell cell = row.createCell(cellNum++);
                cell.setCellValue(obj);
            }

            CreationHelper factory = workbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();
            String[] commentsObjArr = commentsData.get(key);
            cellNum = 0;
            for (String commentsObj : commentsObjArr) {
                if (commentsObj.length() > 0) {
                    Cell cell = row.getCell(cellNum) != null ? row.getCell(cellNum) : row.createCell(cellNum);

                    // When the comment box is visible, have it show in a 1x3 space
                    ClientAnchor anchor = factory.createClientAnchor();
                    anchor.setCol1(cell.getColumnIndex());
                    anchor.setCol2(cell.getColumnIndex()+1);
                    anchor.setRow1(row.getRowNum());
                    anchor.setRow2(row.getRowNum()+1);

                    Comment comment = drawing.createCellComment(anchor);
                    RichTextString str = factory.createRichTextString(commentsObj);
                    comment.setString(str);

                    // Assign the comment to the cell
                    cell.setCellComment(comment);
                }
                cellNum++;
            }
        }

        // Define header style
        Font headerFont = workbook.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setFontName("Courier New");
        headerFont.setItalic(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        // Set header style
        for (int j=0; j<columns.length; j++) {
            Cell cell = sheet.getRow(0).getCell(j);
            cell.setCellStyle(headerStyle);

            if (cell.getCellComment() != null) {
                String comment = cell.getCellComment().getString().toString();

                if (comment.equals(QueryField.CTX_PRODUCT_ID) || comment.equals(QueryField.CTX_SERIAL_NUMBER) || comment.equals(QueryField.PART_MASTER_NUMBER)) {
                    for (int k=0; k<queryResult.getRows().size(); k++) {
                        Cell grayCell = sheet.getRow(k+1).getCell(j) != null ? sheet.getRow(k+1).getCell(j) : sheet.getRow(k+1).createCell(j);
                        grayCell.setCellStyle(headerStyle);
                    }
                }
            }
        }

        try {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(excelFile);
            workbook.write(out);
            out.close();
        } catch (Exception e) {
            LOGGER.log(Level.FINEST, null, e);
        }
        return excelFile;

    }

    private String[] createXLSHeaderRow(String header, String[] columns, Locale locale) {
        String[] headerFormatted = new String[header.split(";").length];
        int headerIndex = 0;

        for (String column : columns) {
            String columnTranslated;
            if (!column.isEmpty()) {
                if (column.trim().startsWith(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)) {
                    columnTranslated = column.substring(column.indexOf(".") + 1);
                }
                else if (column.trim().startsWith(QueryField.PATH_DATA_ATTRIBUTES_PREFIX)) {
                    columnTranslated = column.substring(column.indexOf(".") + 1);
                }
                else {
                    columnTranslated = LangHelper.getLocalizedMessage(column.trim(), locale);
                }
                headerFormatted[headerIndex++] = columnTranslated != null ? columnTranslated : column;
            }
        }

        return headerFormatted;
    }

    private String[] createXLSHeaderRowComments(String header, String[] columns) {
        String[] headerComments = new String[header.split(";").length];
        int headerIndex = 0;

        for (String column : columns) {
            if (column.equals(QueryField.CTX_PRODUCT_ID) || column.equals(QueryField.CTX_SERIAL_NUMBER) || column.equals(QueryField.PART_MASTER_NUMBER)) {
                headerComments[headerIndex++] = column;
            } else if (column.startsWith(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)) {
                headerComments[headerIndex++] = column.substring(0, column.indexOf(".")).substring(QueryField.PART_REVISION_ATTRIBUTES_PREFIX.length());
            } else if (column.startsWith(QueryField.PATH_DATA_ATTRIBUTES_PREFIX)) {
                headerComments[headerIndex++] = column.substring(0, column.indexOf(".")).substring(QueryField.PATH_DATA_ATTRIBUTES_PREFIX.length());
            } else {
                headerComments[headerIndex++] = "";
            }
        }

        return headerComments;
    }

    private String[] createXLSRow(List<String> selects, QueryResultRow row, String baseURL) {
        List<String> data = new ArrayList<>();
        PartRevision part = row.getPartRevision();
        PartIteration lastCheckedInIteration = part.getLastCheckedInIteration();
        PartIteration lastIteration = part.getLastIteration();
        QueryContext context = row.getContext();

        for (String select : selects) {

            switch (select) {
                case QueryField.CTX_PRODUCT_ID:
                    String productId = context != null ? context.getConfigurationItemId() : "";
                    data.add(productId);
                    break;
                case QueryField.CTX_SERIAL_NUMBER:
                    String serialNumber = context != null ? context.getSerialNumber() : "";
                    data.add(serialNumber != null ? serialNumber : "");
                    break;
                case QueryField.PART_MASTER_NUMBER:
                    data.add(part.getPartNumber());
                    break;
                case QueryField.PART_MASTER_NAME:
                    String sName = part.getPartName();
                    data.add(sName != null ? sName : "");
                    break;
                case QueryField.PART_MASTER_TYPE:
                    String sType = part.getType();
                    data.add(sType != null ? sType : "");
                    break;
                case QueryField.PART_REVISION_MODIFICATION_DATE:
                    data.add((lastIteration != null && lastIteration.getModificationDate() != null) ?
                            DateUtils.format(lastIteration.getModificationDate()) : "");
                    break;
                case QueryField.PART_REVISION_CREATION_DATE:
                    data.add((part.getCreationDate() != null) ?
                            DateUtils.format(part.getCreationDate()) : "");
                    break;
                case QueryField.PART_REVISION_CHECKOUT_DATE:
                    data.add((part.getCheckOutDate() != null) ?
                            DateUtils.format(part.getCheckOutDate()) : "");
                    break;
                case QueryField.PART_REVISION_CHECKIN_DATE:
                    data.add((lastCheckedInIteration != null && lastCheckedInIteration.getCheckInDate() != null)
                            ? DateUtils.format(lastCheckedInIteration.getCheckInDate()) : "");
                    break;
                case QueryField.PART_REVISION_VERSION:
                    data.add(part.getVersion() != null ? part.getVersion() : "");
                    break;
                case QueryField.PART_REVISION_LIFECYCLE_STATE:
                    data.add(part.getLifeCycleState() != null ? part.getLifeCycleState() : "");
                    break;
                case QueryField.PART_REVISION_STATUS:
                    data.add(part.getStatus().toString());
                    break;
                case QueryField.AUTHOR_LOGIN:
                    User user = part.getAuthor();
                    data.add(user.getLogin());
                    break;
                case QueryField.AUTHOR_NAME:
                    User userAuthor = part.getAuthor();
                    data.add(userAuthor.getName());
                    break;
                case QueryField.CTX_DEPTH:
                    data.add(row.getDepth() + "");
                    break;
                case QueryField.CTX_AMOUNT:
                    data.add(row.getAmount() + "");
                    break;
                case QueryField.PART_ITERATION_LINKED_DOCUMENTS:
                    StringBuilder sb = new StringBuilder();
                    if (lastCheckedInIteration != null) {
                        Set<DocumentLink> linkedDocuments = lastCheckedInIteration.getLinkedDocuments();
                        for (DocumentLink documentLink : linkedDocuments) {
                            DocumentRevision targetDocument = documentLink.getTargetDocument();
                            sb.append(baseURL).append("/documents/").append(targetDocument.getWorkspaceId()).append("/").append(targetDocument.getId()).append("/").append(targetDocument.getVersion()).append(" ");
                        }
                    }
                    data.add(sb.toString());
                    break;

                case QueryField.CTX_P2P_SOURCE:
                    Map<String, List<PartLinkList>> sources = row.getSources();
                    String sourcePartLinksAsString = Tools.getPartLinksAsExcelString(sources);
                    data.add(sourcePartLinksAsString);
                    break;

                case QueryField.CTX_P2P_TARGET:
                    Map<String, List<PartLinkList>> targets = row.getTargets();
                    String targetPartLinksAsString = Tools.getPartLinksAsExcelString(targets);
                    data.add(targetPartLinksAsString);
                    break;

                default:
                    if (select.startsWith(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)) {
                        String attributeSelectType = select.substring(0, select.indexOf(".")).substring(QueryField.PART_REVISION_ATTRIBUTES_PREFIX.length());
                        String attributeSelectName = select.substring(select.indexOf(".") + 1);
                        String attributeValue;
                        StringBuilder stringBuilder = new StringBuilder();

                        if (lastIteration != null) {
                            List<InstanceAttribute> attributes = lastIteration.getInstanceAttributes();
                            if (attributes != null) {
                                for (InstanceAttribute attribute : attributes) {
                                    InstanceAttributeDTO attrDTO = mapper.map(attribute, InstanceAttributeDTO.class);
                                    mapper.map(attribute, InstanceAttributeDTO.class);

                                    if (attrDTO.getName().equals(attributeSelectName)
                                            && attrDTO.getType().name().equals(attributeSelectType)) {

                                        attributeValue = attribute.getValue() + "";
                                        if (attrDTO.getType() == InstanceAttributeDTO.Type.DATE) {
                                            attributeValue = attribute.getValue() != null ?
                                                    DateUtils.format((Date) attribute.getValue()) : "";
                                        } else if (attribute instanceof InstanceListOfValuesAttribute) {
                                            attributeValue = ((InstanceListOfValuesAttribute) attribute).getSelectedName();
                                        }
                                        stringBuilder.append(attributeValue).append("|");
                                    }
                                }
                            }
                        }
                        String content = stringBuilder.toString().trim();
                        if (content.length() > 0) {
                            content = content.substring(0, content.lastIndexOf("|"));
                        }
                        data.add(content);
                    }
                    if (select.startsWith(QueryField.PATH_DATA_ATTRIBUTES_PREFIX)) {
                        String attributeSelectType = select.substring(0, select.indexOf(".")).substring(QueryField.PATH_DATA_ATTRIBUTES_PREFIX.length());
                        String attributeSelectName = select.substring(select.indexOf(".") + 1);
                        String attributeValue;
                        PathDataIteration pdi = row.getPathDataIteration();
                        StringBuilder stringBuilder = new StringBuilder();

                        if (pdi != null) {
                            List<InstanceAttribute> attributes = pdi.getInstanceAttributes();
                            if (attributes != null) {
                                for (InstanceAttribute attribute : attributes) {
                                    InstanceAttributeDTO attrDTO = mapper.map(attribute, InstanceAttributeDTO.class);
                                    if (attrDTO.getName().equals(attributeSelectName)
                                            && attrDTO.getType().name().equals(attributeSelectType)) {

                                        attributeValue = attribute.getValue() + "";
                                        if (attrDTO.getType() == InstanceAttributeDTO.Type.DATE) {
                                            attributeValue = attribute.getValue() != null ?
                                                    DateUtils.format((Date) attribute.getValue()) : "";
                                        } else if (attribute instanceof InstanceListOfValuesAttribute) {
                                            attributeValue = ((InstanceListOfValuesAttribute) attribute).getSelectedName();
                                        }
                                        stringBuilder.append(attributeValue).append("|");
                                    }
                                }
                            }
                        }
                        String content = stringBuilder.toString().trim();
                        if (content.length() > 0) {
                            content = content.substring(0, content.lastIndexOf("|"));
                        }
                        data.add(content);
                    }
            }

        }

        String rowData = String.join(";", data);
        return rowData.split(";");
    }

    private String[] createXLSRowComments(List<String> selects, QueryResultRow row) {
        List<String> commentsData = new ArrayList<>();
        PartRevision part = row.getPartRevision();
        PartIteration lastIteration = part.getLastIteration();

        for (String select : selects) {

            if (select.equals(QueryField.CTX_SERIAL_NUMBER)) {
                String path = row.getPath();
                if (path != null && !path.isEmpty()) {
                    commentsData.add(path);
                }

            } else if (select.startsWith(QueryField.PART_REVISION_ATTRIBUTES_PREFIX)) {
                String attributeSelectType = select.substring(0, select.indexOf(".")).substring(QueryField.PART_REVISION_ATTRIBUTES_PREFIX.length());
                String attributeSelectName = select.substring(select.indexOf(".") + 1);
                StringBuilder stringBuilder = new StringBuilder();

                if (lastIteration != null) {
                    List<InstanceAttribute> attributes = lastIteration.getInstanceAttributes();
                    if (attributes != null) {
                        for (InstanceAttribute attribute : attributes) {
                            InstanceAttributeDTO attrDTO = mapper.map(attribute, InstanceAttributeDTO.class);

                            if (attrDTO.getName().equals(attributeSelectName)
                                    && attrDTO.getType().name().equals(attributeSelectType)) {
                                stringBuilder.append(attribute.getId()).append("|");
                            }
                        }
                    }
                }

                String commentsContent = stringBuilder.toString().trim();
                if (commentsContent.length() > 0) {
                    commentsContent = commentsContent.substring(0, commentsContent.lastIndexOf("|"));
                }
                commentsData.add(commentsContent);

            } else if (select.startsWith(QueryField.PATH_DATA_ATTRIBUTES_PREFIX)) {
                String attributeSelectType = select.substring(0, select.indexOf(".")).substring(QueryField.PATH_DATA_ATTRIBUTES_PREFIX.length());
                String attributeSelectName = select.substring(select.indexOf(".") + 1);
                PathDataIteration pdi = row.getPathDataIteration();
                StringBuilder stringBuilder = new StringBuilder();

                if (pdi != null) {
                    List<InstanceAttribute> attributes = pdi.getInstanceAttributes();
                    if (attributes != null) {
                        for (InstanceAttribute attribute : attributes) {
                            InstanceAttributeDTO attrDTO = mapper.map(attribute, InstanceAttributeDTO.class);

                            if (attrDTO.getName().equals(attributeSelectName)
                                    && attrDTO.getType().name().equals(attributeSelectType)) {
                                stringBuilder.append(attribute.getId()).append("|");
                            }
                        }
                    }
                }

                String commentsContent = stringBuilder.toString().trim();
                if (commentsContent.length() > 0) {
                    commentsContent = commentsContent.substring(0, commentsContent.lastIndexOf("|"));
                }
                commentsData.add(commentsContent);

            } else {
                commentsData.add("");
            }

        }

        String commentsRowData = String.join(";", commentsData);
        return commentsRowData.split(";");
    }

}
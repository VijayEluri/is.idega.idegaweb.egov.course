/*
 * $Id$ Created on Mar 28, 2007
 * 
 * Copyright (C) 2007 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.course.business;

import is.idega.idegaweb.egov.course.CourseConstants;
import is.idega.idegaweb.egov.course.data.Course;
import is.idega.idegaweb.egov.course.data.CoursePrice;
import is.idega.idegaweb.egov.course.data.CourseProviderType;
import is.idega.idegaweb.egov.course.data.CourseType;
import is.idega.idegaweb.egov.course.presentation.CourseBlock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryInputStream;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;

public class CourseWriter extends DownloadWriter implements MediaWritable {

	private MemoryFileBuffer buffer = null;
	private CourseBusiness business = null;
	private Locale locale;
	private IWResourceBundle iwrb;

	private String schoolTypeName;

	public final static String PARAMETER_PROVIDER_ID = "provider_id";
	public final static String PARAMETER_GROUP_ID = "group_id";
	public final static String PARAMETER_SHOW_NOT_YET_ACTIVE = "show_not_yet_active";
	
	public CourseWriter() {}

	protected CourseBusiness getCourseBusiness() {
		if (this.business == null) {
			try {
				this.business = IBOLookup.getServiceInstance(
						CoreUtil.getIWContext(), CourseBusiness.class);
			} catch (IBOLookupException e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, 
						"Failed to get " + CourseBusiness.class.getSimpleName() + 
						" cause of: ", e);
			}
		}

		return this.business;
	}

	/**
	 * 
	 * @param unparsedDate date to parse, not <code>null</code>;
	 * @return parsed {@link Date};
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	protected Date getParsedDate(String unparsedDate) {
		if (StringUtil.isEmpty(unparsedDate)) {
			return null;
		}

		if (CoreUtil.getCurrentLocale().equals(Locale.ENGLISH)) {
			DateFormat dateFormat = new SimpleDateFormat("mm/dd/yy");
			
			java.util.Date date = null;
			try {
				date = dateFormat.parse(unparsedDate);
			} catch (ParseException e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, 
						"Failed to parse " + unparsedDate);
			}

			if (date != null) {
				return new Date(date.getTime());
			}
		}

		return new IWTimestamp(unparsedDate).getDate();
	}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		try {
			this.locale = iwc.getApplicationSettings().getApplicationLocale();
			this.business = getCourseBusiness(iwc);
			this.iwrb = iwc.getIWMainApplication().getBundle(CourseConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(this.locale);

			if (getCourseSession(iwc).getProvider() != null && iwc.isParameterSet(CourseBlock.PARAMETER_SCHOOL_TYPE_PK)) {
				CourseProviderType type = getSchoolBusiness(iwc).getSchoolType(iwc.getParameter(CourseBlock.PARAMETER_SCHOOL_TYPE_PK));
				schoolTypeName = type.getSchoolTypeName();

				Date fromDate = getParsedDate(
						iwc.getParameter(CourseBlock.PARAMETER_FROM_DATE));
				Date toDate = getParsedDate(
						iwc.getParameter(CourseBlock.PARAMETER_TO_DATE));

				Collection<Course> courses = getCourseBusiness().getCourses(
						-1, 
						getCourseSession(iwc).getProvider().getPrimaryKey().toString(), 
						iwc.getParameter(CourseBlock.PARAMETER_SCHOOL_TYPE_PK), 
						iwc.getParameter(CourseBlock.PARAMETER_COURSE_TYPE_PK), 
						fromDate, 
						toDate);

				this.buffer = writeXLS(iwc, courses);
				setAsDownload(iwc, "students.xls", this.buffer.length());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getMimeType() {
		if (this.buffer != null) {
			return this.buffer.getMimeType();
		}
		return super.getMimeType();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		if (this.buffer != null) {
			MemoryInputStream mis = new MemoryInputStream(this.buffer);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (mis.available() > 0) {
				baos.write(mis.read());
			}
			baos.writeTo(out);
			mis.close();
		}
		else {
			System.err.println("buffer is null");
		}
	}

	public MemoryFileBuffer writeXLS(IWContext iwc, Collection<Course> courses) throws Exception {
		MemoryFileBuffer buffer = new MemoryFileBuffer();
		MemoryOutputStream mos = new MemoryOutputStream(buffer);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(StringHandler.shortenToLength(this.schoolTypeName, 30));

		/* Font style */
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontHeightInPoints((short) 12);
		HSSFCellStyle style = wb.createCellStyle();
		style.setFont(font);

		/* Header style */
		HSSFFont bigFont = wb.createFont();
		bigFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		bigFont.setFontHeightInPoints((short) 13);
		HSSFCellStyle bigStyle = wb.createCellStyle();
		bigStyle.setFont(bigFont);

		/* Setting header */
		int cellRow = 0;
		HSSFRow row = sheet.createRow(cellRow++);
		HSSFCell cell = row.createCell(0);
		cell.setCellValue(this.schoolTypeName);
		cell.setCellStyle(bigStyle);
		cell = row.createCell(1);

		row = sheet.createRow(cellRow++);

		int iCell = 0;
		row = sheet.createRow(cellRow++);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("type", "Type"));
		cell.setCellStyle(style);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("course", "Course"));
		cell.setCellStyle(style);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("from", "From"));
		cell.setCellStyle(style);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("to", "To"));
		cell.setCellStyle(style);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("date_from", "Date from"));
		cell.setCellStyle(style);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("date_to", "Date to"));
		cell.setCellStyle(style);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("max", "Max"));
		cell.setCellStyle(style);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("free_places", "Free places"));
		cell.setCellStyle(style);
		cell = row.createCell(iCell++);
		cell.setCellValue(this.iwrb.getLocalizedString("employee", "Employee"));
		cell.setCellStyle(style);

		/* Setting data if exist */
		if (!ListUtil.isEmpty(courses)) {
			for (Course course : courses) {
				row = sheet.createRow(cellRow++);
				iCell = 0;

				CourseType type = course.getCourseType();
				IWTimestamp dateFrom = new IWTimestamp(course.getStartDate());
				IWTimestamp dateTo = new IWTimestamp(course.getStartDate());
				
				CoursePrice price = course.getPrice();
				if (price != null) {
					dateTo.addDays(price.getNumberOfDays());
				}

				row.createCell(iCell++).setCellValue(type.getName());
				row.createCell(iCell++).setCellValue(course.getName());

				/* Birthdays */
				HSSFCell bFromCell = row.createCell(iCell++);
				int birthyearFrom = course.getBirthyearFrom();
				if (birthyearFrom > 0) {
					bFromCell.setCellValue(String.valueOf(birthyearFrom));
				} else {
					bFromCell.setCellValue(CoreConstants.MINUS);
				}

				HSSFCell bToCell = row.createCell(iCell++);
				int birthyearTo = course.getBirthyearTo();
				if (birthyearTo > 0) {
					bToCell.setCellValue(String.valueOf(birthyearTo));
				} else {
					bToCell.setCellValue(CoreConstants.MINUS);
				}

				row.createCell(iCell++).setCellValue(dateFrom.getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT));
				row.createCell(iCell++).setCellValue(dateTo.getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT));
				row.createCell(iCell++).setCellValue(String.valueOf(course.getMax()));
				row.createCell(iCell++).setCellValue(String.valueOf(business.getNumberOfFreePlaces(course)));

				/* Employee */
				HSSFCell employeeCell = row.createCell(iCell++);
				String courseEmployee = course.getUser();
				if (!StringUtil.isEmpty(courseEmployee)) {
					employeeCell.setCellValue(courseEmployee);
				} else {
					employeeCell.setCellValue(CoreConstants.MINUS);
				}
			}
		}

		/* Formatting size */
		Iterator<Cell> cellIterator = row.cellIterator();
		while (cellIterator.hasNext()) {
			sheet.autoSizeColumn(cellIterator.next().getColumnIndex());
		}

		wb.write(mos);
		buffer.setMimeType(MimeTypeUtil.MIME_TYPE_EXCEL_2);
		return buffer;
	}

	private CourseBusiness getCourseBusiness(IWApplicationContext iwc) throws RemoteException {
		return (CourseBusiness) IBOLookup.getServiceInstance(iwc, CourseBusiness.class);
	}

	private CourseSession getCourseSession(IWUserContext iwc) throws RemoteException {
		return (CourseSession) IBOLookup.getSessionInstance(iwc, CourseSession.class);
	}

	private CourseProviderBusiness getSchoolBusiness(IWApplicationContext iwc) throws RemoteException {
		return (CourseProviderBusiness) IBOLookup.getServiceInstance(iwc, CourseProviderBusiness.class);
	}
}
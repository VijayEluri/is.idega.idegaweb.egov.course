/*
 * $Id$ Created on Mar 27, 2007
 *
 * Copyright (C) 2007 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.course.presentation;

import is.idega.idegaweb.egov.course.CourseConstants;
import is.idega.idegaweb.egov.course.business.CourseComparator;
import is.idega.idegaweb.egov.course.business.CourseWriter;
import is.idega.idegaweb.egov.course.data.Course;
import is.idega.idegaweb.egov.course.data.CourseCategory;
import is.idega.idegaweb.egov.course.data.CoursePrice;
import is.idega.idegaweb.egov.course.data.CourseType;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolType;
import com.idega.business.IBORuntimeException;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IWDatePicker;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.handlers.IWDatePickerHandler;
import com.idega.util.IWTimestamp;
import com.idega.util.PresentationUtil;

public class CourseList extends CourseBlock {

	protected static final String PARAMETER_SORTING = "prm_sorting";

	private SchoolType type = null;
	private boolean showTypes = true;

	@Override
	public void present(IWContext iwc) {
		try {
			Form form = new Form();
			form.setID("courseList");
			form.setStyleClass("adminForm");
			form.setEventListener(this.getClass());

			form.add(getNavigation(iwc));
			if (iwc.isParameterSet(PARAMETER_COURSE_TYPE_PK)) {
				form.add(getPrintouts(iwc));
			}
			form.add(getCourses(iwc));

			if (getBackPage() != null) {
				Layer buttonLayer = new Layer();
				buttonLayer.setStyleClass("buttonLayer");
				form.add(buttonLayer);

				GenericButton back = new GenericButton(localize("back", "Back"));
				back.setPageToOpen(getBackPage());
				buttonLayer.add(back);
			}

			add(form);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	protected Layer getNavigation(IWContext iwc) throws RemoteException {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("formSection");

		List scripts = new ArrayList();
		scripts.add("/dwr/interface/CourseDWRUtil.js");
		scripts.add("/dwr/engine.js");
		scripts.add("/dwr/util.js");
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);

		StringBuffer script2 = new StringBuffer();
		script2.append("function setOptions(data) {\n").append("\tdwr.util.removeAllOptions(\"" + PARAMETER_COURSE_TYPE_PK + "\");\n").append("\tdwr.util.addOptions(\"" + PARAMETER_COURSE_TYPE_PK + "\", data);\n").append("}");
		StringBuffer script = new StringBuffer();
		script.append("function changeValues() {\n").append("\tvar val = +$(\"" + PARAMETER_SCHOOL_TYPE_PK + "\").value;\n").append("\tvar TEST = CourseDWRUtil.getCourseTypesDWR(val, '" + iwc.getCurrentLocale().getCountry() + "', setOptions);\n").append("}");
		PresentationUtil.addJavaScriptActionToBody(iwc, script2.toString());
		PresentationUtil.addJavaScriptActionToBody(iwc, script.toString());

		if (!isSchoolUser()) {
			DropdownMenu providers = null;
			if (iwc.getAccessController().hasRole(CourseConstants.SUPER_ADMINISTRATOR_ROLE_KEY, iwc)) {
				providers = getAllProvidersDropdown(iwc);
			}
			else if (iwc.getAccessController().hasRole(CourseConstants.ADMINISTRATOR_ROLE_KEY, iwc)) {
				providers = getProvidersDropdown(iwc);
			}

			Collection providersList = getBusiness().getProviders();
			if (providersList.size() == 1) {
				School school = (School) providersList.iterator().next();
				getSession().setProvider(school);
				layer.add(new HiddenInput(PARAMETER_PROVIDER_PK, school.getPrimaryKey().toString()));
			}
			else if (providers != null) {
				providers.setToSubmit();

				Layer formItem = new Layer(Layer.DIV);
				formItem.setStyleClass("formItem");
				Label label = new Label(getResourceBundle().getLocalizedString("provider", "Provider"), providers);
				formItem.add(label);
				formItem.add(providers);
				layer.add(formItem);
			}
		}

		DropdownMenu schoolType = new DropdownMenu(PARAMETER_SCHOOL_TYPE_PK);
		schoolType.setId(PARAMETER_SCHOOL_TYPE_PK);
		schoolType.setOnChange("changeValues();");
		schoolType.addMenuElementFirst("", getResourceBundle().getLocalizedString("select_school_type", "Select school type"));
		schoolType.keepStatusOnAction(true);

		if (getSession().getProvider() != null) {
			Collection schoolTypes = getBusiness().getSchoolTypes(getSession().getProvider());
			if (schoolTypes.size() == 1) {
				showTypes = false;
				type = (SchoolType) schoolTypes.iterator().next();
				schoolType.setSelectedElement(type.getPrimaryKey().toString());
			}
			schoolType.addMenuElements(schoolTypes);
		}
		else {
			Collection schoolTypes = getBusiness().getAllSchoolTypes();
			if (schoolTypes.size() == 1) {
				showTypes = false;
				type = (SchoolType) schoolTypes.iterator().next();
				schoolType.setSelectedElement(type.getPrimaryKey().toString());
			}
			schoolType.addMenuElements(schoolTypes);
		}

		DropdownMenu courseType = new DropdownMenu(PARAMETER_COURSE_TYPE_PK);
		courseType.setId(PARAMETER_COURSE_TYPE_PK);
		courseType.addMenuElementFirst("", getResourceBundle().getLocalizedString("select_course_type", "Select course type"));
		courseType.keepStatusOnAction(true);

		if (iwc.isParameterSet(PARAMETER_SCHOOL_TYPE_PK)) {
			Collection courseTypes = getBusiness().getCourseTypes(new Integer(iwc.getParameter(PARAMETER_SCHOOL_TYPE_PK)), true);
			courseType.addMenuElements(courseTypes);
		}
		else if (type != null) {
			Collection courseTypes = getBusiness().getCourseTypes(new Integer(type.getPrimaryKey().toString()), true);
			courseType.addMenuElements(courseTypes);
		}

		boolean useBirthYears = iwc.getApplicationSettings().getBoolean(CourseConstants.PROPERTY_USE_BIRTHYEARS, true);
		int inceptionYear = Integer.parseInt(iwc.getApplicationSettings().getProperty(CourseConstants.PROPERTY_INCEPTION_YEAR, "-1"));

		DropdownMenu sorting = new DropdownMenu(PARAMETER_SORTING);
		sorting.addMenuElement(CourseComparator.ID_SORT, getResourceBundle().getLocalizedString("sort.id", "ID"));
		sorting.addMenuElement(CourseComparator.REVERSE_ID_SORT, getResourceBundle().getLocalizedString("sort.reverse_id", "reverse ID"));
		sorting.addMenuElement(CourseComparator.NAME_SORT, getResourceBundle().getLocalizedString("sort.name", "Name (A-Z)"));
		sorting.addMenuElement(CourseComparator.REVERSE_NAME_SORT, getResourceBundle().getLocalizedString("sort.reverse_name", "Name (Z-A)"));
		sorting.addMenuElement(CourseComparator.TYPE_SORT, getResourceBundle().getLocalizedString("sort.type", "Type"));
		sorting.addMenuElement(CourseComparator.DATE_SORT, getResourceBundle().getLocalizedString("sort.date", "Date"));
		if (useBirthYears) {
			sorting.addMenuElement(CourseComparator.YEAR_SORT, getResourceBundle().getLocalizedString("sort.year", "Year"));
		}
		sorting.addMenuElement(CourseComparator.PLACES_SORT, getResourceBundle().getLocalizedString("sort.places", "Places"));
		sorting.addMenuElement(CourseComparator.FREE_PLACES_SORT, getResourceBundle().getLocalizedString("sort.free_places", "Free places"));
		sorting.keepStatusOnAction(true);

		IWTimestamp stamp = new IWTimestamp();
		stamp.addYears(1);

		IWDatePicker fromDate = new IWDatePicker(PARAMETER_FROM_DATE);
		fromDate.setShowYearChange(true);
		fromDate.setStyleClass("dateInput");
		fromDate.keepStatusOnAction(true);

		IWDatePicker toDate = new IWDatePicker(PARAMETER_TO_DATE);
		toDate.setShowYearChange(true);
		toDate.setStyleClass("dateInput");
		toDate.keepStatusOnAction(true);
		toDate.setDate(stamp.getDate());

		if (inceptionYear > 0) {
			IWTimestamp fromStamp = new IWTimestamp(1, 1, inceptionYear);
			fromDate.setDate(fromStamp.getDate());
		}
		else {
			stamp.addMonths(-1);
			stamp.addYears(-1);
			fromDate.setDate(stamp.getDate());
		}

		if (showTypes) {
			Layer formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			Label label = new Label(getResourceBundle().getLocalizedString("category", "Category"), schoolType);
			formItem.add(label);
			formItem.add(schoolType);
			layer.add(formItem);
		}
		else if (type != null) {
			layer.add(new HiddenInput(PARAMETER_SCHOOL_TYPE_PK, type.getPrimaryKey().toString()));
		}

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label(getResourceBundle().getLocalizedString("type", "Type"), courseType);
		formItem.add(label);
		formItem.add(courseType);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("from", "From"));
		formItem.add(label);
		formItem.add(fromDate);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.setLabel(getResourceBundle().getLocalizedString("to", "To"));
		formItem.add(label);
		formItem.add(toDate);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(getResourceBundle().getLocalizedString("sorting", "Sorting"), sorting);
		formItem.add(label);
		formItem.add(sorting);
		layer.add(formItem);

		SubmitButton fetch = new SubmitButton(getResourceBundle().getLocalizedString("get", "Get"));
		fetch.setStyleClass("indentedButton");
		fetch.setStyleClass("button");
		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.add(fetch);
		layer.add(formItem);

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		layer.add(clearLayer);

		return layer;
	}

	public Layer getPrintouts(IWContext iwc) {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("printIcons");

		layer.add(getXLSLink(iwc));

		return layer;
	}

	protected Link getXLSLink(IWContext iwc) {
		DownloadLink link = new DownloadLink(getBundle().getImage("xls.gif"));
		link.setStyleClass("xls");
		link.setTarget(Link.TARGET_NEW_WINDOW);
		link.maintainParameter(PARAMETER_SCHOOL_TYPE_PK, iwc);
		link.maintainParameter(PARAMETER_COURSE_TYPE_PK, iwc);
		link.maintainParameter(PARAMETER_FROM_DATE, iwc);
		link.maintainParameter(PARAMETER_TO_DATE, iwc);
		link.setMediaWriterClass(CourseWriter.class);

		return link;
	}

	protected Table2 getCourses(IWContext iwc) throws RemoteException {
		boolean useBirthYears = iwc.getApplicationSettings().getBoolean(CourseConstants.PROPERTY_USE_BIRTHYEARS, true);

		Table2 table = new Table2();
		table.setStyleClass("adminTable");
		table.setStyleClass("ruler");
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);

		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();

		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.setStyleClass("number");
		cell.add(new Text(getResourceBundle().getLocalizedString("number_abbreviated", "No.")));

		cell = row.createHeaderCell();
		cell.setStyleClass("course");
		cell.add(new Text(getResourceBundle().getLocalizedString("course", "Course")));

		if (isSchoolSuperAdministrator(iwc)) {
			cell = row.createHeaderCell();
			cell.setStyleClass("provider");
			cell.add(new Text(getResourceBundle().getLocalizedString("provider", "Provider")));
		}

		cell = row.createHeaderCell();
		cell.setStyleClass("type");
		cell.add(new Text(getResourceBundle().getLocalizedString("type", "Type")));

		if (useBirthYears) {
			cell = row.createHeaderCell();
			cell.setStyleClass("from");
			cell.add(new Text(getResourceBundle().getLocalizedString("from", "From")));

			cell = row.createHeaderCell();
			cell.setStyleClass("to");
			cell.add(new Text(getResourceBundle().getLocalizedString("to", "To")));
		}

		cell = row.createHeaderCell();
		cell.setStyleClass("dateFrom");
		cell.add(new Text(getResourceBundle().getLocalizedString("date_from", "Date from")));

		cell = row.createHeaderCell();
		cell.setStyleClass("dateTo");
		cell.add(new Text(getResourceBundle().getLocalizedString("date_to", "Date to")));

		cell = row.createHeaderCell();
		cell.setStyleClass("max");
		cell.add(new Text(getResourceBundle().getLocalizedString("max", "Max")));

		cell = row.createHeaderCell();
		cell.setStyleClass("freePlaces");
		cell.add(new Text(getResourceBundle().getLocalizedString("free_places", "Free places")));

		cell = row.createHeaderCell();
		cell.setStyleClass("lastColumn");
		cell.setStyleClass("employee");
		cell.add(new Text(getResourceBundle().getLocalizedString("employee", "Employee")));

		group = table.createBodyRowGroup();
		int iRow = 1;

		Integer schoolTypePK = null;
		if (iwc.isParameterSet(PARAMETER_SCHOOL_TYPE_PK)) {
			schoolTypePK = new Integer(iwc.getParameter(PARAMETER_SCHOOL_TYPE_PK));
			if (schoolTypePK.intValue() < 0) {
				schoolTypePK = null;
			}
		}
		else if (type != null) {
			schoolTypePK = new Integer(type.getPrimaryKey().toString());
		}

		Integer courseTypePK = null;
		if (iwc.isParameterSet(PARAMETER_COURSE_TYPE_PK)) {
			courseTypePK = new Integer(iwc.getParameter(PARAMETER_COURSE_TYPE_PK));
			if (courseTypePK.intValue() < 0) {
				courseTypePK = null;
			}
		}

		IWTimestamp stamp = new IWTimestamp();
		stamp.addMonths(-1);

		Date fromDate = stamp.getDate();
		if (iwc.isParameterSet(PARAMETER_FROM_DATE)) {
			fromDate = new IWTimestamp(IWDatePickerHandler.getParsedDateByCurrentLocale(iwc.getParameter(PARAMETER_FROM_DATE))).getDate();
		}

		stamp.addMonths(1);
		stamp.addYears(1);
		Date toDate = stamp.getDate();
		if (iwc.isParameterSet(PARAMETER_TO_DATE)) {
			toDate = new IWTimestamp(IWDatePickerHandler.getParsedDateByCurrentLocale(iwc.getParameter(PARAMETER_TO_DATE))).getDate();
		}

		List courses = new ArrayList();
		if (true/* iwc.isParameterSet(PARAMETER_SCHOOL_TYPE_PK) */) {
			if (isSchoolUser() || getSession().getProvider() != null) {
				courses = new ArrayList(getBusiness().getCourses(-1, getSession().getProvider().getPrimaryKey(), schoolTypePK, courseTypePK, fromDate, toDate));
			}
			else {
				courses = new ArrayList(getBusiness().getCourses(getBusiness().getProvidersForUser(iwc.getCurrentUser()), schoolTypePK, courseTypePK, fromDate, toDate));
			}
			Collections.sort(courses, new CourseComparator(iwc.getCurrentLocale(), iwc.isParameterSet(PARAMETER_SORTING) ? Integer.parseInt(iwc.getParameter(PARAMETER_SORTING)) : (useBirthYears ? CourseComparator.DATE_SORT : CourseComparator.ID_SORT)));
		}

		boolean showID = iwc.getApplicationSettings().getBoolean(CourseConstants.PROPERTY_SHOW_ID_IN_NAME, false);

		Iterator iter = courses.iterator();
		while (iter.hasNext()) {
			row = group.createRow();

			Course course = (Course) iter.next();
			CourseType type = course.getCourseType();
			CourseCategory courseCategory = type.getCourseCategory();
			CoursePrice price = course.getPrice();
			IWTimestamp dateFrom = new IWTimestamp(course.getStartDate());
			IWTimestamp dateTo = course.getEndDate() != null ? new IWTimestamp(course.getEndDate()) : new IWTimestamp(getBusiness().getEndDate(price, dateFrom.getDate()));

			cell = row.createCell();
			cell.setStyleClass("firstColumn");
			cell.setStyleClass("number");
			if (type.getAbbreviation() != null) {
				cell.add(new Text(type.getAbbreviation()));
			}
			cell.add(new Text(String.valueOf(showID ? course.getCourseNumber() : course.getPrimaryKey())));

			cell = row.createCell();
			cell.setStyleClass("course");
			if (getResponsePage() != null) {
				Link link = new Link(new Text(course.getName()));
				link.setPage(getResponsePage());
				link.addParameter(PARAMETER_COURSE_PK, course.getPrimaryKey().toString());
				if (type != null) {
					link.addParameter(PARAMETER_COURSE_TYPE_PK, type.getPrimaryKey().toString());
				}
				if (courseCategory != null) {
					link.addParameter(PARAMETER_SCHOOL_TYPE_PK, courseCategory.getPrimaryKey().toString());
				}
				cell.add(link);
			}
			else {
				cell.add(new Text(course.getName()));
			}

			if (isSchoolSuperAdministrator(iwc)) {
				School provider = course.getProvider();

				cell = row.createCell();
				cell.setStyleClass("provider");
				cell.add(new Text(provider.getSchoolName()));
			}

			cell = row.createCell();
			cell.setStyleClass("type");
			cell.add(new Text(type.getName()));

			if (useBirthYears) {
				cell = row.createCell();
				cell.setStyleClass("from");
				cell.add(new Text(String.valueOf(course.getBirthyearFrom())));

				cell = row.createCell();
				cell.setStyleClass("to");
				cell.add(new Text(String.valueOf(course.getBirthyearTo())));
			}

			cell = row.createCell();
			cell.setStyleClass("dateFrom");
			cell.add(new Text(dateFrom.getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT)));

			cell = row.createCell();
			cell.setStyleClass("dateTo");
			cell.add(new Text(dateTo.getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT)));

			cell = row.createCell();
			cell.setStyleClass("max");
			cell.add(new Text(String.valueOf(course.getMax())));

			cell = row.createCell();
			cell.setStyleClass("freePlaces");
			cell.add(new Text(String.valueOf(getBusiness().getNumberOfFreePlaces(course))));

			cell = row.createCell();
			cell.setStyleClass("lastColumn");
			cell.setStyleClass("employee");
			cell.add(new Text(course.getUser() != null && course.getUser().length() > 0 ? course.getUser() : "-"));

			if (iRow % 2 == 0) {
				row.setStyleClass("evenRow");
			}
			else {
				row.setStyleClass("oddRow");
			}
			iRow++;
		}

		return table;
	}
}
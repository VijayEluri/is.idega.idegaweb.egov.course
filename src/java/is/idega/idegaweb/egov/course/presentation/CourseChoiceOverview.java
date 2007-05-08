/*
 * $Id$ Created on Mar 30, 2007
 * 
 * Copyright (C) 2007 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to license terms.
 */
package is.idega.idegaweb.egov.course.presentation;

import is.idega.block.family.business.NoCustodianFound;
import is.idega.block.family.data.Child;
import is.idega.block.family.data.Custodian;
import is.idega.block.family.data.Relative;
import is.idega.idegaweb.egov.course.CourseConstants;
import is.idega.idegaweb.egov.course.data.Course;
import is.idega.idegaweb.egov.course.data.CourseChoice;
import is.idega.idegaweb.egov.course.data.CourseType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.FinderException;

import com.idega.block.creditcard.business.CreditCardAuthorizationException;
import com.idega.block.school.data.School;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;
import com.idega.util.text.TextSoap;

public class CourseChoiceOverview extends CourseBlock {

	private static final String PARAMETER_ACTION = "prm_action";

	private static final String PARAMETER_CARD_NUMBER = "prm_card_number";
	private static final String PARAMETER_VALID_MONTH = "prm_valid_month";
	private static final String PARAMETER_VALID_YEAR = "prm_valid_year";
	private static final String PARAMETER_AMOUNT = "prm_amount";
	private static final String PARAMETER_CCV = "prm_ccv";

	private static final int ACTION_VIEW = 1;
	private static final int ACTION_REFUND_FORM = 2;
	private static final int ACTION_REFUND = 3;

	public void present(IWContext iwc) {
		try {
			CourseChoice choice = null;
			if (iwc.isParameterSet(PARAMETER_CHOICE_PK)) {
				try {
					choice = getBusiness().getCourseChoiceHome().findByPrimaryKey(iwc.getParameter(PARAMETER_CHOICE_PK));
				}
				catch (FinderException fe) {
					fe.printStackTrace();
				}
			}

			if (choice != null) {
				switch (parseAction(iwc)) {
					case ACTION_VIEW:
						getViewerForm(iwc, choice);
						break;

					case ACTION_REFUND_FORM:
						getRefundForm(iwc, choice);
						break;

					case ACTION_REFUND:
						boolean refund = refund(iwc, choice);
						if (!refund) {
							getRefundForm(iwc, choice);
						}
						break;
				}
			}
			else {
				add(new Text("No choice found..."));
			}
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re);
		}
	}

	private int parseAction(IWContext iwc) {
		int action = ACTION_VIEW;
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			action = Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		}

		return action;
	}

	private void getViewerForm(IWContext iwc, CourseChoice choice) throws RemoteException {
		Form form = new Form();
		form.maintainParameter(PARAMETER_CHOICE_PK);
		form.addParameter(PARAMETER_ACTION, ACTION_VIEW);

		form.add(getHeader(getResourceBundle().getLocalizedString("application.course_choice_overview", "Course choice overview")));

		Course course = choice.getCourse();
		CourseType type = course.getCourseType();
		is.idega.idegaweb.egov.course.data.CourseApplication application = choice.getApplication();
		School provider = course.getProvider();

		// User user = iwc.getCurrentUser();
		User applicant = choice.getUser();
		Child child = getUserBusiness().getMemberFamilyLogic().getChild(applicant);
		form.add(getPersonInfo(iwc, applicant, true/* isSchoolAdministrator(iwc) || getMemberFamilyLogic(iwc).isChildInCustodyOf(applicant, user) */));

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");

		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("application.course_information", "Course information"));
		heading.setStyleClass("subHeader");
		heading.setStyleClass("topSubHeader");
		form.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		Layer formItem = new Layer(Layer.DIV);
		Label label = new Label();
		Layer span = new Layer();

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.add(new Text(getResourceBundle().getLocalizedString("application.course_name", "Course name")));
		span = new Layer(Layer.SPAN);
		span.add(new Text(course.getName()));
		formItem.add(label);
		formItem.add(span);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.add(new Text(getResourceBundle().getLocalizedString("application.course_type", "Course type")));
		span = new Layer(Layer.SPAN);
		span.add(new Text(type.getName()));
		formItem.add(label);
		formItem.add(span);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.add(new Text(getResourceBundle().getLocalizedString("application.provider", "Provider")));
		span = new Layer(Layer.SPAN);
		span.add(new Text(provider.getSchoolName()));
		formItem.add(label);
		formItem.add(span);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.add(new Text(getResourceBundle().getLocalizedString("application.choice_date", "Choice date")));
		span = new Layer(Layer.SPAN);
		span.add(new Text(new IWTimestamp(application.getCreated()).getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)));
		formItem.add(label);
		formItem.add(span);
		section.add(formItem);

		section.add(clearLayer);

		heading = new Heading1(getResourceBundle().getLocalizedString("application.payment_information", "Payment information"));
		heading.setStyleClass("subHeader");
		form.add(heading);

		section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		User payer = null;
		if (application.getPayerPersonalID() != null) {
			try {
				payer = getUserBusiness().getUser(application.getPayerPersonalID());
			}
			catch (FinderException e) {
				e.printStackTrace();
				payer = application.getOwner();
			}
			catch (RemoteException re) {
				throw new IBORuntimeException(re);
			}
		}
		else {
			payer = application.getOwner();
		}
		Name name = new Name(payer.getFirstName(), payer.getMiddleName(), payer.getLastName());

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.add(new Text(getResourceBundle().getLocalizedString("application.payer_name", "Payer name")));
		span = new Layer(Layer.SPAN);
		span.add(new Text(name.getName(iwc.getCurrentLocale())));
		formItem.add(label);
		formItem.add(span);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.add(new Text(getResourceBundle().getLocalizedString("application.payer_personal_id", "Payer personal ID")));
		span = new Layer(Layer.SPAN);
		span.add(new Text(PersonalIDFormatter.format(payer.getPersonalID(), iwc.getCurrentLocale())));
		formItem.add(label);
		formItem.add(span);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label();
		label.add(new Text(getResourceBundle().getLocalizedString("application.payment_type", "Payment type")));
		span = new Layer(Layer.SPAN);
		span.add(new Text(getResourceBundle().getLocalizedString("payment_type." + application.getPaymentType(), application.getPaymentType())));
		formItem.add(label);
		formItem.add(span);
		section.add(formItem);

		section.add(clearLayer);

		Collection custodians = null;
		try {
			custodians = child.getCustodians();
		}
		catch (NoCustodianFound ncf) {
			custodians = new ArrayList();
		}
		Custodian custodian = child.getExtraCustodian();
		if (custodian != null) {
			custodians.add(custodian);
		}

		if (!custodians.isEmpty()) {
			heading = new Heading1(getResourceBundle().getLocalizedString("application.custodian_information", "Custodian information"));
			heading.setStyleClass("subHeader");
			form.add(heading);

			form.add(getCustodians(iwc, getResourceBundle(), application.getOwner(), child, custodians));
		}

		List relatives = new ArrayList();
		Relative mainRelative = child.getMainRelative(CourseConstants.COURSE_PREFIX);
		if (mainRelative != null) {
			relatives.add(mainRelative);
		}
		relatives.addAll(child.getRelatives(CourseConstants.COURSE_PREFIX));
		if (!relatives.isEmpty()) {
			heading = new Heading1(getResourceBundle().getLocalizedString("application.relative_information", "Relative information"));
			heading.setStyleClass("subHeader");
			form.add(heading);

			form.add(getRelatives(iwc, getResourceBundle(), relatives));
		}

		heading = new Heading1(getResourceBundle().getLocalizedString("child.child_information", "Child information"));
		heading.setStyleClass("subHeader");
		form.add(heading);

		section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		addChildInformationOverview(iwc, section, getResourceBundle(), child);

		section.add(clearLayer);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link home = getButtonLink(getResourceBundle().getLocalizedString("back", "Back"));
		home.setStyleClass("buttonHome");
		if (getResponsePage() != null) {
			home.setPage(getResponsePage());
		}
		home.addParameter(getBusiness().getSelectedCaseParameter(), application.getPrimaryKey().toString());
		bottom.add(home);

		Link receipt = getButtonLink(getResourceBundle().getLocalizedString("receipt", "Receipt"));
		receipt.setWindowToOpen(CourseApplicationOverviewWindow.class);
		receipt.addParameter(getBusiness().getSelectedCaseParameter(), application.getPrimaryKey().toString());
		bottom.add(receipt);

		if (isSchoolAdministrator(iwc) && getBusiness().canInvalidate(choice)) {
			Link invalidate = getButtonLink(getResourceBundle().getLocalizedString("invalidate", "Invalidate"));
			if (application.getPaymentType().equals(CourseConstants.PAYMENT_TYPE_CARD)) {
				invalidate.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_REFUND_FORM));
				invalidate.setToFormSubmit(form);
			}
			else {
				invalidate.addParameter(PARAMETER_ACTION, String.valueOf(ACTION_REFUND));
				invalidate.maintainParameter(PARAMETER_CHOICE_PK, iwc);
				invalidate.setClickConfirmation(getResourceBundle().getLocalizedString("confirm_invalidation", "Are you sure you want to invalidate this registration?"));
			}
			bottom.add(invalidate);
		}

		add(form);
	}

	private void getRefundForm(IWContext iwc, CourseChoice choice) throws RemoteException {
		Form form = new Form();
		form.maintainParameter(PARAMETER_CHOICE_PK);
		form.addParameter(PARAMETER_ACTION, ACTION_REFUND);

		addErrors(iwc, form);

		form.add(getHeader(getResourceBundle().getLocalizedString("application.course_choice_overview", "Course choice overview")));

		// User user = iwc.getCurrentUser();
		User applicant = choice.getUser();
		form.add(getPersonInfo(iwc, applicant, true/* isSchoolAdministrator(iwc) || getMemberFamilyLogic(iwc).isChildInCustodyOf(applicant, user) */));

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");

		Heading1 heading = new Heading1(getResourceBundle().getLocalizedString("application.creditcard_information", "Creditcard information"));
		heading.setStyleClass("subHeader");
		form.add(heading);

		Layer section = new Layer(Layer.DIV);
		section.setStyleClass("formSection");
		form.add(section);

		Layer helpLayer = new Layer(Layer.DIV);
		helpLayer.setStyleClass("helperText");
		helpLayer.add(new Text(getResourceBundle().getLocalizedString("course_choice.creditcard_information_help", "If you have selected to pay by creditcard, please fill in the creditcard information.  All the fields are required.")));
		section.add(helpLayer);

		TextInput cardNumber = new TextInput(PARAMETER_CARD_NUMBER, null);
		cardNumber.setLength(16);
		cardNumber.setMaxlength(16);
		cardNumber.keepStatusOnAction(true);

		TextInput ccNumber = new TextInput(PARAMETER_CCV, null);
		ccNumber.setLength(3);
		ccNumber.setMaxlength(3);
		ccNumber.keepStatusOnAction(true);

		float amount = getBusiness().calculateRefund(choice);
		TextInput amountInput = new TextInput("hidden_amount", Float.toString(amount));
		amountInput.setDisabled(true);
		HiddenInput refundAmount = new HiddenInput(PARAMETER_AMOUNT, Float.toString(amount));

		DropdownMenu validMonth = new DropdownMenu(PARAMETER_VALID_MONTH);
		validMonth.setWidth("45px");
		validMonth.keepStatusOnAction(true);
		for (int a = 1; a <= 12; a++) {
			validMonth.addMenuElement(TextSoap.addZero(a), TextSoap.addZero(a));
		}

		IWTimestamp stamp = new IWTimestamp();
		DropdownMenu validYear = new DropdownMenu(PARAMETER_VALID_YEAR);
		validYear.setWidth("60px");
		validYear.keepStatusOnAction(true);
		int year = stamp.getYear();
		for (int a = year; a <= year + 10; a++) {
			validYear.addMenuElement(String.valueOf(stamp.getYear()).substring(2), String.valueOf(stamp.getYear()));
			stamp.addYears(1);
		}

		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		if (hasError(PARAMETER_CARD_NUMBER)) {
			formItem.setStyleClass("hasError");
		}
		Label label = new Label(new Span(new Text(getResourceBundle().getLocalizedString("application.card_number", "Card number"))), cardNumber);
		formItem.add(label);
		formItem.add(cardNumber);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		label = new Label(new Span(new Text(getResourceBundle().getLocalizedString("application.ccv_number", "Credit card verification number"))), ccNumber);
		formItem.add(label);
		formItem.add(ccNumber);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		formItem.setStyleClass("required");
		label = new Label(new Span(new Text(getResourceBundle().getLocalizedString("application.card_valid_time", "Card valid through"))), validMonth);
		formItem.add(label);
		formItem.add(validMonth);
		formItem.add(validYear);
		section.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(new Text(getResourceBundle().getLocalizedString("application.amount", "amount")), amountInput);
		formItem.add(label);
		formItem.add(amountInput);
		formItem.add(refundAmount);
		section.add(formItem);

		section.add(clearLayer);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		form.add(bottom);

		Link back = getButtonLink(getResourceBundle().getLocalizedString("back", "Back"));
		back.setStyleClass("buttonHome");
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
		back.setToFormSubmit(form);
		bottom.add(back);

		Link submit = getButtonLink(getResourceBundle().getLocalizedString("refund", "Refund"));
		submit.setToFormSubmit(form);
		bottom.add(submit);

		add(form);
	}

	private boolean refund(IWContext iwc, CourseChoice choice) throws RemoteException {
		is.idega.idegaweb.egov.course.data.CourseApplication application = choice.getApplication();
		if (application.getPaymentType().equals(CourseConstants.PAYMENT_TYPE_CARD)) {
			String cardNumber = iwc.getParameter(PARAMETER_CARD_NUMBER);
			String expiresMonth = iwc.getParameter(PARAMETER_VALID_MONTH);
			String expiresYear = iwc.getParameter(PARAMETER_VALID_YEAR);
			String ccVerifyNumber = iwc.getParameter(PARAMETER_CCV);
			double amount = Double.parseDouble(iwc.getParameter(PARAMETER_AMOUNT));

			try {
				getBusiness().refundPayment(application, cardNumber, expiresMonth, expiresYear, ccVerifyNumber, amount);
			}
			catch (CreditCardAuthorizationException e) {
				setError("", e.getLocalizedMessage());
				return false;
			}
		}
		else if (application.isPaid()) {
			getBusiness().sendRefundMessage(application, choice, iwc.getCurrentLocale());
		}

		getBusiness().invalidateChoice(application, choice, iwc.getCurrentLocale());

		String subject = localize("course_choice.invalidated_receipt_subject", "Choice invalidated");
		String body = "";
		if (application.getPaymentType().equals(CourseConstants.PAYMENT_TYPE_CARD)) {
			body = localize("course_choice.invalidated_receipt_card_body", "The course choice has been invalidated and refunded to the card you supplied.");
		}
		else if (application.getPaymentType().equals(CourseConstants.PAYMENT_TYPE_GIRO)) {
			body = localize("course_choice.invalidated_receipt_giro_body", "The course choice has been invalidated.  If the choice was already paid for a message has been sent to the finance administration that will refund the payer.");
		}

		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("receipt");
		add(layer);

		Layer image = new Layer(Layer.DIV);
		image.setStyleClass("receiptImage");
		layer.add(image);

		Heading1 heading = new Heading1(subject);
		layer.add(heading);

		Paragraph paragraph = new Paragraph();
		paragraph.add(new Text(body));
		layer.add(paragraph);

		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		add(clearLayer);

		Layer bottom = new Layer(Layer.DIV);
		bottom.setStyleClass("bottom");
		add(bottom);

		ICPage backPage = null;
		if (getBackPage() != null) {
			backPage = getBackPage();
		}
		else {
			try {
				backPage = getUserBusiness(iwc).getHomePageForUser(iwc.getCurrentUser());
			}
			catch (FinderException fe) {
				fe.printStackTrace();
			}
		}

		Link link = getButtonLink(localize("back", "Back"));
		link.setStyleClass("homeButton");
		link.setPage(backPage);
		bottom.add(link);

		return true;
	}
}
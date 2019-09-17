import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import oracle.oats.scripting.modules.basic.api.*;
import oracle.oats.scripting.modules.basic.api.Variables.Scope;
import oracle.oats.scripting.modules.browser.api.*;
import oracle.oats.scripting.modules.functionalTest.api.*;
import oracle.oats.scripting.modules.utilities.api.*;
import oracle.oats.scripting.modules.utilities.api.sql.*;
import oracle.oats.scripting.modules.utilities.api.xml.*;
import oracle.oats.scripting.modules.utilities.api.file.*;
import oracle.oats.scripting.modules.webdom.api.*;
import oracle.oats.scripting.modules.webdom.api.elements.DOMElement;
import oracle.oats.scripting.modules.webdom.api.elements.DOMText;
import oracle.oats.scripting.modules.image.api.*;
import lib.*;

public class script extends IteratingVUserScript {
	@ScriptService oracle.oats.scripting.modules.utilities.api.UtilitiesService utilities;
	@ScriptService oracle.oats.scripting.modules.browser.api.BrowserService browser;
	@ScriptService oracle.oats.scripting.modules.functionalTest.api.FunctionalTestService ft;
	@ScriptService oracle.oats.scripting.modules.webdom.api.WebDomService web;
	@ScriptService oracle.oats.scripting.modules.image.api.ImageService img;
	@ScriptService oracle.oats.scripting.modules.datatable.api.DataTableService datatable;
	
	int scheduleRowCnt = 0;
	@FunctionLibrary("HCMfuntionLib") lib.JS.HCM.HCMfuntionLib hCMfuntionLib;
	public void initialize() throws Exception {
	}
		
	public void run() throws Exception {
		getDatabank("PersonalInfo_TestData").getNextDatabankRecord();
		getVariables().set("run","{{db.PersonalInfo_TestData.Run,String}}", Scope.GLOBAL);
		getVariables().set("TestDesc","{{db.PersonalInfo_TestData.TestDescription,String}}", Scope.GLOBAL);
		String run = getVariables().get("run");
		String testDesc = getVariables().get("TestDesc");
		if(run.equalsIgnoreCase("Y")){
			getVariables().set("UserRole","{{db.PersonalInfo_TestData.UserRole,String}}", Scope.GLOBAL);
			getVariables().set("Environment","{{db.PersonalInfo_TestData.Environment,String}}", Scope.GLOBAL);
			String testUserRole = getVariables().get("UserRole");
			String testEnv = getVariables().get("Environment");
			hCMfuntionLib.HCMLogin(testUserRole,testEnv);
			Absence();
			
		}else{
			getLogger().info("Scenario: "+testDesc+" is marked to skip");
		}
	
	}
	public void Absence() throws Exception{
		
		think(3.005);
		getVariables().set("HeaderAbsence", "{{db.NewHire.HeaderAbsence}}");
		String testHeader = getVariables().get("HeaderAbsence").toLowerCase();
		String loginUser = getVariables().get("LoginUser").toLowerCase();
		String TestDesc = getVariables().get("TestDescription").toLowerCase();
		int loopSize =1;
		String appendLoopCount = "";
		if(TestDesc.contains("multiple")){
			loopSize =2;
		}
		for(int i=1;i<=loopSize;i++){
			if(i!=1){
				appendLoopCount = String.valueOf(i);
			}
			if(loginUser.contains("hr") || loginUser.contains("admin") || loginUser.contains("lm")){
				NavigateToColleagueManagement();
				SearchColleague();
				if(testHeader.contains("manage absence records")){
					navigateToManageAbsenceRecords();
					try{
						if(TestDesc.contains("accrual")){
							fillAccruals();
						}
						//String absenceType = CollegueDtls.get("AbsenceType").toString();
						fillFormForAbsenceType(appendLoopCount);
						//clickSubmit();
					}catch(Exception e){
						getVariables().set("EnrolAdjustType", "{{db.NewHire.EnrolAdjustType}}");
						selectEnrollmentsAndAdjustments(getVariables().get("EnrolAdjustType").toString());
						if(getVariables().get("EnrolAdjustType").toString().equalsIgnoreCase("Adjust Balance")){
							fillAdjustBalance();
						}
					}
				}else if(testHeader.contains("manage work schedule assignement")){
					navigateToWorkSchedule();
					fillWorkSchedule();
					clickReview();
					//clickSubmit();
				}else if(testHeader.contains("manage element entries")){
					navigateToManagePayrollElementEntities();
					fillElementEntity();
				}
			}
			clickOkConfirmation();
		}
	}
	public void clickOkConfirmation() throws Exception {
		System.out.println("Entered ok confirmation");
		web.element("{{obj.AbsenceRepo.okConfirmation}}").waitFor(50);
		web.element("{{obj.AbsenceRepo.okConfirmation}}").focus();
		web.element("{{obj.AbsenceRepo.okConfirmation}}").click();	
}
	public void SearchColleague() throws Exception {

		getVariables().set("bretVal", "PASSED", Scope.GLOBAL);
		String strACtVal=getVariables().get("strOutMsg");
		System.out.println("Inside SearchColleague for Absence script" );
		
										
		web.textBox("{{obj.TransferRepo.searchWithColNum}}").waitFor();
		getVariables().set("ColleagueId", "{{db.Absence.ColleagueId}}");
		String CollegueId=getVariables().get("ColleagueId").toString();
		web.textBox("{{obj.TransferRepo.searchWithColNum}}").setText(CollegueId);
		think(2.0);
		
		web.element("{{obj.TransferRepo.searchBtn}}").waitFor();
		web.element("{{obj.TransferRepo.searchBtn}}").click();
		think(3.0);
		web.element("{{obj.TransferRepo.elmColName}}").waitFor();
		web.element("{{obj.TransferRepo.elmColName}}").click();
	
		strACtVal= strACtVal +"Searched for the COlleague ID: " + CollegueId;
		getVariables().set("strOutMsg", eval(strACtVal), Scope.GLOBAL);
		 
	}
	
	public void fillAdjustBalance() throws Exception{
		
		expandAdjustReason();
		String adjustReason="//web:ul[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt2:0:AP1:r3:0:AT2:soc3::pop']";
		getVariables().set("AdjustReason", "{{db.Absence.AdjustReason,String}}");
		selectItemFrmDropdown(adjustReason, getVariables().get("AdjustReason").toString());
		think(3);
		getVariables().set("AdjustmentAmount", "{{db.Absence.AdjustmentAmount,String}}");
		fillAbsenceText("Adjustment Amount", getVariables().get("AdjustmentAmount").toString());
		getVariables().set("AdjustmentDate", "{{db.Absence.AdjustmentDate,String}}");
		fillAbsenceText("Date", getVariables().get("AdjustmentDate").toString());	
		clickSubmitAdjustBal();
	}
	public void fillWorkSchedule() throws Exception{
		clickAddSchedule();
		getVariables().set("ScheduleName", "{{db.Absence.WorkScheduleName,String}}");
		fillWorkScheduleName(getVariables().get("ScheduleName").toString());
		getVariables().set("StartDate", "{{db.Absence.WorkScheduleStartDate}}");
		if(!(getVariables().get("StartDate").toString().equals("")) || getVariables().get("StartDate").toString()!=null ){
			fillWorkScheduleStartDate(getVariables().get("StartDate").toString());
		}
		getVariables().set("EndDate", "{{db.Absence.WorkScheduleEndDate,String}}");
		if(!(getVariables().get("EndDate").toString().equals("")) || getVariables().get("EndDate").toString()!=null ){
			fillWorkScheduleEndDate(getVariables().get("EndDate").toString());
		}
		getVariables().set("Primary",  "{{db.Absence.WorkSchedulePrimary,String}}");
		selectSchedulePrimaryDropdown(getVariables().get("Primary").toString());
		
	}
	
	public void fillFormForAbsenceType(String appendLoopCount) throws Exception{
		getVariables().set("AbsenceType", "{{db.Absence.AbsenceType"+appendLoopCount+",String}}");
		String absenceType = getVariables().get("AbsenceType").toString();
		think(2);
		clickAddAbsence();
		think(2);
		web.element("{{obj.AbsenceRepo.expandAbsenceType}}").focus();
		web.element("{{obj.AbsenceRepo.expandAbsenceType}}").click();
		String AbsenceDropdownList="//web:ul[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt2:1:AP1:soc1::pop']";
		think(2);
		selectItemFrmDropdown(AbsenceDropdownList, absenceType );
		think(5);
		if(absenceType.equalsIgnoreCase("Adoption")){
			getVariables().set("ExpectedDateOfPlacement", "{{db.Absence.ExpectedDateOfPlacement,String}}");
			getVariables().set("PlacementMatchDate", "{{db.Absence.PlacementMatchDate,String}}");
			getVariables().set("PlannedStartDate", "{{db.Absence.PlannedStartDate,String}}");
			getVariables().set("PlannedEndDate", "{{db.Absence.PlannedEndDate,String}}");
			getVariables().set("ActualDateOfPlacement", "{{db.Absence.ActualDateOfPlacement,String}}");
			getVariables().set("ActualStartDate", "{{db.Absence.ActualStartDate,String}}");
			getVariables().set("ActualEndDate", "{{db.Absence.ActualEndDate,String}}");
			getVariables().set("ActualEndDate", "{{db.Absence.SpecialConditions,String}}");
			fillAbsenceText("Expected Date of Placement", getVariables().get("ExpectedDateOfPlacement").toString());
			fillAbsenceText("Placement Match Date", getVariables().get("PlacementMatchDate").toString());
			fillAbsenceText("Planned Start Date", getVariables().get("PlannedStartDate").toString());
			fillAbsenceText("Planned End Date", getVariables().get("PlannedEndDate").toString());
			fillAbsenceText("Actual Date of Placement",getVariables().get("ActualDateOfPlacement").toString());
			fillAbsenceText("Actual Start Date",getVariables().get("ActualStartDate").toString());
			fillAbsenceText("Actual End Date", getVariables().get("ActualEndDate").toString());
			selectSpecialCondition(getVariables().get("SpecialConditions").toString());
			if(fillAbsenceText("Expected Week of Placement", "30.06.2019")){
				throw new Exception("Expected week of child birth is editable, which is not expected");
			}
			getVariables().set("WillNotReturnToWork", "{{db.Absence.WillNotReturnToWork,String}}");
			if(getVariables().get("WillNotReturnToWork").equalsIgnoreCase("Yes")){
				fillAbsenceCheckBox("Will not return to work");	
			}
			
		}else if(absenceType.equalsIgnoreCase("Authorised Paid")){
			
		}else if(absenceType.equalsIgnoreCase("Authorised Unpaid")){
			
		}else if(absenceType.equalsIgnoreCase("Maternity")){
			getVariables().set("ExpectedDateOfChildbirth", "{{db.Absence.ExpectedDateOfChildbirth,String}}");
			getVariables().set("PlannedStartDate", "{{db.Absence.PlannedStartDate,String}}");
			getVariables().set("PlannedEndDate", "{{db.Absence.PlannedEndDate,String}}");
			getVariables().set("ActualDateOfChildbirth", "{{db.Absence.ActualDateOfChildbirth,String}}");
			getVariables().set("ActualStartDate", "{{db.Absence.ActualStartDate,String}}");
			getVariables().set("ActualEndDate", "{{db.Absence.ActualEndDate,String}}");
			getVariables().set("SpecialConditions", "{{db.Absence.SpecialConditions,String}}");
			fillAbsenceText("Expected Date of Childbirth", getVariables().get("ExpectedDateOfChildbirth").toString());
			fillAbsenceText("Planned Start Date", getVariables().get("PlannedStartDate").toString());
			fillAbsenceText("Planned End Date", getVariables().get("PlannedEndDate").toString());
			fillAbsenceText("Actual Date of Childbirth",getVariables().get("ActualDateOfChildbirth").toString());
			fillAbsenceText("Actual Start Date",getVariables().get("ActualStartDate").toString());
			fillAbsenceText("Actual End Date", getVariables().get("ActualEndDate").toString());
			selectSpecialCondition(getVariables().get("SpecialConditions").toString());
			if(fillAbsenceText("Expected Week of Childbirth", "30.06.2019")){
				throw new Exception("Expected week of child birth is editable, which is not expected");
			}
			getVariables().set("WillNotReturnToWork", "{{db.Absence.WillNotReturnToWork,String}}");
			if(getVariables().get("WillNotReturnToWork").equalsIgnoreCase("Yes")){
				fillAbsenceCheckBox("Will not return to work");	
			}
			
			
		}else if(absenceType.equalsIgnoreCase("Paternity Adoption")){
			
		}else if(absenceType.equalsIgnoreCase("Paternity Birth")){
			getVariables().set("ExpectedDateOfChildbirth", "{{db.Absence.ExpectedDateOfChildbirth,String}}");
			getVariables().set("PlannedStartDate", "{{db.Absence.PlannedStartDate,String}}");
			getVariables().set("PlannedEndDate", "{{db.Absence.PlannedEndDate,String}}");
			getVariables().set("ActualDateOfChildbirth", "{{db.Absence.ActualDateOfChildbirth,String}}");
			getVariables().set("ActualStartDate", "{{db.Absence.ActualStartDate,String}}");
			getVariables().set("ActualEndDate", "{{db.Absence.ActualEndDate,String}}");
			fillAbsenceText("Expected Date of Childbirth", getVariables().get("ExpectedDateOfChildbirth").toString());
			fillAbsenceText("Planned Start Date", getVariables().get("PlannedStartDate").toString());
			fillAbsenceText("Planned End Date", getVariables().get("PlannedEndDate").toString());
			fillAbsenceText("Actual Date of Childbirth",getVariables().get("ActualDateOfChildbirth").toString());
			fillAbsenceText("Actual Start Date",getVariables().get("ActualStartDate").toString());
			fillAbsenceText("Actual End Date", getVariables().get("ActualEndDate").toString());
			getVariables().set("WillNotReturnToWork", "{{db.Absence.WillNotReturnToWork,String}}");
			if(getVariables().get("WillNotReturnToWork").equalsIgnoreCase("Yes")){
				fillAbsenceCheckBox("Will not return to work");	
			}
			
		}else if(absenceType.equalsIgnoreCase("Shared Parental Adoption")){
			
		}else if(absenceType.equalsIgnoreCase("Shared Parental Birth")){
			
		}else if(absenceType.equalsIgnoreCase("Sickness")){
			
		}else if(absenceType.equalsIgnoreCase("Unauthorised Unpaid")){
			
		}
		//clickSubmit();
	}
	public void navigateToManageAbsenceRecords() throws Exception {
		web.element("{{obj.AbsenceRepo.taskList_lnk}}").focus();
		web.element("{{obj.AbsenceRepo.taskList_lnk}}").click();
		web.element("{{obj.AbsenceRepo.manageAbsenceRecords_link}}").click();
	}
	
	public void navigateToManagePayrollElementEntities() throws Exception {
		web.element("{{obj.AbsenceRepo.taskList_lnk}}").focus();
		web.element("{{obj.AbsenceRepo.taskList_lnk}}").click();
		web.element("{{obj.AbsenceRepo.managePayrollElementEntities_link}}").click();
	}
	
	
	public void navigateToWorkSchedule() throws Exception {
		web.element("{{obj.AbsenceRepo.taskList_lnk}}").focus();
		web.element("{{obj.AbsenceRepo.taskList_lnk}}").click();
		web.element("{{obj.AbsenceRepo.manageWorkSchedule_link}}").click();
	} 
	public void selectEnrollmentsAndAdjustments(String enrollAdjValue) throws Exception {
		web.element("{{obj.AbsenceRepo.enrollAndAdjust_lnk}}").getParent().getParent().getNextSibling().getChildren().get(0).getChildren().get(0).click();
		think(2);
		String enrollAdjXpath = "/web:window[@index='0' or @title='Manage Absence Records*']/web:document[@index='0']/web:td[@text='"+enrollAdjValue+"']";
		web.element(enrollAdjXpath).click();
		think(2);
	}
	public void selectSpecialCondition(String splCondition) throws Exception {
		String defalutAllSpecialConditionsInputBox = "/web:window[@index='0' or @title='Manage Absence Records*']/web:document[@index='0']/web:form[@id='_FOf1']/web:input_text[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt2:1:AP1:smc1::content' and @value='All']";
		String specialCondition = "/web:window[@index='0' or @title='Manage Absence Records*']/web:document[@index='0']/web:label[@text='\r\n"+splCondition+"']";
		think(5);
		if(!splCondition.equalsIgnoreCase("All")){
			if(web.exists(defalutAllSpecialConditionsInputBox)){
				web.element("{{obj.AbsenceRepo.expandSpecialConditions_dd}}").click();
				String unselectAll = "/web:window[@index='0' or @title='Manage Absence Records*']/web:document[@index='0']/web:label[@text='\r\nAll']";
				think(2);
				web.element(unselectAll).click();
				think(2);
				web.element(specialCondition).click();
			}else{
				web.element("{{obj.AbsenceRepo.expandSpecialConditions_dd}}").click();
				web.element(specialCondition).click();
			}
		}else{
			if(web.exists(defalutAllSpecialConditionsInputBox)){
				//do nothing
			}else{
				web.element("{{obj.AbsenceRepo.expandSpecialConditions_dd}}").click();
				String selectAll = "/web:window[@index='0' or @title='Manage Absence Records*']/web:document[@index='0']/web:label[@text='\r\nAll']";
				think(2);
				web.element(selectAll).click();
				think(2);
			}
		}
	}
	
	public void fillElementEntity() throws Exception {
		clickAddPayrollEntity();
		fillPayrollEntityEffectiveDate();
		fillPayrollEntityElementName();
		clickEntityContinueButton();
		clickEntityGeneralInfo();
		getVariables().set("EntryDays", "{{db.Absence.ElementEntryDays,String}}");
		fillAbsenceText("Days", getVariables().get("EntryDays"));
		think(2);
		clickPayrollEntrySubmit();
		clickEntityRecord();
		getVariables().set("EffectiveAsOfDate", "{{db.Absence.ElementEffectiveAsOfDate,String}}");
		fillAbsenceText("Effective As-of Date", getVariables().get("EffectiveAsOfDate"));
		clickEditEndDateEntity();
		clickEntityContinueButton();
	}
	public void fillRowForColleagueScheduleAbsence(String appendLoopCount) throws Exception {
		int currentRowIndex = 0;
		String tableSize = web.element("//web:table[@summary='Details']").getAttribute("_rowcount");
		int noOfRows = Integer.parseInt(tableSize);
		List<DOMElement> allAbsRows = web.element("//web:table[@summary='Details']").getChildren().get(1).getChildren();
		for(DOMElement eachAbsRow: allAbsRows){
			String index = eachAbsRow.getAttribute("_afrrk");
			currentRowIndex = Integer.parseInt(index);
		}
		think(1);
		fillCollScheduleAbsRowStartDate(currentRowIndex,appendLoopCount);
		think(1);
		selectJobID(currentRowIndex);
		think(1);
	}
	
	public void fillWeeklyRecurrence() throws Exception {
		clickAddWeeklyRecurrence();
		fillWeeklyRecurrFrequency();
		fillWeeklyRangeNoOfOccurences();
		clickWeeklyReccurOK();
	}
	public void fillCollScheduleAbsRowStartDate(int rowPosition,String appendLoopCount) throws Exception {
		String startDate = "//web:input_text[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAnt2:1:pt1:ATt1:"+rowPosition+":id2::content']";
		web.textBox(startDate).click();
		web.textBox(startDate).setText("{{db.Absence.StartDate"+appendLoopCount+",String}}");
	}
	public void selectJobID(int rowPosition) throws Exception {
		String expandJobID = "//web:a[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAnt2:1:pt1:ATt1:"+rowPosition+":soc16::drop']";
		web.element(expandJobID).click();
		String jobID_dd = "//web:ul[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAnt2:1:pt1:ATt1:"+rowPosition+":soc16::pop']"; 
		think(2);
		web.element(jobID_dd).getChildren().get(2).click();
		think(2);
	}
	
	public void checkWillNotReturnToWork() throws Exception {
		web.element("{{obj.AbsenceRepo.willNotReturnToWork_Cbox}}").click();
	}
	
	public void clickAddAbsence() throws Exception {
		web.element("{{obj.AbsenceRepo.addAbsence_btn}}").click();
	}
	
	public void clickScheduleAbsence() throws Exception {
		web.button("{{obj.AbsenceRepo.scheduleAbsence_btn}}").waitFor(60);
		web.button("{{obj.AbsenceRepo.scheduleAbsence_btn}}").click();
		think(3);
	}
	public void clickAddAbsenceRecord() throws Exception {
		think(3);
		web.button("{{obj.AbsenceRepo.addAbsenceRecord_btn}}").click();
		think(3);
	}
	public void clickAddSchedule() throws Exception {
		
		web.element("{{obj.AbsenceRepo.addSchedule}}").click();
	}
	public void clickAddWeeklyRecurrence() throws Exception {
		
		web.element("{{obj.AbsenceRepo.addWeeklyRecurrence_btn}}").click();
	}
public void clickWeeklyReccurOK() throws Exception {
		think(3);
		web.element("{{obj.AbsenceRepo.weeklyRecurrOK_btn}}").click();
	}
	
	public void fillWeeklyRecurrFrequency() throws Exception {
		think(1);
		web.textBox("{{obj.AbsenceRepo.weeklyRecurrFrequency_txt}}").setText("{{db.Absence.WeeklyFrequency,String}}");
		
	}
	public void fillWeeklyRangeNoOfOccurences() throws Exception {
		think(1);
		web.textBox("{{obj.AbsenceRepo.weeklyRange_txt}}").setText("{{db.Absence.WeeklyRangeNumber,String}}");
	}
	public void clickAddPayrollEntity() throws Exception {
		web.element("{{obj.AbsenceRepo.addPayrollEntity}}").click();
	}
	
	public void clickEntityGeneralInfo() throws Exception {
		try{
			web.element("{{obj.AbsenceRepo.payrollElementEntGeneralInfo}}").click();
		}catch(Exception e){
			clickEntityContinueButton();
			web.element("{{obj.AbsenceRepo.payrollElementEntGeneralInfo}}").click();
		}
		
	}
	public void fillAccruals() throws Exception {
		expandAccrual();
		getVariables().set("AccrualType", "{{db.Absence.AccrualType,String}}");
		selectAccrualType(getVariables().get("AccrualType"));
		fillAccrualBalanceAsOfDate();
		clickScheduleSubmit();
		
	} 
	public void fillPayrollEntityEffectiveDate() throws Exception {
		think(10);
		web.textBox("{{obj.AbsenceRepo.payrollEntityEffectiveDate}}").waitFor();
		web.textBox("{{obj.AbsenceRepo.payrollEntityEffectiveDate}}").focus();
		web.textBox("{{obj.AbsenceRepo.payrollEntityEffectiveDate}}").clearText();
		web.textBox("{{obj.AbsenceRepo.payrollEntityEffectiveDate}}").setText("{{db.Absence.ElementEffectiveDate,String}}");
	}
	
	public void fillPayrollEntityElementName() throws Exception {
		
		think(2);
		web.element("{{obj.AbsenceRepo.payrollExpandElementName_dd}}").click();
		think(2);
		web.element("{{obj.AbsenceRepo.payrollSearchElement_lnk}}").waitFor(30);
		web.element("{{obj.AbsenceRepo.payrollSearchElement_lnk}}").click();
		web.textBox("{{obj.AbsenceRepo.payrollSearchElementName_txt}}").setText("{{db.Absence.ElementName,String}}");
		think(1);
		web.element("{{obj.AbsenceRepo.payrollSearchElement_btn}}").click();
		think(5);
		selectElementNameFromTable();
		
	}
	public void selectElementNameFromTable() throws Exception{
		String tablePath = "//web:div[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt2:0:AT1:ElementTypeLOV_afrLovInternalTableId::db']";
		web.element(tablePath).getChildren().get(0).getChildren().get(1).getChildren().get(0).click();
		think(2);
		web.element("{{obj.AbsenceRepo.searchOK_btn}}").click();
	}
	public void clickEntityContinueButton() throws Exception {
		web.element("{{obj.AbsenceRepo.payrollEntityContinue_btn}}").focus();
		web.element("{{obj.AbsenceRepo.payrollEntityContinue_btn}}").click();
	}
	public void clickPayrollEntrySubmit() throws Exception {
		web.element("{{obj.AbsenceRepo.submitPayrollEntry_btn}}").focus();
		web.element("{{obj.AbsenceRepo.submitPayrollEntry_btn}}").click();
	}
	public void clickEntityRecord() throws Exception {
		getVariables().set("ElementName", "{{db.Absence.ElementName,String}}");
		String elementRecordName = "//web:a[@text='"+getVariables().get("ElementName")+"']"; 
		web.element(elementRecordName).click();
	}
	
	public void clickEditEndDateEntity() throws Exception {
		expandEditEntityRecord();
		if(isEditEntityEndDateDisabled()){
			throw new Exception("End date option is disabled in edit drop down");
		}else{
			web.element("//web:td[@text='End Date']").click();
		}
		
	}
	public boolean isEditEntityEndDateDisabled() throws Exception{
		String disableEditPath = "//web:tr[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt2:1:endDateItem' and @class='xue x1op p_AFDisabled']";
		return web.exists(disableEditPath, 5);
	}
	
	public void expandEditEntityRecord() throws Exception {
		web.element("{{obj.AbsenceRepo.editEntity_btn}}").click();
	}
	public void expandAdjustReason() throws Exception {
		web.element("{{obj.AbsenceRepo.expandAdjustReason}}").click();
	}
	
	public void expandEnrollAndAdjust() throws Exception {
		web.element("{{obj.AbsenceRepo.expandEnrollAndAdjust}}").click();
	}
	
	public void clickCalculator() throws Exception {
		web.element("{{obj.AbsenceRepo.calculator_img}}").click();
	}
	
	public void clickSubmit() throws Exception {
		try{
			web.element("{{obj.AbsenceRepo.submit_btn}}").click();	
		}catch(Exception e){
			web.element("{{obj.AbsenceRepo.ScheduleSubmit_btn}}").click();
		}
		
	}
	public void clickScheduleSubmit() throws Exception {
			web.element("{{obj.AbsenceRepo.ScheduleSubmit_btn}}").click();
	}
	public void clickReview() throws Exception {
		web.element("{{obj.AbsenceRepo.review_btn}}").click();
	}
	public void clickSubmitAdjustBal() throws Exception {
		web.element("{{obj.AbsenceRepo.submitAdjustBal_btn}}").click();
	}
	public void expandAccrual() throws Exception {
		web.element("{{obj.AbsenceRepo.expandAccrual_dd}}").click();
	}
	public void selectAccrualType(String accrualType) throws Exception {
		web.element("//web:td[@text='"+accrualType+"']").click();
	}
	
	
	public void fillAbsenceComments() throws Exception{
		web.textArea("{{obj.AbsenceRepo.submitAdjustBal_btn}}").click();
		think(5);
	}
	
	public void fillWorkScheduleName(String schedName) throws Exception{
		think(3);
		String rowCntSchedules = web.element("//web:table[@summary='Schedules']").getAttribute("_rowcount");
		scheduleRowCnt = Integer.parseInt(rowCntSchedules);
		think(2);
		String newScheduleName = "//web:input_text[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt[0-9]:0:appae2:AT12:_ATp:table1:"+(scheduleRowCnt-1)+":scheduleNameId::content']";
		web.textBox(newScheduleName).click();
		web.textBox(newScheduleName).setText(schedName);
	}
	
	public void fillWorkScheduleStartDate(String startDate) throws Exception{
		think(1);
		String newScheduleStartDate = "//web:input_text[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt[0-9]:0:appae2:AT12:_ATp:table1:"+(scheduleRowCnt-1)+":id2::content']";
		web.textBox(newScheduleStartDate).click();
		web.textBox(newScheduleStartDate).setText(startDate);
	}
	
	public void fillWorkScheduleEndDate(String endDate) throws Exception{
		think(1);
		String newScheduleEndDate = "//web:input_text[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt[0-9]:0:appae2:AT12:_ATp:table1:"+(scheduleRowCnt-1)+":id1::content']";
		web.textBox(newScheduleEndDate).click();
		web.textBox(newScheduleEndDate).setText(endDate);
	}
	
	public void selectSchedulePrimaryDropdown(String isPrimary) throws Exception {
	String newSchedulePrimary = "//web:a[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt[0-9]:0:appae2:AT12:_ATp:table1:"+(scheduleRowCnt-1)+":soc3::drop']";
		web.element(newSchedulePrimary).focus();
		web.element(newSchedulePrimary).click();
		think(3);
		String primaryDD="//web:ul[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAt[0-9]:0:appae2:AT12:_ATp:table1:"+(scheduleRowCnt-1)+":soc3::pop']"; 
		selectItemFrmDropdown(primaryDD, isPrimary);
	}
	public void fillStartDateDuration(String startDateDuration) throws Exception {
		web.textBox("//web:input_text[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAnt2:1:pt1:it1::content']").clearText();
		think(1);
		web.textBox("//web:input_text[@id='_FOpt1:_FOr1:0:_FONSr2:0:MAnt2:1:pt1:it1::content']").setText(startDateDuration);
	}
	public boolean fillAbsenceText(String field,String value) throws Exception {
		///web:window[@index='0' or @title='Manage Absence Records - Colleague Management - Oracle Applications']/web:document[@index='0' or @name='14c699uao5']/web:label[@innerText='Expected Date of Childbirth' and @text='Expected Date of Childbirth' and @className='af_inputDate_label-text' and @for='_FOpt1:_FOr1:0:_FONSr2:0:MAt2:1:AP1:id10::content' and @index='20']
		try{
			String labelXpath = "/web:window[@index='0']/web:document[@index='0']/web:label[@text='"+field+"']";
			think(2);
			web.element(labelXpath).waitFor();
			List<DOMElement> listInputfield  = web.element(labelXpath).getParent().getNextSibling().getChildren();
			for(DOMElement inputFieldchild : listInputfield){
				inputFieldchild.focus();
				inputFieldchild.click();
				
				DOMText actualInputField = (DOMText) web.findElementById(inputFieldchild.getAttribute("id"));
				think(1);
				actualInputField.clearText();
				think(1);
				actualInputField.setText(value);
				break;
			}
		}catch(Exception e){
			return false;
		}
		return true;
	
	}
	
	public void fillAccrualBalanceAsOfDate() throws Exception {
		think(10);
		web.textBox("{{obj.AbsenceRepo.accrualBalAsOfDate}}").exists(20,TimeUnit.SECONDS);
		web.textBox("{{obj.AbsenceRepo.accrualBalAsOfDate}}").setText("{{db.Absence.BalanceAsOfDate,String}}");
		}
	
	public void fillAbsenceCheckBox(String field) throws Exception {
		///web:window[@index='0' or @title='Manage Absence Records - Colleague Management - Oracle Applications']/web:document[@index='0' or @name='14c699uao5']/web:label[@innerText='Expected Date of Childbirth' and @text='Expected Date of Childbirth' and @className='af_inputDate_label-text' and @for='_FOpt1:_FOr1:0:_FONSr2:0:MAt2:1:AP1:id10::content' and @index='20']
		String labelXpath = "/web:window[@index='0']/web:document[@index='0']/web:label[@text='"+field+"']";
		DOMElement reqCheckBox  = web.element(labelXpath).getParent().getNextSibling().getChildren().get(0).getChildren().get(0).getChildren().get(1);
		reqCheckBox.click();
	}
	
	public void selectItemFrmDropdown(String object,String itemText)throws Exception{
		
		List<DOMElement> children2 = web.element(object).getChildren();
		System.out.println(children2.size());
		for(int j=0;j<children2.size();j++){
			
			String text2= children2.get(j).getDisplayText();
			System.out.println(text2);
		    if(text2.equalsIgnoreCase(itemText)){ 
		    	children2.get(j).focus();
		    	children2.get(j).dblClick();
			     break;
		     }
		}
}
	
	public void NavigateToColleagueManagement() throws Exception {

		String strACtVal = getVariables().get(
			"strOutMsg");
		strACtVal = strACtVal + "Collegue Id Details Fill:";
		getVariables().set("bretVal","PASSED",Scope.GLOBAL);
		String wndPath = "/web:window[@index='0']/web:document[@index='0']";

		if (web.link(wndPath + "/web:a[@id='*:_UISmmLink']").exists(20,TimeUnit.SECONDS)) {
			web.link(wndPath + "/web:a[@id='*:_UISmmLink']").click();
		} else {
			throw new Exception("Navigator Not Clicked");
		}
		if (web.link(
			wndPath + "/web:a[@id='pt1:_UISnvr:0:nvgcil_groupNode_workforce_management']").exists(20,TimeUnit.SECONDS)) {
			web.link(wndPath + "/web:a[@id='pt1:_UISnvr:0:nvgcil_groupNode_workforce_management']").click();
			think(2.219);
		} else {
			throw new Exception("Worforce Not Expanded");
		}
		if (web.element(
			wndPath + "/web:span[@text='Colleague Management']").exists(20,TimeUnit.SECONDS)) {
			web.element(wndPath + "/web:span[@text='Colleague Management']").click();
			think(2);
		} else {
			throw new Exception("Colleague Management not Clicked");
		}
	}

	public void finish() throws Exception {
	}
}

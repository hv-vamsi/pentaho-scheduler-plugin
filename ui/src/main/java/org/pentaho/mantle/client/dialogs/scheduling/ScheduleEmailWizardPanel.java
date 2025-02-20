/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.dialogs.scheduling;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobParam;

public class ScheduleEmailWizardPanel extends AbstractWizardPanel {

  private static final String PENTAHO_SCHEDULE = "pentaho-schedule-create"; //$NON-NLS-1$
  public static final String EMAIL_MIME = "mime-message/text/html";

  protected RadioButton yes = new RadioButton( "SCH_EMAIL_YESNO", Messages.getString( "yes" ) );
  protected RadioButton no = new RadioButton( "SCH_EMAIL_YESNO", Messages.getString( "no" ) );
  protected TextBox toAddressTextBox = new TextBox();
  protected TextBox subjectTextBox = new TextBox();
  protected TextBox attachmentNameTextBox = new TextBox();
  protected Label attachmentLabel = new Label( Messages.getString( "attachmentName" ) );
  protected TextArea messageTextArea = new TextArea();

  private String filePath;
  private JSONObject jobSchedule;
  private JSONArray scheduleParams;

  public static FlexTable getEmailSchedulePanel() {
    return emailSchedulePanel;
  }

  private static FlexTable emailSchedulePanel = new FlexTable();

  public ScheduleEmailWizardPanel( String filePath, JSONObject jobSchedule, JsJob job ) {
    this( filePath, jobSchedule, job, null );
  }

  public ScheduleEmailWizardPanel( String filePath, JSONObject jobSchedule, JsJob editJob, JSONArray scheduleParams ) {
    super();
    this.filePath = filePath;
    this.jobSchedule = jobSchedule;
    this.scheduleParams = scheduleParams;
    layout( editJob );
    addPanel( getEmailSchedulePanel() );
  }

  private native JsArray<JsSchedulingParameter> getParams( String to, String cc, String bcc, String subject,
      String message, String attachmentName )
  /*-{
    var paramEntries = new Array();
    paramEntries.push({
      name: '_SCH_EMAIL_TO',
      stringValue: to,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_CC',
      stringValue: cc,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_BCC',
      stringValue: bcc,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_SUBJECT',
      stringValue: subject,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_MESSAGE',
      stringValue: message,
      type: 'string'
    });
    paramEntries.push({
      name: '_SCH_EMAIL_ATTACHMENT_NAME',
      stringValue: attachmentName,
      type: 'string'
    });
    return paramEntries;
  }-*/;

  public JsArray<JsSchedulingParameter> getEmailParams() {
    if ( yes.getValue() ) {
      return getParams( toAddressTextBox.getText(), "", "", subjectTextBox.getText(), messageTextArea.getText(),
          attachmentNameTextBox.getText() );
    } else {
      return null;
    }
  }

  protected void layout( JsJob job ) {
    this.addStyleName( PENTAHO_SCHEDULE );

    getEmailSchedulePanel().getElement().setId( "email-schedule-panel" );
    getEmailSchedulePanel().setVisible( false );
    HorizontalPanel emailYesNoPanel = new HorizontalPanel();
    emailYesNoPanel.getElement().setId( "email-yes-no-panel" );
    emailYesNoPanel.add( ( new Label( Messages.getString( "wouldYouLikeToEmail" ) ) ) );
    no.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent event ) {
        getEmailSchedulePanel().setVisible( !no.getValue() );
        setCanContinue( isValidConfig() );
        setCanFinish( isValidConfig() );
      }
    } );
    yes.addClickHandler( new ClickHandler() {
      public void onClick( ClickEvent event ) {
        getEmailSchedulePanel().setVisible( yes.getValue() );
        setCanContinue( isValidConfig() );
        setCanFinish( isValidConfig() );
        setFocus();
      }
    } );
    no.setValue( true );
    yes.setValue( false );
    emailYesNoPanel.add( no );
    emailYesNoPanel.add( yes );
    this.add( emailYesNoPanel, NORTH );

    toAddressTextBox.setVisibleLength( 95 );
    subjectTextBox.setVisibleLength( 95 );
    attachmentNameTextBox.setVisibleLength( 95 );

    boolean hasExtension = filePath.lastIndexOf( "." ) != -1;
    String friendlyFileName = filePath.substring( filePath.lastIndexOf( "/" ) + 1 );
    if ( hasExtension ) {
      // remove it
      friendlyFileName = friendlyFileName.substring( 0, friendlyFileName.lastIndexOf( "." ) );
    }
    subjectTextBox.setText( Messages.getString( "scheduleDefaultSubject", friendlyFileName ) );
    String finalString = null;
    String stringEnd = this.appendName();
    if ( job != null ) {
      if (  stringEnd != "null" ) {
        finalString = job.getJobName();
        finalString += stringEnd;
        attachmentNameTextBox.setText( finalString );
      } else {
        attachmentNameTextBox.setText( job.getJobName() );
      }
    } else {
      if ( stringEnd != "null" ) {
        finalString = jobSchedule.get( "jobName" ).isString().stringValue();
        finalString += stringEnd;
        attachmentNameTextBox.setText( finalString );
      } else {
        attachmentNameTextBox.setText( jobSchedule.get( "jobName" ).isString().stringValue() );
      }
    }

    Label toLabel = new Label( Messages.getString( "to" ) );
    toLabel.getElement().getStyle().setMarginRight( 3, Unit.PX );
    // toLabel.setWidth("130px");
    toLabel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
    Label toAddressLabel = new Label( Messages.getString( "scheduleAddressSeparatorMessage" ) );
    toAddressLabel.setStyleName( "msg-Label" );

    HorizontalPanel toLabelPanel = new HorizontalPanel();
    toLabelPanel.add( toLabel );
    toLabelPanel.add( toAddressLabel );
    toAddressTextBox.addKeyUpHandler( new KeyUpHandler() {
      public void onKeyUp( KeyUpEvent event ) {
        setCanContinue( isValidConfig() );
        setCanFinish( isValidConfig() );
      }
    } );

    getEmailSchedulePanel().setWidget( 0, 0, toLabelPanel );
    getEmailSchedulePanel().setWidget( 1, 0, toAddressTextBox );

    Label subjectLabel = new Label( Messages.getString( "subject" ) );
    subjectLabel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
    getEmailSchedulePanel().setWidget( 3, 0, subjectLabel );
    getEmailSchedulePanel().setWidget( 4, 0, subjectTextBox );

    attachmentLabel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );

    getEmailSchedulePanel().setWidget( 5, 0, attachmentLabel );
    getEmailSchedulePanel().setWidget( 6, 0, attachmentNameTextBox );

    messageTextArea.setVisibleLines( 5 );
    messageTextArea.setWidth( "100%" );
    Label messageLabel = new Label( Messages.getString( "scheduleEmailMessage" ) );
    getEmailSchedulePanel().setWidget( 7, 0, messageLabel );
    getEmailSchedulePanel().setWidget( 8, 0, messageTextArea );

    if ( job != null ) {
      JsArray<JsJobParam> jparams = job.getJobParams();
      for ( int i = 0; i < jparams.length(); i++ ) {
        if ( "_SCH_EMAIL_TO".equals( jparams.get( i ).getName() ) ) {
          yes.setValue( true );
          no.setValue( false );
          getEmailSchedulePanel().setVisible( true );
          toAddressTextBox.setText( jparams.get( i ).getValue() );
        } else if ( "_SCH_EMAIL_SUBJECT".equals( jparams.get( i ).getName() ) ) {
          subjectTextBox.setText( jparams.get( i ).getValue() );
        } else if ( "_SCH_EMAIL_MESSAGE".equals( jparams.get( i ).getName() ) ) {
          messageTextArea.setText( jparams.get( i ).getValue() );
        } else if ( "_SCH_EMAIL_ATTACHMENT_NAME".equals( jparams.get( i ).getName() ) ) {
          attachmentNameTextBox.setText( jparams.get( i ).getValue() );
        }
      }
    }
  }

  protected void addPanel( FlexTable flexTable ) {
    this.add( flexTable, CENTER );
    panelWidgetChanged( null );
  }

  public String appendName() {
    try {
      String appendDateFormat = String.valueOf( this.jobSchedule.get( "appendDateFormat" ) );
      JSONObject objectHolder = this.jobSchedule.get( "simpleJobTrigger" ).isObject();
      String startTime = String.valueOf( objectHolder.get( "startTime" ) );
      startTime = startTime.replaceAll( "\"", "" );
      appendDateFormat = appendDateFormat.replaceAll( "\"", "" );
      String completedVaule = applyDateFormat( appendDateFormat, startTime );
      return completedVaule;
    } catch ( Exception e ) {
      return "null";
    }

  }

  public String applyDateFormat( String pattern, String strDate ) {
    String yyyy = strDate.substring( 0, 4 );
    String yy = strDate.substring( 2, 4 );
    String MM = strDate.substring( 5, 7 );
    String dd = strDate.substring( 8, 10 );
    String HH = strDate.substring( 11, 13 );
    String mm = strDate.substring( 14, 16 );
    String ss = strDate.substring( 17, 19 );
    String datePatterned = "";
    if ( pattern.equals( "yyyy-MM-dd" ) ) {
      datePatterned = yyyy + "-" + MM + "-" + dd;
    } else if ( pattern.equals( "yyyyMMdd" ) ) {
      datePatterned = yyyy + MM + dd;
    } else if ( pattern.equals( "yyyyMMddHHmmss" ) ) {
      datePatterned = yyyy + MM + dd + HH + mm + ss;
    } else if ( pattern.equals( "MM-dd-yyyy" ) ) {
      datePatterned = MM + "-" + dd + "-" + yyyy;
    } else if ( pattern.equals( "MM-dd-yy" ) ) {
      datePatterned = MM + "-" + dd + "-" + yy;
    } else if ( pattern.equals( "dd-MM-yyyy" ) ) {
      datePatterned = dd + "-" + MM + "-" + yyyy;
    }
    return datePatterned;
  }

  private void toggleAttachmentFields() {
    if ( attachmentLabel != null && attachmentNameTextBox != null ) {
      if ( scheduleParams != null && scheduleParams.toString().contains( EMAIL_MIME ) ) {
        attachmentLabel.setVisible( false );
        attachmentNameTextBox.setVisible( false );
      } else {
        attachmentLabel.setVisible( true );
        attachmentNameTextBox.setVisible( true );
      }
    }
  }

  public String getName() {
    return Messages.getString( "schedule.scheduleEdit" );
  }

  protected boolean isValidConfig() {
    if ( no.getValue() ) {
      return true;
    }
    String value = toAddressTextBox.getText();
    boolean empty = StringUtils.isEmpty( value );
    if ( !empty ) {
      boolean bothDelimitersExist = value.contains( ";" ) && value.contains( "," );
      int at = value.indexOf( "@" );
      if ( at > 0 && at < value.length() - 1 && ( !bothDelimitersExist ) ) {
        return true;
      }
    }
    return false;
  }

  protected void panelWidgetChanged( Widget changedWidget ) {
    setCanContinue( isValidConfig() );
    setCanFinish( isValidConfig() );
    toggleAttachmentFields();
  }

  public void setScheduleParams( JSONArray scheduleParams ) {
    this.scheduleParams = scheduleParams;
  }

  public void setFocus() {
    Timer t = new Timer() {
      public void run() {
        toAddressTextBox.getElement().blur();
        toAddressTextBox.setFocus( false );
        toAddressTextBox.setFocus( true );
        toAddressTextBox.getElement().focus();
        if ( toAddressTextBox.isAttached() && toAddressTextBox.isVisible() ) {
          cancel();
        }
      }
    };
    t.scheduleRepeating( 250 );
  }

}

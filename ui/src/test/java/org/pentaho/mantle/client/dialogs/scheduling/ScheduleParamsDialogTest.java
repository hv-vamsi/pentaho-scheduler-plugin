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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.WithClassesToStub;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.mantle.client.workspace.JsJob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyChar;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( GwtMockitoTestRunner.class )
@WithClassesToStub( JSONArray.class )
public class ScheduleParamsDialogTest {
  @Mock
  private ScheduleParamsDialog dialog;
  @Mock
  private ScheduleParamsWizardPanel scheduleParamsWizardPanel;
  @Mock
  JSONObject jobSchedule;
  @Mock
  private JSONArray scheduleParams;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks( this );

    dialog.jobSchedule = jobSchedule;
    dialog.scheduleParamsWizardPanel = scheduleParamsWizardPanel;
    when( dialog.getScheduleParams( false ) ).thenReturn( scheduleParams );
  }

  @Test
  public void testOnKeyDownPreview() {
    doCallRealMethod().when( dialog ).onKeyDownPreview( anyChar(), anyInt() );

    assertTrue( dialog.onKeyDownPreview( (char) KeyCodes.KEY_ENTER, -1 ) );
    verify( dialog, never() ).hide();

    assertTrue( dialog.onKeyDownPreview( (char) KeyCodes.KEY_ESCAPE, -1 ) );
    verify( dialog).hide();
  }

  @Test
  public void testBackClicked() {
    doCallRealMethod().when( dialog ).backClicked();

    dialog.parentDialog = mock( ScheduleRecurrenceDialog.class );
    final JSONArray jsonArray = mock( JSONArray.class );
    when( dialog.getScheduleParams( true ) ).thenReturn( jsonArray );

    dialog.backClicked();

    assertEquals( jsonArray, dialog.scheduleParams );
    verify( dialog.parentDialog ).center();
    verify( dialog ).hide();
  }

  @Test
  public void getScheduleParamsWithoutScheduleParams() {
    doCallRealMethod().when( dialog ).getScheduleParams( true );

    JsArray jsArrayMock = mock( JsArray.class );
    when( scheduleParamsWizardPanel.getParams( true ) ).thenReturn( jsArrayMock );

    JSONArray params = dialog.getScheduleParams( true );
    assertEquals( 0, params.size() );
  }

  @Test
  public void testGetFinishScheduleParams_editExistingSchedule() {
    doCallRealMethod().when( dialog ).getFinishScheduleParams();

    JSONObject lineageId = mock( JSONObject.class );
    when( dialog.generateLineageId() ).thenReturn( lineageId );

    dialog.editJob =  mock( JsJob.class );
    dialog.getFinishScheduleParams();

    verify( scheduleParams ).set( anyInt(), eq( lineageId ) );
  }

  @Test
  public void testGetFinishScheduleParams_newSchedule() {
    doCallRealMethod().when( dialog ).getFinishScheduleParams();

    JSONObject lineageId = mock( JSONObject.class );
    when( dialog.generateLineageId() ).thenReturn( lineageId );

    dialog.editJob = null;
    dialog.getFinishScheduleParams();

    verify( scheduleParams, never() ).set( anyInt(), eq( lineageId ) );
  }

}

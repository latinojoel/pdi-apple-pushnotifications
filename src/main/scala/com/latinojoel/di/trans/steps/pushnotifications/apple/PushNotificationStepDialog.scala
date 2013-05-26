/*
 * Pentaho Data Integration Apple Push Notifications
 * https://github.com/latinojoel/pdi-apple-pushnotifications
 *
 * Copyright (c) 2009 about.me/latinojoel
 *
 * Licensed under the GNU General Public License, Version 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl-3.0.html
 *
 * The program is provided "as is" without any warranty express or implied, including
 * the warranty of non-infringement and the implied warranties of merchantibility and
 * fitness for a particular purpose.  The Copyright owner will not be liable for any
 * damages suffered by you as a result of using the Program.  In no event will the
 * Copyright owner be liable for any special, indirect or consequential damages or
 * lost profits even if the Copyright owner has been advised of the possibility of
 * their occurrence.
 */
package com.latinojoel.di.trans.steps.pushnotifications.apple

import collection.JavaConversions._
import org.pentaho.di.ui.trans.step.BaseStepDialog
import org.eclipse.swt.widgets.Shell
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.trans.step.BaseStepMeta
import org.eclipse.swt.layout.FormData
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Group
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.ModifyEvent
import org.eclipse.swt.layout.FormLayout
import org.pentaho.di.core.Const
import org.pentaho.di.i18n.BaseMessages
import org.eclipse.swt.layout.FormAttachment
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Event
import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.TableItem
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.ShellAdapter
import org.eclipse.swt.events.ShellEvent
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.custom.ScrolledComposite
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TextVar
import org.pentaho.di.ui.core.widget.TableView
import org.pentaho.di.ui.core.widget.ColumnInfo
import java.util.ArrayList
import scala.collection.mutable.ListBuffer
import org.eclipse.swt.custom.CTabFolder
import org.eclipse.swt.custom.CTabItem
import org.pentaho.di.core.Props
import org.eclipse.swt.custom.CCombo
import org.eclipse.swt.events.FocusListener
import org.eclipse.swt.graphics.Cursor
import java.nio.charset.Charset
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.ui.core.dialog.ErrorDialog
import scala.collection.immutable.HashMap
import org.pentaho.di.trans.step.StepMeta
import java.util.SortedMap
import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsService
import com.notnoop.apns.ApnsServiceBuilder
import org.pentaho.di.ui.core.dialog.ShowMessageDialog
import org.pentaho.di.ui.core.dialog.ErrorDialog
import org.pentaho.di.core.vfs.KettleVFS

/**
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 */
class PushNotificationStepDialog(parent: Shell, in: BaseStepMeta, transMeta: TransMeta, sname: String) extends BaseStepDialog(parent: Shell, in: BaseStepMeta, transMeta: TransMeta, sname: String) with StepDialogInterface {
  val PKG: Class[PushNotificationStepDialog] = classOf[PushNotificationStepDialog]
  // List of ColumnInfo that should have the field names of the selected database table
  val tableCustomFieldsFieldColumns: List[ColumnInfo] = List[ColumnInfo]()
  val tableLocalizedArgumentsFieldColumns: List[ColumnInfo] = List[ColumnInfo]()

  var input: PushNotificationStepMeta = in.asInstanceOf[PushNotificationStepMeta]
  var wResponseField: Text = _
  var wDeviceTokenField, wBadgeField, wSoundField, wAlertBodyField, wActionLocalizedKeyField, wLocalizedKeyField, wLaunchImageField: CCombo = _
  var wCertificatePasswordField: LabelTextVar = _
  var wGetFieldsCustomField, wGetFieldsLocalizedArguments, wUseSandboxField, wShrinksBodyField, wNoErrorDetectionField, wAsQueuedField, wTestConnection: Button = _
  var wCertificatePathField, wShrinksPostfixField, wWaitTimeField, wMaxWaitTimeField: TextVar = _
  var ciFieldsCustomFields, ciFieldsLocalizedArgument: Array[ColumnInfo] = _
  var wTabFolder: CTabFolder = _
  var wMainOptionsTab, wPropTab: CTabItem = _
  var wLocalizedArguments, wCustomFields: TableView = _
  var inputFields: Map[String, Int] = new HashMap[String, Int]()

  def open(): String = {
    val parent: Shell = getParent()
    val display: Display = parent.getDisplay()

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    props.setLook(shell)
    setShellImage(shell, this.input)

    var lsMod: ModifyListener = new ModifyListener() {
      override def modifyText(e: ModifyEvent) = {
        input.setChanged()
      }
    }
    backupChanged = input.hasChanged()

    val formLayout: FormLayout = new FormLayout()
    formLayout.marginWidth = Const.FORM_MARGIN
    formLayout.marginHeight = Const.FORM_MARGIN
    shell.setLayout(formLayout)
    shell.setText(BaseMessages.getString(PKG, "ApplePushNotification.Shell.Title"))

    val middle: Int = props.getMiddlePct()
    val margin: Int = Const.MARGIN

    // Stepname line
    wlStepname = new Label(shell, SWT.RIGHT)
    wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"))
    props.setLook(wlStepname)
    fdlStepname = new FormData()
    fdlStepname.left = new FormAttachment(0, 0)
    fdlStepname.right = new FormAttachment(middle, -margin)
    fdlStepname.top = new FormAttachment(0, margin)
    wlStepname.setLayoutData(fdlStepname)

    wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER)
    wStepname.setText(stepname)
    props.setLook(wStepname)
    wStepname.addModifyListener(lsMod)
    fdStepname = new FormData()
    fdStepname.left = new FormAttachment(middle, 0)
    fdStepname.top = new FormAttachment(0, margin)
    fdStepname.right = new FormAttachment(100, 0)
    wStepname.setLayoutData(fdStepname)

    val sc: ScrolledComposite = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL)

    wTabFolder = new CTabFolder(sc, SWT.BORDER);
    props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

    ///////////////////////////////////
    // START OF Main Options TAB     //
    ///////////////////////////////////
    wMainOptionsTab = new CTabItem(wTabFolder, SWT.NONE)
    wMainOptionsTab.setText(BaseMessages.getString(PKG, "ApplePushNotification.MainOptionTab.CTabItem.Title"))

    val mainOptionsLayout: FormLayout = new FormLayout()
    mainOptionsLayout.marginWidth = 3
    mainOptionsLayout.marginHeight = 3

    val wMainOptionsComp: Composite = new Composite(wTabFolder, SWT.NONE)
    props.setLook(wMainOptionsComp)
    wMainOptionsComp.setLayout(mainOptionsLayout)

    // Registration Id field
    val wlDeviceTokenField = new Label(wMainOptionsComp, SWT.RIGHT)
    wlDeviceTokenField.setText(BaseMessages.getString(PKG, "ApplePushNotification.wlDeviceTokenField.Label"))
    props.setLook(wlDeviceTokenField)
    val fdlDeviceTokenField: FormData = new FormData()
    fdlDeviceTokenField.left = new FormAttachment(0, -margin)
    fdlDeviceTokenField.top = new FormAttachment(0, margin)
    fdlDeviceTokenField.right = new FormAttachment(middle, -2 * margin)
    wlDeviceTokenField.setLayoutData(fdlDeviceTokenField)

    wDeviceTokenField = new CCombo(wMainOptionsComp, SWT.BORDER | SWT.READ_ONLY)
    wDeviceTokenField.setEditable(true)
    props.setLook(wDeviceTokenField)
    wDeviceTokenField.addModifyListener(lsMod)
    val fdDeviceTokenField = new FormData()
    fdDeviceTokenField.left = new FormAttachment(middle, -margin)
    fdDeviceTokenField.top = new FormAttachment(0, margin)
    fdDeviceTokenField.right = new FormAttachment(100, -margin)
    wDeviceTokenField.setLayoutData(fdDeviceTokenField)
    wDeviceTokenField.addFocusListener(new FocusListener() {
      def focusLost(e: org.eclipse.swt.events.FocusEvent) = {}
      def focusGained(e: org.eclipse.swt.events.FocusEvent) = getStreamFields(wDeviceTokenField)
    })

    // //////////////////////
    // START OF APS GROUP  //
    /////////////////////////
    val gAPSFields: Group = new Group(wMainOptionsComp, SWT.SHADOW_NONE);
    props.setLook(gAPSFields);
    gAPSFields.setText(BaseMessages.getString(PKG, "ApplePushNotification.gAPSFields.Label"));
    val flAPSFieldsLayout: FormLayout = new FormLayout()
    flAPSFieldsLayout.marginWidth = 10
    flAPSFieldsLayout.marginHeight = 10
    gAPSFields.setLayout(flAPSFieldsLayout)

    // badge value
    val wlBadgeField = new Label(gAPSFields, SWT.RIGHT)
    wlBadgeField.setText(BaseMessages.getString(PKG, "ApplePushNotification.BadgeField.Label"))
    props.setLook(wlBadgeField)

    val fdlBadgeField: FormData = new FormData();
    fdlBadgeField.left = new FormAttachment(0, 0)
    fdlBadgeField.right = new FormAttachment(middle, -margin)
    fdlBadgeField.top = new FormAttachment(wDeviceTokenField, margin)
    wlBadgeField.setLayoutData(fdlBadgeField)

    wBadgeField = new CCombo(gAPSFields, SWT.BORDER | SWT.READ_ONLY)
    wBadgeField.setEditable(true)
    props.setLook(wBadgeField)
    wBadgeField.addModifyListener(lsMod)
    val fdBadgeFeild: FormData = new FormData()
    fdBadgeFeild.left = new FormAttachment(middle, 0)
    fdBadgeFeild.right = new FormAttachment(100, 0)
    fdBadgeFeild.top = new FormAttachment(wDeviceTokenField, margin)
    wBadgeField.setLayoutData(fdBadgeFeild)
    wBadgeField.addFocusListener(new FocusListener() {
      def focusLost(e: org.eclipse.swt.events.FocusEvent) = {}
      def focusGained(e: org.eclipse.swt.events.FocusEvent) = getStreamFields(wBadgeField)
    })

    //sound value
    val wlSoundField = new Label(gAPSFields, SWT.RIGHT)
    wlSoundField.setText(BaseMessages.getString(PKG, "ApplePushNotification.SoundField.Label"))
    props.setLook(wlSoundField)
    val fdlSoundField: FormData = new FormData()
    fdlSoundField.left = new FormAttachment(0, -margin)
    fdlSoundField.top = new FormAttachment(wBadgeField, margin)
    fdlSoundField.right = new FormAttachment(middle, -2 * margin)
    wlSoundField.setLayoutData(fdlSoundField)

    wSoundField = new CCombo(gAPSFields, SWT.BORDER | SWT.READ_ONLY)
    wSoundField.setEditable(true)
    props.setLook(wSoundField)
    val fdSoundField: FormData = new FormData()
    fdSoundField.left = new FormAttachment(middle, 0)
    fdSoundField.right = new FormAttachment(100, 0)
    fdSoundField.top = new FormAttachment(wBadgeField, margin)
    wSoundField.setLayoutData(fdSoundField)
    wSoundField.addFocusListener(new FocusListener() {
      def focusLost(e: org.eclipse.swt.events.FocusEvent) = {}
      def focusGained(e: org.eclipse.swt.events.FocusEvent) = getStreamFields(wSoundField)
    })

    // ////////////////////////
    // START OF Alert GROUP  //
    ///////////////////////////
    val gAlertFields: Group = new Group(gAPSFields, SWT.SHADOW_NONE);
    props.setLook(gAlertFields);
    gAlertFields.setText(BaseMessages.getString(PKG, "ApplePushNotification.gAlertFields.Label"));
    val flAlertFieldsLayout: FormLayout = new FormLayout()
    flAlertFieldsLayout.marginWidth = 10
    flAlertFieldsLayout.marginHeight = 10
    gAlertFields.setLayout(flAlertFieldsLayout)

    //Alert body value
    val wlAlertBodyField = new Label(gAlertFields, SWT.RIGHT)
    wlAlertBodyField.setText(BaseMessages.getString(PKG, "ApplePushNotification.AlertBodyField.Label"))
    props.setLook(wlAlertBodyField)
    val fdlAlertBodyField: FormData = new FormData()
    fdlAlertBodyField.left = new FormAttachment(0, -margin)
    fdlAlertBodyField.top = new FormAttachment(wSoundField, margin)
    fdlAlertBodyField.right = new FormAttachment(middle, -2 * margin)
    wlAlertBodyField.setLayoutData(fdlAlertBodyField)

    wAlertBodyField = new CCombo(gAlertFields, SWT.BORDER | SWT.READ_ONLY)
    wAlertBodyField.setEditable(true)
    props.setLook(wAlertBodyField)
    val fdAlertBodyField: FormData = new FormData()
    fdAlertBodyField.left = new FormAttachment(middle, 0)
    fdAlertBodyField.top = new FormAttachment(wSoundField, margin)
    fdAlertBodyField.right = new FormAttachment(100, 0)
    wAlertBodyField.setLayoutData(fdAlertBodyField)
    wAlertBodyField.addFocusListener(new FocusListener() {
      def focusLost(e: org.eclipse.swt.events.FocusEvent) = {}
      def focusGained(e: org.eclipse.swt.events.FocusEvent) = getStreamFields(wAlertBodyField)
    })

    //Action localized key value
    val wlActionLocalizedKeyField = new Label(gAlertFields, SWT.RIGHT)
    wlActionLocalizedKeyField.setText(BaseMessages.getString(PKG, "ApplePushNotification.ActionLocalizedKeyField.Label"))
    props.setLook(wlActionLocalizedKeyField)
    val fdlActionLocalizedKeyField: FormData = new FormData()
    fdlActionLocalizedKeyField.left = new FormAttachment(0, -margin)
    fdlActionLocalizedKeyField.top = new FormAttachment(wAlertBodyField, margin)
    fdlActionLocalizedKeyField.right = new FormAttachment(middle, -2 * margin)
    wlActionLocalizedKeyField.setLayoutData(fdlActionLocalizedKeyField)

    wActionLocalizedKeyField = new CCombo(gAlertFields, SWT.BORDER | SWT.READ_ONLY)
    wActionLocalizedKeyField.setEditable(true)
    props.setLook(wActionLocalizedKeyField)
    val fdActionLocalizedKeyField: FormData = new FormData()
    fdActionLocalizedKeyField.left = new FormAttachment(middle, 0)
    fdActionLocalizedKeyField.top = new FormAttachment(wAlertBodyField, margin)
    fdActionLocalizedKeyField.right = new FormAttachment(100, 0)
    wActionLocalizedKeyField.setLayoutData(fdActionLocalizedKeyField)
    wActionLocalizedKeyField.addFocusListener(new FocusListener() {
      def focusLost(e: org.eclipse.swt.events.FocusEvent) = {}
      def focusGained(e: org.eclipse.swt.events.FocusEvent) = getStreamFields(wActionLocalizedKeyField)
    })

    //Localized key value
    val wlLocalizedKeyField = new Label(gAlertFields, SWT.RIGHT)
    wlLocalizedKeyField.setText(BaseMessages.getString(PKG, "ApplePushNotification.LocalizedKeyField.Label"))
    props.setLook(wlLocalizedKeyField)
    val fdlLocalizedKeyField: FormData = new FormData()
    fdlLocalizedKeyField.left = new FormAttachment(0, -margin)
    fdlLocalizedKeyField.top = new FormAttachment(wActionLocalizedKeyField, margin)
    fdlLocalizedKeyField.right = new FormAttachment(middle, -2 * margin)
    wlLocalizedKeyField.setLayoutData(fdlLocalizedKeyField)

    wLocalizedKeyField = new CCombo(gAlertFields, SWT.BORDER | SWT.READ_ONLY)
    wLocalizedKeyField.setEditable(true)
    props.setLook(wLocalizedKeyField)
    val fdLocalizedKeyField: FormData = new FormData()
    fdLocalizedKeyField.left = new FormAttachment(middle, 0)
    fdLocalizedKeyField.top = new FormAttachment(wActionLocalizedKeyField, margin)
    fdLocalizedKeyField.right = new FormAttachment(100, 0)
    wLocalizedKeyField.setLayoutData(fdLocalizedKeyField)
    wLocalizedKeyField.addFocusListener(new FocusListener() {
      def focusLost(e: org.eclipse.swt.events.FocusEvent) = {}
      def focusGained(e: org.eclipse.swt.events.FocusEvent) = getStreamFields(wLocalizedKeyField)
    })

    // The localized arguments table
    val wlLocalizedArguments = new Label(gAlertFields, SWT.NONE)
    wlLocalizedArguments.setText(BaseMessages.getString(PKG, "ApplePushNotification.LocalizedArgumentsFields.Label"))
    props.setLook(wlLocalizedArguments)
    val fdlLocalizedArguments: FormData = new FormData()
    fdlLocalizedArguments.left = new FormAttachment(0, 0)
    fdlLocalizedArguments.top = new FormAttachment(wLocalizedKeyField, margin)
    wlLocalizedArguments.setLayoutData(fdlLocalizedArguments)

    val tableColsLocalizedArguments: Int = 1
    val localizedArgumentsRows: Int = (if (input.getLocalizedArgumentsDataPush != null) input.getLocalizedArgumentsDataPush.length else 1)

    ciFieldsLocalizedArgument = Array.ofDim[ColumnInfo](tableColsLocalizedArguments)
    ciFieldsLocalizedArgument(0) = new ColumnInfo(BaseMessages.getString(PKG, "ApplePushNotification.ColumnInfo.LocalizedArgumentField"), ColumnInfo.COLUMN_TYPE_CCOMBO, Array[String](""), false)
    List(ciFieldsLocalizedArgument(0)) ::: tableLocalizedArgumentsFieldColumns
    wLocalizedArguments = new TableView(transMeta, gAlertFields,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
      ciFieldsLocalizedArgument, localizedArgumentsRows, lsMod, props)

    wGetFieldsLocalizedArguments = new Button(gAlertFields, SWT.PUSH)
    wGetFieldsLocalizedArguments.setText(BaseMessages.getString(PKG, "ApplePushNotification.GetFields.Button"))
    val fdGetFieldsLocalizedArguments = new FormData()
    fdGetFieldsLocalizedArguments.top = new FormAttachment(wlLocalizedArguments, margin)
    fdGetFieldsLocalizedArguments.right = new FormAttachment(100, 0)
    wGetFieldsLocalizedArguments.setLayoutData(fdGetFieldsLocalizedArguments)

    val fdLocalizedArguments: FormData = new FormData()
    fdLocalizedArguments.left = new FormAttachment(0, 0)
    fdLocalizedArguments.top = new FormAttachment(wlLocalizedArguments, margin)
    fdLocalizedArguments.right = new FormAttachment(wGetFieldsLocalizedArguments, -margin)
    fdLocalizedArguments.bottom = new FormAttachment(wlLocalizedArguments, 100)
    wLocalizedArguments.setLayoutData(fdLocalizedArguments)

    //Launch image value
    val wlLaunchImageField = new Label(gAlertFields, SWT.RIGHT)
    wlLaunchImageField.setText(BaseMessages.getString(PKG, "ApplePushNotification.LaunchImageField.Label"))
    props.setLook(wlLaunchImageField)
    val fdlLaunchImageField: FormData = new FormData()
    fdlLaunchImageField.left = new FormAttachment(0, -margin)
    fdlLaunchImageField.top = new FormAttachment(wLocalizedArguments, margin)
    fdlLaunchImageField.right = new FormAttachment(middle, -2 * margin)
    wlLaunchImageField.setLayoutData(fdlLaunchImageField)

    wLaunchImageField = new CCombo(gAlertFields, SWT.BORDER | SWT.READ_ONLY)
    wLaunchImageField.setEditable(true)
    props.setLook(wLaunchImageField)
    val fdLaunchImageField: FormData = new FormData()
    fdLaunchImageField.left = new FormAttachment(middle, 0)
    fdLaunchImageField.top = new FormAttachment(wLocalizedArguments, margin)
    fdLaunchImageField.right = new FormAttachment(100, 0)
    fdLaunchImageField.bottom = new FormAttachment(100, -2 * margin)
    wLaunchImageField.setLayoutData(fdLaunchImageField)
    wLaunchImageField.addFocusListener(new FocusListener() {
      def focusLost(e: org.eclipse.swt.events.FocusEvent) = {}
      def focusGained(e: org.eclipse.swt.events.FocusEvent) = getStreamFields(wLaunchImageField)
    })

    val fdAlertFields = new FormData()
    fdAlertFields.left = new FormAttachment(0, margin)
    fdAlertFields.top = new FormAttachment(wSoundField, 2 * margin)
    fdAlertFields.right = new FormAttachment(100, -margin)
    gAlertFields.setLayoutData(fdAlertFields)
    // ////////////////////////
    // End OF Alert GROUP    //
    // ////////////////////////

    val fdAPSFields = new FormData()
    fdAPSFields.left = new FormAttachment(0, margin)
    fdAPSFields.top = new FormAttachment(wDeviceTokenField, 2 * margin)
    fdAPSFields.right = new FormAttachment(100, -margin)
    gAPSFields.setLayoutData(fdAPSFields)
    // ////////////////////
    // END OF APS GROUP  //
    ///////////////////////

    // The custom fields table
    val wlCustomFields = new Label(wMainOptionsComp, SWT.NONE)
    wlCustomFields.setText(BaseMessages.getString(PKG, "ApplePushNotification.CustomFields.Label"))
    props.setLook(wlCustomFields)
    val fdlCustomFields: FormData = new FormData()
    fdlCustomFields.left = new FormAttachment(0, 0)
    fdlCustomFields.top = new FormAttachment(gAPSFields, margin)
    wlCustomFields.setLayoutData(fdlCustomFields)

    val tableColsCustomFields: Int = 2
    val customFieldsRows: Int = (if (input.getCustomFieldsStream != null) input.getCustomFieldsStream.length else 1)

    ciFieldsCustomFields = Array.ofDim[ColumnInfo](tableColsCustomFields)
    ciFieldsCustomFields(0) = new ColumnInfo(BaseMessages.getString(PKG, "ApplePushNotification.ColumnInfo.CustomField.Key"), ColumnInfo.COLUMN_TYPE_TEXT, Array[String](""), false)
    ciFieldsCustomFields(1) = new ColumnInfo(BaseMessages.getString(PKG, "ApplePushNotification.ColumnInfo.CustomField.Value"), ColumnInfo.COLUMN_TYPE_CCOMBO, Array[String](""), false)
    List(ciFieldsCustomFields(0)) ::: tableCustomFieldsFieldColumns
    wCustomFields = new TableView(transMeta, wMainOptionsComp,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
      ciFieldsCustomFields, customFieldsRows, lsMod, props)

    wGetFieldsCustomField = new Button(wMainOptionsComp, SWT.PUSH)
    wGetFieldsCustomField.setText(BaseMessages.getString(PKG, "ApplePushNotification.GetFields.Button"))
    val fdGetFieldsCustomField = new FormData()
    fdGetFieldsCustomField.top = new FormAttachment(wlCustomFields, margin)
    fdGetFieldsCustomField.right = new FormAttachment(100, -margin)
    wGetFieldsCustomField.setLayoutData(fdGetFieldsCustomField)

    val fdCustomFields: FormData = new FormData()
    fdCustomFields.left = new FormAttachment(0, 0)
    fdCustomFields.top = new FormAttachment(wlCustomFields, margin)
    fdCustomFields.right = new FormAttachment(wGetFieldsCustomField, -margin)
    fdCustomFields.bottom = new FormAttachment(100, -margin)
    wCustomFields.setLayoutData(fdCustomFields)

    val fdMainOptions: FormData = new FormData();
    fdMainOptions.left = new FormAttachment(0, 0);
    fdMainOptions.top = new FormAttachment(0, 0);
    fdMainOptions.right = new FormAttachment(100, 0);
    fdMainOptions.bottom = new FormAttachment(100, 0);
    wMainOptionsComp.setLayoutData(fdMainOptions);

    wMainOptionsComp.layout();
    wMainOptionsTab.setControl(wMainOptionsComp);
    ///////////////////////////////////
    // END OF Main Options TAB       //
    ///////////////////////////////////

    val fdMainOptionsTab = new FormData();
    fdMainOptionsTab.left = new FormAttachment(0, 0);
    fdMainOptionsTab.top = new FormAttachment(wStepname, margin);
    fdMainOptionsTab.right = new FormAttachment(100, 0);
    fdMainOptionsTab.bottom = new FormAttachment(100, -50);
    wTabFolder.setLayoutData(fdMainOptionsTab);

    /////////////////////////////////
    // START Properties TAB       //
    ////////////////////////////////
    wPropTab = new CTabItem(wTabFolder, SWT.NONE)
    wPropTab.setText(BaseMessages.getString(PKG, "ApplePushNotification.PropTab.CTabItem.Title"))

    val wPropComp: Composite = new Composite(wTabFolder, SWT.NONE)
    props.setLook(wPropComp)

    val propsCompLayout: FormLayout = new FormLayout()
    propsCompLayout.marginWidth = Const.FORM_MARGIN
    propsCompLayout.marginHeight = Const.FORM_MARGIN
    wPropComp.setLayout(propsCompLayout)

    // Certificate path value
    val wlCertificatePathField = new Label(wPropComp, SWT.RIGHT)
    wlCertificatePathField.setText(BaseMessages.getString(PKG, "ApplePushNotification.CertificatePathField.Label"))
    props.setLook(wlCertificatePathField)
    val fdlCertificatePathField: FormData = new FormData()
    fdlCertificatePathField.left = new FormAttachment(0, -margin)
    fdlCertificatePathField.top = new FormAttachment(0, margin)
    fdlCertificatePathField.right = new FormAttachment(middle, -2 * margin)
    wlCertificatePathField.setLayoutData(fdlCertificatePathField)

    wCertificatePathField = new TextVar(this.transMeta, wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER)
    props.setLook(wCertificatePathField)
    val fdCertificatePathField: FormData = new FormData()
    fdCertificatePathField.left = new FormAttachment(middle, -margin)
    fdCertificatePathField.top = new FormAttachment(0, margin)
    fdCertificatePathField.right = new FormAttachment(100, -margin)
    wCertificatePathField.setLayoutData(fdCertificatePathField)

    //Certificate password value
    wCertificatePasswordField = new LabelTextVar(transMeta, wPropComp, BaseMessages.getString(PKG, "ApplePushNotification.CertificatePasswordField.Label"), BaseMessages.getString(PKG, "ApplePushNotification.CertificatePasswordField.Tooltip"))
    props.setLook(wCertificatePasswordField)
    wCertificatePasswordField.setEchoChar('*')
    wCertificatePasswordField.addModifyListener(lsMod)
    val fdCertificatePassword = new FormData()
    fdCertificatePassword.left = new FormAttachment(0, -margin)
    fdCertificatePassword.top = new FormAttachment(wCertificatePathField, margin)
    fdCertificatePassword.right = new FormAttachment(100, -margin)
    wCertificatePasswordField.setLayoutData(fdCertificatePassword)

    // OK, if the password contains a variable, we don't want to have the password hidden...
    wCertificatePasswordField.getTextWidget().addModifyListener(new ModifyListener() {
      def modifyText(e: ModifyEvent) {
        checkPasswordVisible()
      }
    })

    //Use sandbox
    val wlUseSandboxField = new Label(wPropComp, SWT.RIGHT)
    wlUseSandboxField.setText(BaseMessages.getString(PKG, "ApplePushNotification.UseSandboxField.Label"))
    props.setLook(wlUseSandboxField)
    val fdlUseSandboxField: FormData = new FormData()
    fdlUseSandboxField.left = new FormAttachment(0, -margin)
    fdlUseSandboxField.top = new FormAttachment(wCertificatePasswordField, margin)
    fdlUseSandboxField.right = new FormAttachment(middle, -2 * margin)
    wlUseSandboxField.setLayoutData(fdlUseSandboxField)

    wUseSandboxField = new Button(wPropComp, SWT.CHECK)
    props.setLook(wUseSandboxField)

    wTestConnection = new Button(wPropComp, SWT.PUSH)
    wTestConnection.setText(BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.Button"))
    val fdTestConnection = new FormData()
    fdTestConnection.top = new FormAttachment(wCertificatePasswordField, margin)
    fdTestConnection.right = new FormAttachment(100, -margin)
    wTestConnection.setLayoutData(fdTestConnection)

    val fdUseSandboxField: FormData = new FormData()
    fdUseSandboxField.left = new FormAttachment(middle, -margin)
    fdUseSandboxField.top = new FormAttachment(wCertificatePasswordField, margin)
    wUseSandboxField.setLayoutData(fdUseSandboxField)

    //Response field name
    val wlResponseField = new Label(wPropComp, SWT.RIGHT)
    wlResponseField.setText(BaseMessages.getString(PKG, "ApplePushNotification.ResponseField.Label"))
    props.setLook(wlResponseField)
    val fdlResponseField: FormData = new FormData()
    fdlResponseField.left = new FormAttachment(0, -margin)
    fdlResponseField.top = new FormAttachment(wTestConnection, margin)
    fdlResponseField.right = new FormAttachment(middle, -2 * margin)
    wlResponseField.setLayoutData(fdlResponseField)

    wResponseField = new Text(wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER)
    props.setLook(wResponseField)
    val fdResponseField: FormData = new FormData()
    fdResponseField.left = new FormAttachment(middle, -margin)
    fdResponseField.top = new FormAttachment(wTestConnection, margin)
    fdResponseField.right = new FormAttachment(100, -margin)
    wResponseField.setLayoutData(fdResponseField)

    //Shrinks body
    val wlShrinksBodyField = new Label(wPropComp, SWT.RIGHT)
    wlShrinksBodyField.setText(BaseMessages.getString(PKG, "ApplePushNotification.ShrinksBodyField.Label"))
    props.setLook(wlShrinksBodyField)
    val fdlShrinksBodyField: FormData = new FormData()
    fdlShrinksBodyField.left = new FormAttachment(0, -margin)
    fdlShrinksBodyField.top = new FormAttachment(wResponseField, margin)
    fdlShrinksBodyField.right = new FormAttachment(middle, -2 * margin)
    wlShrinksBodyField.setLayoutData(fdlShrinksBodyField)

    wShrinksBodyField = new Button(wPropComp, SWT.CHECK)
    props.setLook(wShrinksBodyField)
    val fdShrinksBodyField: FormData = new FormData()
    fdShrinksBodyField.left = new FormAttachment(middle, -margin)
    fdShrinksBodyField.top = new FormAttachment(wResponseField, margin)
    fdShrinksBodyField.right = new FormAttachment(100, -margin)
    wShrinksBodyField.setLayoutData(fdShrinksBodyField)

    // Shrinks postfix value
    val wlShrinksPostfixField = new Label(wPropComp, SWT.RIGHT)
    wlShrinksPostfixField.setText(BaseMessages.getString(PKG, "ApplePushNotification.ShrinksPostfixField.Label"))
    props.setLook(wlShrinksPostfixField)
    val fdlShrinksPostfixField: FormData = new FormData()
    fdlShrinksPostfixField.left = new FormAttachment(0, -margin)
    fdlShrinksPostfixField.top = new FormAttachment(wShrinksBodyField, margin)
    fdlShrinksPostfixField.right = new FormAttachment(middle, -2 * margin)
    wlShrinksPostfixField.setLayoutData(fdlShrinksPostfixField)

    wShrinksPostfixField = new TextVar(this.transMeta, wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER)
    props.setLook(wShrinksPostfixField)
    val fdShrinksPostfixField: FormData = new FormData()
    fdShrinksPostfixField.left = new FormAttachment(middle, -margin)
    fdShrinksPostfixField.top = new FormAttachment(wShrinksBodyField, margin)
    fdShrinksPostfixField.right = new FormAttachment(100, -margin)
    wShrinksPostfixField.setLayoutData(fdShrinksPostfixField)

    // No Error Detection value
    val wlNoErrorDetectionField = new Label(wPropComp, SWT.RIGHT)
    wlNoErrorDetectionField.setText(BaseMessages.getString(PKG, "ApplePushNotification.NoErrorDetectionField.Label"))
    props.setLook(wlNoErrorDetectionField)
    val fdlNoErrorDetectionField: FormData = new FormData()
    fdlNoErrorDetectionField.left = new FormAttachment(0, -margin)
    fdlNoErrorDetectionField.top = new FormAttachment(wShrinksPostfixField, margin)
    fdlNoErrorDetectionField.right = new FormAttachment(middle, -2 * margin)
    wlNoErrorDetectionField.setLayoutData(fdlNoErrorDetectionField)

    wNoErrorDetectionField = new Button(wPropComp, SWT.CHECK)
    props.setLook(wNoErrorDetectionField)
    val fdNoErrorDetectionField: FormData = new FormData()
    fdNoErrorDetectionField.left = new FormAttachment(middle, -margin)
    fdNoErrorDetectionField.top = new FormAttachment(wShrinksPostfixField, margin)
    fdNoErrorDetectionField.right = new FormAttachment(100, -margin)
    wNoErrorDetectionField.setLayoutData(fdNoErrorDetectionField)

    // As queued value
    val wlAsQueuedField = new Label(wPropComp, SWT.RIGHT)
    wlAsQueuedField.setText(BaseMessages.getString(PKG, "ApplePushNotification.AsQueuedField.Label"))
    props.setLook(wlAsQueuedField)
    val fdlAsQueuedField: FormData = new FormData()
    fdlAsQueuedField.left = new FormAttachment(0, -margin)
    fdlAsQueuedField.top = new FormAttachment(wNoErrorDetectionField, margin)
    fdlAsQueuedField.right = new FormAttachment(middle, -2 * margin)
    wlAsQueuedField.setLayoutData(fdlAsQueuedField)

    wAsQueuedField = new Button(wPropComp, SWT.CHECK)
    props.setLook(wAsQueuedField)
    val fdAsQueuedField: FormData = new FormData()
    fdAsQueuedField.left = new FormAttachment(middle, -margin)
    fdAsQueuedField.top = new FormAttachment(wNoErrorDetectionField, margin)
    fdAsQueuedField.right = new FormAttachment(100, -margin)
    wAsQueuedField.setLayoutData(fdAsQueuedField)

    // Wait Time value
    val wlWaitTimeField = new Label(wPropComp, SWT.RIGHT)
    wlWaitTimeField.setText(BaseMessages.getString(PKG, "ApplePushNotification.WaitTimeField.Label"))
    props.setLook(wlWaitTimeField)
    val fdlWaitTimeField: FormData = new FormData()
    fdlWaitTimeField.left = new FormAttachment(0, -margin)
    fdlWaitTimeField.top = new FormAttachment(wAsQueuedField, margin)
    fdlWaitTimeField.right = new FormAttachment(middle, -2 * margin)
    wlWaitTimeField.setLayoutData(fdlWaitTimeField)

    wWaitTimeField = new TextVar(this.transMeta, wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER)
    props.setLook(wWaitTimeField)
    val fdWaitTimeField: FormData = new FormData()
    fdWaitTimeField.left = new FormAttachment(middle, -margin)
    fdWaitTimeField.top = new FormAttachment(wAsQueuedField, margin)
    fdWaitTimeField.right = new FormAttachment(100, -margin)
    wWaitTimeField.setLayoutData(fdWaitTimeField)

    // Max Wait Time value
    val wlMaxWaitTimeField = new Label(wPropComp, SWT.RIGHT)
    wlMaxWaitTimeField.setText(BaseMessages.getString(PKG, "ApplePushNotification.MaxWaitTimeField.Label"))
    props.setLook(wlMaxWaitTimeField)
    val fdlMaxWaitTimeField: FormData = new FormData()
    fdlMaxWaitTimeField.left = new FormAttachment(0, -margin)
    fdlMaxWaitTimeField.top = new FormAttachment(wWaitTimeField, margin)
    fdlMaxWaitTimeField.right = new FormAttachment(middle, -2 * margin)
    wlMaxWaitTimeField.setLayoutData(fdlMaxWaitTimeField)

    wMaxWaitTimeField = new TextVar(this.transMeta, wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER)
    props.setLook(wMaxWaitTimeField)
    val fdMaxWaitTimeField: FormData = new FormData()
    fdMaxWaitTimeField.left = new FormAttachment(middle, -margin)
    fdMaxWaitTimeField.top = new FormAttachment(wWaitTimeField, margin)
    fdMaxWaitTimeField.right = new FormAttachment(100, -margin)
    wMaxWaitTimeField.setLayoutData(fdMaxWaitTimeField)
    //
    // End Properties tab...
    //

    val fdPropsComp: FormData = new FormData();
    fdPropsComp.left = new FormAttachment(0, 0);
    fdPropsComp.top = new FormAttachment(0, 0);
    fdPropsComp.right = new FormAttachment(100, 0);
    fdPropsComp.bottom = new FormAttachment(100, 0);
    wPropComp.setLayoutData(fdPropsComp);

    wPropComp.layout();
    wPropTab.setControl(wPropComp);

    // OK and cancel buttons
    wOK = new Button(shell, SWT.PUSH)
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"))
    wCancel = new Button(shell, SWT.PUSH)
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"))

    setButtonPositions(Array[Button](wOK, wCancel), margin, sc)

    val fdTabFolder: FormData = new FormData()
    fdTabFolder.left = new FormAttachment(0, 0)
    fdTabFolder.top = new FormAttachment(wStepname, margin)
    fdTabFolder.right = new FormAttachment(100, 0)
    fdTabFolder.bottom = new FormAttachment(wOK, -margin)
    wTabFolder.setLayoutData(fdTabFolder)

    wTabFolder.setSelection(0)

    val fdSc: FormData = new FormData()
    fdSc.left = new FormAttachment(0, 0)
    fdSc.top = new FormAttachment(wStepname, margin)
    fdSc.right = new FormAttachment(100, 0)
    fdSc.bottom = new FormAttachment(100, -50)
    sc.setLayoutData(fdSc)

    sc.setContent(wTabFolder)

    // 
    // Search the fields in the background
    //
    val runnable: Runnable = new Runnable() {
      def run() = {
        val stepMeta: StepMeta = transMeta.findStep(stepname);
        if (stepMeta != null) {
          try {
            val row: RowMetaInterface = transMeta.getPrevStepFields(stepMeta)
            // Remember these fields...
            var i: Int = 0
            if (row != null) {
              for (fieldName <- row.getFieldNames()) {
                inputFields += fieldName -> i
                i += 1
              }
            }
            setComboBoxes()
          } catch {
            case e: KettleException => logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
          }
        }
      }
    };
    new Thread(runnable).start();

    // Add listeners
    lsCancel = new Listener() {
      def handleEvent(e: Event) = {
        cancel()
      }
    }
    lsOK = new Listener() {
      def handleEvent(e: Event) = {
        ok()
      }
    }
    val lsGetFieldsLocalizedArguments: Listener = new Listener() { def handleEvent(e: Event) = { get(wLocalizedArguments) } }
    val lsGetFieldsCustomField: Listener = new Listener() { def handleEvent(e: Event) = { get(wCustomFields) } }
    val lsTestConn: Listener = new Listener() {
      def handleEvent(e: Event) = {
        testConnection(wCertificatePathField.getText(), wCertificatePasswordField.getText(), wUseSandboxField.getSelection())
      }
    }

    wCancel.addListener(SWT.Selection, lsCancel)
    wOK.addListener(SWT.Selection, lsOK)
    wTestConnection.addListener(SWT.Selection, lsTestConn)

    wGetFieldsCustomField.addListener(SWT.Selection, lsGetFieldsCustomField);
    wGetFieldsLocalizedArguments.addListener(SWT.Selection, lsGetFieldsLocalizedArguments);

    lsDef = new SelectionAdapter() {
      override def widgetDefaultSelected(e: SelectionEvent) = {
        ok()
      }
    }
    wStepname.addSelectionListener(lsDef)

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      override def shellClosed(e: ShellEvent) = {
        cancel()
      }
    })

    getData()

    // determine scrollable area
    sc.setMinSize(wTabFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT))
    sc.setExpandHorizontal(true)
    sc.setExpandVertical(true)

    // set window size
    BaseStepDialog.setSize(shell, 600, 400, true)

    input.setChanged(backupChanged)

    shell.open()
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep()
    }
    stepname
  }

  // Read data and place it in the dialog
  def getData() = {
    if (input.getDeviceTokenField ne null) wDeviceTokenField.setText(input.getDeviceTokenField)
    if (input.getBadgeField ne null) wBadgeField.setText(input.getBadgeField)
    if (input.getSoundField ne null) wSoundField.setText(input.getSoundField)
    if (input.getAlertBodyField ne null) wAlertBodyField.setText(input.getAlertBodyField)
    if (input.getActionLocalizedKeyField ne null) wActionLocalizedKeyField.setText(input.getActionLocalizedKeyField)
    if (input.getLocalizedKeyField ne null) wLocalizedKeyField.setText(input.getLocalizedKeyField)

    if (input.getLocalizedArgumentsDataPush() ne null) {
      var i: Int = 0
      while (i < input.getLocalizedArgumentsDataPush().size) {
        val item: TableItem = wLocalizedArguments.table.getItem(i)
        if (input.getLocalizedArgumentsDataPush.apply(i) != null) item.setText(1, input.getLocalizedArgumentsDataPush.apply(i))
        i += 1
      }
    }

    if (input.getLaunchImageField ne null) wLaunchImageField.setText(input.getLaunchImageField)

    if (input.getCustomFieldsStream() != null && input.getCustomFieldsDataPush() != null) {
      var i: Int = 0
      while (i < input.getCustomFieldsStream().size) {
        val item: TableItem = wCustomFields.table.getItem(i);
        if (input.getCustomFieldsStream.apply(i) != null) item.setText(1, input.getCustomFieldsStream.apply(i));
        if (input.getCustomFieldsDataPush.apply(i) != null) item.setText(2, input.getCustomFieldsDataPush.apply(i));
        i += 1
      }
    }

    if (input.getCertificatePathField ne null) wCertificatePathField.setText(input.getCertificatePathField)
    if (input.getCertificatePasswordField ne null) wCertificatePasswordField.setText(input.getCertificatePasswordField)
    wUseSandboxField.setSelection(input.isUseSandboxField)
    if (input.getResponseField ne null) wResponseField.setText(input.getResponseField)
    wShrinksBodyField.setSelection(input.isShrinksBodyField)
    if (input.getShrinksPostfixField ne null) wShrinksPostfixField.setText(input.getShrinksPostfixField)
    wNoErrorDetectionField.setSelection(input.isNoErrorDetectionField)
    wAsQueuedField.setSelection(input.isAsQueuedField)
    if (input.getWaitTimeField ne null) wWaitTimeField.setText(input.getWaitTimeField)
    if (input.getMaxWaitTimeField ne null) wMaxWaitTimeField.setText(input.getMaxWaitTimeField)

    wStepname.selectAll()
  }

  def checkPasswordVisible() = {
    val password: String = wCertificatePasswordField.getText()
    val list: java.util.List[String] = new java.util.ArrayList[String]()
    StringUtil.getUsedVariables(password, list, true)
    if (list.size() == 0)
      wCertificatePasswordField.setEchoChar('*');
    else {
      var variableName: String = null
      if ((password.startsWith(StringUtil.UNIX_OPEN) && password.endsWith(StringUtil.UNIX_CLOSE))) {
        variableName = password.substring(StringUtil.UNIX_OPEN.length(), password.length() - StringUtil.UNIX_CLOSE.length());
      }
      if ((password.startsWith(StringUtil.WINDOWS_OPEN) && password.endsWith(StringUtil.WINDOWS_CLOSE))) {
        variableName = password.substring(StringUtil.WINDOWS_OPEN.length(), password.length() - StringUtil.WINDOWS_CLOSE.length());
      }
      if (variableName != null && System.getProperty(variableName) != null) {
        wCertificatePasswordField.setEchoChar('\0');
      } else {
        wCertificatePasswordField.setEchoChar('*');
      }
    }
  }

  def cancel() = {
    stepname = null
    input.setChanged(backupChanged)
    dispose()
  }

  // let the plugin know about the entered data
  def ok() = {
    if (!Const.isEmpty(wStepname.getText())) {
      stepname = wStepname.getText();
      getInfo(input);
      if (input.getDeviceTokenField == null || input.getDeviceTokenField.equals("")) {
        val mb: MessageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR)
        mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.DeviceTokenError.DialogMessage"))
        mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"))
        mb.open()
      } else if (input.getCertificatePathField == null || input.getCertificatePathField.equals("")) {
        val mb: MessageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR)
        mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.CertificatePathError.DialogMessage"))
        mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"))
        mb.open()
      } else if (input.getCertificatePasswordField == null || input.getCertificatePasswordField.equals("")) {
        val mb: MessageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR)
        mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.CertificatePasswordError.DialogMessage"))
        mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"))
        mb.open()
      } else if (input.getResponseField == null || input.getResponseField.equals("")) {
        val mb: MessageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR)
        mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.ResponseFieldError.DialogMessage"))
        mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"))
        mb.open()
      } else if (input.getMaxWaitTimeField == null || input.getMaxWaitTimeField.equals("")) {
        val mb: MessageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR)
        mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.MaxWaitTimeFieldError.DialogMessage"))
        mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"))
        mb.open()
      } else if (input.getWaitTimeField == null || input.getWaitTimeField.equals("")) {
        val mb: MessageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR)
        mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.WaitTimeFieldError.DialogMessage"))
        mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"))
        mb.open()
      } else {
        stepname = wStepname.getText()
        dispose()
      }
    }
  }

  def getInfo(info: PushNotificationStepMeta) = {
    input.setDeviceTokenField(wDeviceTokenField.getText())
    input.setBadgeField(wBadgeField.getText())
    input.setSoundField(wSoundField.getText())
    input.setAlertBodyField(wAlertBodyField.getText())
    input.setActionLocalizedKeyField(wActionLocalizedKeyField.getText())
    input.setLocalizedKeyField(wLocalizedKeyField.getText())
    input.setLaunchImageField(wLaunchImageField.getText())
    input.setCertificatePathField(wCertificatePathField.getText())
    input.setCertificatePasswordField(wCertificatePasswordField.getText())
    input.setResponseField(wResponseField.getText())
    input.setShrinksPostfixField(wShrinksPostfixField.getText())
    input.setWaitTimeField(wWaitTimeField.getText())
    input.setMaxWaitTimeField(wMaxWaitTimeField.getText())
    input.setUseSandboxField(wUseSandboxField.getSelection())
    input.setShrinksBodyField(wShrinksBodyField.getSelection())
    input.setNoErrorDetectionField(wNoErrorDetectionField.getSelection())
    input.setAsQueuedField(wAsQueuedField.getSelection())

    val nrCustomFieldsRows: Int = wCustomFields.nrNonEmpty();
    val nrLocalizedArgumentsRows: Int = wLocalizedArguments.nrNonEmpty();
    val customFieldsStream: ListBuffer[String] = new ListBuffer[String]
    val customFieldsDataPush: ListBuffer[String] = new ListBuffer[String]
    val localizedArgumentsDataPush: ListBuffer[String] = new ListBuffer[String]
    var i: Int = 0
    while (i < nrCustomFieldsRows) {
      val item: TableItem = wCustomFields.getNonEmpty(i)
      customFieldsStream += Const.NVL(item.getText(1), "")
      customFieldsDataPush += Const.NVL(item.getText(2), "")
      i += 1
    }
    input.setCustomFieldsStream(customFieldsStream)
    input.setCustomFieldsDataPush(customFieldsDataPush)

    i = 0
    while (i < nrLocalizedArgumentsRows) {
      val item: TableItem = wLocalizedArguments.getNonEmpty(i)
      localizedArgumentsDataPush += Const.NVL(item.getText(1), "")
      i += 1
    }
    input.setLocalizedArgumentsDataPush(localizedArgumentsDataPush)
  }

  def getStreamFields(cCombo: CCombo) = {
    try {
      val source: String = cCombo.getText()
      cCombo.removeAll()
      val r: RowMetaInterface = transMeta.getPrevStepFields(stepname)
      if (r != null) {
        cCombo.setItems(r.getFieldNames())
        if (source != null) cCombo.setText(source)
      }
    } catch {
      case ke: KettleException => new ErrorDialog(shell,
        BaseMessages.getString(PKG, "SyslogMessageDialog.FailedToGetFields/.DialogTitle"),
        BaseMessages.getString(PKG, "SyslogMessageDialog.FailedToGetFields.DialogMessage"), ke)
    }
  }

  /**
   * Fill up the fields table with the incoming fields.
   */
  private def get(tableView: TableView) = {
    try {
      val r: RowMetaInterface = transMeta.getPrevStepFields(stepname);
      if (r != null && !r.isEmpty()) {
        BaseStepDialog.getFieldsFromPrevious(r, tableView, 1, Array[Int](1, 2), Array[Int](), -1, -1, null);
      }
    } catch {
      case ke: KettleException => new ErrorDialog(shell,
        BaseMessages.getString(PKG, "AndroidPushNotification.FailedToGetFields.DialogTitle"),
        BaseMessages.getString(PKG, "AndroidPushNotification.FailedToGetFields.DialogMessage"), ke)
    }
  }

  def setComboBoxes() = {
    //     Something was changed in the row.
    val fields: Map[String, Int] = inputFields
    val keySet: scala.collection.Set[String] = fields.keySet
    val fieldNames: Array[String] = keySet.toArray
    Const.sortStrings(fieldNames)
    ciFieldsCustomFields.apply(1).setComboValues(fieldNames)
    ciFieldsLocalizedArgument.apply(0).setComboValues(fieldNames)
  }

  def testConnection(certPath: String, pass: String, useSandbox: Boolean) = {
    try {
      val fileInputStream = KettleVFS.getInputStream(transMeta.environmentSubstitute(certPath))
      
      var apnsServiceBuilder: ApnsServiceBuilder = APNS.newService().withCert(fileInputStream, transMeta.environmentSubstitute(pass))
      if (useSandbox)
        apnsServiceBuilder = apnsServiceBuilder.withSandboxDestination()
      else
        apnsServiceBuilder = apnsServiceBuilder.withProductionDestination()
      val apnsService: ApnsService = apnsServiceBuilder.build()
      apnsService.testConnection()
      val msgDialog: ShowMessageDialog = new ShowMessageDialog(parent, SWT.ICON_INFORMATION | SWT.OK, BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.title"), BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.Success.DialogMessage"))
      msgDialog.open()
    } catch {
      case e: Exception =>
        {
          logDebug(BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.title"), e)
          new ErrorDialog(shell, BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.title"), BaseMessages.getString(PKG, "ApplePushNotification.Exception.UnexpectedErrorInTestConnection.Dialog.Error"), e)
        }
    }
  }
}
/*
 * Pentaho Data Integration Apple Push Notifications https://github.com/latinojoel/pdi-apple-pushnotifications
 * 
 * Copyright (c) 2009 about.me/latinojoel
 * 
 * Licensed under the GNU General Public License, Version 3.0; you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * The program is provided "as is" without any warranty express or implied, including the warranty of non-infringement
 * and the implied warranties of merchantibility and fitness for a particular purpose. The Copyright owner will not be
 * liable for any damages suffered by you as a result of using the Program. In no event will the Copyright owner be
 * liable for any special, indirect or consequential damages or lost profits even if the Copyright owner has been
 * advised of the possibility of their occurrence.
 */
package com.latinojoel.di.trans.steps.pushnotifications.apple;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;

/**
 * This class is responsible for Push notification UI on Spoon.
 * 
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 * @version $Revision: 666 $
 * 
 */
public class PushNotificationStepDialog extends BaseStepDialog implements StepDialogInterface {

    /** for i18n purposes. **/
    private static final Class<?> PKG = PushNotificationStepDialog.class;

    /** Fields from previous step. */
    private RowMetaInterface prevFields;

    private PushNotificationStepMeta input;
    private Text wResponseField;
    private CCombo wDeviceTokenField, wBadgeField, wSoundField, wAlertBodyField, wActionLocalizedKeyField,
    wLocalizedKeyField, wLaunchImageField;
    private LabelTextVar wCertificatePasswordField;
    private Button wGetFieldsCustomField, wGetFieldsLocalizedArguments, wUseSandboxField, wShrinksBodyField,
    wNoErrorDetectionField, wAsQueuedField, wTestConnection;
    private TextVar wCertificatePathField, wShrinksPostfixField, wWaitTimeField, wMaxWaitTimeField;
    private ColumnInfo[] ciFieldsCustomFields, ciFieldsLocalizedArgument;
    private CTabFolder wTabFolder;
    private CTabItem wMainOptionsTab, wPropTab;
    private TableView wLocalizedArguments, wCustomFields;
    private Map<String, Integer> inputFields = new HashMap<String, Integer>();
    private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();
    private Shell parent;

    public PushNotificationStepDialog(Shell shell, BaseStepMeta meta, TransMeta transMeta, String name) {
        super(shell, meta, transMeta, name);
        this.input = (PushNotificationStepMeta) meta;
    }

    /**
     * Opens a step dialog window.
     * 
     * @return the (potentially new) name of the step
     */
    public String open() {
        this.parent = getParent();
        final Display display = parent.getDisplay();

        this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, this.input);

        final ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                input.setChanged();
            }
        };
        backupChanged = input.hasChanged();

        final FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "ApplePushNotification.Shell.Title"));

        final int middle = props.getMiddlePct();
        final int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);

        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        final ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);

        wTabFolder = new CTabFolder(sc, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

        // /////////////////////////////////
        // START OF Main Options TAB //
        // /////////////////////////////////
        wMainOptionsTab = new CTabItem(wTabFolder, SWT.NONE);
        wMainOptionsTab.setText(BaseMessages.getString(PKG, "ApplePushNotification.MainOptionTab.CTabItem.Title"));

        final FormLayout mainOptionsLayout = new FormLayout();
        mainOptionsLayout.marginWidth = 3;
        mainOptionsLayout.marginHeight = 3;

        final Composite wMainOptionsComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wMainOptionsComp);
        wMainOptionsComp.setLayout(mainOptionsLayout);

        // Registration Id field
        final Label wlDeviceTokenField = new Label(wMainOptionsComp, SWT.RIGHT);
        wlDeviceTokenField.setText(BaseMessages.getString(PKG, "ApplePushNotification.wlDeviceTokenField.Label"));
        props.setLook(wlDeviceTokenField);
        final FormData fdlDeviceTokenField = new FormData();
        fdlDeviceTokenField.left = new FormAttachment(0, -margin);
        fdlDeviceTokenField.top = new FormAttachment(0, margin);
        fdlDeviceTokenField.right = new FormAttachment(middle, -2 * margin);
        wlDeviceTokenField.setLayoutData(fdlDeviceTokenField);

        wDeviceTokenField = new CCombo(wMainOptionsComp, SWT.BORDER | SWT.READ_ONLY);
        wDeviceTokenField.setEditable(true);
        props.setLook(wDeviceTokenField);
        wDeviceTokenField.addModifyListener(lsMod);
        final FormData fdDeviceTokenField = new FormData();
        fdDeviceTokenField.left = new FormAttachment(middle, -margin);
        fdDeviceTokenField.top = new FormAttachment(0, margin);
        fdDeviceTokenField.right = new FormAttachment(100, -margin);
        wDeviceTokenField.setLayoutData(fdDeviceTokenField);
        wDeviceTokenField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                getStreamFields(wDeviceTokenField);
            }
        });

        // //////////////////////
        // START OF APS GROUP //
        // ///////////////////////
        final Group gAPSFields = new Group(wMainOptionsComp, SWT.SHADOW_NONE);
        props.setLook(gAPSFields);
        gAPSFields.setText(BaseMessages.getString(PKG, "ApplePushNotification.gAPSFields.Label"));
        final FormLayout flAPSFieldsLayout = new FormLayout();
        flAPSFieldsLayout.marginWidth = 10;
        flAPSFieldsLayout.marginHeight = 10;
        gAPSFields.setLayout(flAPSFieldsLayout);

        // badge value
        final Label wlBadgeField = new Label(gAPSFields, SWT.RIGHT);
        wlBadgeField.setText(BaseMessages.getString(PKG, "ApplePushNotification.BadgeField.Label"));
        props.setLook(wlBadgeField);

        final FormData fdlBadgeField = new FormData();
        fdlBadgeField.left = new FormAttachment(0, 0);
        fdlBadgeField.right = new FormAttachment(middle, -margin);
        fdlBadgeField.top = new FormAttachment(wDeviceTokenField, margin);
        wlBadgeField.setLayoutData(fdlBadgeField);

        wBadgeField = new CCombo(gAPSFields, SWT.BORDER | SWT.READ_ONLY);
        wBadgeField.setEditable(true);
        props.setLook(wBadgeField);
        wBadgeField.addModifyListener(lsMod);
        final FormData fdBadgeFeild = new FormData();
        fdBadgeFeild.left = new FormAttachment(middle, 0);
        fdBadgeFeild.right = new FormAttachment(100, 0);
        fdBadgeFeild.top = new FormAttachment(wDeviceTokenField, margin);
        wBadgeField.setLayoutData(fdBadgeFeild);
        wBadgeField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                getStreamFields(wBadgeField);
            }
        });

        // sound value
        final Label wlSoundField = new Label(gAPSFields, SWT.RIGHT);
        wlSoundField.setText(BaseMessages.getString(PKG, "ApplePushNotification.SoundField.Label"));
        props.setLook(wlSoundField);
        final FormData fdlSoundField = new FormData();
        fdlSoundField.left = new FormAttachment(0, -margin);
        fdlSoundField.top = new FormAttachment(wBadgeField, margin);
        fdlSoundField.right = new FormAttachment(middle, -2 * margin);
        wlSoundField.setLayoutData(fdlSoundField);

        wSoundField = new CCombo(gAPSFields, SWT.BORDER | SWT.READ_ONLY);
        wSoundField.setEditable(true);
        props.setLook(wSoundField);
        final FormData fdSoundField = new FormData();
        fdSoundField.left = new FormAttachment(middle, 0);
        fdSoundField.right = new FormAttachment(100, 0);
        fdSoundField.top = new FormAttachment(wBadgeField, margin);
        wSoundField.setLayoutData(fdSoundField);
        wSoundField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                getStreamFields(wSoundField);
            }
        });

        // ////////////////////////
        // START OF Alert GROUP //
        // /////////////////////////
        final Group gAlertFields = new Group(gAPSFields, SWT.SHADOW_NONE);
        props.setLook(gAlertFields);
        gAlertFields.setText(BaseMessages.getString(PKG, "ApplePushNotification.gAlertFields.Label"));
        final FormLayout flAlertFieldsLayout = new FormLayout();
        flAlertFieldsLayout.marginWidth = 10;
        flAlertFieldsLayout.marginHeight = 10;
        gAlertFields.setLayout(flAlertFieldsLayout);

        // Alert body value
        final Label wlAlertBodyField = new Label(gAlertFields, SWT.RIGHT);
        wlAlertBodyField.setText(BaseMessages.getString(PKG, "ApplePushNotification.AlertBodyField.Label"));
        props.setLook(wlAlertBodyField);
        final FormData fdlAlertBodyField = new FormData();
        fdlAlertBodyField.left = new FormAttachment(0, -margin);
        fdlAlertBodyField.top = new FormAttachment(wSoundField, margin);
        fdlAlertBodyField.right = new FormAttachment(middle, -2 * margin);
        wlAlertBodyField.setLayoutData(fdlAlertBodyField);

        wAlertBodyField = new CCombo(gAlertFields, SWT.BORDER | SWT.READ_ONLY);
        wAlertBodyField.setEditable(true);
        props.setLook(wAlertBodyField);
        final FormData fdAlertBodyField = new FormData();
        fdAlertBodyField.left = new FormAttachment(middle, 0);
        fdAlertBodyField.top = new FormAttachment(wSoundField, margin);
        fdAlertBodyField.right = new FormAttachment(100, 0);
        wAlertBodyField.setLayoutData(fdAlertBodyField);
        wAlertBodyField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                getStreamFields(wAlertBodyField);
            }
        });

        // Action localized key value
        final Label wlActionLocalizedKeyField = new Label(gAlertFields, SWT.RIGHT);
        wlActionLocalizedKeyField.setText(BaseMessages.getString(PKG,
                "ApplePushNotification.ActionLocalizedKeyField.Label"));
        props.setLook(wlActionLocalizedKeyField);
        final FormData fdlActionLocalizedKeyField = new FormData();
        fdlActionLocalizedKeyField.left = new FormAttachment(0, -margin);
        fdlActionLocalizedKeyField.top = new FormAttachment(wAlertBodyField, margin);
        fdlActionLocalizedKeyField.right = new FormAttachment(middle, -2 * margin);
        wlActionLocalizedKeyField.setLayoutData(fdlActionLocalizedKeyField);

        wActionLocalizedKeyField = new CCombo(gAlertFields, SWT.BORDER | SWT.READ_ONLY);
        wActionLocalizedKeyField.setEditable(true);
        props.setLook(wActionLocalizedKeyField);
        final FormData fdActionLocalizedKeyField = new FormData();
        fdActionLocalizedKeyField.left = new FormAttachment(middle, 0);
        fdActionLocalizedKeyField.top = new FormAttachment(wAlertBodyField, margin);
        fdActionLocalizedKeyField.right = new FormAttachment(100, 0);
        wActionLocalizedKeyField.setLayoutData(fdActionLocalizedKeyField);
        wActionLocalizedKeyField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                getStreamFields(wActionLocalizedKeyField);
            }
        });

        // Localized key value
        final Label wlLocalizedKeyField = new Label(gAlertFields, SWT.RIGHT);
        wlLocalizedKeyField.setText(BaseMessages.getString(PKG, "ApplePushNotification.LocalizedKeyField.Label"));
        props.setLook(wlLocalizedKeyField);
        final FormData fdlLocalizedKeyField = new FormData();
        fdlLocalizedKeyField.left = new FormAttachment(0, -margin);
        fdlLocalizedKeyField.top = new FormAttachment(wActionLocalizedKeyField, margin);
        fdlLocalizedKeyField.right = new FormAttachment(middle, -2 * margin);
        wlLocalizedKeyField.setLayoutData(fdlLocalizedKeyField);

        wLocalizedKeyField = new CCombo(gAlertFields, SWT.BORDER | SWT.READ_ONLY);
        wLocalizedKeyField.setEditable(true);
        props.setLook(wLocalizedKeyField);
        final FormData fdLocalizedKeyField = new FormData();
        fdLocalizedKeyField.left = new FormAttachment(middle, 0);
        fdLocalizedKeyField.top = new FormAttachment(wActionLocalizedKeyField, margin);
        fdLocalizedKeyField.right = new FormAttachment(100, 0);
        wLocalizedKeyField.setLayoutData(fdLocalizedKeyField);
        wLocalizedKeyField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                getStreamFields(wLocalizedKeyField);
            }
        });

        // The localized arguments table
        final Label wlLocalizedArguments = new Label(gAlertFields, SWT.NONE);
        wlLocalizedArguments.setText(BaseMessages
                .getString(PKG, "ApplePushNotification.LocalizedArgumentsFields.Label"));
        props.setLook(wlLocalizedArguments);
        final FormData fdlLocalizedArguments = new FormData();
        fdlLocalizedArguments.left = new FormAttachment(0, 0);
        fdlLocalizedArguments.top = new FormAttachment(wLocalizedKeyField, margin);
        wlLocalizedArguments.setLayoutData(fdlLocalizedArguments);

        final int tableColsLocalizedArguments = 1;
        final int localizedArgumentsRows = input.getLocalizedArgumentsDataPush() != null ? input
                .getLocalizedArgumentsDataPush().size() : 1;

        ciFieldsLocalizedArgument = new ColumnInfo[tableColsLocalizedArguments];
        ciFieldsLocalizedArgument[0] = new ColumnInfo(BaseMessages.getString(PKG,
                "ApplePushNotification.ColumnInfo.LocalizedArgumentField"), ColumnInfo.COLUMN_TYPE_CCOMBO,
                new String[] { "" }, false);
        fieldColumns.add(ciFieldsLocalizedArgument[0]);
        wLocalizedArguments = new TableView(transMeta, gAlertFields, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL, ciFieldsLocalizedArgument, localizedArgumentsRows, lsMod, props);

        wGetFieldsLocalizedArguments = new Button(gAlertFields, SWT.PUSH);
        wGetFieldsLocalizedArguments.setText(BaseMessages.getString(PKG, "ApplePushNotification.GetFields.Button"));
        final FormData fdGetFieldsLocalizedArguments = new FormData();
        fdGetFieldsLocalizedArguments.top = new FormAttachment(wlLocalizedArguments, margin);
        fdGetFieldsLocalizedArguments.right = new FormAttachment(100, 0);
        wGetFieldsLocalizedArguments.setLayoutData(fdGetFieldsLocalizedArguments);

        final FormData fdLocalizedArguments = new FormData();
        fdLocalizedArguments.left = new FormAttachment(0, 0);
        fdLocalizedArguments.top = new FormAttachment(wlLocalizedArguments, margin);
        fdLocalizedArguments.right = new FormAttachment(wGetFieldsLocalizedArguments, -margin);
        fdLocalizedArguments.bottom = new FormAttachment(wlLocalizedArguments, 100);
        wLocalizedArguments.setLayoutData(fdLocalizedArguments);

        // Launch image value
        final Label wlLaunchImageField = new Label(gAlertFields, SWT.RIGHT);
        wlLaunchImageField.setText(BaseMessages.getString(PKG, "ApplePushNotification.LaunchImageField.Label"));
        props.setLook(wlLaunchImageField);
        final FormData fdlLaunchImageField = new FormData();
        fdlLaunchImageField.left = new FormAttachment(0, -margin);
        fdlLaunchImageField.top = new FormAttachment(wLocalizedArguments, margin);
        fdlLaunchImageField.right = new FormAttachment(middle, -2 * margin);
        wlLaunchImageField.setLayoutData(fdlLaunchImageField);

        wLaunchImageField = new CCombo(gAlertFields, SWT.BORDER | SWT.READ_ONLY);
        wLaunchImageField.setEditable(true);
        props.setLook(wLaunchImageField);
        final FormData fdLaunchImageField = new FormData();
        fdLaunchImageField.left = new FormAttachment(middle, 0);
        fdLaunchImageField.top = new FormAttachment(wLocalizedArguments, margin);
        fdLaunchImageField.right = new FormAttachment(100, 0);
        fdLaunchImageField.bottom = new FormAttachment(100, -2 * margin);
        wLaunchImageField.setLayoutData(fdLaunchImageField);
        wLaunchImageField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                getStreamFields(wLaunchImageField);
            }
        });

        final FormData fdAlertFields = new FormData();
        fdAlertFields.left = new FormAttachment(0, margin);
        fdAlertFields.top = new FormAttachment(wSoundField, 2 * margin);
        fdAlertFields.right = new FormAttachment(100, -margin);
        gAlertFields.setLayoutData(fdAlertFields);
        // ////////////////////////
        // End OF Alert GROUP //
        // ////////////////////////

        final FormData fdAPSFields = new FormData();
        fdAPSFields.left = new FormAttachment(0, margin);
        fdAPSFields.top = new FormAttachment(wDeviceTokenField, 2 * margin);
        fdAPSFields.right = new FormAttachment(100, -margin);
        gAPSFields.setLayoutData(fdAPSFields);
        // ////////////////////
        // END OF APS GROUP //
        // /////////////////////

        // The custom fields table
        final Label wlCustomFields = new Label(wMainOptionsComp, SWT.NONE);
        wlCustomFields.setText(BaseMessages.getString(PKG, "ApplePushNotification.CustomFields.Label"));
        props.setLook(wlCustomFields);
        final FormData fdlCustomFields = new FormData();
        fdlCustomFields.left = new FormAttachment(0, 0);
        fdlCustomFields.top = new FormAttachment(gAPSFields, margin);
        wlCustomFields.setLayoutData(fdlCustomFields);

        final int tableColsCustomFields = 2;
        final int customFieldsRows = input.getCustomFieldsStream() != null ? input.getCustomFieldsStream().size() : 1;

        ciFieldsCustomFields = new ColumnInfo[tableColsCustomFields];
        ciFieldsCustomFields[0] = new ColumnInfo(BaseMessages.getString(PKG,
                "ApplePushNotification.ColumnInfo.CustomField.Key"), ColumnInfo.COLUMN_TYPE_TEXT, new String[] { "" },
                false);
        ciFieldsCustomFields[1] = new ColumnInfo(BaseMessages.getString(PKG,
                "ApplePushNotification.ColumnInfo.CustomField.Value"), ColumnInfo.COLUMN_TYPE_CCOMBO,
                new String[] { "" }, false);
        fieldColumns.add(ciFieldsCustomFields[1]);
        wCustomFields = new TableView(transMeta, wMainOptionsComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL, ciFieldsCustomFields, customFieldsRows, lsMod, props);

        wGetFieldsCustomField = new Button(wMainOptionsComp, SWT.PUSH);
        wGetFieldsCustomField.setText(BaseMessages.getString(PKG, "ApplePushNotification.GetFields.Button"));
        final FormData fdGetFieldsCustomField = new FormData();
        fdGetFieldsCustomField.top = new FormAttachment(wlCustomFields, margin);
        fdGetFieldsCustomField.right = new FormAttachment(100, -margin);
        wGetFieldsCustomField.setLayoutData(fdGetFieldsCustomField);

        final FormData fdCustomFields = new FormData();
        fdCustomFields.left = new FormAttachment(0, 0);
        fdCustomFields.top = new FormAttachment(wlCustomFields, margin);
        fdCustomFields.right = new FormAttachment(wGetFieldsCustomField, -margin);
        fdCustomFields.bottom = new FormAttachment(100, -margin);
        wCustomFields.setLayoutData(fdCustomFields);

        final FormData fdMainOptions = new FormData();
        fdMainOptions.left = new FormAttachment(0, 0);
        fdMainOptions.top = new FormAttachment(0, 0);
        fdMainOptions.right = new FormAttachment(100, 0);
        fdMainOptions.bottom = new FormAttachment(100, 0);
        wMainOptionsComp.setLayoutData(fdMainOptions);

        wMainOptionsComp.layout();
        wMainOptionsTab.setControl(wMainOptionsComp);
        // /////////////////////////////////
        // END OF Main Options TAB //
        // /////////////////////////////////

        final FormData fdMainOptionsTab = new FormData();
        fdMainOptionsTab.left = new FormAttachment(0, 0);
        fdMainOptionsTab.top = new FormAttachment(wStepname, margin);
        fdMainOptionsTab.right = new FormAttachment(100, 0);
        fdMainOptionsTab.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdMainOptionsTab);

        // ///////////////////////////////
        // START Properties TAB //
        // //////////////////////////////
        wPropTab = new CTabItem(wTabFolder, SWT.NONE);
        wPropTab.setText(BaseMessages.getString(PKG, "ApplePushNotification.PropTab.CTabItem.Title"));

        final Composite wPropComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wPropComp);

        final FormLayout propsCompLayout = new FormLayout();
        propsCompLayout.marginWidth = Const.FORM_MARGIN;
        propsCompLayout.marginHeight = Const.FORM_MARGIN;
        wPropComp.setLayout(propsCompLayout);

        // Certificate path value
        final Label wlCertificatePathField = new Label(wPropComp, SWT.RIGHT);
        wlCertificatePathField.setText(BaseMessages.getString(PKG, "ApplePushNotification.CertificatePathField.Label"));
        props.setLook(wlCertificatePathField);
        final FormData fdlCertificatePathField = new FormData();
        fdlCertificatePathField.left = new FormAttachment(0, -margin);
        fdlCertificatePathField.top = new FormAttachment(0, margin);
        fdlCertificatePathField.right = new FormAttachment(middle, -2 * margin);
        wlCertificatePathField.setLayoutData(fdlCertificatePathField);

        wCertificatePathField = new TextVar(this.transMeta, wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wCertificatePathField);
        final FormData fdCertificatePathField = new FormData();
        fdCertificatePathField.left = new FormAttachment(middle, -margin);
        fdCertificatePathField.top = new FormAttachment(0, margin);
        fdCertificatePathField.right = new FormAttachment(100, -margin);
        wCertificatePathField.setLayoutData(fdCertificatePathField);

        // Certificate password value
        wCertificatePasswordField = new LabelTextVar(transMeta, wPropComp, BaseMessages.getString(PKG,
                "ApplePushNotification.CertificatePasswordField.Label"), BaseMessages.getString(PKG,
                    "ApplePushNotification.CertificatePasswordField.Tooltip"));
        props.setLook(wCertificatePasswordField);
        wCertificatePasswordField.setEchoChar('*');
        wCertificatePasswordField.addModifyListener(lsMod);
        final FormData fdCertificatePassword = new FormData();
        fdCertificatePassword.left = new FormAttachment(0, -margin);
        fdCertificatePassword.top = new FormAttachment(wCertificatePathField, margin);
        fdCertificatePassword.right = new FormAttachment(100, -margin);
        wCertificatePasswordField.setLayoutData(fdCertificatePassword);

        // OK, if the password contains a variable, we don't want to have the password hidden...
        wCertificatePasswordField.getTextWidget().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                checkPasswordVisible();
            }
        });

        // Use sandbox
        final Label wlUseSandboxField = new Label(wPropComp, SWT.RIGHT);
        wlUseSandboxField.setText(BaseMessages.getString(PKG, "ApplePushNotification.UseSandboxField.Label"));
        props.setLook(wlUseSandboxField);
        final FormData fdlUseSandboxField = new FormData();
        fdlUseSandboxField.left = new FormAttachment(0, -margin);
        fdlUseSandboxField.top = new FormAttachment(wCertificatePasswordField, margin);
        fdlUseSandboxField.right = new FormAttachment(middle, -2 * margin);
        wlUseSandboxField.setLayoutData(fdlUseSandboxField);

        wUseSandboxField = new Button(wPropComp, SWT.CHECK);
        props.setLook(wUseSandboxField);

        wTestConnection = new Button(wPropComp, SWT.PUSH);
        wTestConnection.setText(BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.Button"));
        final FormData fdTestConnection = new FormData();
        fdTestConnection.top = new FormAttachment(wCertificatePasswordField, margin);
        fdTestConnection.right = new FormAttachment(100, -margin);
        wTestConnection.setLayoutData(fdTestConnection);

        final FormData fdUseSandboxField = new FormData();
        fdUseSandboxField.left = new FormAttachment(middle, -margin);
        fdUseSandboxField.top = new FormAttachment(wCertificatePasswordField, margin);
        wUseSandboxField.setLayoutData(fdUseSandboxField);

        // Response field name
        final Label wlResponseField = new Label(wPropComp, SWT.RIGHT);
        wlResponseField.setText(BaseMessages.getString(PKG, "ApplePushNotification.ResponseField.Label"));
        props.setLook(wlResponseField);
        final FormData fdlResponseField = new FormData();
        fdlResponseField.left = new FormAttachment(0, -margin);
        fdlResponseField.top = new FormAttachment(wTestConnection, margin);
        fdlResponseField.right = new FormAttachment(middle, -2 * margin);
        wlResponseField.setLayoutData(fdlResponseField);

        wResponseField = new Text(wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wResponseField);
        final FormData fdResponseField = new FormData();
        fdResponseField.left = new FormAttachment(middle, -margin);
        fdResponseField.top = new FormAttachment(wTestConnection, margin);
        fdResponseField.right = new FormAttachment(100, -margin);
        wResponseField.setLayoutData(fdResponseField);

        // Shrinks body
        final Label wlShrinksBodyField = new Label(wPropComp, SWT.RIGHT);
        wlShrinksBodyField.setText(BaseMessages.getString(PKG, "ApplePushNotification.ShrinksBodyField.Label"));
        props.setLook(wlShrinksBodyField);
        final FormData fdlShrinksBodyField = new FormData();
        fdlShrinksBodyField.left = new FormAttachment(0, -margin);
        fdlShrinksBodyField.top = new FormAttachment(wResponseField, margin);
        fdlShrinksBodyField.right = new FormAttachment(middle, -2 * margin);
        wlShrinksBodyField.setLayoutData(fdlShrinksBodyField);

        wShrinksBodyField = new Button(wPropComp, SWT.CHECK);
        props.setLook(wShrinksBodyField);
        final FormData fdShrinksBodyField = new FormData();
        fdShrinksBodyField.left = new FormAttachment(middle, -margin);
        fdShrinksBodyField.top = new FormAttachment(wResponseField, margin);
        fdShrinksBodyField.right = new FormAttachment(100, -margin);
        wShrinksBodyField.setLayoutData(fdShrinksBodyField);

        // Shrinks postfix value
        final Label wlShrinksPostfixField = new Label(wPropComp, SWT.RIGHT);
        wlShrinksPostfixField.setText(BaseMessages.getString(PKG, "ApplePushNotification.ShrinksPostfixField.Label"));
        props.setLook(wlShrinksPostfixField);
        final FormData fdlShrinksPostfixField = new FormData();
        fdlShrinksPostfixField.left = new FormAttachment(0, -margin);
        fdlShrinksPostfixField.top = new FormAttachment(wShrinksBodyField, margin);
        fdlShrinksPostfixField.right = new FormAttachment(middle, -2 * margin);
        wlShrinksPostfixField.setLayoutData(fdlShrinksPostfixField);

        wShrinksPostfixField = new TextVar(this.transMeta, wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wShrinksPostfixField);
        final FormData fdShrinksPostfixField = new FormData();
        fdShrinksPostfixField.left = new FormAttachment(middle, -margin);
        fdShrinksPostfixField.top = new FormAttachment(wShrinksBodyField, margin);
        fdShrinksPostfixField.right = new FormAttachment(100, -margin);
        wShrinksPostfixField.setLayoutData(fdShrinksPostfixField);

        // No Error Detection value
        final Label wlNoErrorDetectionField = new Label(wPropComp, SWT.RIGHT);
        wlNoErrorDetectionField.setText(BaseMessages
                .getString(PKG, "ApplePushNotification.NoErrorDetectionField.Label"));
        props.setLook(wlNoErrorDetectionField);
        final FormData fdlNoErrorDetectionField = new FormData();
        fdlNoErrorDetectionField.left = new FormAttachment(0, -margin);
        fdlNoErrorDetectionField.top = new FormAttachment(wShrinksPostfixField, margin);
        fdlNoErrorDetectionField.right = new FormAttachment(middle, -2 * margin);
        wlNoErrorDetectionField.setLayoutData(fdlNoErrorDetectionField);

        wNoErrorDetectionField = new Button(wPropComp, SWT.CHECK);
        props.setLook(wNoErrorDetectionField);
        final FormData fdNoErrorDetectionField = new FormData();
        fdNoErrorDetectionField.left = new FormAttachment(middle, -margin);
        fdNoErrorDetectionField.top = new FormAttachment(wShrinksPostfixField, margin);
        fdNoErrorDetectionField.right = new FormAttachment(100, -margin);
        wNoErrorDetectionField.setLayoutData(fdNoErrorDetectionField);

        // As queued value
        final Label wlAsQueuedField = new Label(wPropComp, SWT.RIGHT);
        wlAsQueuedField.setText(BaseMessages.getString(PKG, "ApplePushNotification.AsQueuedField.Label"));
        props.setLook(wlAsQueuedField);
        final FormData fdlAsQueuedField = new FormData();
        fdlAsQueuedField.left = new FormAttachment(0, -margin);
        fdlAsQueuedField.top = new FormAttachment(wNoErrorDetectionField, margin);
        fdlAsQueuedField.right = new FormAttachment(middle, -2 * margin);
        wlAsQueuedField.setLayoutData(fdlAsQueuedField);

        wAsQueuedField = new Button(wPropComp, SWT.CHECK);
        props.setLook(wAsQueuedField);
        final FormData fdAsQueuedField = new FormData();
        fdAsQueuedField.left = new FormAttachment(middle, -margin);
        fdAsQueuedField.top = new FormAttachment(wNoErrorDetectionField, margin);
        fdAsQueuedField.right = new FormAttachment(100, -margin);
        wAsQueuedField.setLayoutData(fdAsQueuedField);

        // Wait Time value
        final Label wlWaitTimeField = new Label(wPropComp, SWT.RIGHT);
        wlWaitTimeField.setText(BaseMessages.getString(PKG, "ApplePushNotification.WaitTimeField.Label"));
        props.setLook(wlWaitTimeField);
        final FormData fdlWaitTimeField = new FormData();
        fdlWaitTimeField.left = new FormAttachment(0, -margin);
        fdlWaitTimeField.top = new FormAttachment(wAsQueuedField, margin);
        fdlWaitTimeField.right = new FormAttachment(middle, -2 * margin);
        wlWaitTimeField.setLayoutData(fdlWaitTimeField);

        wWaitTimeField = new TextVar(this.transMeta, wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wWaitTimeField);
        final FormData fdWaitTimeField = new FormData();
        fdWaitTimeField.left = new FormAttachment(middle, -margin);
        fdWaitTimeField.top = new FormAttachment(wAsQueuedField, margin);
        fdWaitTimeField.right = new FormAttachment(100, -margin);
        wWaitTimeField.setLayoutData(fdWaitTimeField);

        // Max Wait Time value
        final Label wlMaxWaitTimeField = new Label(wPropComp, SWT.RIGHT);
        wlMaxWaitTimeField.setText(BaseMessages.getString(PKG, "ApplePushNotification.MaxWaitTimeField.Label"));
        props.setLook(wlMaxWaitTimeField);
        final FormData fdlMaxWaitTimeField = new FormData();
        fdlMaxWaitTimeField.left = new FormAttachment(0, -margin);
        fdlMaxWaitTimeField.top = new FormAttachment(wWaitTimeField, margin);
        fdlMaxWaitTimeField.right = new FormAttachment(middle, -2 * margin);
        wlMaxWaitTimeField.setLayoutData(fdlMaxWaitTimeField);

        wMaxWaitTimeField = new TextVar(this.transMeta, wPropComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wMaxWaitTimeField);
        final FormData fdMaxWaitTimeField = new FormData();
        fdMaxWaitTimeField.left = new FormAttachment(middle, -margin);
        fdMaxWaitTimeField.top = new FormAttachment(wWaitTimeField, margin);
        fdMaxWaitTimeField.right = new FormAttachment(100, -margin);
        wMaxWaitTimeField.setLayoutData(fdMaxWaitTimeField);
        //
        // End Properties tab...
        //

        final FormData fdPropsComp = new FormData();
        fdPropsComp.left = new FormAttachment(0, 0);
        fdPropsComp.top = new FormAttachment(0, 0);
        fdPropsComp.right = new FormAttachment(100, 0);
        fdPropsComp.bottom = new FormAttachment(100, 0);
        wPropComp.setLayoutData(fdPropsComp);

        wPropComp.layout();
        wPropTab.setControl(wPropComp);

        // OK and cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[] { wOK, wCancel }, margin, sc);

        final FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(wStepname, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(wOK, -margin);
        wTabFolder.setLayoutData(fdTabFolder);

        wTabFolder.setSelection(0);

        final FormData fdSc = new FormData();
        fdSc.left = new FormAttachment(0, 0);
        fdSc.top = new FormAttachment(wStepname, margin);
        fdSc.right = new FormAttachment(100, 0);
        fdSc.bottom = new FormAttachment(100, -50);
        sc.setLayoutData(fdSc);

        sc.setContent(wTabFolder);

        //
        // Search the fields in the background
        //
        final Runnable runnable = new Runnable() {
            public void run() {
                final StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta != null) {
                    try {
                        final RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                        // Remember these fields...
                        int i = 0;
                        if (row != null) {
                            for (String fieldName : row.getFieldNames()) {
                                inputFields.put(fieldName, i);
                                i++;
                            }
                        }
                        setComboBoxes();
                    } catch (KettleException e) {
                        logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();

        // Add listeners
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };
        final Listener lsGetFieldsLocalizedArguments = new Listener() {
            public void handleEvent(Event e) {
                get(wLocalizedArguments);
            }
        };
        final Listener lsGetFieldsCustomField = new Listener() {
            public void handleEvent(Event e) {
                get(wCustomFields);
            }
        };
        final Listener lsTestConn = new Listener() {
            public void handleEvent(Event e) {
                testConnection(wCertificatePathField.getText(), wCertificatePasswordField.getText(),
                        wUseSandboxField.getSelection());
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wTestConnection.addListener(SWT.Selection, lsTestConn);

        wGetFieldsCustomField.addListener(SWT.Selection, lsGetFieldsCustomField);
        wGetFieldsLocalizedArguments.addListener(SWT.Selection, lsGetFieldsLocalizedArguments);

        lsDef = new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        getData();

        // determine scrollable area
        sc.setMinSize(wTabFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        // set window size
        BaseStepDialog.setSize(shell, 600, 400, true);

        input.setChanged(backupChanged);

        setComboValues();

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return stepname;
    }

    /**
     * Read data and place it in the dialog.
     */
    public void getData() {
        if (input.getDeviceTokenField() != null) {
            wDeviceTokenField.setText(input.getDeviceTokenField());
        }
        if (input.getBadgeField() != null) {
            wBadgeField.setText(input.getBadgeField());
        }
        if (input.getSoundField() != null) {
            wSoundField.setText(input.getSoundField());
        }
        if (input.getAlertBodyField() != null) {
            wAlertBodyField.setText(input.getAlertBodyField());
        }
        if (input.getActionLocalizedKeyField() != null) {
            wActionLocalizedKeyField.setText(input.getActionLocalizedKeyField());
        }
        if (input.getLocalizedKeyField() != null) {
            wLocalizedKeyField.setText(input.getLocalizedKeyField());
        }

        if (input.getLocalizedArgumentsDataPush() != null) {
            for (int i = 0; i < input.getLocalizedArgumentsDataPush().size(); i++) {
                final TableItem item = wLocalizedArguments.table.getItem(i);
                if (input.getLocalizedArgumentsDataPush().get(i) != null) {
                    item.setText(1, input.getLocalizedArgumentsDataPush().get(i));
                }
            }
        }

        if (input.getLaunchImageField() != null) {
            wLaunchImageField.setText(input.getLaunchImageField());
        }

        if (input.getCustomFieldsStream() != null && input.getCustomFieldsDataPush() != null) {
            for (int i = 0; i < input.getCustomFieldsStream().size(); i++) {
                final TableItem item = wCustomFields.table.getItem(i);
                if (input.getCustomFieldsStream().get(i) != null) {
                    item.setText(1, input.getCustomFieldsStream().get(i));
                }
                if (input.getCustomFieldsDataPush().get(i) != null) {
                    item.setText(2, input.getCustomFieldsDataPush().get(i));
                }
            }
        }

        if (input.getCertificatePathField() != null) {
            wCertificatePathField.setText(input.getCertificatePathField());
        }
        if (input.getCertificatePasswordField() != null) {
            wCertificatePasswordField.setText(input.getCertificatePasswordField());
        }
        wUseSandboxField.setSelection(input.isUseSandboxField());
        if (input.getResponseField() != null) {
            wResponseField.setText(input.getResponseField());
        }
        wShrinksBodyField.setSelection(input.isShrinksBodyField());
        if (input.getShrinksPostfixField() != null) {
            wShrinksPostfixField.setText(input.getShrinksPostfixField());
        }
        wNoErrorDetectionField.setSelection(input.isNoErrorDetectionField());
        wAsQueuedField.setSelection(input.isAsQueuedField());
        if (input.getWaitTimeField() != null) {
            wWaitTimeField.setText(input.getWaitTimeField());
        }
        if (input.getMaxWaitTimeField() != null) {
            wMaxWaitTimeField.setText(input.getMaxWaitTimeField());
        }

        wStepname.selectAll();
    }

    /**
     * Checks the password visible.
     */
    private void checkPasswordVisible() {
        final String password = wCertificatePasswordField.getText();
        final List<String> list = new ArrayList<String>();
        StringUtil.getUsedVariables(password, list, true);
        if (list.size() == 0) {
            wCertificatePasswordField.setEchoChar('*');
        } else {
            String variableName = null;
            if (password.startsWith(StringUtil.UNIX_OPEN) && password.endsWith(StringUtil.UNIX_CLOSE)) {
                variableName = password.substring(StringUtil.UNIX_OPEN.length(), password.length()
                        - StringUtil.UNIX_CLOSE.length());
            }
            if (password.startsWith(StringUtil.WINDOWS_OPEN) && password.endsWith(StringUtil.WINDOWS_CLOSE)) {
                variableName = password.substring(StringUtil.WINDOWS_OPEN.length(), password.length()
                        - StringUtil.WINDOWS_CLOSE.length());
            }
            if (variableName != null && System.getProperty(variableName) != null) {
                wCertificatePasswordField.setEchoChar('\0');
            } else {
                wCertificatePasswordField.setEchoChar('*');
            }
        }
    }

    /**
     * Cancel.
     */
    private void cancel() {
        stepname = null;
        input.setChanged(backupChanged);
        dispose();
    }

    /**
     * Let the plugin know about the entered data.
     */
    private void ok() {
        if (!Const.isEmpty(wStepname.getText())) {
            stepname = wStepname.getText();
            getInfo(input);
            if (input.getDeviceTokenField() == null || "".equals(input.getDeviceTokenField())) {
                final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.DeviceTokenError.DialogMessage"));
                mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
                mb.open();
            } else if (input.getCertificatePathField() == null || "".equals(input.getCertificatePathField())) {
                final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.CertificatePathError.DialogMessage"));
                mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
                mb.open();
            } else if (input.getCertificatePasswordField() == null || "".equals(input.getCertificatePasswordField())) {
                final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                mb.setMessage(BaseMessages.getString(PKG,
                        "ApplePushNotification.CertificatePasswordError.DialogMessage"));
                mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
                mb.open();
            } else if (input.getResponseField() == null || "".equals(input.getResponseField())) {
                final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.ResponseFieldError.DialogMessage"));
                mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
                mb.open();
            } else if (input.getMaxWaitTimeField() == null || "".equals(input.getMaxWaitTimeField())) {
                final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.MaxWaitTimeFieldError.DialogMessage"));
                mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
                mb.open();
            } else if (input.getWaitTimeField() == null || "".equals(input.getWaitTimeField())) {
                final MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                mb.setMessage(BaseMessages.getString(PKG, "ApplePushNotification.WaitTimeFieldError.DialogMessage"));
                mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
                mb.open();
            } else {
                stepname = wStepname.getText();
                dispose();
            }
        }
    }

    /**
     * Gets the information.
     * 
     * @param info the information.
     */
    private void getInfo(PushNotificationStepMeta info) {
        input.setDeviceTokenField(wDeviceTokenField.getText());
        input.setBadgeField(wBadgeField.getText());
        input.setSoundField(wSoundField.getText());
        input.setAlertBodyField(wAlertBodyField.getText());
        input.setActionLocalizedKeyField(wActionLocalizedKeyField.getText());
        input.setLocalizedKeyField(wLocalizedKeyField.getText());
        input.setLaunchImageField(wLaunchImageField.getText());
        input.setCertificatePathField(wCertificatePathField.getText());
        input.setCertificatePasswordField(wCertificatePasswordField.getText());
        input.setResponseField(wResponseField.getText());
        input.setShrinksPostfixField(wShrinksPostfixField.getText());
        input.setWaitTimeField(wWaitTimeField.getText());
        input.setMaxWaitTimeField(wMaxWaitTimeField.getText());
        input.setUseSandboxField(wUseSandboxField.getSelection());
        input.setShrinksBodyField(wShrinksBodyField.getSelection());
        input.setNoErrorDetectionField(wNoErrorDetectionField.getSelection());
        input.setAsQueuedField(wAsQueuedField.getSelection());

        final int nrCustomFieldsRows = wCustomFields.nrNonEmpty();
        final int nrLocalizedArgumentsRows = wLocalizedArguments.nrNonEmpty();
        final List<String> customFieldsStream = new ArrayList<String>();
        final List<String> customFieldsDataPush = new ArrayList<String>();
        final List<String> localizedArgumentsDataPush = new ArrayList<String>();
        for (int i = 0; i < nrCustomFieldsRows; i++) {
            final TableItem item = wCustomFields.getNonEmpty(i);
            customFieldsStream.add(Const.NVL(item.getText(1), ""));
            customFieldsDataPush.add(Const.NVL(item.getText(2), ""));
        }
        input.setCustomFieldsStream(customFieldsStream);
        input.setCustomFieldsDataPush(customFieldsDataPush);
        for (int i = 0; i < nrLocalizedArgumentsRows; i++) {
            final TableItem item = wLocalizedArguments.getNonEmpty(i);
            localizedArgumentsDataPush.add(Const.NVL(item.getText(1), ""));
        }
        input.setLocalizedArgumentsDataPush(localizedArgumentsDataPush);
    }

    /**
     * Gets the values in the stream for ComboBox.
     * 
     * @param cCombo the ComboBox.
     */
    private void getStreamFields(CCombo cCombo) {
        try {
            final String source = cCombo.getText();
            cCombo.removeAll();
            final RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r != null && !r.isEmpty()) {
                cCombo.setItems(r.getFieldNames());
                if (source != null) {
                    cCombo.setText(source);
                }
            }
        } catch (KettleException ke) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "SyslogMessageDialog.FailedToGetFields.DialogTitle"),
                    BaseMessages.getString(PKG, "SyslogMessageDialog.FailedToGetFields.DialogMessage"), ke);
        }
    }

    /**
     * Fill up the fields table with the incoming fields.
     * 
     * @param tableView the table view.
     */
    private void get(TableView tableView) {
        try {
            final RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r != null && !r.isEmpty()) {
                BaseStepDialog.getFieldsFromPrevious(r, tableView, 1, new int[] { 1, 2 }, new int[] {}, -1, -1, null);
            }
        } catch (KettleException ke) {
            new ErrorDialog(shell,
                    BaseMessages.getString(PKG, "AndroidPushNotification.FailedToGetFields.DialogTitle"),
                    BaseMessages.getString(PKG, "AndroidPushNotification.FailedToGetFields.DialogMessage"), ke);
        }
    }

    /**
     * Sets the values of combo boxes.
     */
    private void setComboBoxes() {
        // Something was changed in the row.
        final Map<String, Integer> fields = inputFields;
        final Set<String> keySet = fields.keySet();
        final String[] fieldNames = (String[]) keySet.toArray();
        Const.sortStrings(fieldNames);
        ciFieldsCustomFields[1].setComboValues(fieldNames);
        ciFieldsLocalizedArgument[0].setComboValues(fieldNames);
    }

    /**
     * Sets the values of combo boxes.
     */
    private void setComboValues() {
        final Runnable fieldLoader = new Runnable() {
            public void run() {
                try {
                    prevFields = transMeta.getPrevStepFields(stepname);
                } catch (KettleException e) {
                    prevFields = new RowMeta();
                    final String msg = BaseMessages.getString(PKG, "AndroidPushNotification.DoMapping.UnableToFindInput");
                    logError(msg);
                }
                final String[] prevStepFieldNames = prevFields.getFieldNames();
                Arrays.sort(prevStepFieldNames);
                for (int i = 0; i < fieldColumns.size(); i++) {
                    final ColumnInfo colInfo = (ColumnInfo) fieldColumns.get(i);
                    colInfo.setComboValues(prevStepFieldNames);
                }
            }
        };
        shell.getDisplay().asyncExec(fieldLoader);
    }

    /**
     * Test the connection with APNS server.
     * 
     * @param certPath the certificate path.
     * @param pass the password.
     * @param useSandbox if use Sandbox.
     */
    private void testConnection(String certPath, String pass, boolean useSandbox) {
        try {
            final InputStream fileInputStream = KettleVFS.getInputStream(transMeta.environmentSubstitute(certPath));

            ApnsServiceBuilder apnsServiceBuilder = APNS.newService().withCert(fileInputStream,
                    transMeta.environmentSubstitute(pass));
            if (useSandbox) {
                apnsServiceBuilder = apnsServiceBuilder.withSandboxDestination();
            } else {
                apnsServiceBuilder = apnsServiceBuilder.withProductionDestination();
            }
            final ApnsService apnsService = apnsServiceBuilder.build();
            apnsService.testConnection();
            final ShowMessageDialog msgDialog = new ShowMessageDialog(parent, SWT.ICON_INFORMATION | SWT.OK,
                    BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.title"), BaseMessages.getString(
                            PKG, "ApplePushNotification.TestConnection.Success.DialogMessage"));
            msgDialog.open();
        } catch (Exception e) {
            logDebug(BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.title"), e);
            new ErrorDialog(shell, BaseMessages.getString(PKG, "ApplePushNotification.TestConnection.title"),
                    BaseMessages.getString(PKG,
                            "ApplePushNotification.Exception.UnexpectedErrorInTestConnection.Dialog.Error"), e);
        }
    }
}
